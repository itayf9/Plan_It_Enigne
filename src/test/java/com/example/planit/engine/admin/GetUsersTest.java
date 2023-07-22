package com.example.planit.engine.admin;

import com.example.planit.engine.AdminEngine;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.dto.DTOusersResponseToController;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.example.planit.model.mongo.user.User;
import com.example.planit.utill.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GetUsersTest {

    @MockBean
    private UserRepository userRepo;

    @Autowired
    private AdminEngine adminEngine;

    @Test
    public void testGetAllUsersFromDB_ValidAdminUser_ReturnsUsers() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Prepare test users data
        List<User> users = List.of(new User());

        // Mock the userRepo.findAll method to return the test users
        when(userRepo.findAll()).thenReturn(users);

        // Call the method
        DTOusersResponseToController response = adminEngine.getAllUsersFromDB(adminSub);

        // Verify the result
        assertTrue(response.isSucceed());
        assertEquals(Constants.NO_PROBLEM, response.getDetails());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
    }

    @Test
    public void testGetAllUsersFromDB_NonAdminUser_Unauthorized() {
        // Prepare test data
        String nonAdminSub = "nonAdminSubjectID";
        User nonAdminUser = new User();
        nonAdminUser.setAdmin(false);

        // Mock the userRepo.findUserBySubjectID method to return the nonAdminUser
        when(userRepo.findUserBySubjectID(nonAdminSub)).thenReturn(Optional.of(nonAdminUser));

        // Call the method
        DTOusersResponseToController response = adminEngine.getAllUsersFromDB(nonAdminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_UNAUTHORIZED_USER, response.getDetails());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getHttpStatus());

        // Verify that the userRepo.findAll method was not called (non-admin user cannot access users)
        verify(userRepo, never()).findAll();
    }

    @Test
    public void testGetAllUsersFromDB_UserNotFound_ReturnsBadRequest() {
        // Prepare test data
        String unknownUserSub = "unknownSubjectID";

        // Mock the userRepo.findUserBySubjectID method to return an empty Optional (user not found)
        when(userRepo.findUserBySubjectID(unknownUserSub)).thenReturn(Optional.empty());

        // Call the method
        DTOusersResponseToController response = adminEngine.getAllUsersFromDB(unknownUserSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_USER_NOT_FOUND, response.getDetails());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

        // Verify that the userRepo.findAll method was not called (user not found)
        verify(userRepo, never()).findAll();
    }

    @Test
    public void testGetAllUsersFromDB_ExceptionThrown_InternalServerError() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Simulate an exception when calling userRepo.findAll
        when(userRepo.findAll()).thenThrow(new RuntimeException("Test Exception"));

        // Call the method
        DTOusersResponseToController response = adminEngine.getAllUsersFromDB(adminSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_DEFAULT, response.getDetails());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
    }
}
