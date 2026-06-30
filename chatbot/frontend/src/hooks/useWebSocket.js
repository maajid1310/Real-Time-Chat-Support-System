import { useEffect, useRef, useState, useCallback } from 'react';
import webSocketService from '../services/websocket/webSocketService';

/**
 * useWebSocket Hook
 *
 * Manages the full WebSocket lifecycle for a chat session.
 * Handles:
 *  - Connection with JWT
 *  - Subscriptions to chat, typing, and status topics
 *  - Reconnection on disconnect
 *  - Cleanup on unmount
 *
 * @param {number}   sessionId  - the chat session to connect to
 * @param {string}   token      - JWT from localStorage
 * @param {Function} onMessage  - callback when a new message arrives
 * @param {Function} onTyping   - callback when typing event arrives
 * @param {Function} onStatus   - callback when a status update arrives
 *
 * @returns {object} { connected, error, sendMessage, sendTyping, sendStatusUpdate }
 */
const useWebSocket = (sessionId, token, onMessage, onTyping, onStatus) => {
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState(null);

  // Keep stable refs to callbacks to avoid re-subscriptions on re-render
  const onMessageRef = useRef(onMessage);
  const onTypingRef  = useRef(onTyping);
  const onStatusRef  = useRef(onStatus);

  useEffect(() => { onMessageRef.current = onMessage; }, [onMessage]);
  useEffect(() => { onTypingRef.current  = onTyping;  }, [onTyping]);
  useEffect(() => { onStatusRef.current  = onStatus;  }, [onStatus]);

  useEffect(() => {
    if (!token || !sessionId) return;

    const handleConnected = () => {
      setConnected(true);
      setError(null);

      // Subscribe to all three topics for this session
      webSocketService.subscribeToChat(sessionId, (msg) => {
        onMessageRef.current && onMessageRef.current(msg);
      });

      webSocketService.subscribeToTyping(sessionId, (typing) => {
        onTypingRef.current && onTypingRef.current(typing);
      });

      webSocketService.subscribeToStatus(sessionId, (status) => {
        onStatusRef.current && onStatusRef.current(status);
      });
    };

    const handleError = (errMsg) => {
      setConnected(false);
      setError(errMsg || 'WebSocket connection failed');
    };

    webSocketService.connect(token, handleConnected, handleError);

    return () => {
      webSocketService.unsubscribeFromSession(sessionId);
      webSocketService.disconnect();
      setConnected(false);
    };
    // Only re-run when sessionId or token changes
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessionId, token]);

  const sendMessage = useCallback((content) => {
    webSocketService.sendMessage(sessionId, content);
  }, [sessionId]);

  const sendTyping = useCallback((isTyping) => {
    webSocketService.sendTyping(sessionId, isTyping);
  }, [sessionId]);

  const sendStatusUpdate = useCallback((messageId, status) => {
    webSocketService.sendStatusUpdate(messageId, sessionId, status);
  }, [sessionId]);

  return { connected, error, sendMessage, sendTyping, sendStatusUpdate };
};

export default useWebSocket;
