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
   - Set deadline (optional)
   - Status: `NOT_STARTED`
   - Click "Add Item"

5. **Add Dependencies**:
   - Create another item: `Review data`
   - Click ?? on "Write report"
   - Select "Review data" as dependency
   - Now "Write report" can't be completed until "Review data" is done!

6. **Filter and Sort**:
   - Use dropdowns to filter by status
   - Search by name
   - Click sort buttons to organize
   - Toggle ?? for ascending/descending

7. **Complete Items**:
   - Click ? on "Review data" to mark complete
   - Now you can complete "Write report"

## Common Tasks

### Create Multiple Lists
```
Personal ? Shopping List
Work ? Sprint Tasks
Study ? Exam Prep
```

### Use Status Effectively
- `NOT_STARTED`: New tasks
- `IN_PROGRESS`: Currently working on
- `COMPLETED`: Done ?

### Set Up Complex Dependencies
```
Task: Deploy App
?? Depends on: Run Tests
?  ?? Depends on: Write Tests
?     ?? Depends on: Implement Feature
```

### Find Expired Tasks
1. Filter by "Expired"
2. See all overdue items highlighted in red
3. Update deadlines or complete them

## Troubleshooting

**Backend won't start?**
- Check Java version: `java -version` (need 17+)
- Check port 8080 is free
- Run: `.\mvnw.cmd clean install`

**Frontend won't start?**
- Check Node version: `node -v` (need 16+)
- Delete `node_modules` and run `npm install` again
- Check port 3000 is free

**Can't login after registration?**
- Check backend console for errors
- Try registering with a different username
- Make sure backend is running

**Items won't complete?**
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

Enjoy your organized tasks! ???
