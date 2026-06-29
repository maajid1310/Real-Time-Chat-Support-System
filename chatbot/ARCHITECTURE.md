# 🏗️ ARCHITECTURE DIAGRAM - Issue 2

## Complete Authentication & Authorization Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           CLIENT (React App)                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────────────┐  │
│  │ Login Page   │    │ Register Page│    │ Dashboard Pages          │  │
│  │ Login.jsx    │    │ Register.jsx │    │ - AdminDashboard.jsx     │  │
│  └──────┬───────┘    └──────┬───────┘    │ - AgentDashboard.jsx     │  │
│         │                   │             │ - CustomerDashboard.jsx  │  │
│         └───────┬───────────┘             └──────────────────────────┘  │
│                 │                                                         │
│         ┌───────▼────────┐                                               │
│         │  AuthContext   │ ◄─── useAuth() hook                          │
│         │  (Global State)│                                               │
│         └───────┬────────┘                                               │
│                 │                                                         │
│         ┌───────▼────────┐                                               │
│         │  authService   │                                               │
│         │  - register()  │                                               │
│         │  - login()     │                                               │
│         │  - logout()    │                                               │
│         └───────┬────────┘                                               │
│                 │                                                         │
│         ┌───────▼────────┐                                               │
│         │  axiosConfig   │                                               │
│         │  (HTTP Client) │                                               │
│         │  + Interceptors│                                               │
│         └───────┬────────┘                                               │
│                 │                                                         │
└─────────────────┼─────────────────────────────────────────────────────┘
                  │
                  │ HTTP Request (JWT in Header)
                  │
┌─────────────────▼─────────────────────────────────────────────────────┐
│                      SPRING SECURITY FILTER CHAIN                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  1. ┌──────────────────────────────────┐                                │
│     │ JwtAuthenticationFilter          │                                │
│     │ - Extract JWT from header        │                                │
│     │ - Validate token                 │                                │
│     │ - Load user from database        │                                │
│     │ - Set SecurityContext            │                                │
│     └─────────────┬────────────────────┘                                │
│                   │                                                       │
│  2. ┌─────────────▼────────────────────┐                                │
│     │ UsernamePasswordAuthenticationFilter                              │
│     └─────────────┬────────────────────┘                                │
│                   │                                                       │
│  3. ┌─────────────▼────────────────────┐                                │
│     │ Other Security Filters           │                                │
│     └─────────────┬────────────────────┘                                │
│                   │                                                       │
└───────────────────┼──────────────────────────────────────────────────┘
                    │
┌───────────────────▼──────────────────────────────────────────────────┐
│                        SPRING BOOT APPLICATION                         │
├────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────── CONTROLLER LAYER ────────────────────┐           │
│  │                                                           │           │
│  │  ┌────────────────────────────────────────────────────┐ │           │
│  │  │         AuthController                             │ │           │
│  │  │  POST /api/auth/register                          │ │           │
│  │  │  POST /api/auth/login                             │ │           │
│  │  └────────────────┬───────────────────────────────────┘ │           │
│  └───────────────────┼───────────────────────────────────┘           │
│                      │                                                  │
│  ┌───────────────────▼─── SERVICE LAYER ──────────────────┐           │
│  │                                                           │           │
│  │  ┌────────────────────────────────────────────────────┐ │           │
│  │  │         AuthServiceImpl                            │ │           │
│  │  │  + register(RegisterRequest)                      │ │           │
│  │  │    - Check email exists                           │ │           │
│  │  │    - Encrypt password                             │ │           │
│  │  │    - Save user                                    │ │           │
│  │  │    - Generate JWT                                 │ │           │
│  │  │  + login(LoginRequest)                            │ │           │
│  │  │    - Authenticate credentials                     │ │           │
│  │  │    - Generate JWT                                 │ │           │
│  │  └────┬───────────────────────┬──────────────────────┘ │           │
│  └───────┼───────────────────────┼─────────────────────────┘           │
│          │                       │                                      │
│          │                       │                                      │
│  ┌───────▼────────┐    ┌─────────▼──────────┐                          │
│  │ PasswordEncoder│    │AuthenticationManager│                          │
│  │   (BCrypt)     │    │                     │                          │
│  └────────────────┘    └─────────┬───────────┘                          │
│                                  │                                      │
│  ┌───────────────────────────────▼────────────────────────────┐        │
│  │         CustomUserDetailsService                            │        │
│  │  + loadUserByUsername(email)                               │        │
│  │    - Query UserRepository                                  │        │
│  │    - Return Spring Security UserDetails                    │        │
│  └───────────────────────┬─────────────────────────────────────┘        │
│                          │                                              │
│  ┌───────────────────────▼─── REPOSITORY LAYER ───────────────┐        │
│  │                                                              │        │
│  │  ┌──────────────────┐      ┌─────────────────────┐         │        │
│  │  │ UserRepository   │      │ RoleRepository      │         │        │
│  │  │ - findByEmail()  │      │ - findByRoleName()  │         │        │
│  │  │ - existsByEmail()│      │                     │         │        │
│  │  └────────┬─────────┘      └──────┬──────────────┘         │        │
│  └───────────┼────────────────────────┼─────────────────────────┘        │
│              │                        │                                  │
│  ┌───────────▼────────────────────────▼───── ENTITY LAYER ─────┐        │
│  │                                                               │        │
│  │  ┌──────────────────┐      ┌─────────────────────┐          │        │
│  │  │ User Entity      │      │ Role Entity         │          │        │
│  │  │ - id             │      │ - id                │          │        │
│  │  │ - firstName      │      │ - roleName          │          │        │
│  │  │ - lastName       │      │ - description       │          │        │
│  │  │ - email          │      │ - users (List)      │          │        │
│  │  │ - password       │      └─────────────────────┘          │        │
│  │  │ - phone          │                                       │        │
│  │  │ - active         │                                       │        │
│  │  │ - role           │                                       │        │
│  │  │ - createdAt      │                                       │        │
│  │  │ - updatedAt      │                                       │        │
│  │  └────────┬─────────┘                                       │        │
│  └───────────┼──────────────────────────────────────────────────┘        │
│              │                                                           │
└──────────────┼──────────────────────────────────────────────────────────┘
               │
