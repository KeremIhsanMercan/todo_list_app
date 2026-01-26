
# Quick Start Guide

## First Time Setup

### 1. Start the Backend
```powershell
cd todo_app
.\mvnw.cmd spring-boot:run
```
Wait for the message: "Started TodoAppApplication"

### 2. Start the Frontend (new terminal)
```powershell
cd frontend
npm install
npm run dev
```
Open browser to: http://localhost:3000

## Create Your First Todo List

1. **Register** (first time only):
   - Click "Register here"
   - Username: `demo`
   - Email: `demo@example.com`
   - Password: `demo123`
   - Click "Register"

2. **Login**:
   - Username: `demo`
   - Password: `demo123`
   - Click "Login"

3. **Create a List**:
   - Click "+ New List"
   - Enter name: `Work Tasks`
   - Click "Save"

4. **Add Items**:
   - Click "+ Add Item"
   - Name: `Write report`
   - Description: `Q1 financial report`
   - Set deadline
   - Status: `NOT_STARTED`
   - Click "Add Item"

5. **Add Dependencies**:
   - Create another item: `Review data`
   - Click  on "Write report"
   - Select "Review data" as dependency
   - Now "Write report" can't be completed until "Review data" is done!

6. **Filter and Sort**:
   - Use dropdowns to filter by status
   - Search by name
   - Click sort buttons to organize
   - Toggle  for ascending/descending

7. **Complete Items**:
   - Click  on "Review data" to mark complete
   - Now you can complete "Write report"

## Common Tasks

### Create Multiple Lists
```
Personal  Shopping List
Work  Sprint Tasks
Study  Exam Prep
```

### Use Status Effectively
- `NOT_STARTED`: New tasks
- `IN_PROGRESS`: Currently working on
- `COMPLETED`: Done 

### Set Up Complex Dependencies
```
Task: Deploy App
 Depends on: Run Tests
   Depends on: Write Tests
      Depends on: Implement Feature
```

### Find Expired Tasks
1. Filter by "Expired"
2. See all overdue items highlighted in red
3. Update deadlines or complete them

## Troubleshooting

**Backend won't start**
- Check Java version: `java -version` (need 17+)
- Check port 8080 is free
- Run: `.\mvnw.cmd clean install`

**Frontend won't start**
- Check Node version: `node -v` (need 16+)
- Delete `node_modules` and run `npm install` again
- Check port 3000 is free

**Can't login after registration**
- Check backend console for errors
- Try registering with a different username
- Make sure backend is running

**Items won't complete**
- Check if they have dependencies
- Complete dependency items first
- Look for the error message

## Keyboard Tips

- `Tab` to navigate forms
- `Enter` to submit forms
- `Escape` to close modals
- Click outside modals to close them

## Next Steps

- Explore filtering options
- Create complex dependency chains
- Try sorting by different fields
- Create multiple lists for different projects

Enjoy your organized tasks! 


# Implementation Summary

##  All Required Features Implemented

### 1. User Management 
- [x] User registration with validation
- [x] User login with JWT authentication
- [x] Secure password storage (BCrypt)
- [x] Session management with tokens

### 2. Todo List Management 
- [x] Create multiple todo lists per user
- [x] Each list has a name
- [x] View all user's lists
- [x] Delete todo lists
- [x] User isolation (users only see their own lists)

### 3. Todo Item Management 
- [x] Add items to existing lists
- [x] Each item has:
  - Name 
  - Description 
  - Deadline (with date and time) 
  - Status (NOT_STARTED, IN_PROGRESS, COMPLETED) 
- [x] Mark items as "Complete"
- [x] Delete items from lists

### 4. Dependencies 
- [x] Add dependencies between items
- [x] Items with dependencies cannot be completed if dependency is incomplete
- [x] Circular dependency prevention
- [x] Visual dependency display

### 5. Filtering 
- [x] Filter by status (complete or not)
- [x] Filter by expired status
- [x] Filter by name (search)

### 6. Sorting 
- [x] Sort by create date
- [x] Sort by deadline
- [x] Sort by name
- [x] Sort by status
- [x] Ascending/descending order

##  Technology Choices

### Backend: Spring Boot
- **Reason**: Well-known, industry-standard Java framework
- **Plus**: Explicitly requested in requirements
- **Includes**: Spring Web, Spring Security, Spring Data JPA

### Frontend: React
- **Reason**: Modern, component-based UI library
- **Plus**: Explicitly requested in requirements  
- **Tools**: Vite (fast dev server), React Router (navigation)

### Database: SQLite
- **Reason**: Simple and lightweight data storage
- **Easy Switch**: Can easily migrate to PostgreSQL/MySQL for production

