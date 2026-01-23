package com.kerem.todo_app.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kerem.todo_app.dto.JwtResponse;
import com.kerem.todo_app.model.User;
import com.kerem.todo_app.repository.UserRepository;
import com.kerem.todo_app.security.JwtUtils;
import com.kerem.todo_app.security.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder encoder;
    
    @Mock
    private JwtUtils jwtUtils;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    private UserDetailsImpl userDetails;
    
    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setId(1L);
        
        userDetails = UserDetailsImpl.build(testUser);
    }
    
    @Test
    void testAuthenticateUser_Success() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String jwt = "jwt.token.here";
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(jwt);
        
        // Act
        JwtResponse response = authService.authenticateUser(username, password);
        
        // Assert
        assertNotNull(response);
        assertEquals(jwt, response.getToken());
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
    }
    
    @Test
    void testRegisterUser_Success() {
        // Arrange
        String username = "newuser";
        String email = "new@example.com";
        String password = "password123";
        
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(encoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        authService.registerUser(username, email, password);
        
        // Assert
        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(encoder).encode(password);
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testRegisterUser_NullUsername() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(null, "test@example.com", "password123");
        });
        
        assertEquals("Error: Username, email, and password can not be empty!", exception.getMessage());
    }
    
    @Test
    void testRegisterUser_ShortUsername() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser("ab", "test@example.com", "password123");
        });
        
        assertEquals("Error: Username must be between 3 and 20 characters!", exception.getMessage());
    }
    
    @Test
    void testRegisterUser_LongUsername() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser("thisusernameiswaytoolong", "test@example.com", "password123");
        });
        
        assertEquals("Error: Username must be between 3 and 20 characters!", exception.getMessage());
    }
    
    @Test
    void testRegisterUser_InvalidEmail() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser("testuser", "invalidemail", "password123");
        });
        
        assertEquals("Error: Invalid email format!", exception.getMessage());
    }
    
    @Test
    void testRegisterUser_ShortPassword() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser("testuser", "test@example.com", "12345");
        });
        
        assertEquals("Error: Password must be at least 6 characters!", exception.getMessage());
    }
    
    @Test
    void testRegisterUser_UsernameTaken() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser("testuser", "test@example.com", "password123");
        });
        
        assertEquals("Error: Username is already taken!", exception.getMessage());
    }
    
    @Test
    void testRegisterUser_EmailInUse() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser("newuser", "test@example.com", "password123");
        });
        
        assertEquals("Error: Email is already in use!", exception.getMessage());
    }
    
    @Test
    void testUpdateUser_Success() {
        // Arrange
        Long userId = 1L;
        String newUsername = "updateduser";
        String newEmail = "updated@example.com";
        String password = "password123";
        String jwt = "new.jwt.token";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(encoder.matches(password, testUser.getPassword())).thenReturn(true);
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(jwt);
        
        // Act
        JwtResponse response = authService.updateUser(userId, newUsername, newEmail, password);
        
        // Assert
        assertNotNull(response);
        assertEquals(jwt, response.getToken());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testUpdateUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.updateUser(1L, "newuser", "new@example.com", "password123");
        });
        
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    void testUpdateUser_IncorrectPassword() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(encoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.updateUser(1L, "newuser", "new@example.com", "wrongpassword");
        });
        
        assertEquals("Error: Incorrect password!", exception.getMessage());
    }
    
    @Test
    void testUpdateUser_UsernameTaken() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(encoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.updateUser(1L, "takenuser", "new@example.com", "password123");
        });
        
        assertEquals("Error: Username is already taken!", exception.getMessage());
    }
    
    @Test
    void testDeleteAccount_Success() {
        // Arrange
        Long userId = 1L;
        String password = "password123";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(encoder.matches(password, testUser.getPassword())).thenReturn(true);
        
        // Act
        authService.deleteAccount(userId, password);
        
        // Assert
        verify(userRepository).delete(testUser);
    }
    
    @Test
    void testDeleteAccount_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.deleteAccount(1L, "password123");
        });
        
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    void testDeleteAccount_IncorrectPassword() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(encoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.deleteAccount(1L, "wrongpassword");
        });
        
        assertEquals("Error: Incorrect password!", exception.getMessage());
    }
}
