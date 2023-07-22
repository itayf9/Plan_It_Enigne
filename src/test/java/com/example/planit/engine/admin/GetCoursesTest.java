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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GetCoursesTest {

    @MockBean
    private CoursesRepository coursesRepo;

    @MockBean
    private UserRepository userRepo;

    @Autowired
    private AdminEngine adminEngine;

    @Test
    public void testGetAllCoursesFromDB_ValidAdminUser_ReturnsCourses() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Prepare test courses data
        List<Course> courses = List.of(new Course());

        // Mock the courseRepo.findAll method to return the test courses
        when(coursesRepo.findAll()).thenReturn(courses);

        // Call the method
        DTOcoursesResponseToController response = adminEngine.getAllCoursesFromDB(adminSub);

        // Verify the result
        assertTrue(response.isSucceed());
        assertEquals(Constants.NO_PROBLEM, response.getDetails());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(courses, response.getCourses());
    }

    @Test
    public void testGetAllCoursesFromDB_NonAdminUser_Unauthorized() {
        // Prepare test data
        String nonAdminSub = "nonAdminSubjectID";
        User nonAdminUser = new User();
        nonAdminUser.setAdmin(false);

        // Mock the userRepo.findUserBySubjectID method to return the nonAdminUser
        when(userRepo.findUserBySubjectID(nonAdminSub)).thenReturn(Optional.of(nonAdminUser));

        // Call the method
        DTOcoursesResponseToController response = adminEngine.getAllCoursesFromDB(nonAdminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_UNAUTHORIZED_USER, response.getDetails());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getHttpStatus());

        // Make sure courseRepo.findAll is not called (non-admin user should not access courses)
        verify(coursesRepo, never()).findAll();
    }

    @Test
    public void testGetAllCoursesFromDB_UserNotFound_ReturnsBadRequest() {
        // Prepare test data
        String unknownUserSub = "unknownSubjectID";

        // Mock the userRepo.findUserBySubjectID method to return an empty Optional (user not found)
        when(userRepo.findUserBySubjectID(unknownUserSub)).thenReturn(Optional.empty());

        // Call the method
        DTOcoursesResponseToController response = adminEngine.getAllCoursesFromDB(unknownUserSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_USER_NOT_FOUND, response.getDetails());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

        // Make sure courseRepo.findAll is not called (user not found)
        verify(coursesRepo, never()).findAll();
    }

    @Test
    public void testGetAllCoursesFromDB_ExceptionThrown_InternalServerError() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Mock the userRepo.findUserBySubjectID method to throw an exception when called with existingUserSub
        when(coursesRepo.findAll()).thenThrow(new RuntimeException("Simulated database exception"));

        // Call the method
        DTOcoursesResponseToController response = adminEngine.getAllCoursesFromDB(adminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_DEFAULT, response.getDetails());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
    }
}
