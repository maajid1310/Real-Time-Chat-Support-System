# Issue 3: Role-Based Authorization - Testing Guide

## Overview
This guide provides step-by-step testing instructions for all role-based authorization endpoints.

---

## Prerequisites

1. **Application Running**: Ensure Spring Boot application is running on `http://localhost:8080`
2. **Database Setup**: MySQL database with roles table populated:
   ```sql
   INSERT INTO roles (role_name, description) VALUES 
   ('ADMIN', 'Administrator with full access'),
   ('AGENT', 'Support agent handling customer queries'),
   ('CUSTOMER', 'Customer seeking support');
   ```
3. **Postman Installed**: Download from https://www.postman.com/downloads/

---

## Testing Strategy

### Test Order:
1. Register users for each role
2. Login and obtain JWT tokens
3. Test ADMIN endpoints
4. Test AGENT endpoints
5. Test CUSTOMER endpoints
6. Test authorization failures (403 Forbidden)

---

## Step 1: Register Test Users

### 1.1 Register ADMIN User

**Endpoint**: `POST http://localhost:8080/api/auth/register`

**Headers**:
```
Content-Type: application/json
```

**Body** (raw JSON):
```json
{
  "firstName": "Admin",
  "lastName": "User",
  "email": "admin@chatbot.com",
  "password": "Admin@123",
  "phone": "1234567890",
  "role": "ADMIN"
}
```

**Expected Response** (201 Created):
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "firstName": "Admin",
    "lastName": "User",
    "email": "admin@chatbot.com",
    "role": "ADMIN"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 1.2 Register AGENT User

**Endpoint**: `POST http://localhost:8080/api/auth/register`

**Body** (raw JSON):
```json
{
  "firstName": "Agent",
  "lastName": "Smith",
  "email": "agent@chatbot.com",
  "password": "Agent@123",
  "phone": "9876543210",
  "role": "AGENT"
}
```

### 1.3 Register CUSTOMER User

**Endpoint**: `POST http://localhost:8080/api/auth/register`

**Body** (raw JSON):
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "customer@chatbot.com",
  "password": "Customer@123",
  "phone": "5551234567",
  "role": "CUSTOMER"
}
```

---

## Step 2: Login and Get JWT Tokens

### 2.1 Login as ADMIN

**Endpoint**: `POST http://localhost:8080/api/auth/login`

**Body** (raw JSON):
```json
{
  "email": "admin@chatbot.com",
  "password": "Admin@123"
}
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBjaGF0Ym90LmNvbSIsImlhdCI6MTcwNTMxNDYwMCwiZXhwIjoxNzA1MzE4MjAwfQ.xxx",
    "email": "admin@chatbot.com",
    "role": "ADMIN"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Action**: Copy the `token` value and save it as `ADMIN_TOKEN`

### 2.2 Login as AGENT

**Endpoint**: `POST http://localhost:8080/api/auth/login`

**Body** (raw JSON):
```json
{
  "email": "agent@chatbot.com",
  "password": "Agent@123"
}
```

**Action**: Copy the `token` value and save it as `AGENT_TOKEN`

### 2.3 Login as CUSTOMER

**Endpoint**: `POST http://localhost:8080/api/auth/login`

**Body** (raw JSON):
```json
{
  "email": "customer@chatbot.com",
  "password": "Customer@123"
}
```

**Action**: Copy the `token` value and save it as `CUSTOMER_TOKEN`

---

## Step 3: Test ADMIN Endpoints

### 3.1 Get All Users (ADMIN Only)

**Endpoint**: `GET http://localhost:8080/api/admin/users`

