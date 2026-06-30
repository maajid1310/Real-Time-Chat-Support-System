import React from 'react';
import { useParams, Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import ChatWindow from '../../components/chat/ChatWindow';
import './Chat.css';

/**
 * Chat Page
 *
 * Route: /chat/:sessionId
 *
 * Extracts sessionId from URL and currentUser from AuthContext.
 * Renders the ChatWindow for the given session.
 *
 * Example URLs:
 *   /chat/1  → customer or agent joins session 1
 */
const ChatPage = () => {
  const { sessionId } = useParams();
  const { user, isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!sessionId) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="chat-page">
      <div className="chat-page-container">
        <ChatWindow
          sessionId={parseInt(sessionId, 10)}
          currentUser={user}
          otherParty={null}
        />
      </div>
    </div>
  );
};

export default ChatPage;
