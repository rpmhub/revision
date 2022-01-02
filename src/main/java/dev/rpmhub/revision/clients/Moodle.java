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

package dev.rpmhub.revision.clients;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import dev.rpmhub.revision.mappers.moodle.ListCourse;
import dev.rpmhub.revision.mappers.moodle.ListUser;
import dev.rpmhub.revision.mappers.moodle.Module;

@RegisterRestClient
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public interface Moodle {

        @POST
        public ListUser getUser(
                        @FormParam("wstoken") String wstoken,
                        @FormParam("wsfunction") String wsfunction,
                        @FormParam("moodlewsrestformat") String restFormat,
                        @FormParam("criteria[0][key]") String key,
                        @FormParam("criteria[0][value]") String value);

        @POST
        public Module getModule(
                        @FormParam("wstoken") String wstoken,
                        @FormParam("wsfunction") String wsfunction,
                        @FormParam("moodlewsrestformat") String restFormat,
                        @FormParam("cmid") String cmid);

        @POST
        public ListCourse getCourses(
                        @FormParam("wstoken") String wstoken,
                        @FormParam("wsfunction") String wsfunction,
                        @FormParam("moodlewsrestformat") String restFormat,
                        @FormParam("courseids[0]") String idCourse);

        @POST
        public void updateGrade(
                        @FormParam("wstoken") String wstoken,
                        @FormParam("wsfunction") String wsfunction,
                        @FormParam("moodlewsrestformat") String restFormat,
                        @FormParam("assignmentid") int idAssign,
                        @FormParam("userid") int idUser,
                        @FormParam("grade") float grade,
                        @FormParam("attemptnumber") int attemptnumber,
                        @FormParam("addattempt") int addattempt,
                        @FormParam("workflowstate") String workflowstate,
                        @FormParam("applytoall") int applytoall,
                        @FormParam("plugindata[assignfeedbackcomments_editor][text]") String comment,
                        @FormParam("plugindata[assignfeedbackcomments_editor][format]") int editorFormat);

}
