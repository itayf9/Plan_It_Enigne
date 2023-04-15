package com.example.planit.engine;

import com.example.planit.model.mongo.course.Course;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOcoursesResponseToController;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

public class AdminEngine {

    private final CoursesRepository courseRepo;

    private final UserRepository userRepo;

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

            // Check if course already exists in database
            Optional<Course> existingCourse = courseRepo.findCourseById(course.getId());
            if (existingCourse.isPresent()) {
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
            Optional<Course> maybeExistingCourse = courseRepo.findCourseById(course.getId());
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
}
