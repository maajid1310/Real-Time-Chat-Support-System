import React, { useState, useRef, useCallback } from 'react';
import './MessageInput.css';

/**
 * MessageInput Component
 *
 * Text input for composing and sending messages.
 * Features:
 *  - Send on Enter key (Shift+Enter for newline)
 *  - Triggers typing indicator with 500ms debounce
 *  - Stops typing indicator on send or after 2s of no input
 *  - Disabled when WebSocket is not connected
 *
 * @param {Function} onSend        - called with content string when sending
 * @param {Function} onTypingStart - called when user starts typing
 * @param {Function} onTypingStop  - called when user stops typing
 * @param {boolean}  disabled      - disable input when not connected
 */
const MessageInput = ({ onSend, onTypingStart, onTypingStop, disabled }) => {
  const [content, setContent] = useState('');
  const typingTimer = useRef(null);
  const isTyping = useRef(false);

  const stopTyping = useCallback(() => {
    if (isTyping.current) {
      isTyping.current = false;
      onTypingStop && onTypingStop();
    }
  }, [onTypingStop]);

  const handleChange = (e) => {
    const value = e.target.value;
    setContent(value);

    if (value.trim().length > 0) {
      if (!isTyping.current) {
        isTyping.current = true;
        onTypingStart && onTypingStart();
      }

      // Reset the stop-typing timer on every keystroke
      clearTimeout(typingTimer.current);
      typingTimer.current = setTimeout(stopTyping, 2000);
    } else {
      stopTyping();
      clearTimeout(typingTimer.current);
    }
  };

  const handleSend = () => {
    const trimmed = content.trim();
    if (!trimmed || disabled) return;

    stopTyping();
    clearTimeout(typingTimer.current);
    onSend(trimmed);
    setContent('');
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="message-input-container">
      <textarea
        className="message-input"
        placeholder={disabled ? 'Connecting...' : 'Type a message...'}
        value={content}
        onChange={handleChange}
        onKeyDown={handleKeyDown}
        disabled={disabled}
        rows={1}
      />
      <button
        className="send-button"
        onClick={handleSend}
        disabled={disabled || !content.trim()}
        title="Send message"
      >
        ➤
      </button>
    </div>
  );
};

export default MessageInput;
