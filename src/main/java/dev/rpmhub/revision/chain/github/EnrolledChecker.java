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
import dev.rpmhub.revision.mappers.moodle.Module;

/**
 * Check if the student is enrolled in the course
 *
 * @author Paulo Serpa Antunes
 * @version May. 2022
 */
@ApplicationScoped
public class EnrolledChecker extends AbstractChecker implements Checker {

  private static final Logger LOGGER = Logger.getLogger(RepositoryChecker.class.getName());

  @Override
  public boolean check(Map<String, String> input) {
    LOGGER.info("EnrolledChecker");

    boolean result = false;

    // Get course module
    Module module = getCourseModule(input.get("moodleAssignURL"));

    // Get Moodle user
    ListUser mUsers = getMoodleUser(input.get("moodleProfileURL"));

    // Get list of enrolled students
    ListUser mCourseUsers = getMoodleEnrolledUsers(module);
    boolean i = false;
    
    for (User user : mCourseUsers) {
      // Verifies if the user are enrolled in this course module
      if (mUsers.getFirstUserName().equalsIgnoreCase(user.getFullname())) {
        i = true;        
        break;
      } 
    }

    if (i == true) {
      LOGGER.info("O usuário está matrículado no curso");
      result = this.getNextChecker().check(input);
    } else {
      String message = messages.getString("EnrolledChecker.notEnrolled");
      LOGGER.log(Level.WARNING, message);
      throw new RevisionServiceException(message, Response.Status.BAD_REQUEST);
    }
    
    return result;
  }
}
