# Advanced Todo List Application

A full-stack enterprise-grade Todo List application with **Spring Boot 4** backend and **React** frontend, featuring user authentication, multiple todo lists, advanced filtering, sorting, and item dependencies.

## ? Features

### User Management
- ? User registration with validation
- ? User login with JWT authentication
- ? Secure password encryption
- ? Session management

### Todo List Management
- ? Create multiple todo lists
- ? Edit and delete todo lists
- ? Each user has their own isolated lists
- ? List selection and navigation

### Todo Item Management
- ? Add items with name, description, deadline, and status
- ? Three status types: NOT_STARTED, IN_PROGRESS, COMPLETED
- ? Edit and update items
- ? Delete items
- ? Mark items as complete (with dependency validation)
- ? Track creation and completion dates

### Advanced Features
- ? **Item Dependencies**: Link items so they can't be completed until dependencies are done
- ? **Circular Dependency Prevention**: Smart validation prevents dependency loops
- ? **Filtering**: Filter by status, expiration, and name
- ? **Sorting**: Sort by create date, deadline, name, or status (ascending/descending)
- ? **Expiration Tracking**: Automatically track overdue items
- ? **Real-time Updates**: Instant UI updates after every action

## ??? Architecture

```
todo_tool/
??? todo_app/              # Spring Boot Backend
?   ??? src/main/java/com/example/todo_app/
?   ?   ??? model/         # Entity classes (User, TodoList, TodoItem)
?   ?   ??? repository/    # JPA Repositories
?   ?   ??? controller/    # REST API Controllers
?   ?   ??? security/      # JWT & Security components
?   ?   ??? config/        # Security configuration
?   ?   ??? dto/           # Data Transfer Objects
?   ??? pom.xml
??? frontend/              # React Frontend
    ??? src/
    ?   ??? components/    # React components
    ?   ??? api.js         # API service layer
    ?   ??? AuthContext.jsx # Auth state management
    ?   ??? App.jsx        # Main app with routing
    ??? package.json
```

## ?? Technology Stack

### Backend
- **Java 17**
- **Spring Boot 4.0.1**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA** (Database ORM)
- **H2 Database** (In-memory database)
- **Bean Validation**
- **JJWT** (JSON Web Token)

### Frontend
- **React 18**
- **React Router 6** (Navigation)
- **Axios** (HTTP client)
- **Vite** (Build tool & dev server)
- **Modern CSS** (Responsive design)

## ?? REST API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Todo Lists
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/todolists` | Get all user's lists | Yes |
| GET | `/api/todolists/{id}` | Get specific list | Yes |
| POST | `/api/todolists` | Create new list | Yes |
| PUT | `/api/todolists/{id}` | Update list | Yes |
| DELETE | `/api/todolists/{id}` | Delete list | Yes |

### Todo Items
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/todolists/{listId}/items` | Get all items (with filtering/sorting) | Yes |
| GET | `/api/todolists/{listId}/items/{itemId}` | Get specific item | Yes |
| POST | `/api/todolists/{listId}/items` | Create new item | Yes |
| PUT | `/api/todolists/{listId}/items/{itemId}` | Update item | Yes |
| PATCH | `/api/todolists/{listId}/items/{itemId}/complete` | Mark as complete | Yes |
| DELETE | `/api/todolists/{listId}/items/{itemId}` | Delete item | Yes |
| POST | `/api/todolists/{listId}/items/{itemId}/dependencies/{depId}` | Add dependency | Yes |
| DELETE | `/api/todolists/{listId}/items/{itemId}/dependencies/{depId}` | Remove dependency | Yes |

### Query Parameters for Items
- `status`: Filter by status (NOT_STARTED, IN_PROGRESS, COMPLETED)
- `expired`: Filter by expiration (true/false)
- `name`: Search by name (partial match)
- `sortBy`: Sort field (createdate, deadline, name, status)
- `sortOrder`: Sort direction (asc, desc)

## ?? Getting Started

### Prerequisites
- **Java 17** or higher
- **Node.js 16** or higher
- **Maven** (included via wrapper)

### Backend Setup

1. Navigate to backend directory:
```bash
cd todo_app
```

