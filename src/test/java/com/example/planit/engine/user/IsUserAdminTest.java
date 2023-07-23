package com.example.planit.engine.user;

import com.example.planit.engine.UserEngine;
import com.example.planit.model.mongo.user.User;
import com.example.planit.model.mongo.user.UserRepository;
import com.example.planit.utill.exception.UnauthorizedUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IsUserAdminTest {

    @MockBean
    private UserRepository userRepo;

    @Autowired
    private UserEngine userEngine;

    @Test
    public void testIsTheUserWithThisSubjectIdAnAdmin_UserIsAdmin_ReturnsTrue() {
        // Prepare test data
        String subjectID = "adminSubjectID";
        User adminUser = new User();
        adminUser.setAdmin(true);

        // Mock the userRepo.findUserBySubjectID method to return the adminUser
        when(userRepo.findUserBySubjectID(subjectID)).thenReturn(Optional.of(adminUser));

        // Call the method
        boolean isAdmin = userEngine.isTheUserWithThisSubjectIdAnAdmin(subjectID);

        // Verify the result
        assertTrue(isAdmin);
    }

    @Test
    public void testIsTheUserWithThisSubjectIdAnAdmin_UserIsNotAdmin_ReturnsFalse() {
        // Prepare test data
        String subjectID = "nonAdminSubjectID";
        User nonAdminUser = new User();
        nonAdminUser.setAdmin(false);

        // Mock the userRepo.findUserBySubjectID method to return the nonAdminUser
        when(userRepo.findUserBySubjectID(subjectID)).thenReturn(Optional.of(nonAdminUser));

        // Call the method
        boolean isAdmin = userEngine.isTheUserWithThisSubjectIdAnAdmin(subjectID);

        // Verify the result
        assertFalse(isAdmin);
    }

    @Test
    public void testIsTheUserWithThisSubjectIdAnAdmin_UserNotFound_ThrowsUnauthorizedUserException() {
        // Prepare test data
        String unknownSubjectID = "unknownSubjectID";

        // Mock the userRepo.findUserBySubjectID method to return an empty Optional (user not found)
        when(userRepo.findUserBySubjectID(unknownSubjectID)).thenReturn(Optional.empty());

        // Verify that the method throws the expected exception
        assertThrows(UnauthorizedUserException.class, () -> userEngine.isTheUserWithThisSubjectIdAnAdmin(unknownSubjectID));
    }
}
