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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import dev.rpmhub.revision.chain.AbstractChecker;
import dev.rpmhub.revision.chain.Checker;
import dev.rpmhub.revision.exceptions.RevisionServiceException;
import dev.rpmhub.revision.mappers.github.ListWorkflow;
import dev.rpmhub.revision.mappers.github.Run;
import dev.rpmhub.revision.mappers.moodle.ListCourse;
import dev.rpmhub.revision.mappers.moodle.Module;

/**
 * Checks if the latest run of github action is a success
 * and verifies if the repository is a fork
 *
 * @author Rodrigo Prestes Machado
 * @version Jan. 2022
 */
@ApplicationScoped
public class RepositoryChecker extends AbstractChecker implements Checker {

    private static final Logger LOGGER = Logger.getLogger(RepositoryChecker.class.getName());

    @Override
    public boolean check(Map<String, String> input) {
        LOGGER.info("RepositoryChecker");

        boolean result = false;

        String moodleAssignURL = input.get("moodleAssignURL");
        String githubLogin = getGithubLogin(input.get("githubProfileURL"));

        // Module in this case will be the assign
        // This step discoveries the course id and the instance id (the data base id)
        // The instance id is necessary to update the grade
        Module module = getCurseModule(moodleAssignURL);

        // Returns the courses and the assigns
        // We need this step to retrieve the assign intro (description)
        ListCourse courses = getMoodleCourse(module);

        // Gets the assign intro (description)
        String intro = this.getAssignIntro(courses, moodleAssignURL);

         // Get the YAML from the assign
         if (intro != null) {
            Map<String, String> config = this.getAssignConfig(intro);
            if (config != null) {

                ListWorkflow actions = github.getRuns(githubLogin, config.get("repo"), config.get("workflow"));
                Run run = actions.getLatestRun();
                // Verifies if the latest run was a success and the repository is a fork
                if (run.getConclusion().equalsIgnoreCase("success") && run.getRepository().isFork()) {
                    LOGGER.info("Os testes passaram e é um fork");
                    result = this.getNextChecker().check(input);
                }
                else{
                    String message = messages.getString("RepositoryChecker.repo");
                    LOGGER.log(Level.WARNING, message);
                    throw new RevisionServiceException(message, Response.Status.BAD_REQUEST);
                }
            }
        }
        return result;
    }
}
