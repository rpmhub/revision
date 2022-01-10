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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.hibernate.validator.constraints.URL;

import dev.rpmhub.revision.chain.Checker;
import dev.rpmhub.revision.chain.github.MoodleUserChecker;
import dev.rpmhub.revision.chain.github.RepositoryChecker;
import dev.rpmhub.revision.chain.github.TestChangeChecker;
import dev.rpmhub.revision.chain.moodle.MoodleSendChecker;

/**
 * @author Rodrigo Prestes Machado
 * @version Jan. 2022
 */
@Path("/check")
public class RevisionService {

    private static final Logger LOGGER = Logger.getLogger(RevisionService.class.getName());

    @Inject protected MoodleUserChecker moodleUser;
    @Inject protected RepositoryChecker repository;
    @Inject protected TestChangeChecker testChange;
    @Inject protected MoodleSendChecker moodleSend;

    /**
     * Executes a Github and Moodle chain:
     *
     * 1 - Check if the Github and Moodle users has the same full name
     * 2 - Verifies if the repository is a fork and if the latest workflow in Github
     * Actions was executed with success
     * 3 - Checks if the users changed the test cases
     * 4 - Sends the result to Moodle
     *
     * @param githubProfileURL : The URL of a profile in Github
     * @param moodleProfileURL : The URL of a profile in Moodle
     * @param moodleAssignURL : The URL of an assign in Moodle
     * @param language The language of the return messages
     *
     * @return true if the method was able to execute all chain
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    //@Retry(maxRetries = 2, delay = 1000)
    //@Timeout(5000)
    @Bulkhead(3)
    public boolean check(
            @URL @NotBlank @FormParam("githubProfileURL") String githubProfileURL,
            @URL @NotBlank @FormParam("moodleProfileURL") String moodleProfileURL,
            @URL @NotBlank @FormParam("moodleAssignURL") String moodleAssignURL,
            @HeaderParam("Content-Language") String language) {

                LOGGER.info("msg");
                Map<String,String> input = this.createGithubInput(githubProfileURL, moodleProfileURL, moodleAssignURL, language);
                Checker githubChain = this.createGithubChain();
                return githubChain.check(input);
    }

    /**
     * Creates a Github Chain of checkers
     *
     * @return The first Checker
     */
    private Checker createGithubChain(){
        moodleUser.setNextChecker(repository);
        repository.setNextChecker(testChange);
        testChange.setNextChecker(moodleSend);
        return moodleUser;
    }

    /**
     * Encapsulates the input to a Map object
     *
     * @param githubProfileURL : The URL of a profile in Github
     * @param moodleProfileURL : The URL of a profile in Moodle
     * @param moodleAssignURL : The URL of an assign in Moodle
     * @param language The language of the return messages
     *
     * @return A Map object with all inputs
     */
    private Map<String,String> createGithubInput(
        String githubProfileURL,
        String moodleProfileURL,
        String moodleAssignURL,
        String language){

        Map<String,String> input = new HashMap<>();
        input.put("githubProfileURL", githubProfileURL);
        input.put("moodleProfileURL", moodleProfileURL);
        input.put("moodleAssignURL", moodleAssignURL);
        input.put("language", language);

        return input;
    }

}