2. Build the project:
```bash
./mvnw clean install
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

Backend will start on **http://localhost:8080**

### Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm run dev
```

Frontend will start on **http://localhost:3000**

## ?? Usage Guide

### 1. Register an Account
- Navigate to http://localhost:3000/register
- Enter username (3-20 characters), email, and password (min 6 characters)
- Click "Register"

### 2. Login
- Go to http://localhost:3000/login
- Enter your credentials
- You'll be redirected to the main app

### 3. Create a Todo List
- Click "+ New List" in the sidebar
- Enter a list name
- Click "Save"

### 4. Add Todo Items
- Select a list from the sidebar
- Click "+ Add Item"
- Fill in:
  - **Name** (required): Item title
  - **Description** (optional): Detailed description
  - **Deadline** (optional): Due date and time
  - **Status**: NOT_STARTED, IN_PROGRESS, or COMPLETED
- Click "Add Item"

### 5. Add Dependencies
- Click the ?? button on any item
- Select which other item must be completed first
- Items with incomplete dependencies cannot be marked as complete

### 6. Filter and Sort
- Use the filter bar to:
  - Filter by status
  - Show only expired/not expired items
  - Search by name
  - Sort by different fields
  - Toggle sort direction (??)

### 7. Complete Items
- Click the ? button to mark an item as complete
- If the item has dependencies, they must be completed first
- Completed items show a green status badge

## ??? Database Schema

### Users Table
- id (PK)
- username (unique)
- email (unique)
- password (encrypted)
- created_at

### Todo Lists Table
- id (PK)
- name
- user_id (FK)
- created_at

### Todo Items Table
- id (PK)
- name
- description
- status
- deadline
- created_at
- completed_at
- todo_list_id (FK)

### Todo Item Dependencies Table (Join Table)
- dependent_item_id (FK)
- dependency_item_id (FK)

## ?? Security

- **JWT Authentication**: Secure token-based authentication
- **Password Encryption**: BCrypt password hashing
- **CORS Configuration**: Configured for frontend-backend communication
- **Authorization**: All API endpoints (except auth) require authentication
- **User Isolation**: Users can only access their own data

## ?? Testing the Application

### Using the H2 Console (Development)
1. Start the backend
2. Navigate to: http://localhost:8080/h2-console
3. Use these credentials:
   - JDBC URL: `jdbc:h2:mem:tododb`
   - Username: `sa`
   - Password: (leave empty)

### API Testing with curl

**Register a user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

**Create a todo list (use token from login):**
```bash
curl -X POST http://localhost:8080/api/todolists \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"name":"My First List"}'
```

## ?? UI Features

- **Modern Gradient Design**: Beautiful purple gradient theme
- **Responsive Layout**: Works on desktop, tablet, and mobile
- **Sidebar Navigation**: Easy list switching
- **Real-time Filtering**: Instant results
- **Visual Status Indicators**: Color-coded status badges
- **Expiration Warnings**: Red highlights for overdue items
- **Dependency Tags**: Visual representation of item dependencies
- **Modal Dialogs**: Clean UX for dependency management

## ?? Future Enhancements

Potential features for future versions:
- [ ] User profile management
- [ ] Email notifications for deadlines
- [ ] Recurring tasks
- [ ] Task priority levels
- [ ] Collaboration and sharing lists
- [ ] File attachments
- [ ] Activity history and audit log
- [ ] Dark mode
- [ ] Export to CSV/PDF
- [ ] Mobile apps (iOS/Android)
- [ ] Persistent database (PostgreSQL/MySQL)
- [ ] Redis caching
- [ ] Docker containerization

## ?? Configuration

### Backend Configuration (application.properties)
```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:tododb
spring.jpa.hibernate.ddl-auto=update

# JWT
app.jwtExpirationMs=86400000  # 24 hours
```

### Frontend Configuration (vite.config.js)
```javascript
server: {
  port: 3000,
  proxy: {
    '/api': 'http://localhost:8080'
  }
}
```

## ?? Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## ?? License

This project is open source and available for educational purposes.

## ?? Author

Created for Turkcell Internship Project

## ?? Support

For issues and questions, please create an issue in the repository.

---

**Happy Task Managing! ???**
