-- ============================================================
-- Issue 4: WebSocket Messaging System — SQL Verification Script
-- Database: real_time_chatbot_support_db
-- Run in: MySQL Workbench / DBeaver / MySQL CLI
-- ============================================================

USE real_time_chatbot_support_db;

-- ============================================================
-- SECTION 1: Verify Table Structure
-- ============================================================

-- Verify messages table has all required columns including new ones
DESCRIBE messages;

-- Expected columns:
-- id             bigint        NOT NULL  AUTO_INCREMENT
-- content        text          NOT NULL
-- status         varchar(20)   NOT NULL  (SENT/DELIVERED/READ)
-- sent_at        datetime      NOT NULL
-- delivered_at   datetime      NULL      ← NEW in Issue 4
-- read_at        datetime      NULL      ← NEW in Issue 4
-- sender_id      bigint        NOT NULL  FK → users.id
-- chat_session_id bigint       NOT NULL  FK → chat_sessions.id

-- ============================================================
-- SECTION 2: Verify All Messages in Database
-- ============================================================

SELECT
    m.id            AS message_id,
    m.content,
    m.status,
    m.sent_at,
    m.delivered_at,
    m.read_at,
    CONCAT(sender.first_name, ' ', sender.last_name) AS sender_name,
    sender.email    AS sender_email,
    r.role_name     AS sender_role,
    m.chat_session_id AS session_id
FROM messages m
JOIN users sender ON m.sender_id = sender.id
JOIN roles r ON sender.role_id = r.id
ORDER BY m.sent_at DESC;

-- ============================================================
-- SECTION 3: Verify Messages Per Session
-- ============================================================

SELECT
    cs.id           AS session_id,
    cs.status       AS session_status,
    COUNT(m.id)     AS total_messages,
    SUM(CASE WHEN m.status = 'SENT'      THEN 1 ELSE 0 END) AS sent_count,
    SUM(CASE WHEN m.status = 'DELIVERED' THEN 1 ELSE 0 END) AS delivered_count,
    SUM(CASE WHEN m.status = 'READ'      THEN 1 ELSE 0 END) AS read_count,
    CONCAT(cust.first_name, ' ', cust.last_name) AS customer_name,
    CONCAT(ag.first_name,   ' ', ag.last_name)   AS agent_name
FROM chat_sessions cs
LEFT JOIN messages m  ON m.chat_session_id = cs.id
LEFT JOIN users cust  ON cs.customer_id = cust.id
LEFT JOIN users ag    ON cs.agent_id = ag.id
GROUP BY cs.id, cs.status, cust.first_name, cust.last_name, ag.first_name, ag.last_name
ORDER BY cs.id;

-- ============================================================
-- SECTION 4: Verify Message Status Transitions
-- ============================================================

-- Messages in SENT status (not yet delivered)
SELECT id, content, sender_id, chat_session_id, sent_at
FROM messages
WHERE status = 'SENT'
ORDER BY sent_at DESC;

-- Messages in DELIVERED status
SELECT id, content, sender_id, chat_session_id, sent_at, delivered_at
FROM messages
WHERE status = 'DELIVERED'
ORDER BY delivered_at DESC;

-- Messages in READ status
SELECT id, content, sender_id, chat_session_id, sent_at, delivered_at, read_at
FROM messages
WHERE status = 'READ'
ORDER BY read_at DESC;

-- ============================================================
-- SECTION 5: Verify Unread Messages Per User Per Session
-- ============================================================

-- For customer_id = 3, session_id = 1: count messages from agent NOT yet READ
SELECT COUNT(*) AS unread_for_customer
FROM messages m
JOIN users sender ON m.sender_id = sender.id
JOIN roles r ON sender.role_id = r.id
WHERE m.chat_session_id = 1
  AND m.sender_id != 3        -- replace 3 with actual customer ID
  AND m.status != 'READ';

-- ============================================================
-- SECTION 6: Verify Pagination Query
-- ============================================================

