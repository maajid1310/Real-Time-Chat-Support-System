import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_BASE_URL = 'http://localhost:8080/ws';
const RECONNECT_DELAY = 5000;

/**
 * WebSocket Service
 *
 * Manages the SockJS + STOMP connection lifecycle.
 * Passes JWT as a query parameter during the SockJS HTTP upgrade
 * (SockJS does not support custom headers on the initial GET request).
 *
 * Usage:
 *   webSocketService.connect(token, onConnected, onError);
 *   webSocketService.subscribeToChat(sessionId, callback);
 *   webSocketService.subscribeToTyping(sessionId, callback);
 *   webSocketService.subscribeToStatus(sessionId, callback);
 *   webSocketService.sendMessage(sessionId, content);
 *   webSocketService.sendTyping(sessionId, isTyping);
 *   webSocketService.sendStatusUpdate(messageId, sessionId, status);
 *   webSocketService.disconnect();
 */
class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = {};
    this.connected = false;
    this.token = null;
  }

  /**
   * Establish WebSocket connection with JWT authentication.
   *
   * @param {string}   token        - JWT from localStorage
   * @param {Function} onConnected  - callback when STOMP is ready
   * @param {Function} onError      - callback on error / disconnect
   */
  connect(token, onConnected, onError) {
    this.token = token;

    this.client = new Client({
      // SockJS factory: token passed as query param
      webSocketFactory: () => new SockJS(`${WS_BASE_URL}?token=${token}`),

      // Auto-reconnect every 5 seconds on disconnect
      reconnectDelay: RECONNECT_DELAY,

      // Debug logs (set to () => {} in production)
      debug: (msg) => console.log('[STOMP DEBUG]', msg),

      onConnect: () => {
        this.connected = true;
        console.log('[WebSocket] Connected successfully');
        if (onConnected) onConnected();
      },

      onDisconnect: () => {
        this.connected = false;
        console.log('[WebSocket] Disconnected');
      },

      onStompError: (frame) => {
        this.connected = false;
        console.error('[WebSocket] STOMP error:', frame.headers['message']);
        if (onError) onError(frame.headers['message']);
      },

      onWebSocketError: (event) => {
        this.connected = false;
        console.error('[WebSocket] Connection error:', event);
        if (onError) onError('WebSocket connection failed');
      },
    });

    this.client.activate();
  }

  /**
   * Disconnect and clean up all subscriptions.
   */
  disconnect() {
    if (this.client) {
      // Unsubscribe all active subscriptions
      Object.values(this.subscriptions).forEach((sub) => {
        try {
          sub.unsubscribe();
        } catch (_) {}
      });
      this.subscriptions = {};
      this.client.deactivate();
      this.connected = false;
      console.log('[WebSocket] Disconnected and cleaned up');
    }
  }

  // ──────────────────────────────────────────────
  // Subscribe Methods
  // ──────────────────────────────────────────────

  /**
   * Subscribe to live messages for a chat session.
   * Destination: /topic/chat/{sessionId}
   *
   * @param {number}   sessionId - chat session ID
   * @param {Function} callback  - called with ChatMessageResponse on each message
   */
  subscribeToChat(sessionId, callback) {
    return this._subscribe(
      `chat_${sessionId}`,
      `/topic/chat/${sessionId}`,
      callback
    );
  }

  /**
   * Subscribe to typing indicators for a chat session.
   * Destination: /topic/typing/{sessionId}
   *
   * @param {number}   sessionId - chat session ID
   * @param {Function} callback  - called with TypingResponse
   */
  subscribeToTyping(sessionId, callback) {
    return this._subscribe(
      `typing_${sessionId}`,
      `/topic/typing/${sessionId}`,
      callback
    );
  }

  /**
   * Subscribe to message status updates for a chat session.
   * Destination: /topic/status/{sessionId}
   *
   * @param {number}   sessionId - chat session ID
   * @param {Function} callback  - called with MessageStatusResponse
   */
  subscribeToStatus(sessionId, callback) {
    return this._subscribe(
      `status_${sessionId}`,
      `/topic/status/${sessionId}`,
      callback
    );
  }

  // ──────────────────────────────────────────────
  // Send Methods
  // ──────────────────────────────────────────────

  /**
   * Send a chat message.
   * Destination: /app/chat.send
   *
   * @param {number} sessionId - chat session ID
   * @param {string} content   - message text
   */
  sendMessage(sessionId, content) {
    this._send('/app/chat.send', { sessionId, content });
  }

  /**
   * Broadcast a typing indicator.
   * Destination: /app/chat.typing
   *
   * @param {number}  sessionId - chat session ID
   * @param {boolean} isTyping  - true = typing, false = stopped
   */
  sendTyping(sessionId, isTyping) {
    this._send('/app/chat.typing', { sessionId, typing: isTyping });
  }

  /**
   * Update message status.
   * Destination: /app/chat.status
   *
   * @param {number} messageId - the message ID
   * @param {number} sessionId - chat session ID
   * @param {string} status    - "DELIVERED" or "READ"
   */
  sendStatusUpdate(messageId, sessionId, status) {
    this._send('/app/chat.status', { messageId, sessionId, status });
  }

  // ──────────────────────────────────────────────
  // Utilities
  // ──────────────────────────────────────────────

  isConnected() {
    return this.connected && this.client?.connected;
  }

  unsubscribeFromSession(sessionId) {
    ['chat', 'typing', 'status'].forEach((type) => {
      const key = `${type}_${sessionId}`;
      if (this.subscriptions[key]) {
        try {
          this.subscriptions[key].unsubscribe();
        } catch (_) {}
        delete this.subscriptions[key];
      }
    });
  }

  // ──────────────────────────────────────────────
  // Private Helpers
  // ──────────────────────────────────────────────

  _subscribe(key, destination, callback) {
    if (!this.client || !this.connected) {
      console.warn('[WebSocket] Cannot subscribe: not connected');
      return null;
    }

    // Unsubscribe existing subscription for this key
    if (this.subscriptions[key]) {
      this.subscriptions[key].unsubscribe();
    }

    const subscription = this.client.subscribe(destination, (frame) => {
      try {
        const data = JSON.parse(frame.body);
        callback(data);
      } catch (e) {
        console.error('[WebSocket] Failed to parse message:', e);
      }
    });

    this.subscriptions[key] = subscription;
    console.log(`[WebSocket] Subscribed to: ${destination}`);
    return subscription;
  }

  _send(destination, payload) {
    if (!this.client || !this.connected) {
      console.warn('[WebSocket] Cannot send: not connected');
      return;
    }

    this.client.publish({
      destination,
      body: JSON.stringify(payload),
    });
  }
}

// Singleton instance shared across the entire app
const webSocketService = new WebSocketService();
export default webSocketService;
