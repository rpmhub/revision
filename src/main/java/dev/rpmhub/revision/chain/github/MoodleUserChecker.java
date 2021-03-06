
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
import dev.rpmhub.revision.mappers.moodle.ListUser;
import dev.rpmhub.revision.mappers.moodle.User;

/**
 * Verifies if the users names of Github and Moodle are the same
 *
 * @author Rodrigo Prestes Machado
 * @version Jan. 2022
 */
@ApplicationScoped
public class MoodleUserChecker extends AbstractChecker implements Checker {

    private static final Logger LOGGER = Logger.getLogger(MoodleUserChecker.class.getName());

    @Override
    public boolean check(Map<String, String> input) {

        LOGGER.info("MoodleChecker");

        boolean result = false;

        // Get Github user
        User gUser = getGithubUser(input.get("githubProfileURL"));

        // Get Moodle user
        ListUser mUsers = getMoodleUser(input.get("moodleProfileURL"));

        if (mUsers != null &&  gUser != null){
            // Verifies if the Moodle and Github name are the same
            if (mUsers.getFirstUserName().equalsIgnoreCase(gUser.getName())) {
                LOGGER.info("O usuário do Moodle e Github são os mesmos");
                // send to the next checker
                result = this.getNextChecker().check(input);
            }
            else{
                String message = messages.getString("MoodleUserChecker.users");
                LOGGER.log(Level.WARNING, message);
                throw new RevisionServiceException(message, Response.Status.BAD_REQUEST);
            }
        }
        return result;
    }

}