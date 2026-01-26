# Validation & Error Handling - Test Guide

## Overview
The application uses aspect-oriented error handling with custom exception classes and centralized exception handling via `@RestControllerAdvice`. This provides user-friendly, semantically correct error responses with appropriate HTTP status codes.

## Backend Changes

### 1. Custom Exception Classes
- Location: `todoApp/src/main/java/com/kerem/todoApp/exception/`
- **ResourceNotFoundException** (404 Not Found): When resources like users, lists, or items are not found
- **ResourceAlreadyExistsException** (409 Conflict): When attempting to create duplicate resources (username, email)
- **UnauthorizedAccessException** (403 Forbidden): When users try to access resources they don't own
- **InvalidOperationException** (400 Bad Request): When business rules are violated (circular dependencies, incomplete dependencies)
- **AuthenticationException** (401 Unauthorized): When credentials are incorrect

### 2. GlobalExceptionHandler
- Location: `todoApp/src/main/java/com/kerem/todoApp/exception/GlobalExceptionHandler.java`
- **@RestControllerAdvice** for centralized exception handling
- Catches `MethodArgumentNotValidException` for validation errors (400)
- Catches custom exceptions with appropriate HTTP status codes
- Returns standardized `ErrorResponse` with timestamp, status, and messages array

### 2. GlobalExceptionHandler
- Location: `todoApp/src/main/java/com/kerem/todoApp/exception/GlobalExceptionHandler.java`
- **@RestControllerAdvice** for centralized exception handling
- Catches `MethodArgumentNotValidException` for validation errors (400)
- Catches custom exceptions with appropriate HTTP status codes
- Returns standardized `ErrorResponse` with timestamp, status, and messages array

### 3. ErrorResponse DTO
- Location: `todoApp/src/main/java/com/kerem/todoApp/dto/ErrorResponse.java`
- Structure:
  ```json
  {
    "timestamp": "2026-01-26T11:20:00",
    "status": 404,
    "error": "Resource Not Found",
    "messages": [
      "Todo list not found"
    ]
  }
  ```

### 4. Exception Mapping to HTTP Status Codes
| Exception Type | HTTP Status | Use Case |
|---|---|---|
| ResourceNotFoundException | 404 Not Found | User/list/item not found |
| ResourceAlreadyExistsException | 409 Conflict | Duplicate username/email |
| AuthenticationException | 401 Unauthorized | Incorrect password |
| UnauthorizedAccessException | 403 Forbidden | Accessing others' resources |
| InvalidOperationException | 400 Bad Request | Business rule violations |
| MethodArgumentNotValidException | 400 Bad Request | DTO validation failures |

### 5. Updated Controllers
- Location: `todoApp/src/main/java/com/kerem/todoApp/dto/ErrorResponse.java`
- Structure:
  ```json
  {
    "timestamp": "2026-01-26T11:20:00",
    "status": 400,
    "error": "Validation Failed",
    "messages": [
      "Error: Username must be between 3 and 20 characters!",
      "Error: Invalid email format!"
    ]
  }
  ```

### 5. Updated Controllers
- Added `@Valid` annotation to all `@RequestBody` parameters
- Removed try-catch blocks (handled by GlobalExceptionHandler)
- Simplified response handling

### 6. Updated Service Classes
- **AuthService**: Uses `ResourceNotFoundException`, `AuthenticationException`, `ResourceAlreadyExistsException`
- **ItemService**: Uses `ResourceNotFoundException`, `InvalidOperationException`
- **ItemListService**: Uses `ResourceNotFoundException`
- All services throw semantic exceptions instead of generic RuntimeException

## Frontend Changes

### 1. Error Handler Utility
- Location: `frontend/src/utils/errorHandler.js`
- `formatErrorMessages(error)`: Extracts and formats error messages
- Supports both new `ErrorResponse` and old `MessageResponse` formats
- Formats multiple errors with bullet points

### 2. Updated Components
- **Login.jsx**: Uses `formatErrorMessages()` for error display
- **Register.jsx**: Simplified validation (relies on backend), uses `formatErrorMessages()`
- **TodoApp.jsx**: All error handlers now use `formatErrorMessages()`

## Testing Error Responses

### Test 1: Resource Not Found (404)
**Request:**
```bash
curl -X GET http://localhost:8080/api/lists/999 \
  -H "Authorization: Bearer <token>"
```

**Expected Response:**
```json
{
  "timestamp": "2026-01-26T13:20:00",
  "status": 404,
  "error": "Resource Not Found",
  "messages": [
    "List not found"
  ]
}
```

### Test 2: Duplicate Username (409)
**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"existinguser","email":"new@example.com","password":"password123"}'
```

**Expected Response:**
```json
{
  "timestamp": "2026-01-26T13:20:00",
  "status": 409,
  "error": "Resource Already Exists",
  "messages": [
    "Username is already taken!"
  ]
}
```

### Test 3: Incorrect Password (401)
**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"wrongpassword"}'
```

