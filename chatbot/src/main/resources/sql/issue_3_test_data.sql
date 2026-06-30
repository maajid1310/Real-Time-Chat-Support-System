-- ============================================
-- Issue 3: Role-Based Authorization Test Data
-- ============================================
-- This script populates the database with initial roles
-- Execute this BEFORE testing the application
-- ============================================

USE chatbot_db;

-- ============================================
-- 1. Insert Roles (if not already present)
-- ============================================

INSERT INTO roles (role_name, description) VALUES 
('ADMIN', 'System administrator with full access to all features')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO roles (role_name, description) VALUES 
('AGENT', 'Support agent who handles customer queries and chats')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO roles (role_name, description) VALUES 
('CUSTOMER', 'End user who seeks support and creates chat sessions')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Verify roles inserted
SELECT * FROM roles;

-- ============================================
-- Expected Output:
-- +----+-----------+-----------------------------------------------+
-- | id | role_name | description                                   |
-- +----+-----------+-----------------------------------------------+
-- |  1 | ADMIN     | System administrator with full access...      |
-- |  2 | AGENT     | Support agent who handles customer queries... |
-- |  3 | CUSTOMER  | End user who seeks support...                 |
-- +----+-----------+-----------------------------------------------+
-- ============================================

-- ============================================
-- 2. Verify Table Structure
-- ============================================

DESCRIBE users;
DESCRIBE chat_sessions;
DESCRIBE agent_availability;

-- ============================================
-- 3. Query Helper Scripts
-- ============================================

-- Count users by role
SELECT 
    r.role_name,
    COUNT(u.id) as user_count
FROM roles r
LEFT JOIN users u ON r.id = u.role_id
GROUP BY r.id, r.role_name;

-- View all users with roles
SELECT 
    u.id,
    u.first_name,
    u.last_name,
    u.email,
    r.role_name,
    u.active,
    u.created_at
FROM users u
JOIN roles r ON u.role_id = r.id
ORDER BY u.created_at DESC;