┌──────────────▼──────────────────────────────────────────────────────────┐
│                         MySQL DATABASE                                   │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌────────────────────┐         ┌─────────────────────┐                 │
│  │  users             │         │  roles              │                 │
│  │  - id (PK)         │         │  - id (PK)          │                 │
│  │  - first_name      │         │  - role_name        │                 │
│  │  - last_name       │         │  - description      │                 │
│  │  - email (UNIQUE)  │         └─────────────────────┘                 │
│  │  - password        │                                                  │
│  │  - phone           │                                                  │
│  │  - active          │                                                  │
│  │  - role_id (FK) ───┼──────────┐                                      │
│  │  - created_at      │          │                                      │
│  │  - updated_at      │          │                                      │
│  └────────────────────┘          │                                      │
│                                  │                                      │
└──────────────────────────────────┼──────────────────────────────────────┘
                                   │
                        Foreign Key Relationship


═══════════════════════════════════════════════════════════════════════════

## JWT UTILITY COMPONENTS

┌─────────────────────────────────────────────────────────────────────────┐
│                          JWT COMPONENTS                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  JwtUtil                                                         │    │
│  │  + generateToken(username)        → Creates signed JWT          │    │
│  │  + validateToken(token)            → Validates signature        │    │
│  │  + getUsernameFromToken(token)     → Extracts username          │    │
│  │  + getExpirationDate(token)        → Gets expiry date           │    │
│  │  + isTokenExpired(token)           → Checks expiration          │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  JwtAuthenticationEntryPoint                                     │    │
│  │  + commence()                      → Returns 401 response       │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════

## SECURITY CONFIGURATION

