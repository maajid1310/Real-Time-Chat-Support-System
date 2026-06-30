import React from 'react';
import './TypingIndicator.css';

/**
 * TypingIndicator Component
 *
 * Displays an animated "X is typing..." indicator.
 * Shown when the other party broadcasts a typing event.
 *
 * @param {string}  displayName - name of the typing user
 * @param {boolean} visible     - whether to show the indicator
 */
const TypingIndicator = ({ displayName, visible }) => {
  if (!visible) return null;

  return (
    <div className="typing-indicator-wrapper">
      <div className="typing-indicator">
        <span className="typing-name">{displayName}</span>
        <span className="typing-text"> is typing</span>
        <span className="typing-dots">
          <span className="dot" />
          <span className="dot" />
          <span className="dot" />
        </span>
      </div>
    </div>
  );
};

export default TypingIndicator;