-- Simulate: GET /api/messages/1?page=0&size=20
-- Shows first 20 messages in session 1 ordered by sentAt ASC
SELECT
    m.id,
    m.content,
    m.status,
    m.sent_at,
    CONCAT(u.first_name, ' ', u.last_name) AS sender_name
FROM messages m
JOIN users u ON m.sender_id = u.id
WHERE m.chat_session_id = 1
ORDER BY m.sent_at ASC
LIMIT 20 OFFSET 0;

-- Simulate: GET /api/messages/1?page=1&size=20 (second page)
SELECT
    m.id,
    m.content,
    m.status,
    m.sent_at,
    CONCAT(u.first_name, ' ', u.last_name) AS sender_name
FROM messages m
JOIN users u ON m.sender_id = u.id
WHERE m.chat_session_id = 1
ORDER BY m.sent_at ASC
LIMIT 20 OFFSET 20;

-- ============================================================
-- SECTION 7: Verify Active Chat Sessions
-- ============================================================

SELECT
    cs.id,
    cs.status,
    cs.start_time,
    cs.end_time,
    CONCAT(cust.first_name, ' ', cust.last_name) AS customer,
    cust.email   AS customer_email,
    CONCAT(ag.first_name,   ' ', ag.last_name)   AS agent,
    ag.email     AS agent_email,
    COUNT(m.id)  AS message_count
FROM chat_sessions cs
LEFT JOIN users cust ON cs.customer_id = cust.id
LEFT JOIN users ag   ON cs.agent_id = ag.id
LEFT JOIN messages m ON m.chat_session_id = cs.id
GROUP BY cs.id, cs.status, cs.start_time, cs.end_time,
         cust.first_name, cust.last_name, cust.email,
         ag.first_name, ag.last_name, ag.email
ORDER BY cs.start_time DESC;

-- ============================================================
-- SECTION 8: Insert Test Data for Manual Testing
-- ============================================================

-- Step 1: Make sure you have registered users via API first.
-- Step 2: Create a chat session manually (or via API POST /api/chat/start)
-- Step 3: Insert test messages for session_id = 1

-- Example test messages (adjust user IDs to match your DB)
/*
INSERT INTO messages (content, status, sent_at, sender_id, chat_session_id)
VALUES
('Hello, I need help with my order!',   'READ',      NOW() - INTERVAL 10 MINUTE, 3, 1),
('Hi! I am happy to help. What order?', 'READ',      NOW() - INTERVAL 9  MINUTE, 2, 1),
('Order #12345 placed yesterday',       'DELIVERED', NOW() - INTERVAL 5  MINUTE, 3, 1),
('Let me check that for you.',          'SENT',      NOW() - INTERVAL 2  MINUTE, 2, 1);
*/

-- ============================================================
-- SECTION 9: Verify Message Timestamps (Status Lifecycle)
-- ============================================================

SELECT
    id,
    content,
    status,
    sent_at,
    delivered_at,
    read_at,
    TIMESTAMPDIFF(SECOND, sent_at, delivered_at) AS seconds_to_deliver,
    TIMESTAMPDIFF(SECOND, delivered_at, read_at) AS seconds_to_read
FROM messages
WHERE delivered_at IS NOT NULL
ORDER BY sent_at DESC;

-- ============================================================
-- SECTION 10: Summary Dashboard
-- ============================================================

SELECT
    (SELECT COUNT(*) FROM messages)                        AS total_messages,
    (SELECT COUNT(*) FROM messages WHERE status = 'SENT')      AS sent,
    (SELECT COUNT(*) FROM messages WHERE status = 'DELIVERED') AS delivered,
    (SELECT COUNT(*) FROM messages WHERE status = 'READ')      AS read_count,
    (SELECT COUNT(*) FROM chat_sessions)                       AS total_sessions,
    (SELECT COUNT(*) FROM chat_sessions WHERE status = 'ACTIVE')  AS active_sessions,
    (SELECT COUNT(*) FROM chat_sessions WHERE status = 'WAITING') AS waiting_sessions,
    (SELECT COUNT(*) FROM chat_sessions WHERE status = 'CLOSED')  AS closed_sessions;
