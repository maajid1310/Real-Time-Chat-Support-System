import React, { useState, useEffect, useRef, useCallback } from 'react';
import ChatHeader from './ChatHeader';
import MessageBubble from './MessageBubble';
import TypingIndicator from './TypingIndicator';
import MessageInput from './MessageInput';
import useWebSocket from '../../hooks/useWebSocket';
import chatService from '../../services/chat/chatService';
import './ChatWindow.css';

/**
 * ChatWindow Component
 *
 * The main chat interface. Wires together:
 *  - WebSocket connection (via useWebSocket hook)
 *  - Message history loading via REST (GET /api/messages/{sessionId})
 *  - Live message receiving and sending
 *  - Typing indicator (broadcast + receive)
 *  - Message status updates (DELIVERED / READ)
 *  - Infinite scroll for older messages (pagination)
 *  - Auto-scroll to latest message
 *  - Error display and reconnection feedback
 *
 * @param {number} sessionId       - the chat session ID to connect to
 * @param {object} currentUser     - { userId, email, firstName, lastName, role }
 * @param {object} otherParty      - { firstName, lastName, role } for header display
 */
const ChatWindow = ({ sessionId, currentUser, otherParty }) => {
  const [messages, setMessages]         = useState([]);
  const [page, setPage]                 = useState(0);
  const [hasMore, setHasMore]           = useState(true);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [typingUser, setTypingUser]     = useState(null);
  const [wsError, setWsError]           = useState(null);

  const messagesEndRef   = useRef(null);
  const messagesTopRef   = useRef(null);
  const typingTimerRef   = useRef(null);
  const token            = localStorage.getItem('token');

  // ─── WebSocket callbacks ────────────────────────────────────────────

  const handleNewMessage = useCallback((msg) => {
    setMessages((prev) => {
      // Avoid duplicates if REST history already has this message
      const exists = prev.some((m) => m.messageId === msg.messageId);
      if (exists) return prev;
      return [...prev, msg];
    });

    // If message is from the other party, mark as DELIVERED via WebSocket
    if (msg.senderEmail !== currentUser?.email) {
      sendStatusUpdate(msg.messageId, 'DELIVERED');
    }
  }, [currentUser]);

  const handleTyping = useCallback((typingData) => {
    // Only show indicator for the other party
    if (typingData.username !== currentUser?.email) {
      if (typingData.typing) {
        setTypingUser(typingData.displayName);
        clearTimeout(typingTimerRef.current);
        // Auto-hide after 3 seconds if no follow-up typing event
        typingTimerRef.current = setTimeout(() => setTypingUser(null), 3000);
      } else {
        clearTimeout(typingTimerRef.current);
        setTypingUser(null);
      }
    }
  }, [currentUser]);

  const handleStatusUpdate = useCallback((statusData) => {
    setMessages((prev) =>
      prev.map((m) =>
        m.messageId === statusData.messageId
          ? { ...m, status: statusData.status }
          : m
      )
    );
  }, []);

  // ─── WebSocket hook ─────────────────────────────────────────────────

  const { connected, error, sendMessage, sendTyping, sendStatusUpdate } =
    useWebSocket(sessionId, token, handleNewMessage, handleTyping, handleStatusUpdate);

  // ─── Load message history on mount ─────────────────────────────────

  useEffect(() => {
    if (!sessionId) return;
    loadHistory(0);
  }, [sessionId]);

  const loadHistory = async (pageNum) => {
    if (loadingHistory) return;
    setLoadingHistory(true);
    try {
      const response = await chatService.getMessageHistory(sessionId, pageNum, 20);
      if (response.success) {
        const pageData = response.data;
        const newMessages = pageData.content || [];

        if (pageNum === 0) {
          setMessages(newMessages);
        } else {
          setMessages((prev) => [...newMessages, ...prev]);
        }

        setHasMore(!pageData.last);
        setPage(pageNum);
      }
    } catch (err) {
      console.error('Failed to load message history:', err);
    } finally {
      setLoadingHistory(false);
    }
  };

  // ─── Auto-scroll to bottom on new messages ──────────────────────────

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // ─── Mark messages as READ when window is focused ───────────────────

  useEffect(() => {
    if (!connected || messages.length === 0) return;

    messages.forEach((msg) => {
      if (msg.senderEmail !== currentUser?.email && msg.status !== 'READ') {
        sendStatusUpdate(msg.messageId, 'READ');
      }
    });
  }, [connected, messages.length]);

  // ─── Clean up typing timer on unmount ───────────────────────────────

  useEffect(() => {
    return () => clearTimeout(typingTimerRef.current);
  }, []);

  // ─── Handlers ───────────────────────────────────────────────────────

  const handleSend = (content) => {
    if (!connected) return;
    sendMessage(content);
  };

  const handleTypingStart = () => {
    if (connected) sendTyping(true);
  };

  const handleTypingStop = () => {
    if (connected) sendTyping(false);
  };

  const handleLoadMore = () => {
    if (hasMore && !loadingHistory) {
      loadHistory(page + 1);
    }
  };

  const isOwnMessage = (msg) => msg.senderEmail === currentUser?.email;

  // ─── Render ──────────────────────────────────────────────────────────

  return (
    <div className="chat-window">
      <ChatHeader
        otherPartyName={
          otherParty
            ? `${otherParty.firstName} ${otherParty.lastName}`
            : `Session #${sessionId}`
        }
        otherPartyRole={otherParty?.role}
        connected={connected}
        sessionId={sessionId}
      />

      {(error || wsError) && (
        <div className="chat-error-banner">
          ⚠ Connection issue: {error || wsError}. Reconnecting...
        </div>
      )}

      <div className="chat-messages-container">

        {/* Load more button */}
        {hasMore && (
          <div className="load-more-wrapper">
            <button
              className="load-more-btn"
              onClick={handleLoadMore}
              disabled={loadingHistory}
            >
              {loadingHistory ? 'Loading...' : 'Load earlier messages'}
            </button>
          </div>
        )}

        {/* Message list */}
        {messages.map((msg) => (
          <MessageBubble
            key={msg.messageId}
            message={msg}
            isOwnMessage={isOwnMessage(msg)}
          />
        ))}

        {/* Typing indicator */}
        <TypingIndicator
          displayName={typingUser}
          visible={!!typingUser}
        />

        {/* Scroll anchor */}
        <div ref={messagesEndRef} />
      </div>

      <MessageInput
        onSend={handleSend}
        onTypingStart={handleTypingStart}
        onTypingStop={handleTypingStop}
        disabled={!connected}
      />
    </div>
  );
};

export default ChatWindow;
