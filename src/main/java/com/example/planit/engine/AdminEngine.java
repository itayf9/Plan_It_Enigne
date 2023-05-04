package com.example.planit.engine;

import com.example.planit.model.mongo.course.Course;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserClientRepresentation;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOcoursesResponseToController;
import com.example.planit.utill.dto.DTOusersResponseToController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.planit.model.mongo.user.UserClientRepresentation.buildUserClientRepresentationFromUser;

public class AdminEngine {

    private final CoursesRepository courseRepo;

    private final UserRepository userRepo;

    public static Logger logger = LogManager.getLogger(AdminEngine.class);

    public AdminEngine(CoursesRepository courseRepo, UserRepository userRepo) {
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
    }

    public DTOcoursesResponseToController getAllCoursesFromDB(String sub) {

        try {
            Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);

            if (maybeUser.isEmpty()) {
                return new DTOcoursesResponseToController(false, Constants.ERROR_USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
            User maybeAdminUser = maybeUser.get();

            if (!maybeAdminUser.isAdmin()) {
                return new DTOcoursesResponseToController(false, Constants.ERROR_UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
            }

            return new DTOcoursesResponseToController(true, Constants.NO_PROBLEM, HttpStatus.OK, courseRepo.findAll());

        } catch (Exception e) {
            return new DTOcoursesResponseToController(false, "Error fetching courses from database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public DTOcoursesResponseToController addCourseToDB(Course course, String sub) {

        try {

            Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);

            if (maybeUser.isEmpty()) {
                return new DTOcoursesResponseToController(false, Constants.ERROR_USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            User maybeAdminUser = maybeUser.get();

            if (!maybeAdminUser.isAdmin()) {
                return new DTOcoursesResponseToController(false, Constants.ERROR_UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
            }

            // Check if course already exists in database by id
            Optional<Course> existingCourseById = courseRepo.findCourseById(course.getCourseId());
            if (existingCourseById.isPresent()) {
                return new DTOcoursesResponseToController(false, Constants.COURSE_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
            }

            // Check if course already exists in database by name
            Optional<Course> existingCourseByName = courseRepo.findCourseByCourseName(course.getCourseName());
            if (existingCourseByName.isPresent()) {
                return new DTOcoursesResponseToController(false, Constants.COURSE_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
            }

            // Save the new course to the database
            courseRepo.save(course);
            return new DTOcoursesResponseToController(true, Constants.NO_PROBLEM, HttpStatus.CREATED);

        } catch (Exception e) {
            return new DTOcoursesResponseToController(false, "Error adding course to database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public DTOcoursesResponseToController updateCourseInDB(Course course, String sub) {
        try {

            Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);

            if (maybeUser.isEmpty()) {
                return new DTOcoursesResponseToController(false, Constants.ERROR_USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            User maybeAdminUser = maybeUser.get();

            if (!maybeAdminUser.isAdmin()) {
                return new DTOcoursesResponseToController(false, Constants.ERROR_UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
            }


            // Check if course exists in database
            Optional<Course> maybeExistingCourse = courseRepo.findCourseById(course.getCourseId());
            if (maybeExistingCourse.isEmpty()) {
                return new DTOcoursesResponseToController(false, Constants.ERROR_COURSE_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            // Update the existing course in the database
            Course existingCourse = maybeExistingCourse.get();

            existingCourse.setCourseName(course.getCourseName());
            existingCourse.setCourseSubjects(course.getCourseSubjects());
            existingCourse.setCredits(course.getCredits());
            existingCourse.setDifficultyLevel(course.getDifficultyLevel());
            existingCourse.setRecommendedStudyTime(course.getRecommendedStudyTime());
            existingCourse.setSubjectsPracticePercentage(course.getSubjectsPracticePercentage());

            courseRepo.save(existingCourse);
            return new DTOcoursesResponseToController(true, Constants.NO_PROBLEM, HttpStatus.OK);

        } catch (Exception e) {
            return new DTOcoursesResponseToController(false, "Error updating course in database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public DTOcoursesResponseToController getCourseFromDB(String courseId, String sub) {
        try {
            Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);

            if (maybeUser.isEmpty()) {
                return new DTOcoursesResponseToController(false, Constants.ERROR_USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
            User maybeAdminUser = maybeUser.get();

            if (!maybeAdminUser.isAdmin()) {
                return new DTOcoursesResponseToController(false, Constants.ERROR_UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
            }

            Optional<Course> maybeCourse = courseRepo.findCourseById(courseId);

            if (maybeCourse.isEmpty()) {
                return new DTOcoursesResponseToController(false, Constants.ERROR_COURSE_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
            // return list of single Course because I am tired, and it's 04:44
            return new DTOcoursesResponseToController(true, Constants.NO_PROBLEM, HttpStatus.OK, List.of(maybeCourse.get()));

        } catch (Exception e) {
            return new DTOcoursesResponseToController(false, "Error fetching course from database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public DTOusersResponseToController getAllUsersFromDB(String sub) {
        try {
            Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);

            if (maybeUser.isEmpty()) {
                return new DTOusersResponseToController(false, Constants.ERROR_USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
            User maybeAdminUser = maybeUser.get();

            if (!maybeAdminUser.isAdmin()) {
                return new DTOusersResponseToController(false, Constants.ERROR_UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
            }

            List<User> users = userRepo.findAll();

            List<UserClientRepresentation> userClientRepresentations = new ArrayList<>();

            for (User user : users) {
                userClientRepresentations.add(buildUserClientRepresentationFromUser(user));
            }

            return new DTOusersResponseToController(true, Constants.NO_PROBLEM, HttpStatus.OK, userClientRepresentations);

        } catch (Exception e) {
            return new DTOusersResponseToController(false, "Error fetching users from database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public DTOusersResponseToController makeUserAdminInDB(String sub, String userSubId) {

        try {

            Optional<User> maybeUser = userRepo.findUserBySubjectID(sub);

            if (maybeUser.isEmpty()) {
                return new DTOusersResponseToController(false, Constants.ERROR_USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            User maybeAdminUser = maybeUser.get();

            if (!maybeAdminUser.isAdmin()) {
                return new DTOusersResponseToController(false, Constants.ERROR_UNAUTHORIZED_USER, HttpStatus.UNAUTHORIZED);
            }

            // Check if user exists in database
            Optional<User> maybeExistingUser = userRepo.findUserBySubjectID(userSubId);
            if (maybeExistingUser.isEmpty()) {
                return new DTOusersResponseToController(false, Constants.ERROR_USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            // Update the existing user in the database
            User existingUser = maybeExistingUser.get();
            existingUser.setAdmin(true);

            userRepo.save(existingUser);
            return new DTOusersResponseToController(true, Constants.NO_PROBLEM, HttpStatus.OK);

        } catch (Exception e) {
            return new DTOusersResponseToController(false, "Error updating user in database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