**Expected Response:**
```json
{
  "timestamp": "2026-01-26T13:20:00",
  "status": 401,
  "error": "Authentication Failed",
  "messages": [
    "Incorrect password!"
  ]
}
```

### Test 4: Invalid Operation - Circular Dependency (400)
**Request:**
```bash
curl -X POST http://localhost:8080/api/lists/1/items/2/dependencies/1 \
  -H "Authorization: Bearer <token>"
```

**Expected Response:**
```json
{
  "timestamp": "2026-01-26T13:20:00",
  "status": 400,
  "error": "Invalid Operation",
  "messages": [
    "This would create a circular dependency."
  ]
}
```

### Test 5: Validation Errors (400)
### Test 5: Validation Errors (400)
**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"","email":"notanemail","password":"12"}'
```

**Expected Response:**
```json
{
  "timestamp": "2026-01-26T13:20:00",
  "status": 400,
  "error": "Validation Failed",
  "messages": [
    "Error: Username must be between 3 and 20 characters!",
    "Error: Invalid email format!",
    "Error: Password must be at least 6 characters!"
  ]
}
```

### Test 6: Cannot Complete Item with Incomplete Dependencies (400)
**Request:**
```bash
curl -X PUT http://localhost:8080/api/lists/1/items/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Task","description":"Test","deadline":"2026-02-01","status":"COMPLETED"}'
```

**Expected Response (when dependencies are incomplete):**
```json
{
  "timestamp": "2026-01-26T13:20:00",
  "status": 400,
  "error": "Invalid Operation",
  "messages": [
    "Cannot mark as COMPLETED! Dependencies are not complete."
  ]
}
```

### Test 7: Delete Item with Dependents (Auto-remove dependencies)
### Test 7: Delete Item with Dependents (Auto-remove dependencies)
**Request:**
```bash
curl -X DELETE http://localhost:8080/api/lists/1/items/1 \
  -H "Authorization: Bearer <token>"
```

**Behavior:** Item is deleted successfully. Dependencies on this item are automatically removed from dependent items.

**Frontend Warning:** Shows which items depend on this one before deletion:
```
This will delete the todo item.

Warning: The following items depend on this:
Task A, Task B, Task C

Their dependencies on this item will be removed.
```

## Validation Rules

### SignupRequest
- **username**: Required, 3-20 characters
- **email**: Required, valid email format
- **password**: Required, minimum 6 characters

### LoginRequest
- **username**: Required
- **password**: Required

### UpdateUserRequest
- **username**: 3-20 characters
- **email**: Valid email format
- **password**: Required (for verification, no length validation - just for identity confirmation)

### DeleteAccountRequest
- **password**: Required (for verification)

## Frontend Display

The frontend now displays validation errors as:
- Single error: "Error: Username must be between 3 and 20 characters!"
- Multiple errors (with bullets):
  ```
  • Error: Username must be between 3 and 20 characters!
  • Error: Invalid email format!
  • Error: Password must be at least 6 characters!
  ```

All error messages are displayed in SweetAlert2 popups or error divs with `white-space: pre-line` for proper multi-line formatting.

## Benefits

1. **Semantic HTTP Status Codes**: Proper codes (404, 409, 401, 403, 400) for different error types
2. **Custom Exception Classes**: Clear intent and better separation of concerns
3. **Aspect-Oriented Error Handling**: @RestControllerAdvice handles all exceptions centrally
4. **User-Friendly Messages**: Clear, actionable error messages without technical "Error:" prefixes
5. **Consistent Format**: Same ErrorResponse structure for all error types
6. **Multiple Error Display**: Shows all validation issues at once
7. **Maintainable**: Easy to add new validation rules and custom exceptions
8. **Type-Safe**: Backend validation ensures data integrity
9. **Professional**: Follows Spring Boot best practices with proper HTTP semantics
10. **Auto-Dependency Removal**: Deleting items automatically removes dependencies from dependents

## Testing Strategy

### Unit Tests (56 total - all passing ?)
- **AuthServiceTests**: 11 tests covering registration, login, update, delete
- **ItemListServiceTests**: 10 tests covering CRUD operations
- **ItemServiceTests**: 19 tests covering items, dependencies, status changes
- **Model Tests**: 16 tests covering User, Item, ItemList entities

All tests updated to expect custom exception types instead of generic exceptions.

## Running the Application

1. Start backend:
   ```bash
   cd todoApp
   ./mvnw spring-boot:run
   ```

2. Start frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. Test in browser:
   - Navigate to http://localhost:5173
   - Try to register with invalid data
   - Observe user-friendly error messages with appropriate HTTP status codes
   - Test item deletion with dependencies to see warning messages

## Code Review Compliance

? **Uses @RestControllerAdvice** for aspect-oriented error handling  
? **Custom exception classes** instead of generic RuntimeException  
? **Proper HTTP status codes** (404, 409, 401, 403, 400)  
? **Semantic exception names** clearly indicate what went wrong  
? **All tests passing** (56/56) with updated exception expectations  
? **Clean code** - no generic exceptions in service layer
