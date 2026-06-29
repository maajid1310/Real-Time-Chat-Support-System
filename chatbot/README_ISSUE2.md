# Issue 2: Authentication & Authorization - COMPLETE ✅

## Implementation Summary

This document provides a complete overview of the JWT-based Authentication and Authorization system implemented for the Real-Time Chat Support System.

---

## 🏗️ Architecture Overview

### Backend (Spring Boot)
```
Security Layer
├── SecurityConfig (Spring Security Configuration)
├── JWT Layer
│   ├── JwtUtil (Token generation & validation)
│   ├── JwtAuthenticationFilter (Request interceptor)
│   └── JwtAuthenticationEntryPoint (Unauthorized handler)
├── UserDetailsService
│   └── CustomUserDetailsService (Load user from database)
├── DTOs
│   ├── RegisterRequest
│   ├── LoginRequest
│   ├── LoginResponse
│   └── ApiResponse
├── Service Layer
│   ├── AuthService (Interface)
│   └── AuthServiceImpl (Business logic)
├── Controller Layer
│   └── AuthController (REST endpoints)
└── Exception Handling
    └── GlobalExceptionHandler
```

### Frontend (React)
```
Authentication System
├── Services
│   ├── axiosConfig (HTTP client with interceptors)
│   └── authService (API calls)
├── Context
│   └── AuthContext (Global state management)
├── Components
│   ├── Login Page
│   ├── Register Page
│   ├── ProtectedRoute (Route guard)
│   └── Dashboard Pages (Admin, Agent, Customer)
└── Routes
    └── AppRoutes (Routing configuration)
```

---

## 🔐 Security Features

### 1. JWT Token-Based Authentication
- Stateless authentication
- Token expiry: 24 hours
- HS512 encryption algorithm
- Secure token storage in localStorage

### 2. Password Security
- BCrypt hashing algorithm
- Automatic salt generation
- One-way encryption
- Cannot be reversed

### 3. Role-Based Access Control (RBAC)
- Three roles: ADMIN, AGENT, CUSTOMER
- Role-based routing
- Protected endpoints
- Authorization checks

### 4. CORS Configuration
- Frontend-backend communication
- Allowed origin: http://localhost:3000
- Secure cross-origin requests

### 5. Session Management
- Stateless sessions (no server-side storage)
- CSRF protection disabled (safe for JWT)
- Token-based authentication on every request

---

## 📋 Implementation Details

### Backend Components

#### 1. SecurityConfig.java
**Package:** `com.chatbot.chatbot.security.config`

**Purpose:** Core Spring Security configuration

**Key Features:**
- SecurityFilterChain bean
- Password encoder (BCrypt)
- Authentication manager
- Public/protected endpoint configuration
- CORS & CSRF settings
- Stateless session policy

**Public Endpoints:**
- `/api/auth/**` (Login, Register)
- `/v3/api-docs/**` (Swagger API docs)
- `/swagger-ui/**` (Swagger UI)
- `/ws/**` (WebSocket)

**Protected Endpoints:**
- All other endpoints require valid JWT

---

#### 2. JwtUtil.java
**Package:** `com.chatbot.chatbot.security.jwt`

**Purpose:** JWT token operations

**Methods:**
- `generateToken(username)` - Creates signed JWT token
- `validateToken(token)` - Validates token signature and expiry
- `getUsernameFromToken(token)` - Extracts username from token
- `getExpirationDate(token)` - Gets token expiry date
- `isTokenExpired(token)` - Checks if token expired
- `validateTokenWithUser(token, userDetails)` - Validates token against user

