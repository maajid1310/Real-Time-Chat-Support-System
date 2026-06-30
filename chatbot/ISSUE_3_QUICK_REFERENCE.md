# Issue 3: Role-Based Authorization - Quick Reference

## 🎯 Quick Overview

**Issue 3 implements fine-grained role-based access control using Spring Security's @PreAuthorize annotations.**

---

## 👥 Role Hierarchy

```
┌─────────────────┐
│     ADMIN       │  ← Full System Control
├─────────────────┤
│     AGENT       │  ← Chat Management
├─────────────────┤
│    CUSTOMER     │  ← Self-Service Only
└─────────────────┘
```

---

## 🔐 Permission Matrix

| Feature | ADMIN | AGENT | CUSTOMER |
|---------|-------|-------|----------|
| Manage All Users | ✅ | ❌ | ❌ |
| View System Stats | ✅ | ❌ | ❌ |
| Create/Delete Users | ✅ | ❌ | ❌ |
| Update Availability | ❌ | ✅ | ❌ |
| Accept Chat Sessions | ❌ | ✅ | ❌ |
| End Chat Sessions | ❌ | ✅ (own) | ❌ |
| View Agent Chats | ❌ | ✅ (own) | ❌ |
| Create Chat Request | ❌ | ❌ | ✅ |
| View Own Chats | ❌ | ❌ | ✅ |
| View Own Profile | ❌ | ❌ | ✅ |

---

## 🚀 API Endpoints

### 🔴 ADMIN Endpoints (Requires ADMIN role)

```
Base: /api/admin
Authorization: Bearer {ADMIN_TOKEN}
```

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users` | Get all users |
| GET | `/users/{id}` | Get user by ID |
| POST | `/users` | Create new user |
| PUT | `/users/{id}` | Update user |
| DELETE | `/users/{id}` | Delete user (soft) |
| PATCH | `/users/{id}/toggle-status` | Toggle active status |
| GET | `/users/role/{role}` | Get users by role |
| GET | `/statistics` | Get system stats |

### 🟢 AGENT Endpoints (Requires AGENT role)

```
Base: /api/agent
Authorization: Bearer {AGENT_TOKEN}
```

| Method | Endpoint | Description |
|--------|----------|-------------|
| PUT | `/availability` | Update availability |
| GET | `/availability` | Get own availability |
| GET | `/chats` | Get all assigned chats |
| GET | `/chats/active` | Get active chats only |
| POST | `/chats/{id}/accept` | Accept waiting chat |
| POST | `/chats/{id}/end` | End active chat |

### 🔵 CUSTOMER Endpoints (Requires CUSTOMER role)

```
Base: /api/customer
Authorization: Bearer {CUSTOMER_TOKEN}
```

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profile` | Get own profile |
| GET | `/chats` | Get own chat history |
| GET | `/chats/{id}` | Get specific chat |
| POST | `/chats` | Create new chat session |

### ⚪ PUBLIC Endpoints (No authentication required)

```
Base: /api/auth
Authorization: None
```

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register new user |
| POST | `/login` | Login and get JWT token |

---

## 📝 Request/Response Examples

### Login (Get JWT Token)

**Request**:
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@chatbot.com",
  "password": "Admin@123"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "admin@chatbot.com",
    "role": "ADMIN"
  }
}
```

### Create User (ADMIN)

**Request**:
```http
POST /api/admin/users
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john@chatbot.com",
  "phone": "1234567890",
  "role": "AGENT",
  "active": true
}
```

**Response**:
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 5,
    "firstName": "John",
    "lastName": "Smith",
    "email": "john@chatbot.com",
    "phone": "1234567890",
    "role": "AGENT",
    "active": true,
    "createdAt": "2024-01-15T12:00:00",
    "updatedAt": "2024-01-15T12:00:00"
  }
}
```

### Update Availability (AGENT)

**Request**:
```http
PUT /api/agent/availability
Authorization: Bearer {AGENT_TOKEN}
Content-Type: application/json

{
  "status": "AVAILABLE"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Availability updated successfully",
  "data": {
    "id": 1,
    "agentId": 2,
    "agentName": "Agent Smith",
    "status": "AVAILABLE",
    "lastUpdated": "2024-01-15T12:05:00"
  }
}
```

### Create Chat (CUSTOMER)

**Request**:
```http
POST /api/customer/chats
Authorization: Bearer {CUSTOMER_TOKEN}
```

**Response**:
```json
{
  "success": true,
  "message": "Chat session created successfully",
  "data": {
    "id": 10,
    "status": "WAITING",
    "startTime": "2024-01-15T12:10:00",
    "customerId": 3,
    "customerName": "John Doe",
    "customerEmail": "customer@chatbot.com"
  }
}
```

---

## ⚠️ Error Responses

### 401 Unauthorized (No Token)

```json
{
  "success": false,
  "message": "Unauthorized: JWT token is missing",
  "data": null,
  "timestamp": "2024-01-15T12:15:00"
}
```

### 403 Forbidden (Wrong Role)

