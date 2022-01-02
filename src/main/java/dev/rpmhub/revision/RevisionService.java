/**
 * Copyright 2022 RPMHub Revision Service @ https://github.com/rpmhub/revision
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.rpmhub.revision;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.hibernate.validator.constraints.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.yaml.snakeyaml.Yaml;

import dev.rpmhub.revision.exceptions.RevisionException;
import dev.rpmhub.revision.mappers.github.Author;
import dev.rpmhub.revision.mappers.github.Commit;
import dev.rpmhub.revision.mappers.github.CommitData;
import dev.rpmhub.revision.mappers.github.File;
import dev.rpmhub.revision.mappers.github.ListWorkflow;
import dev.rpmhub.revision.mappers.github.Run;
import dev.rpmhub.revision.mappers.moodle.Assign;
import dev.rpmhub.revision.mappers.moodle.ListCourse;
import dev.rpmhub.revision.mappers.moodle.ListUser;
import dev.rpmhub.revision.mappers.moodle.Module;
import dev.rpmhub.revision.mappers.moodle.User;

/**
 * Revision Service is able to:
 *
 * 1 - Check if the Github and Moodle users has the same full name
 * 2 - Verifies if the repository is a fork
 * 3 - Checks if the users changed the test cases
 * 4 - Verifies if the latest run of a specific workflow in Github
 * Actions was executed with success
 *
 * @author Rodrigo Prestes Machado
 * @version Jan. 2022
 */
@Path("/verify")
public class RevisionService extends AbstractService {

    private static final Logger LOGGER = Logger.getLogger(RevisionService.class.getName());

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Retry(maxRetries = 2, delay = 1000)
    @Timeout(5000)
    @Bulkhead(3)
    public String verify(
            @URL @NotBlank @FormParam("githubProfileURL") String githubProfileURL,
            @URL @NotBlank @FormParam("moodleProfileURL") String moodleProfileURL,
            @URL @NotBlank @FormParam("moodleAssignURL") String moodleAssignURL,
            @HeaderParam("Content-Language") String language) {

        // Creates a has to identify the log, like a receipt
        String hash = UUID.randomUUID().toString();

        try {
            // Sets the localization of the messages
            this.setLocation(language);

            // Get Moodle user
            ListUser mUsers = moodle.getUser(MOODLE_TOKEN, MOODLE_USERS, MOODLE_JSON_FORMAT, "id",
                    this.getMoodleId(moodleProfileURL));

            // Get Github user
            User gUser = github.getUser(this.getGithubLogin(githubProfileURL));

            // Verifies if the Moodle and Github name are the same
            if (mUsers.getFirstUserName().equalsIgnoreCase(gUser.getName())) {
                LOGGER.info(hash + ' ' + gUser.getName());

                // Module in this case will be the assign
                // This step discoveries the course id and the instance id (real data base id)
                // The instance id is necessary to update the grade
                Module module = moodle.getModule(MOODLE_TOKEN, MOODLE_MODULE, MOODLE_JSON_FORMAT,
                        this.getMoodleId(moodleAssignURL));

                // Returns the courses and the assigns
                // We need this step to retrieve the assign intro (description)
                ListCourse courses = moodle.getCourses(MOODLE_TOKEN, MOODLE_ASSIGN, MOODLE_JSON_FORMAT,
                        module.getCm().getCourse());

                // Gets the assign intro (description)
                String intro = this.getAssignIntro(courses, moodleAssignURL);

                // Get the YAML from the assign
                if (intro != null) {
                    Map<String, String> config = this.getAssignConfig(intro);
                    if (config != null) {

                        // Verifica se o arquivo de teste foi modificado em algum commit
                        // Atualmente suporta apenas um arquivo de teste
                        if (config.get("test-file") != null){
                            List<CommitData> commits = github.getCommits(gUser.getLogin(), config.get("repo"));
                            List<CommitData> userCommits = checkCommits(commits, gUser.getLogin());

                            for (CommitData commit : userCommits) {
                                Commit c = github.getCommit(gUser.getLogin(), config.get("repo"), commit.getSha());
                                List<File> files = c.getFiles();

                                for (File file : files) {
                                    if (config.get("test-file").equalsIgnoreCase(file.getFilename())){
                                        System.out.println("O arquivo de teste foi modificado");
                                    }
                                }
                            }
                        }

                        ListWorkflow actions = github.getRuns(gUser.getLogin(), config.get("repo"), config.get("workflow"));
                        Run run = actions.getLatestRun();
                        // Verifies if the latest run was a success and the repository is a fork
                        if (run.getConclusion().equalsIgnoreCase("success") && run.getRepository().isFork()) {

                            LOGGER.info(hash + ' ' + "grade saved");
                            moodle.updateGrade(MOODLE_TOKEN, MOODLE_GRADE, MOODLE_JSON_FORMAT,
                                    module.getCm().getInstance(), mUsers.getFirstUserId(),
                                    10, -1, 1, "rpm", 1, hash, 2);
                        }
                    }
                }
            } else {
                throw new RevisionException(messages.getString("dev.orion.moodle.service.differentUser"));
            }
            return hash;

        } catch (Exception exception) {
            System.err.println(exception);
            throw new RevisionException(messages.getString("dev.orion.moodle.service.restclient"));
        }
    }

    /**
     * Returns a list of commits of the user
     *
     * @param A list of commits
     * @param login The github login name
     */
    private List<CommitData> checkCommits(List<CommitData> commits, String login) {
        List<CommitData> userCommits = new ArrayList<>();
        for (CommitData commit : commits) {
            Author author = commit.getAuthor();
            if(author != null && author.getLogin().equalsIgnoreCase(login)){
                userCommits.add(commit);
            }
        }
        return userCommits;
    }

    /**
     * Extracts the YAML from the assign's intro (description). The YAML contens
     * information about the repository (repo) and the workflow of the original
     * exercise
     *
     * @param intro The HTML of the assign intro
     * @return A map with 'repo' and 'workflow' keys
     */
    private Map<String, String> getAssignConfig(String intro) {
        Map<String, String> map = null;
        Document doc = Jsoup.parse(intro);
        Element body = doc.body();

        for (int i = 0; i < body.childNodeSize(); i++) {
            if (body.childNode(i).nodeName().equals("#comment")) {
                Node comment = body.childNode(i);
                String text = comment.outerHtml();
                text = text.substring(4, text.length() - 4);

                Yaml yaml = new Yaml();
                map = yaml.load(text);
            }
        }
        return map;
    }

    /**
     * Extracts the github login from profile URL
     *
     * @param A github profile URL
     * @return The github login
     */
    private String getGithubLogin(String url) {
        int index = url.lastIndexOf('/');
        return url.substring(index + 1);
    }

    /**
     * Extracts the 'id' at the end of a Moodle URL
     *
     * @param A URL from Moodle with an 'id' at the end
     * @return A Moodle 'id'
     */
    private String getMoodleId(String url) {
        int index = url.lastIndexOf('=');
        return url.substring(index + 1);
    }

    private String getAssignIntro(ListCourse courses, String moodleAssignURL) {
        // Discovers the assign intro
        String intro = null;
        List<Assign> assigns = courses.getCourses().get(0).getAssignments();
        for (Assign assign : assigns) {
            if (assign.getCmid() == Integer.valueOf(this.getMoodleId(moodleAssignURL))) {
                intro = assign.getIntro();
                break;
            }
        }
        return intro;
    }
}