┌─────────────────────────────────────────────────────────────────────────┐
│                          SecurityConfig                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  SecurityFilterChain Bean                                        │    │
│  │  - CSRF: DISABLED                                               │    │
│  │  - CORS: ENABLED                                                │    │
│  │  - Session: STATELESS                                            │    │
│  │  - Public Endpoints: /api/auth/**, /swagger-ui/**, /ws/**      │    │
│  │  - Protected: All other endpoints                               │    │
│  │  - JWT Filter: Added before UsernamePasswordAuthenticationFilter│    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  PasswordEncoder Bean (BCrypt)                                  │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  AuthenticationManager Bean                                      │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════

## EXCEPTION HANDLING

┌─────────────────────────────────────────────────────────────────────────┐
│                     GlobalExceptionHandler                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  Handles:                                                                │
│  ✓ MethodArgumentNotValidException     → 400 Bad Request                │
│  ✓ BadCredentialsException             → 401 Unauthorized               │
│  ✓ UsernameNotFoundException           → 404 Not Found                  │
│  ✓ ResourceNotFoundException           → 404 Not Found                  │
│  ✓ BadRequestException                 → 400 Bad Request                │
│  ✓ UnauthorizedException               → 401 Unauthorized               │
│  ✓ ExpiredJwtException                 → 401 Unauthorized               │
│  ✓ MalformedJwtException               → 401 Unauthorized               │
│  ✓ SignatureException                  → 401 Unauthorized               │
│  ✓ Exception                           → 500 Internal Server Error      │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════

## DATA FLOW DIAGRAMS

### REGISTRATION FLOW
┌──────────┐   Register    ┌──────────┐   Validate   ┌──────────┐
│          │   Request     │          │   & Save     │          │
│  Client  ├──────────────►│Controller├─────────────►│ Service  │
│          │               │          │              │          │
└────┬─────┘               └──────────┘              └────┬─────┘
     │                                                     │
     │                                                     ▼
     │                                            ┌────────────────┐
     │                                            │1. Check email  │
     │                                            │2. Encrypt pass │
     │                                            │3. Save user    │
     │                                            │4. Generate JWT │
     │                                            └────────┬───────┘
     │                                                     │
     │    JWT Token                                       │
     │    + User Info                                     ▼
     │◄───────────────────────────────────────────────Database
     │
     └──► Store in localStorage


### LOGIN FLOW
┌──────────┐   Login      ┌──────────┐   Auth       ┌──────────┐
│          │   Request    │          │   Manager    │          │
│  Client  ├─────────────►│Controller├─────────────►│ Service  │
│          │              │          │              │          │
└────┬─────┘              └──────────┘              └────┬─────┘
     │                                                    │
     │                                                    ▼
     │                                          ┌──────────────────┐
     │                                          │1. Validate creds │
     │                                          │2. Load user      │
     │                                          │3. Generate JWT   │
     │                                          └────────┬─────────┘
     │                                                   │
     │    JWT Token                                     │
     │    + User Info                                   ▼
     │◄────────────────────────────────────────────Database
     │
     └──► Store in localStorage


### PROTECTED REQUEST FLOW
┌──────────┐   Request    ┌──────────┐   Validate   ┌──────────┐
│          │   + JWT      │          │   Token      │          │
│  Client  ├─────────────►│JWT Filter├─────────────►│Controller│
│          │              │          │              │          │
└──────────┘              └────┬─────┘              └────┬─────┘
                               │                         │
                               ▼                         ▼
                      ┌──────────────────┐     ┌──────────────┐
                      │1. Extract token  │     │Process req   │
                      │2. Validate       │     │Return data   │
                      │3. Load user      │     └──────────────┘
                      │4. Set context    │
                      └──────────────────┘


═══════════════════════════════════════════════════════════════════════════

## PACKAGE STRUCTURE

com.chatbot.chatbot
├── config
│   ├── CorsConfig.java
│   ├── ModelMapperConfig.java
│   └── SwaggerConfig.java
├── controller.auth
│   └── AuthController.java
├── dto
│   ├── request
│   │   ├── LoginRequest.java
│   │   └── RegisterRequest.java
│   └── response
│       ├── ApiResponse.java
│       └── LoginResponse.java
├── entity
│   ├── User.java
│   └── Role.java
├── enums
│   └── RoleType.java
├── exception
│   ├── BadRequestException.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── GlobalExceptionHandler.java
├── repository
│   ├── UserRepository.java
│   └── RoleRepository.java
├── security
│   ├── config
│   │   └── SecurityConfig.java
│   ├── filter
│   │   └── JwtAuthenticationFilter.java
│   ├── jwt
│   │   ├── JwtUtil.java
│   │   └── JwtAuthenticationEntryPoint.java
│   └── service
│       └── CustomUserDetailsService.java
├── service
│   ├── AuthService.java
│   └── impl
│       └── AuthServiceImpl.java
└── ChatbotApplication.java


═══════════════════════════════════════════════════════════════════════════

## TECHNOLOGY STACK

Backend:
├── Spring Boot 3.5.3
├── Spring Security 6
├── Spring Data JPA
├── MySQL 8.0
├── JWT (jjwt 0.11.5)
├── BCrypt (Password Encryption)
├── Swagger/OpenAPI 3
└── Java 17

Frontend:
├── React 18
├── Vite
├── React Router DOM
├── Axios
├── Context API
└── CSS3

═══════════════════════════════════════════════════════════════════════════
