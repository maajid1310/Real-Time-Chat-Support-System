import React from 'react';
import './MessageBubble.css';

/**
 * MessageBubble Component
 *
 * Renders a single chat message with:
 *  - Alignment based on whether the current user sent it
 *  - Sender name and timestamp
 *  - Status ticks: ✓ (SENT) / ✓✓ (DELIVERED) / ✓✓ blue (READ)
 *
 * @param {object}  message       - ChatMessageResponse
 * @param {boolean} isOwnMessage  - true if the current user sent this message
 */
const MessageBubble = ({ message, isOwnMessage }) => {

  const formatTime = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const renderStatusTick = () => {
    if (!isOwnMessage) return null;

    switch (message.status) {
      case 'SENT':
        return <span className="msg-tick sent" title="Sent">✓</span>;
      case 'DELIVERED':
        return <span className="msg-tick delivered" title="Delivered">✓✓</span>;
      case 'READ':
        return <span className="msg-tick read" title="Read">✓✓</span>;
      default:
        return null;
    }
  };

  return (
    <div className={`message-bubble-wrapper ${isOwnMessage ? 'own' : 'other'}`}>
      <div className={`message-bubble ${isOwnMessage ? 'own' : 'other'}`}>
        {!isOwnMessage && (
          <div className="message-sender">{message.senderName}</div>
        )}
        <div className="message-content">{message.content}</div>
        <div className="message-meta">
          <span className="message-time">{formatTime(message.sentAt)}</span>
          {renderStatusTick()}
        </div>
      </div>
    </div>
  );
};

export default MessageBubble;
