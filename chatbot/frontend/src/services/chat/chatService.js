import axiosInstance from '../api/axiosConfig';

/**
 * Chat API Service
 *
 * Handles HTTP calls for chat-related operations.
 * WebSocket messages are handled by webSocketService.js.
 */
const chatService = {

  /**
   * Get paginated message history for a session.
   *
   * GET /api/messages/{sessionId}?page=0&size=20
   *
   * @param {number} sessionId
   * @param {number} page - zero-based page index
   * @param {number} size - messages per page
   * @returns {Promise} ApiResponse with Page<ChatMessageResponse>
   */
  getMessageHistory: async (sessionId, page = 0, size = 20) => {
    const response = await axiosInstance.get(
      `/messages/${sessionId}?page=${page}&size=${size}`
    );
    return response.data;
  },

  /**
   * Get unread message count for the current user in a session.
   *
   * GET /api/messages/{sessionId}/unread
   *
   * @param {number} sessionId
   * @returns {Promise} ApiResponse with unread count
   */
  getUnreadCount: async (sessionId) => {
    const response = await axiosInstance.get(`/messages/${sessionId}/unread`);
    return response.data;
  },

  /**
   * Update message status via REST (HTTP fallback).
   *
   * PUT /api/messages/{messageId}/status
   *
   * @param {number} messageId
   * @param {number} sessionId
   * @param {string} status - "DELIVERED" or "READ"
   * @returns {Promise} ApiResponse with MessageStatusResponse
   */
  updateMessageStatus: async (messageId, sessionId, status) => {
    const response = await axiosInstance.put(`/messages/${messageId}/status`, {
      messageId,
      sessionId,
      status,
    });
    return response.data;
  },
};

export default chatService;