**Token Structure:**
```
Header: { "alg": "HS512", "typ": "JWT" }
Payload: { "sub": "user@example.com", "iat": 1709550000, "exp": 1709636400 }
Signature: HMACSHA512(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

---

#### 3. JwtAuthenticationFilter.java
**Package:** `com.chatbot.chatbot.security.filter`

**Purpose:** Intercepts every HTTP request to validate JWT

**Flow:**
1. Extract Authorization header
2. Extract JWT token (remove "Bearer " prefix)
3. Validate token using JwtUtil
4. Extract username from token
5. Load user details from database
6. Create authentication object
7. Set authentication in SecurityContext
8. Continue filter chain

**Filter Order:**
```
JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → Other Filters
```

---

#### 4. JwtAuthenticationEntryPoint.java
**Package:** `com.chatbot.chatbot.security.jwt`

**Purpose:** Handles unauthorized access attempts

**Behavior:**
- Returns 401 Unauthorized
- JSON error response
- Triggered when user accesses protected resource without valid token

---

#### 5. CustomUserDetailsService.java
**Package:** `com.chatbot.chatbot.security.service`

**Purpose:** Bridges Spring Security with database

**Method:** `loadUserByUsername(email)`

**Flow:**
1. Receive username (email)
2. Query UserRepository
3. Throw exception if not found
4. Convert User entity to Spring Security UserDetails
5. Return UserDetails with authorities

**Authority Format:** `ROLE_ADMIN`, `ROLE_AGENT`, `ROLE_CUSTOMER`

---

#### 6. AuthServiceImpl.java
**Package:** `com.chatbot.chatbot.service.impl`

**Purpose:** Business logic for authentication

**Methods:**

**register(RegisterRequest):**
1. Check if email exists
2. Fetch role from database
3. Create user entity
4. Encrypt password using BCrypt
5. Save user to database
6. Generate JWT token
7. Return LoginResponse

**login(LoginRequest):**
1. Authenticate credentials using AuthenticationManager
2. Fetch user from database
3. Generate JWT token
4. Return LoginResponse

---

#### 7. AuthController.java
**Package:** `com.chatbot.chatbot.controller.auth`

**Purpose:** REST API endpoints

**Endpoints:**

**POST /api/auth/register**
- Public endpoint
- Validates request body
- Calls AuthService.register()
- Returns 201 Created with JWT token

**POST /api/auth/login**
- Public endpoint
- Validates credentials
- Calls AuthService.login()
- Returns 200 OK with JWT token

---

#### 8. GlobalExceptionHandler.java
**Package:** `com.chatbot.chatbot.exception`

**Purpose:** Centralized exception handling

**Handles:**
- Validation errors (400 Bad Request)
- Bad credentials (401 Unauthorized)
- User not found (404 Not Found)
- Email already exists (400 Bad Request)
- Expired JWT (401 Unauthorized)
- Malformed JWT (401 Unauthorized)
- JWT signature errors (401 Unauthorized)
- Generic exceptions (500 Internal Server Error)

---

### Frontend Components

#### 1. axiosConfig.js
**Path:** `frontend/src/services/api/axiosConfig.js`

**Purpose:** HTTP client configuration

**Features:**
- Base URL: http://localhost:8080/api
- Request interceptor: Adds JWT token to every request
- Response interceptor: Handles token expiration (401)
- Automatic redirect to login on unauthorized

---

#### 2. authService.js
**Path:** `frontend/src/services/auth/authService.js`

**Purpose:** Authentication API calls

**Methods:**
- `register(userData)` - Register new user
- `login(credentials)` - Login user
- `logout()` - Clear local storage
- `getCurrentUser()` - Get user from localStorage
- `isAuthenticated()` - Check if token exists

---

#### 3. AuthContext.jsx
**Path:** `frontend/src/context/AuthContext.jsx`

**Purpose:** Global authentication state management

**State:**
- `user` - Current user object
- `loading` - Loading state

**Methods:**
- `register(userData)` - Register and update state
- `login(credentials)` - Login and update state
- `logout()` - Clear state
- `hasRole(role)` - Check user role
- `isAuthenticated` - Authentication status

**Custom Hook:** `useAuth()` - Access auth context

---

#### 4. Login.jsx
**Path:** `frontend/src/pages/Login/Login.jsx`

**Features:**
- Email and password inputs
- Form validation
- Error message display
- Loading state
- Role-based redirect after login
- Link to registration page

---

#### 5. Register.jsx
**Path:** `frontend/src/pages/Register/Register.jsx`

**Features:**
- First name, last name, email, password, phone, role inputs
- Form validation
- Error message display
- Loading state
- Role-based redirect after registration
- Link to login page

---

#### 6. ProtectedRoute.jsx
**Path:** `frontend/src/components/common/ProtectedRoute.jsx`

**Purpose:** Route protection

**Logic:**
1. Check if user authenticated
2. If not, redirect to /login
3. Check role authorization
4. If unauthorized, redirect to /unauthorized
5. If authorized, render children

---

#### 7. AppRoutes.jsx
**Path:** `frontend/src/routes/AppRoutes.jsx`

**Routes:**
- `/login` - Public
- `/register` - Public
- `/admin/dashboard` - Protected (ADMIN only)
- `/agent/dashboard` - Protected (AGENT only)
- `/customer/dashboard` - Protected (CUSTOMER only)
- `/` - Redirect to login
- `*` - 404 redirect to login

---

## 🔄 Authentication Flow

### Registration Flow
```
1. User fills registration form
2. Frontend sends POST to /api/auth/register
3. Backend validates data
4. Backend checks email uniqueness
5. Backend encrypts password
6. Backend saves user to database
7. Backend generates JWT token
8. Backend returns token + user info
9. Frontend stores token in localStorage
10. Frontend redirects to appropriate dashboard
```

### Login Flow
```
1. User enters credentials
2. Frontend sends POST to /api/auth/login
3. Backend validates credentials
4. AuthenticationManager checks password
5. If valid, backend generates JWT token
6. Backend returns token + user info
7. Frontend stores token in localStorage
8. Frontend redirects to appropriate dashboard
```

### Protected Request Flow
```
1. User makes request to protected endpoint
2. Axios interceptor adds JWT to Authorization header
3. Request reaches JwtAuthenticationFilter
4. Filter extracts and validates JWT
5. Filter loads user from database
6. Filter sets authentication in SecurityContext
7. Request proceeds to controller
8. Controller processes request
9. Response returned to frontend
```

### Token Expiration Flow
```
1. User makes request with expired token
2. Backend validates token
3. Token validation fails (expired)
4. Backend returns 401 Unauthorized
5. Axios response interceptor catches 401
6. Frontend clears localStorage
7. Frontend redirects to login page
```

---

## 📦 Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  phone VARCHAR(15),
  active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  role_id BIGINT NOT NULL,
  FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### Roles Table
```sql
CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_name VARCHAR(50) UNIQUE NOT NULL,
  description VARCHAR(255)
);
```

---

## 🧪 Testing

See `TESTING_GUIDE.md` for comprehensive testing instructions.

---

## ✅ Completion Checklist

### Backend
- [x] SecurityConfig with SecurityFilterChain
- [x] PasswordEncoder (BCrypt)
- [x] AuthenticationManager
- [x] JwtUtil (token generation & validation)
- [x] JwtAuthenticationFilter
- [x] JwtAuthenticationEntryPoint
- [x] CustomUserDetailsService
- [x] RegisterRequest DTO
- [x] LoginRequest DTO
- [x] LoginResponse DTO
- [x] ApiResponse DTO
- [x] AuthService interface
- [x] AuthServiceImpl
- [x] AuthController
- [x] GlobalExceptionHandler
- [x] Exception classes

### Frontend
- [x] Axios configuration with interceptors
- [x] authService (API calls)
- [x] AuthContext (state management)
- [x] Login page
- [x] Register page
- [x] ProtectedRoute component
- [x] Dashboard pages (placeholders)
- [x] AppRoutes configuration
- [x] Token storage in localStorage
- [x] Role-based navigation
- [x] Logout functionality

### Documentation
- [x] Code comments and explanations
- [x] Testing guide
- [x] README documentation

---

## 🚀 Running the Application

### Backend
```bash
cd chatbot
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Database
```sql
-- Create database
CREATE DATABASE real_time_chatbot_support_db;

-- Insert roles
INSERT INTO roles (role_name, description) VALUES 
('ADMIN', 'Administrator'),
('AGENT', 'Support agent'),
('CUSTOMER', 'End user');
```

---

## 📝 Configuration

### application.properties
```properties
# JWT Configuration
jwt.secret=ChatSupportSystemSecretKey2026SpringBootProject@123
jwt.expiration=86400000

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/real_time_chatbot_support_db
spring.datasource.username=root
spring.datasource.password=Sanama@123

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

## 🎯 Issue 2 - COMPLETED

All requirements from Issue 2 have been successfully implemented:

✅ Spring Security Configuration
✅ JWT Token Generation & Validation
✅ User Registration with Password Encryption
✅ User Login with JWT Response
✅ Protected Routes & Authorization
✅ Exception Handling
✅ Frontend Authentication UI
✅ Role-Based Access Control
✅ Token Storage & Management
✅ Logout Functionality

**Status:** READY FOR TESTING ✅

---

## 📚 Next Steps (Milestone 2)

After testing Issue 2, proceed to:
- Real-time Chat Implementation (WebSocket)
- User Profile Management
- Chat Session Management
- Message History
- Agent Assignment Logic
