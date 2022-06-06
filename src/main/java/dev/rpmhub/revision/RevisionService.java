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
import java.util.ResourceBundle;
import java.util.UUID;
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
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.hibernate.validator.constraints.URL;
import org.jboss.resteasy.client.exception.ResteasyWebApplicationException;

import dev.rpmhub.revision.chain.AbstractChecker;
import dev.rpmhub.revision.chain.Checker;
import dev.rpmhub.revision.chain.github.MoodleUserChecker;
import dev.rpmhub.revision.chain.github.RepositoryChecker;
import dev.rpmhub.revision.chain.github.TestChangeChecker;
import dev.rpmhub.revision.chain.moodle.MoodleSendChecker;
import dev.rpmhub.revision.exceptions.RevisionServiceException;

/**
 * Revision service
 *
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
    @Produces(MediaType.APPLICATION_JSON)
    //@Retry(maxRetries = 2, delay = 1000)
    @Timeout(5000)
    //@Bulkhead(3)
    public Map<String,String> check(
            @URL @NotBlank @FormParam("githubProfileURL") String githubProfileURL,
            @URL @NotBlank @FormParam("moodleProfileURL") String moodleProfileURL,
            @URL @NotBlank @FormParam("moodleAssignURL") String moodleAssignURL,
            @HeaderParam("Content-Language") String language) {
                ResourceBundle messages = AbstractChecker.setLocation(language);
                String message = null;
                String result = null;
                try {
                    Map<String,String> input = this.createGithubInput(githubProfileURL, moodleProfileURL, moodleAssignURL, language);
                    Checker githubChain = this.createGithubChain();
                    LOGGER.info(input.get("hash"));
                    result = Boolean.toString(githubChain.check(input));
                }
                catch (IndexOutOfBoundsException e) {
                    message = messages.getString("RevisionService.IndexOutOfBoundsException");
                    throw new RevisionServiceException(message, Response.Status.NOT_FOUND);
                }
                catch(ResteasyWebApplicationException e){
                    message = messages.getString("RevisionService.ResteasyWebApplicationException");
                    throw new RevisionServiceException(message, Response.Status.NOT_FOUND);
                }
                catch(NullPointerException e){
                    message = messages.getString("RevisionService.NullPointerException");
                    throw new RevisionServiceException(message, Response.Status.NOT_FOUND);
                }

                if (result == "true") {
                    message = "Tarefa enviada com sucesso!";
                    System.out.println("revision.SUCESSO --- git: " + githubProfileURL + "  moodle: " + moodleProfileURL);

                }
                Map<String, String> response = Map.of("Message", message);
                return response;
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
        input.put("hash", UUID.randomUUID().toString());

        return input;
    }

}