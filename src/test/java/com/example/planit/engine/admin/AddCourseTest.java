package com.example.planit.engine.admin;

import com.example.planit.engine.AdminEngine;
import com.example.planit.model.mongo.course.Course;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOcoursesResponseToController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class AddCourseTest {

    @MockBean
    private CoursesRepository coursesRepo;

    @MockBean
    private UserRepository userRepo;

    @Autowired
    private AdminEngine adminEngine;

    @Test
    public void testAddCourseToDB_ValidAdminUser_CourseAdded() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Prepare test course data
        Course courseToAdd = new Course();

        // Mock the courseRepo.findCourseById and courseRepo.findCourseByCourseName methods
        when(coursesRepo.findCourseById(courseToAdd.getCourseId())).thenReturn(Optional.empty());
        when(coursesRepo.findCourseByCourseName(courseToAdd.getCourseName())).thenReturn(Optional.empty());

        // Call the method
        DTOcoursesResponseToController response = adminEngine.addCourseToDB(courseToAdd, adminSub);

        // Verify the result
        assertTrue(response.isSucceed());
        assertEquals(Constants.NO_PROBLEM, response.getDetails());
        assertEquals(HttpStatus.CREATED, response.getHttpStatus());

        // Verify that the courseRepo.save method was called with the correct course
        verify(coursesRepo).save(courseToAdd);
    }

    @Test
    public void testAddCourseToDB_CourseAlreadyExistsById_ReturnsBadRequest() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Prepare test course data
        Course existingCourse = new Course();

        // Mock the courseRepo.findCourseById and courseRepo.findCourseByCourseName methods
        when(coursesRepo.findCourseById(existingCourse.getCourseId())).thenReturn(Optional.of(existingCourse));

        // Call the method
        DTOcoursesResponseToController response = adminEngine.addCourseToDB(existingCourse, adminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_COURSE_ALREADY_EXIST, response.getDetails());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

        // Verify that the courseRepo.save method was not called (course already exists)
        verify(coursesRepo, never()).save(existingCourse);
    }

    @Test
    public void testAddCourseToDB_CourseAlreadyExistsByName_ReturnsBadRequest() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Prepare test course data
        Course existingCourse = new Course();
        existingCourse.setCourseName("someExistingCourseName");

        // Mock the courseRepo.findCourseById and courseRepo.findCourseByCourseName methods
        when(coursesRepo.findCourseByCourseName(existingCourse.getCourseName())).thenReturn(Optional.of(existingCourse));

        // Call the method
        DTOcoursesResponseToController response = adminEngine.addCourseToDB(existingCourse, adminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_COURSE_ALREADY_EXIST, response.getDetails());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

        // Verify that the courseRepo.save method was not called (course already exists)
        verify(coursesRepo, never()).save(existingCourse);
    }

    @Test
    public void testAddCourseToDB_NonAdminUser_Unauthorized() {
        // Prepare test data
        String nonAdminSub = "nonAdminSubjectID";
        User nonAdminUser = new User();
        nonAdminUser.setAdmin(false);

        // Mock the userRepo.findUserBySubjectID method to return the nonAdminUser
        when(userRepo.findUserBySubjectID(nonAdminSub)).thenReturn(Optional.of(nonAdminUser));

        // Prepare test course data
        Course courseToAdd = new Course();

        // Call the method
        DTOcoursesResponseToController response = adminEngine.addCourseToDB(courseToAdd, nonAdminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_UNAUTHORIZED_USER, response.getDetails());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getHttpStatus());

        // Verify that the courseRepo.save method was not called (non-admin user cannot add courses)
        verify(coursesRepo, never()).save(courseToAdd);
    }

    @Test
    public void testAddCourseToDB_adminUserNotExist_ReturnsBadRequest() {
        // Prepare test data
        String nonExistingAdminSub = "adminSubjectID";

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(nonExistingAdminSub)).thenReturn(Optional.empty());

        // Mock the courseRepo.findCourseById method to return an empty Optional (course not found)
        when(coursesRepo.findCourseById(any())).thenReturn(Optional.empty());

        // Call the method
        DTOcoursesResponseToController response = adminEngine.addCourseToDB(new Course(), nonExistingAdminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_USER_NOT_FOUND, response.getDetails());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    }

    @Test
    public void testAddCourseToDB_ExceptionThrown_InternalServerError() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Mock the userRepo.findUserBySubjectID method to throw an exception when called with existingUserSub
        when(coursesRepo.findCourseById(any())).thenThrow(new RuntimeException("Simulated database exception"));

        // Call the method
        DTOcoursesResponseToController response = adminEngine.addCourseToDB(new Course(), adminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_DEFAULT, response.getDetails());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
    }
}
