import React from 'react';
import './ChatHeader.css';

/**
 * ChatHeader Component
 *
 * Displays the name of the other party in the chat,
 * connection status, and session information.
 *
 * @param {string}  otherPartyName - name of the other participant
 * @param {string}  otherPartyRole - CUSTOMER / AGENT
 * @param {boolean} connected      - WebSocket connection state
 * @param {number}  sessionId      - chat session ID
 */
const ChatHeader = ({ otherPartyName, otherPartyRole, connected, sessionId }) => {
  return (
    <div className="chat-header">
      <div className="chat-header-avatar">
        {otherPartyName ? otherPartyName.charAt(0).toUpperCase() : '?'}
      </div>
      <div className="chat-header-info">
        <div className="chat-header-name">
          {otherPartyName || 'Chat Session #' + sessionId}
        </div>
        <div className="chat-header-role">
          {otherPartyRole || 'Support'}
        </div>
      </div>
      <div className="chat-header-status">
        <span className={`status-dot ${connected ? 'online' : 'offline'}`} />
        <span className="status-text">{connected ? 'Online' : 'Connecting...'}</span>
      </div>
    </div>
  );
};

export default ChatHeader;
