package com.example.planit.engine.admin;

import com.example.planit.engine.AdminEngine;
import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.Constants;
import com.example.planit.utill.dto.DTOusersResponseToController;
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
public class MakeUserAdminTest {

    @MockBean
    private UserRepository userRepo;

    @Autowired
    private AdminEngine adminEngine;

    @Test
    public void testMakeUserAdminInDB_ValidAdminUser_Success() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        String existingUserSub = "existingUserSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);
        User existingUser = new User();
        existingUser.setAdmin(false);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser and existingUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));
        when(userRepo.findUserBySubjectID(existingUserSub)).thenReturn(Optional.of(existingUser));


        // Call the method
        DTOusersResponseToController response = adminEngine.makeUserAdminInDB(adminSub, existingUserSub);

        // Verify the result
        assertTrue(response.isSucceed());
        assertEquals(Constants.NO_PROBLEM, response.getDetails());
        assertEquals(HttpStatus.OK, response.getHttpStatus());

        // Verify if the user is set as an admin in the mock userRepo.save method
        verify(userRepo, times(1)).save(existingUser);
        assertTrue(existingUser.isAdmin());
    }

    @Test
    public void testMakeUserAdminInDB_NonAdminUser_Unauthorized() {
        // Prepare test data
        String notAdminSub = "notAdminSubjectID";
        String existingUserSub = "existingUserSubjectID";
        User notAdminUser = new User();
        notAdminUser.setAdmin(false);
        User existingUser = new User();
        existingUser.setAdmin(false);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser and existingUser
        when(userRepo.findUserBySubjectID(notAdminSub)).thenReturn(Optional.of(notAdminUser));
        when(userRepo.findUserBySubjectID(existingUserSub)).thenReturn(Optional.of(existingUser));

        // Call the method
        DTOusersResponseToController response = adminEngine.makeUserAdminInDB(notAdminSub, existingUserSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_UNAUTHORIZED_USER, response.getDetails());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getHttpStatus());

        // Verify that the userRepo.save method was not called (user should not be set as an admin)
        verify(userRepo, never()).save(existingUser);
    }

    @Test
    public void testMakeUserAdminInDB_NotFoundAdminUser() {
        // Prepare test data
        String nonExistingAdminSub = "nonExistingAdminUserSubjectID";
        String existingUserSub = "existingUserSubjectID";
        User existingUser = new User();
        existingUser.setAdmin(false);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser and existingUser
        when(userRepo.findUserBySubjectID(nonExistingAdminSub)).thenReturn(Optional.empty());
        when(userRepo.findUserBySubjectID(existingUserSub)).thenReturn(Optional.of(existingUser));

        // Call the method
        DTOusersResponseToController response = adminEngine.makeUserAdminInDB(nonExistingAdminSub, existingUserSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_USER_NOT_FOUND, response.getDetails());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

        // Verify that the userRepo.save method was not called (user should not be set as an admin)
        verify(userRepo, never()).save(existingUser);
    }

    @Test
    public void testMakeUserAdminInDB_NotFoundExistingUser() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        String nonExistingUserSub = "nonExistingUserSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);


        // Mock the userRepo.findUserBySubjectID method to return the adminUser and existingUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));
        when(userRepo.findUserBySubjectID(nonExistingUserSub)).thenReturn(Optional.empty());

        // Call the method
        DTOusersResponseToController response = adminEngine.makeUserAdminInDB(adminSub, nonExistingUserSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_USER_NOT_FOUND, response.getDetails());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    }

    @Test
    public void testMakeUserAdminInDB_ExceptionThrown_InternalServerError() {
        // Prepare test data
        String adminSub = "adminSubjectID";
        String existingUserSub = "existingUserSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(adminSub)).thenReturn(Optional.of(adminUser));

        // Mock the userRepo.findUserBySubjectID method to throw an exception when called with existingUserSub
        when(userRepo.findUserBySubjectID(existingUserSub)).thenThrow(new RuntimeException("Simulated database exception"));

        // Call the method
        DTOusersResponseToController response = adminEngine.makeUserAdminInDB(adminSub, existingUserSub);

        // Verify the result
        assertFalse(response.isSucceed());
        assertEquals(Constants.ERROR_DEFAULT, response.getDetails());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());

    }
}
