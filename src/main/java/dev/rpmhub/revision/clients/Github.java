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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import dev.rpmhub.revision.mappers.github.Commit;
import dev.rpmhub.revision.mappers.github.CommitData;
import dev.rpmhub.revision.mappers.github.ListWorkflow;
import dev.rpmhub.revision.mappers.moodle.User;

/**
 * Github Rest client
 *
 * @author Rodrigo Prestes Machado
 * @version Jan. 2022
 */
@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
public interface Github {

        @GET
        @Path("/users/{login}")
        public User getUser(@PathParam("login") String login);

        @GET
        @Path("/repos/{owner}/{repo}/actions/workflows/{workflow_id}/runs")
        public ListWorkflow getRuns(
                @PathParam("owner") String owner,
                @PathParam("repo") String repo,
                @PathParam("workflow_id") String idWorkflow);

        @GET
        @Path("/repos/{owner}/{repo}/commits")
        public List<CommitData> getCommits(
                @PathParam("owner") String owner,
                @PathParam("repo") String repo);

        @GET
        @Path("/repos/{owner}/{repo}/commits/{sha}")
        public Commit getCommit(
                @PathParam("owner") String owner,
                @PathParam("repo") String repo,
                @PathParam("sha") String sha);
}
