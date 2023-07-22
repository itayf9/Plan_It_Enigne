package com.example.planit.engine.admin;

import com.example.planit.engine.AdminEngine;
import com.example.planit.model.mongo.course.CoursesRepository;
import com.example.planit.model.mongo.user.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.example.planit.model.mongo.course.Course;
import com.example.planit.model.mongo.user.User;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOcoursesResponseToController;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GetCourseTest {

    @MockBean
    private CoursesRepository coursesRepo;

    @MockBean
    private UserRepository userRepo;

    @Autowired
    private AdminEngine adminEngine;

    @Test
    public void testGetCourseFromDB_ValidAdminUser_CourseFound() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Prepare test course data
        Course foundCourse = new Course();

        // Mock the courseRepo.findCourseById method to return the foundCourse
        when(coursesRepo.findCourseById(foundCourse.getCourseId())).thenReturn(Optional.of(foundCourse));

        // Call the method
        DTOcoursesResponseToController response = adminEngine.getCourseFromDB(foundCourse.getCourseId(), adminSub);

        // Verify the result
        assertTrue(response.isSucceed());
        assertEquals(Constants.NO_PROBLEM, response.getDetails());
        assertEquals(HttpStatus.OK, response.getHttpStatus());

        // Verify that the response contains the foundCourse
        assertEquals(1, response.getCourses().size());
        assertEquals(foundCourse, response.getCourses().get(0));
    }

    @Test
    public void testGetCourseFromDB_CourseNotFound_ReturnsBadRequest() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Prepare test course data
        String courseId = "unknownCourseId";

        // Mock the courseRepo.findCourseById method to return an empty Optional (course not found)
        when(coursesRepo.findCourseById(courseId)).thenReturn(Optional.empty());

        // Call the method
        DTOcoursesResponseToController response = adminEngine.getCourseFromDB(courseId, adminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_COURSE_NOT_FOUND, response.getDetails());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

        // Verify that the response does not contain any courses
        assertEquals(0, response.getCourses().size());
    }

    @Test
    public void testGetCourseFromDB_adminUserNotExist_ReturnsBadRequest() {
        // Prepare test data
        String nonExistingAdminSub = "adminSubjectID";

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(nonExistingAdminSub)).thenReturn(Optional.empty());

        // Mock the courseRepo.findCourseById method to return an empty Optional (course not found)
        when(coursesRepo.findCourseById(any())).thenReturn(Optional.empty());

        // Call the method
        DTOcoursesResponseToController response = adminEngine.getCourseFromDB(any(), nonExistingAdminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_USER_NOT_FOUND, response.getDetails());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    }

    @Test
    public void testGetCourseFromDB_NonAdminUser_Unauthorized() {
        // Prepare test data
        String nonAdminSub = "nonAdminSubjectID";
        User nonAdminUser = new User();
        nonAdminUser.setAdmin(false);

        // Mock the userRepo.findUserBySubjectID method to return the nonAdminUser
        when(userRepo.findUserBySubjectID(nonAdminSub)).thenReturn(Optional.of(nonAdminUser));

        // Prepare test course data
        String courseId = "someCourseId";

        // Call the method
        DTOcoursesResponseToController response = adminEngine.getCourseFromDB(courseId, nonAdminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_UNAUTHORIZED_USER, response.getDetails());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getHttpStatus());

        // Verify that the courseRepo.findCourseById method was not called (non-admin user cannot access courses)
        verify(coursesRepo, never()).findCourseById(courseId);
    }

    @Test
    public void testGetCourseFromDB_ExceptionThrown_InternalServerError() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Mock the userRepo.findUserBySubjectID method to throw an exception when called with existingUserSub
        when(coursesRepo.findCourseById(any())).thenThrow(new RuntimeException("Simulated database exception"));

        // Call the method
        DTOcoursesResponseToController response = adminEngine.getCourseFromDB("1", adminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_DEFAULT, response.getDetails());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
    }

}
