package com.khazhimetov.library.service;

import com.khazhimetov.library.dao.UserDao;
import com.khazhimetov.library.exception.InactiveAccountException;
import com.khazhimetov.library.exception.ServiceException;
import com.khazhimetov.library.model.User;
import com.khazhimetov.library.model.UserRole;
import com.khazhimetov.library.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userDao);
    }

    @Test
    void register_SuccessfullyRegistersNewUser() {
        // Given
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("plainPassword");
        newUser.setFirstName("New");
        newUser.setLastName("User");

        when(userDao.findByEmail(newUser.getEmail())).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        try (MockedStatic<PasswordEncoder> mockedEncoder = mockStatic(PasswordEncoder.class)) {
            mockedEncoder.when(() -> PasswordEncoder.encode("plainPassword"))
                    .thenReturn("hashedPassword");

            // When
            User registeredUser = userService.register(newUser);

            // Then
            assertNotNull(registeredUser);
            assertEquals(1L, registeredUser.getId());
            assertEquals("hashedPassword", registeredUser.getPassword());
            assertEquals(UserRole.READER, registeredUser.getRole());
            assertTrue(registeredUser.isActive());

            verify(userDao).findByEmail(newUser.getEmail());
            verify(userDao).save(any(User.class));
        }
    }

    @Test
    void register_ThrowsException_WhenEmailAlreadyExists() {
        // Given
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("password");

        when(userDao.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.register(existingUser));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(userDao).findByEmail(existingUser.getEmail());
        verify(userDao, never()).save(any());
    }

    @Test
    void authenticate_ReturnsUser_WhenCredentialsValid() {
        // Given
        String email = "test@example.com";
        String plainPassword = "password123";
        String hashedPassword = "hashedPassword";

        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setActive(true);

        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));

        try (MockedStatic<PasswordEncoder> mockedEncoder = mockStatic(PasswordEncoder.class)) {
            mockedEncoder.when(() -> PasswordEncoder.matches(plainPassword, hashedPassword))
                    .thenReturn(true);

            // When
            Optional<User> result = userService.authenticate(email, plainPassword);

            // Then
            assertTrue(result.isPresent());
            assertEquals(user.getId(), result.get().getId());
            assertEquals(email, result.get().getEmail());
        }
    }

    @Test
    void authenticate_ThrowsException_WhenAccountInactive() {
        // Given
        String email = "inactive@example.com";
        String password = "password";

        User inactiveUser = new User();
        inactiveUser.setEmail(email);
        inactiveUser.setPassword("hashedPassword");
        inactiveUser.setActive(false);

        when(userDao.findByEmail(email)).thenReturn(Optional.of(inactiveUser));

        // When & Then
        assertThrows(InactiveAccountException.class,
                () -> userService.authenticate(email, password));
    }

    @Test
    void authenticate_ReturnsEmpty_WhenPasswordIncorrect() {
        // Given
        String email = "test@example.com";
        String wrongPassword = "wrongPassword";
        String hashedPassword = "hashedPassword";

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setActive(true);

        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));

        try (MockedStatic<PasswordEncoder> mockedEncoder = mockStatic(PasswordEncoder.class)) {
            mockedEncoder.when(() -> PasswordEncoder.matches(wrongPassword, hashedPassword))
                    .thenReturn(false);

            // When
            Optional<User> result = userService.authenticate(email, wrongPassword);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Test
    void authenticate_ReturnsEmpty_WhenUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        String password = "password";

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.authenticate(email, password);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findById_ReturnsUser_WhenExists() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        verify(userDao).findById(userId);
    }

    @Test
    void findAll_ReturnsAllUsers() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        List<User> users = Arrays.asList(user1, user2);
        when(userDao.findAll()).thenReturn(users);

        // When
        List<User> result = userService.findAll();

        // Then
        assertEquals(2, result.size());
        verify(userDao).findAll();
    }

    @Test
    void update_UpdatesUser_WhenExists() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPassword("oldHashedPassword");

        User updateUser = new User();
        updateUser.setId(1L);
        updateUser.setEmail("updated@example.com");
        updateUser.setPassword("newPassword");

        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userDao.update(any(User.class))).thenReturn(updateUser);

        try (MockedStatic<PasswordEncoder> mockedEncoder = mockStatic(PasswordEncoder.class)) {
            mockedEncoder.when(() -> PasswordEncoder.encode("newPassword"))
                    .thenReturn("newHashedPassword");

            // When
            User result = userService.update(updateUser);

            // Then
            assertNotNull(result);
            verify(userDao).findById(1L);
            verify(userDao).update(argThat(user ->
                    "newHashedPassword".equals(user.getPassword())));
        }
    }

    @Test
    void update_KeepsOldPassword_WhenNewPasswordNotProvided() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPassword("existingHashedPassword");

        User updateUser = new User();
        updateUser.setId(1L);
        updateUser.setEmail("updated@example.com");
        updateUser.setPassword(""); // Empty password

        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userDao.update(any(User.class))).thenReturn(updateUser);

        // When
        User result = userService.update(updateUser);

        // Then
        verify(userDao).update(argThat(user ->
                "existingHashedPassword".equals(user.getPassword())));
    }

    @Test
    void update_ThrowsException_WhenUserNotFound() {
        // Given
        User updateUser = new User();
        updateUser.setId(999L);

        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.update(updateUser));

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userDao, never()).update(any());
    }

    @Test
    void delete_CallsDao() {
        // Given
        Long userId = 1L;
        when(userDao.deleteById(userId)).thenReturn(true);

        // When
        boolean result = userService.delete(userId);

        // Then
        assertTrue(result);
        verify(userDao).deleteById(userId);
    }

    @Test
    void toggleActiveStatus_TogglesUserStatus() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setActive(true);
        user.setEmail("test@example.com");

        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        when(userDao.update(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        // When
        User result = userService.toggleActiveStatus(userId);

        // Then
        assertFalse(result.isActive());
        verify(userDao).findById(userId);
        verify(userDao).update(argThat(u -> !u.isActive()));
    }

    @Test
    void toggleActiveStatus_ThrowsException_WhenUserNotFound() {
        // Given
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.toggleActiveStatus(userId));

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userDao, never()).update(any());
    }
}