**Headers**:
```
Authorization: Bearer {ADMIN_TOKEN}
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [
    {
      "id": 1,
      "firstName": "Admin",
      "lastName": "User",
      "email": "admin@chatbot.com",
      "phone": "1234567890",
      "role": "ADMIN",
      "active": true,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    },
    {
      "id": 2,
      "firstName": "Agent",
      "lastName": "Smith",
      "email": "agent@chatbot.com",
      "phone": "9876543210",
      "role": "AGENT",
      "active": true,
      "createdAt": "2024-01-15T10:31:00",
      "updatedAt": "2024-01-15T10:31:00"
    }
  ],
  "timestamp": "2024-01-15T10:35:00"
}
```

### 3.2 Get User by ID

**Endpoint**: `GET http://localhost:8080/api/admin/users/2`

**Headers**:
```
Authorization: Bearer {ADMIN_TOKEN}
```

**Expected Response** (200 OK): Single user object

### 3.3 Create New User (ADMIN Only)

**Endpoint**: `POST http://localhost:8080/api/admin/users`

**Headers**:
```
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json
```

**Body** (raw JSON):
```json
{
  "firstName": "Test",
  "lastName": "Agent",
  "email": "testagent@chatbot.com",
  "phone": "1112223333",
  "role": "AGENT",
  "active": true
}
```

**Expected Response** (201 Created):
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 4,
    "firstName": "Test",
    "lastName": "Agent",
    "email": "testagent@chatbot.com",
    "phone": "1112223333",
    "role": "AGENT",
    "active": true,
    "createdAt": "2024-01-15T10:40:00",
    "updatedAt": "2024-01-15T10:40:00"
  },
  "timestamp": "2024-01-15T10:40:00"
}
```

### 3.4 Update User

**Endpoint**: `PUT http://localhost:8080/api/admin/users/4`

**Headers**:
```
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json
```

**Body** (raw JSON):
```json
{
  "firstName": "Updated",
  "lastName": "Agent",
  "email": "testagent@chatbot.com",
  "phone": "9998887777",
  "role": "AGENT",
  "active": true
}
```

**Expected Response** (200 OK): Updated user object

### 3.5 Toggle User Status

**Endpoint**: `PATCH http://localhost:8080/api/admin/users/4/toggle-status`

**Headers**:
```
Authorization: Bearer {ADMIN_TOKEN}
```

**Expected Response** (200 OK): User with `active: false`

### 3.6 Get Users by Role

**Endpoint**: `GET http://localhost:8080/api/admin/users/role/AGENT`

**Headers**:
```
Authorization: Bearer {ADMIN_TOKEN}
```

**Expected Response** (200 OK): Array of users with AGENT role

### 3.7 Get User Statistics

**Endpoint**: `GET http://localhost:8080/api/admin/statistics`

**Headers**:
```
Authorization: Bearer {ADMIN_TOKEN}
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Statistics retrieved successfully",
  "data": {
    "totalUsers": 4,
    "totalAdmins": 1,
    "totalAgents": 2,
    "totalCustomers": 1,
    "activeUsers": 3
  },
  "timestamp": "2024-01-15T10:45:00"
}
```

### 3.8 Delete User

**Endpoint**: `DELETE http://localhost:8080/api/admin/users/4`

**Headers**:
```
Authorization: Bearer {ADMIN_TOKEN}
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null,
  "timestamp": "2024-01-15T10:50:00"
}
```

---

## Step 4: Test AGENT Endpoints

### 4.1 Update Agent Availability

**Endpoint**: `PUT http://localhost:8080/api/agent/availability`

**Headers**:
```
Authorization: Bearer {AGENT_TOKEN}
Content-Type: application/json
```

**Body** (raw JSON):
```json
{
  "status": "AVAILABLE"
}
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Availability updated successfully",
  "data": {
    "id": 1,
    "agentId": 2,
    "agentName": "Agent Smith",
    "status": "AVAILABLE",
    "lastUpdated": "2024-01-15T11:00:00"
  },
  "timestamp": "2024-01-15T11:00:00"
}
```

### 4.2 Get Agent Availability

**Endpoint**: `GET http://localhost:8080/api/agent/availability`

