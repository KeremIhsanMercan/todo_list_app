# API Documentation

Base URL: `http://localhost:8080/api`

## Authentication

All endpoints except `/auth/*` require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## Authentication Endpoints

### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "message": "User registered successfully!"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Error: Username is already taken!"
}
```

---

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Bad credentials"
}
```

---

## Todo List Endpoints

### Get All Todo Lists
```http
GET /todolists
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Work Tasks",
    "createdAt": "2026-01-20T10:30:00",
    "items": []
  },
  {
    "id": 2,
    "name": "Personal",
    "createdAt": "2026-01-20T11:00:00",
    "items": []
  }
]
```

---

### Get Todo List by ID
```http
GET /todolists/{id}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Work Tasks",
  "createdAt": "2026-01-20T10:30:00",
  "items": [...]
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Not found"
}
```

---

### Create Todo List
```http
POST /todolists
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "New Project Tasks"
}
```

**Response (201 Created):**
```json
{
  "id": 3,
  "name": "New Project Tasks",
  "createdAt": "2026-01-20T12:00:00",
  "items": []
}
```

---

### Update Todo List
```http
PUT /todolists/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated List Name"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Updated List Name",
  "createdAt": "2026-01-20T10:30:00",
  "items": []
}
```

---

### Delete Todo List
```http
DELETE /todolists/{id}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "message": "Todo list deleted successfully"
}
```

---

## Todo Item Endpoints

### Get All Items in a List
```http
GET /todolists/{listId}/items?status=NOT_STARTED&sortBy=deadline&sortOrder=asc
Authorization: Bearer <token>
```

**Query Parameters:**
- `status` (optional): `NOT_STARTED`, `IN_PROGRESS`, or `COMPLETED`
- `expired` (optional): `true` or `false`
- `name` (optional): Search string (partial match)
- `sortBy` (optional): `createdate`, `deadline`, `name`, or `status`
- `sortOrder` (optional): `asc` or `desc`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Complete project documentation",
    "description": "Write comprehensive docs",
    "status": "IN_PROGRESS",
    "deadline": "2026-01-25T17:00:00",
    "createdAt": "2026-01-20T10:00:00",
    "completedAt": null,
    "dependencies": []
  },
  {
    "id": 2,
    "name": "Review code",
    "description": "Review PR #123",
    "status": "COMPLETED",
    "deadline": "2026-01-22T15:00:00",
    "createdAt": "2026-01-20T11:00:00",
    "completedAt": "2026-01-21T14:30:00",
    "dependencies": []
  }
]
```

---

### Get Item by ID
```http
GET /todolists/{listId}/items/{itemId}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Complete project documentation",
  "description": "Write comprehensive docs",
  "status": "IN_PROGRESS",
  "deadline": "2026-01-25T17:00:00",
  "createdAt": "2026-01-20T10:00:00",
  "completedAt": null,
  "dependencies": []
}
```

---

### Create Todo Item
```http
POST /todolists/{listId}/items
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Implement new feature",
  "description": "Add user profile page",
  "status": "NOT_STARTED",
  "deadline": "2026-01-30T17:00:00"
}
```

**Response (201 Created):**
```json
{
  "id": 3,
  "name": "Implement new feature",
  "description": "Add user profile page",
  "status": "NOT_STARTED",
  "deadline": "2026-01-30T17:00:00",
  "createdAt": "2026-01-20T12:00:00",
  "completedAt": null,
  "dependencies": []
}
```

**Validation Errors:**
- Name: 1-200 characters (required)
- Description: 0-1000 characters
- Status: Must be `NOT_STARTED`, `IN_PROGRESS`, or `COMPLETED`
- Deadline: ISO 8601 datetime format

---

### Update Todo Item
```http
PUT /todolists/{listId}/items/{itemId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Implement new feature (updated)",
  "description": "Add enhanced user profile page",
  "status": "IN_PROGRESS",
  "deadline": "2026-02-01T17:00:00"
}
```

**Response (200 OK):**
```json
{
  "id": 3,
  "name": "Implement new feature (updated)",
  "description": "Add enhanced user profile page",
  "status": "IN_PROGRESS",
  "deadline": "2026-02-01T17:00:00",
  "createdAt": "2026-01-20T12:00:00",
  "completedAt": null,
  "dependencies": []
}
```

---

### Mark Item as Complete
```http
PATCH /todolists/{listId}/items/{itemId}/complete
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": 3,
  "name": "Implement new feature",
  "description": "Add user profile page",
  "status": "COMPLETED",
  "deadline": "2026-01-30T17:00:00",
  "createdAt": "2026-01-20T12:00:00",
  "completedAt": "2026-01-29T16:45:00",
  "dependencies": []
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Cannot complete: Dependencies not satisfied"
}
```

---

### Delete Todo Item
```http
DELETE /todolists/{listId}/items/{itemId}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "message": "Todo item deleted successfully"
}
```

---

## Dependency Management

### Add Dependency
```http
POST /todolists/{listId}/items/{itemId}/dependencies/{dependencyId}
Authorization: Bearer <token>
```

Makes `itemId` depend on `dependencyId` (meaning `dependencyId` must be completed before `itemId` can be completed).

**Response (200 OK):**
```json
{
  "message": "Dependency added successfully"
}
```

**Error Responses:**

**Circular Dependency (400 Bad Request):**
```json
{
  "message": "This would create a circular dependency"
}
```

**Self Dependency (400 Bad Request):**
```json
{
  "message": "Item cannot depend on itself"
}
```

---

### Remove Dependency
```http
DELETE /todolists/{listId}/items/{itemId}/dependencies/{dependencyId}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "message": "Dependency removed successfully"
}
```

---

## Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 400 | Bad Request - Invalid input or validation error |
| 401 | Unauthorized - Missing or invalid JWT token |
| 404 | Not Found - Resource doesn't exist |
| 500 | Internal Server Error - Server error |

---

## Error Response Format

All errors follow this format:
```json
{
  "message": "Error description here"
}
```

---

## Testing with curl

**Complete workflow example:**

1. Register:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"test123"}'
```

2. Login and save token:
```bash
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}' \
  | jq -r '.token')
```

3. Create a list:
```bash
LIST_ID=$(curl -X POST http://localhost:8080/api/todolists \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"My List"}' \
  | jq -r '.id')
```

4. Create an item:
```bash
curl -X POST http://localhost:8080/api/todolists/$LIST_ID/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Task 1","description":"First task","status":"NOT_STARTED"}'
```

5. Get all items:
```bash
curl -X GET "http://localhost:8080/api/todolists/$LIST_ID/items" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Rate Limiting

Currently no rate limiting is implemented. For production, consider adding:
- Request rate limiting per user
- Token refresh mechanism
- API versioning

## Security Notes

- All passwords are hashed with BCrypt
- JWT tokens expire after 24 hours
- CORS is configured for `http://localhost:3000`
- For production, update CORS settings
- Use HTTPS in production
- Consider implementing refresh tokens

---

**Need help? Check the [README.md](README.md) or [QUICK_START.md](QUICK_START.md)**
