# AUTHENTICATION & AUTHORIZATION TESTING GUIDE

## Prerequisites
1. MySQL database running
2. Spring Boot application running on http://localhost:8080
3. React application running on http://localhost:3000
4. Postman installed

---

## STEP 1: DATABASE SETUP

### Insert Roles (Run this SQL first)

```sql
INSERT INTO roles (role_name, description) VALUES 
('ADMIN', 'Administrator with full system access'),
('AGENT', 'Support agent handling customer queries'),
('CUSTOMER', 'End user seeking support');
```

---

## STEP 2: POSTMAN TESTING

### Test 1: Register User (Customer)

**Endpoint:** POST http://localhost:8080/api/auth/register

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "phone": "1234567890",
  "role": "CUSTOMER"
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "userId": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "CUSTOMER"
  },
  "timestamp": "2026-01-15T10:30:00"
}
```

---

### Test 2: Login Success

**Endpoint:** POST http://localhost:8080/api/auth/login

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "userId": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "CUSTOMER"
  }
}
```

---

### Test 3: Access Protected Endpoint WITH Valid Token

**Endpoint:** GET http://localhost:8080/api/users/1

**Headers:**
```
Authorization: Bearer <paste_token_here>
```

**Expected Response:** 200 OK (Protected resource accessed)

---

## SUCCESS CRITERIA

✅ Users can register with validation
✅ Users can login with correct credentials
✅ JWT token generated on registration/login
✅ Token sent with protected requests
✅ Invalid tokens rejected
✅ Role-based access control working
✅ Logout clears authentication