**Headers**:
```
Authorization: Bearer {AGENT_TOKEN}
```

**Expected Response** (200 OK): Agent availability object

### 4.3 Get Agent Chat Sessions

**Endpoint**: `GET http://localhost:8080/api/agent/chats`

**Headers**:
```
Authorization: Bearer {AGENT_TOKEN}
```

**Expected Response** (200 OK): Array of chat sessions assigned to agent

### 4.4 Get Active Chats

**Endpoint**: `GET http://localhost:8080/api/agent/chats/active`

**Headers**:
```
Authorization: Bearer {AGENT_TOKEN}
```

**Expected Response** (200 OK): Array of active chat sessions

### 4.5 Accept Chat Session (After customer creates one)

**Endpoint**: `POST http://localhost:8080/api/agent/chats/1/accept`

**Headers**:
```
Authorization: Bearer {AGENT_TOKEN}
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Chat session accepted successfully",
  "data": {
    "id": 1,
    "status": "ACTIVE",
    "startTime": "2024-01-15T11:05:00",
    "endTime": null,
    "customerId": 3,
    "customerName": "John Doe",
    "customerEmail": "customer@chatbot.com",
    "agentId": 2,
    "agentName": "Agent Smith",
    "agentEmail": "agent@chatbot.com",
    "messageCount": 0
  },
  "timestamp": "2024-01-15T11:10:00"
}
```

### 4.6 End Chat Session

**Endpoint**: `POST http://localhost:8080/api/agent/chats/1/end`

**Headers**:
```
Authorization: Bearer {AGENT_TOKEN}
```

**Expected Response** (200 OK): Chat session with `status: "CLOSED"`

---

## Step 5: Test CUSTOMER Endpoints

### 5.1 Get Customer Profile

**Endpoint**: `GET http://localhost:8080/api/customer/profile`

**Headers**:
```
Authorization: Bearer {CUSTOMER_TOKEN}
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": 3,
    "firstName": "John",
    "lastName": "Doe",
    "email": "customer@chatbot.com",
    "phone": "5551234567",
    "active": true,
    "createdAt": "2024-01-15T10:32:00",
    "totalChats": 1,
    "activeChats": 0
  },
  "timestamp": "2024-01-15T11:15:00"
}
```

### 5.2 Create Chat Session

**Endpoint**: `POST http://localhost:8080/api/customer/chats`

**Headers**:
```
Authorization: Bearer {CUSTOMER_TOKEN}
```

**Expected Response** (201 Created):
```json
{
  "success": true,
  "message": "Chat session created successfully",
  "data": {
    "id": 2,
    "status": "WAITING",
    "startTime": "2024-01-15T11:20:00",
    "endTime": null,
    "customerId": 3,
    "customerName": "John Doe",
    "customerEmail": "customer@chatbot.com",
    "agentId": null,
    "agentName": null,
    "agentEmail": null,
    "messageCount": 0
  },
  "timestamp": "2024-01-15T11:20:00"
}
```

### 5.3 Get Customer Chat Sessions

**Endpoint**: `GET http://localhost:8080/api/customer/chats`

**Headers**:
```
Authorization: Bearer {CUSTOMER_TOKEN}
```

**Expected Response** (200 OK): Array of customer's chat sessions

### 5.4 Get Specific Chat Session

**Endpoint**: `GET http://localhost:8080/api/customer/chats/2`

**Headers**:
```
Authorization: Bearer {CUSTOMER_TOKEN}
```

**Expected Response** (200 OK): Single chat session object

---

## Step 6: Test Authorization Failures (403 Forbidden)

### 6.1 Customer Trying to Access Admin Endpoint

**Endpoint**: `GET http://localhost:8080/api/admin/users`

**Headers**:
```
Authorization: Bearer {CUSTOMER_TOKEN}
```

