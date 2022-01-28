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

package dev.rpmhub.revision.chain.github;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import dev.rpmhub.revision.chain.AbstractChecker;
import dev.rpmhub.revision.chain.Checker;
import dev.rpmhub.revision.mappers.github.Commit;
import dev.rpmhub.revision.mappers.github.CommitData;
import dev.rpmhub.revision.mappers.github.File;
import dev.rpmhub.revision.mappers.moodle.ListCourse;
import dev.rpmhub.revision.mappers.moodle.Module;

/**
 * Verifies if the user changed the test case
 */
@ApplicationScoped
public class TestChangeChecker extends AbstractChecker implements Checker {

    private static final Logger LOGGER = Logger.getLogger(TestChangeChecker.class.getName());

    @Override
    public boolean check(Map<String, String> input) {
        LOGGER.info("TestChangeChecker");

        boolean result = false;

        String githubLogin = this.getGithubLogin(input.get("githubProfileURL"));

        // Module in this case will be the assign
        // This step discoveries the course id and the instance id (real data base id)
        // The instance id is necessary to update the grade
        Module module = getCurseModule(input.get("moodleAssignURL"));

        // Returns the courses and the assigns
        // We need this step to retrieve the assign intro (description)
        ListCourse courses = getMoodleCourse(module);

        // Gets the assign intro (description)
        String intro = this.getAssignIntro(courses, input.get("moodleAssignURL"));

        // Get the YAML from the assign
        if (intro != null) {
            Map<String, String> config = this.getAssignConfig(intro);
            if (config != null) {

                // Verifica se o arquivo de teste foi modificado em algum commit
                // Atualmente suporta apenas um arquivo de teste
                if (config.get("test-file") != null){
                    List<CommitData> commits = github.getCommits(githubLogin, config.get("repo"));
                    List<CommitData> userCommits = checkCommits(commits, githubLogin);

                    boolean flag = true;
                    for (CommitData commit : userCommits) {
                        Commit c = github.getCommit(githubLogin, config.get("repo"), commit.getSha());
                        List<File> files = c.getFiles();

                        for (File file : files) {
                            if (config.get("test-file").equalsIgnoreCase(file.getFilename())){
                                LOGGER.info("O arquivo de teste foi modificado");
                                flag = false;
                                break;
                            }
                            else{
                                LOGGER.info("O arquivo de teste NAO foi modificado");
                            }
                        }
                    }
                    if (flag)
                            result = this.getNextChecker().check(input);
                        else
                            result = flag;
                }
             }
        }
        return result;
    }
}
