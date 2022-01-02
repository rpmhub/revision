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

import java.util.Locale;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import dev.rpmhub.revision.clients.Github;
import dev.rpmhub.revision.clients.Moodle;

public abstract class AbstractService {

    protected ResourceBundle messages;

    @Inject
    @RestClient
    protected Github github;

    @Inject
    @RestClient
    protected Moodle moodle;

    @ConfigProperty(name = "moodle.client.wstoken")
    protected String MOODLE_TOKEN;

    @ConfigProperty(name = "moodle.client.moodlewsrestformat")
    protected String MOODLE_JSON_FORMAT;

    @ConfigProperty(name = "moodle.client.wsfunction.users")
    protected String MOODLE_USERS;

    @ConfigProperty(name = "moodle.client.wsfunction.module")
    protected String MOODLE_MODULE;

    @ConfigProperty(name = "moodle.client.wsfunction.assign")
    protected String MOODLE_ASSIGN;

    @ConfigProperty(name = "moodle.client.wsfunction.grade")
    protected String MOODLE_GRADE;

    /**
     * Sets the localization of the strings
     *
     * @param The HTTP Content-Language of the request
     */
    protected void setLocation(String language) {
        Locale locale;
        if (language != null) {
            locale = language.contains("en") ? new Locale("en", "US") : new Locale("pt", "BR");
        } else {
            locale = new Locale("pt", "BR");

        }
        this.messages = ResourceBundle.getBundle("Messages", locale);
    }

}