**Expected Response** (403 Forbidden):
```json
{
  "success": false,
  "message": "Access Denied",
  "data": null,
  "timestamp": "2024-01-15T11:25:00"
}
```

### 6.2 Agent Trying to Access Admin Endpoint

**Endpoint**: `POST http://localhost:8080/api/admin/users`

**Headers**:
```
Authorization: Bearer {AGENT_TOKEN}
Content-Type: application/json
```

**Body**: Any user creation request

**Expected Response** (403 Forbidden)

### 6.3 Customer Trying to Access Another Customer's Chat

First, login with a different customer account and create a chat session.
Then try to access it with the first customer's token.

**Endpoint**: `GET http://localhost:8080/api/customer/chats/{other_customer_session_id}`

**Headers**:
```
Authorization: Bearer {CUSTOMER_TOKEN}
```

**Expected Response** (403 Forbidden):
```json
{
  "success": false,
  "message": "You are not authorized to view this chat session",
  "data": null,
  "timestamp": "2024-01-15T11:30:00"
}
```

### 6.4 No Token Provided

**Endpoint**: `GET http://localhost:8080/api/admin/users`

**Headers**: (No Authorization header)

**Expected Response** (401 Unauthorized):
```json
{
  "success": false,
  "message": "Unauthorized: JWT token is missing",
  "data": null,
  "timestamp": "2024-01-15T11:35:00"
}
```

---

## Verification Checklist

### ✅ ADMIN Role Tests
- [ ] Can get all users
- [ ] Can get user by ID
- [ ] Can create new users
- [ ] Can update users
- [ ] Can delete users
- [ ] Can toggle user status
- [ ] Can get users by role
- [ ] Can view statistics
- [ ] AGENT cannot access admin endpoints
- [ ] CUSTOMER cannot access admin endpoints

### ✅ AGENT Role Tests
- [ ] Can update availability
- [ ] Can get own availability
- [ ] Can view assigned chats
- [ ] Can view active chats
- [ ] Can accept waiting chats
- [ ] Can end own active chats
- [ ] Cannot end other agent's chats
- [ ] Cannot access admin endpoints
- [ ] Cannot access customer-specific data

### ✅ CUSTOMER Role Tests
- [ ] Can view own profile
- [ ] Can create chat sessions
- [ ] Can view own chat sessions
- [ ] Can view specific owned chat
- [ ] Cannot view other customer's chats
- [ ] Cannot access admin endpoints
- [ ] Cannot access agent endpoints

### ✅ Security Tests
- [ ] 401 Unauthorized when no token provided
- [ ] 403 Forbidden when wrong role accesses endpoint
- [ ] 403 Forbidden when accessing non-owned resources
- [ ] JWT token expires after configured time
- [ ] Invalid JWT tokens are rejected

---

## Troubleshooting

### Issue: 401 Unauthorized
- **Cause**: Token missing or invalid
- **Solution**: Re-login and get fresh token

### Issue: 403 Forbidden
- **Cause**: User role doesn't have permission
- **Solution**: Use correct role's token

### Issue: 404 Not Found
- **Cause**: Endpoint URL incorrect
- **Solution**: Verify URL spelling and path variables

### Issue: 400 Bad Request with Validation Errors
- **Cause**: Request body validation failed
- **Solution**: Check field requirements and formats

---

## Expected Output Summary

✅ **All endpoints respond with proper HTTP status codes**
✅ **Role-based authorization works correctly**
✅ **Users can only access their own resources**
✅ **Admin has full system access**
✅ **Agent can manage availability and chats**
✅ **Customer can manage own profile and chats**
✅ **Unauthorized access returns 403 Forbidden**
✅ **Missing token returns 401 Unauthorized**

---

## Next Steps

After completing Issue 3 testing:
1. Proceed to Milestone 2: WebSocket real-time messaging
2. Implement message sending functionality
3. Add file attachment support
4. Implement notification system

---

**Issue 3 Testing Completed Successfully! ✅**