### Authentication: JWT
- **Reason**: Stateless, scalable authentication
- **Security**: Industry-standard, works well with REST APIs

##  Project Structure

```
todo_list_app/
 todoApp/                    # Backend (Java/Spring Boot)
    src/main/java/com/kerem/todoApp/
       model/
          User.java        # User entity
          TodoList.java    # TodoList entity
          TodoItem.java    # TodoItem entity with dependencies
       repository/
          UserRepository.java
          TodoListRepository.java
          TodoItemRepository.java
       controller/
          AuthController.java       # Registration & Login
          TodoListController.java   # List CRUD
          TodoItemScheduler.java    # Check expired items regularly
          TodoItemController.java   # Item CRUD, dependencies
       security/
          JwtUtils.java             # JWT token generation
          UserDetailsImpl.java
          UserDetailsServiceImpl.java
          AuthTokenFilter.java
          AuthEntryPointJwt.java
       config/
          SecurityConfig.java       # Spring Security setup
       dto/                          # Request/Response objects
    pom.xml

 frontend/                    # Frontend (React)
    src/
       components/
          Login.jsx        # Login page
          Register.jsx     # Registration page
          TodoApp.jsx      # Main application
       api.js               # API service layer
       AuthContext.jsx      # Authentication context
       App.jsx              # Router setup
    package.json

```

##  Key Features Demonstrated

### Backend Skills
- RESTful API design
- JWT authentication
- JPA entity relationships (One-to-Many, Many-to-Many)
- Complex queries with filtering and sorting
- Business logic (dependency validation, circular dependency prevention)
- Exception handling
- Bean validation
- CORS configuration

### Frontend Skills
- React Hooks (useState, useEffect, useContext)
- React Router for navigation
- Protected routes
- Context API for state management
- Axios for HTTP requests
- Form handling and validation
- Responsive CSS design
- Modal dialogs
- Real-time filtering and sorting

### Database Design
- Proper entity relationships
- Junction table for many-to-many (dependencies)
- Timestamp tracking (created_at, completed_at)
- Cascading deletes
- Lazy loading for performance

##  Security Features

1. **Password Hashing**: BCrypt with salt
2. **JWT Tokens**: Stateless authentication, 24-hour expiration
3. **Authorization**: All endpoints protected except /auth
4. **User Isolation**: Users can only access their own data
5. **CORS**: Configured for frontend-backend communication
6. **Input Validation**: Server-side validation on all inputs

##  Database Schema

### Tables Created
1. **users**: User accounts
2. **todo_lists**: Todo lists (many-to-one with users)
3. **todo_items**: Todo items (many-to-one with lists)
4. **todo_item_dependencies**: Junction table for item dependencies

### Relationships
- User  TodoList (One-to-Many)
- TodoList  TodoItem (One-to-Many)
- TodoItem  TodoItem (Many-to-Many for dependencies)

##  Extra Features Beyond Requirements

1. **Dependency Management**:
   - Circular dependency prevention
   - Visual dependency display
   - Remove dependencies

2. **Advanced UI**:
   - Real-time search
   - Multi-field sorting
   - Status badges with colors
   - Expired item highlighting
   - Responsive design
   - Modal for dependency selection

3. **Developer Experience**:
   - Comprehensive API documentation
   - Quick start guide
   - Error messages
   - Loading states
   - Confirmation dialogs

4. **Code Quality**:
   - Separation of concerns
   - RESTful design
   - Consistent naming
   - Comments where needed
   - Validation on both frontend and backend

##  Testing the Application

### Quick Test Scenario
1. Register user: `demo` / `demo@test.com` / `demo123`
2. Login
3. Create list: "Project Tasks"
4. Add item: "Design Database" (no dependencies)
5. Add item: "Implement API" (depends on "Design Database")
6. Add item: "Build UI" (depends on "Implement API")
7. Try to complete "Build UI"  should fail (dependencies not satisfied)
8. Complete "Design Database"  should succeed
9. Complete "Implement API"  should succeed
10. Complete "Build UI"  should now succeed
11. Filter by "Completed"
12. Sort by "Deadline"

##  Notes

- All required features are fully implemented and working
- Spring Boot and React were used as recommended
- Backend and frontend are completely separated
- Communication is through REST APIs
- All data is validated on both client and server
- Security is implemented with industry standards

##  Learning Outcomes

This project demonstrates:
- Full-stack development (Java + React)
- RESTful API design and implementation
- Authentication and authorization
- Complex data relationships
- State management
- Responsive UI design

---

**Status**:  **All features implemented and tested**

**Ready for**: Demonstration, code review, or deployment