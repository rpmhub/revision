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

package dev.rpmhub.revision.chain.moodle;

import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import dev.rpmhub.revision.chain.AbstractChecker;
import dev.rpmhub.revision.chain.Checker;
import dev.rpmhub.revision.mappers.moodle.Module;
/**
 * Sends the grade to the Moodle
 *
 * @author Rodrigo Prestes Machado
 * @version Jan. 2022
 */
@ApplicationScoped
public class MoodleSendChecker extends AbstractChecker implements Checker {

    private static final Logger LOGGER = Logger.getLogger(MoodleSendChecker.class.getName());

    @Override
    public boolean check(Map<String, String> input) {
        LOGGER.info("MoodleSendChecker");

        // Module in this case will be the assign
        // This step discoveries the course id and the instance id (the data base id)
        // The instance id is necessary to update the grade
        Module module = getCourseModule(input.get("moodleAssignURL"));

        int idUser = Integer.parseInt(getMoodleId(input.get("moodleProfileURL")));

        moodle.updateGrade(MOODLE_TOKEN, MOODLE_GRADE, MOODLE_JSON_FORMAT,
                module.getCm().getInstance(), idUser,
                10, -1, 1, "rpm", 1, input.get("hash"), 2);

        LOGGER.info("A atividade foi atualizada");
        return true;
    }

}
