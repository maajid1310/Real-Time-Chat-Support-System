# 🚀 QUICK START GUIDE - Authentication System

## ⚡ 5-Minute Setup

### Step 1: Database Setup (1 minute)
```sql
-- Run this in MySQL Workbench or command line
USE real_time_chatbot_support_db;

INSERT INTO roles (role_name, description) VALUES 
('ADMIN', 'Administrator with full system access'),
('AGENT', 'Support agent handling customer queries'),
('CUSTOMER', 'End user seeking support');
```

### Step 2: Start Backend (1 minute)
```bash
cd "e:\Application Development (Real_Time-Chat Bot System)\chatbot"
mvnw spring-boot:run
```

Wait for: `Started ChatbotApplication in X seconds`

### Step 3: Start Frontend (1 minute)
```bash
cd "e:\Application Development (Real_Time-Chat Bot System)\chatbot\frontend"
npm run dev
```

Open: http://localhost:3000

### Step 4: Test Registration (1 minute)
1. Click "Register here"
2. Fill form:
   - First Name: John
   - Last Name: Doe
   - Email: john@test.com
   - Password: password123
   - Phone: 1234567890
   - Role: CUSTOMER
3. Click Register
4. ✅ You should be redirected to Customer Dashboard

### Step 5: Test Login (1 minute)
1. Logout
2. Login with:
   - Email: john@test.com
   - Password: password123
3. ✅ You should see Customer Dashboard

---

## 📌 Quick Test URLs

### Backend
- Health Check: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

### Frontend
- Login: http://localhost:3000/login
- Register: http://localhost:3000/register

---

## 🧪 Postman Quick Tests

### Register
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "firstName": "Test",
  "lastName": "User",
  "email": "test@example.com",
  "password": "password123",
  "phone": "1234567890",
  "role": "CUSTOMER"
}
```

### Login
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

### Access Protected Endpoint
```
GET http://localhost:8080/api/users/1
Authorization: Bearer YOUR_TOKEN_HERE
```

---

## ✅ Success Indicators

### Backend Running Successfully
```
✓ Tomcat started on port(s): 8080
✓ Started ChatbotApplication
✓ No errors in console
```

### Frontend Running Successfully
```
✓ Local: http://localhost:3000
✓ ready in XXX ms
```

### Authentication Working
```
✓ Registration creates user and returns token
✓ Login returns token
✓ Dashboard displays user info
✓ Logout clears token and redirects to login
✓ Protected routes redirect to login when not authenticated
```

---

## 🐛 Common Issues & Quick Fixes

### Issue: "Role not found"
**Fix:** Run the SQL insert statement for roles

### Issue: "Port 8080 already in use"
**Fix:** Kill process on port 8080
```bash
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Issue: "Cannot connect to database"
**Fix:** 
1. Check MySQL is running
2. Verify credentials in application.properties
3. Ensure database exists

### Issue: "CORS error in browser"
**Fix:** Ensure backend is running on port 8080

### Issue: "Token not working"
**Fix:** 
1. Check token is in localStorage (F12 → Application → Local Storage)
2. Verify Authorization header format: "Bearer <token>"

---

## 📊 Test User Credentials

After running the system, create these test users:

| Role | Email | Password |
|------|-------|----------|
| ADMIN | admin@test.com | admin123 |
| AGENT | agent@test.com | agent123 |
| CUSTOMER | customer@test.com | customer123 |

---

## 🎯 What to Test

1. ✅ Register new user
2. ✅ Login with correct credentials
3. ✅ Login with wrong password (should fail)
4. ✅ Register with duplicate email (should fail)
5. ✅ Access dashboard after login
6. ✅ Logout and verify redirect to login
7. ✅ Try accessing protected route without login
8. ✅ Verify token in localStorage
9. ✅ Check role-based dashboard access

---

## 📞 Support

If you encounter any issues:
1. Check console errors (Backend + Frontend)
2. Review TESTING_GUIDE.md
3. Review README_ISSUE2.md
4. Check database has roles inserted

---

## ✨ Features Implemented

✅ User Registration with validation
✅ User Login with JWT authentication
✅ Password encryption (BCrypt)
✅ Token-based authentication
✅ Protected routes
✅ Role-based access control (RBAC)
✅ Logout functionality
✅ Error handling
✅ Responsive UI
✅ Token auto-refresh handling

**Issue 2 Status: COMPLETE ✅**