```json
{
  "success": false,
  "message": "Access Denied",
  "data": null,
  "timestamp": "2024-01-15T12:16:00"
}
```

### 404 Not Found

```json
{
  "success": false,
  "message": "User not found with ID: 999",
  "data": null,
  "timestamp": "2024-01-15T12:17:00"
}
```

### 400 Bad Request (Validation Error)

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email must be valid",
    "firstName": "First name is required"
  },
  "timestamp": "2024-01-15T12:18:00"
}
```

---

## 🛠️ Testing with Postman

### Step 1: Login

1. POST to `/api/auth/login`
2. Copy the `token` from response
3. Save it as environment variable

### Step 2: Set Authorization

1. Go to Authorization tab
2. Select "Bearer Token"
3. Paste your token
4. Or use: `{{ADMIN_TOKEN}}`, `{{AGENT_TOKEN}}`, `{{CUSTOMER_TOKEN}}`

### Step 3: Make Requests

Use appropriate token for each endpoint based on required role.

---

## 🔒 Security Features

### 1. Method-Level Security
```java
@PreAuthorize("hasRole('ADMIN')")
public class AdminController { ... }
```

### 2. URL-Based Security
```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/agent/**").hasRole("AGENT")
.requestMatchers("/api/customer/**").hasRole("CUSTOMER")
```

### 3. Resource Ownership Validation
```java
if (!session.getCustomer().getId().equals(customerId)) {
    throw new ForbiddenException("Not authorized");
}
```

### 4. JWT Token Validation
- Token required for all protected endpoints
- Token contains user email and role
- Token expires after configured time
- Invalid tokens are rejected

---

## 📊 HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PUT, DELETE |
| 201 | Created | Successful POST |
| 400 | Bad Request | Validation error |
| 401 | Unauthorized | Missing/invalid token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Unexpected error |

---

## 🧪 Quick Test Scenarios

### Test 1: ADMIN can manage users
```bash
1. Login as ADMIN
2. GET /api/admin/users → Should return all users
3. POST /api/admin/users → Should create user
```

### Test 2: AGENT can update availability
```bash
1. Login as AGENT
2. PUT /api/agent/availability → Should update
3. GET /api/agent/chats → Should return assigned chats
```

### Test 3: CUSTOMER can create chats
```bash
1. Login as CUSTOMER
2. POST /api/customer/chats → Should create session
3. GET /api/customer/chats → Should return own chats
```

### Test 4: Authorization failures
```bash
1. Login as CUSTOMER
2. GET /api/admin/users → Should return 403 Forbidden
3. Login as AGENT
4. GET /api/admin/statistics → Should return 403 Forbidden
```

---

## 🎓 Key Concepts

### @PreAuthorize Annotation
```java
@PreAuthorize("hasRole('ADMIN')")  // Class level - applies to all methods

@PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")  // Multiple roles

@PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.user.id")  // Complex logic
```

### CustomUserDetails
```java
// Allows access to full User entity in controllers
CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
User user = userDetails.getUser();
Long userId = user.getId();
```

### Transaction Management
```java
@Transactional  // Write operations
@Transactional(readOnly = true)  // Read-only operations (optimization)
```

---

## 📚 Related Files

### Configuration
- `SecurityConfig.java` - Security rules
- `CustomUserDetailsService.java` - User loading
- `CustomUserDetails.java` - User wrapper

### Services
- `UserManagementServiceImpl.java` - Admin operations
- `AgentManagementServiceImpl.java` - Agent operations
- `CustomerManagementServiceImpl.java` - Customer operations

### Controllers
- `AdminController.java` - Admin endpoints
- `AgentController.java` - Agent endpoints
- `CustomerController.java` - Customer endpoints

### DTOs
- `UserManagementRequest/Response.java`
- `AgentAvailabilityRequest/Response.java`
- `ChatSessionDetailResponse.java`

---

## 🚀 Next Steps After Issue 3

1. **Milestone 2**: WebSocket real-time messaging
2. **Message System**: Send/receive messages
3. **File Attachments**: Upload/download support
4. **Notifications**: Real-time alerts
5. **Feedback System**: Customer ratings

---

## 💡 Pro Tips

1. **Always use appropriate role token** for testing
2. **Check token expiration** if getting 401 errors
3. **Verify role name format**: Must be "ROLE_ADMIN", not "ADMIN"
4. **Use Postman environments** to manage multiple tokens
5. **Test authorization failures** to ensure security works
6. **Check resource ownership** for customer/agent operations

---

## 📞 Support

If you encounter issues:
1. Check ISSUE_3_TESTING_GUIDE.md for detailed steps
2. Verify ISSUE_3_VERIFICATION_CHECKLIST.md completion
3. Review application logs for errors
4. Ensure database has roles populated

---

**Issue 3 Quick Reference Complete! ✅**

For detailed testing instructions, see `ISSUE_3_TESTING_GUIDE.md`
For verification checklist, see `ISSUE_3_VERIFICATION_CHECKLIST.md`
