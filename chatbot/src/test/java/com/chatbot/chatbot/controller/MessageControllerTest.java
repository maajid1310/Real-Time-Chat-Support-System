package com.chatbot.chatbot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.chatbot.chatbot.controller.chat.MessageController;
import com.chatbot.chatbot.dto.response.ChatMessageResponse;
import com.chatbot.chatbot.dto.response.MessageStatusResponse;
import com.chatbot.chatbot.enums.MessageStatus;
import com.chatbot.chatbot.exception.ResourceNotFoundException;
import com.chatbot.chatbot.security.config.SecurityConfig;
import com.chatbot.chatbot.security.filter.JwtAuthenticationFilter;
import com.chatbot.chatbot.security.jwt.JwtAuthenticationEntryPoint;
import com.chatbot.chatbot.security.jwt.JwtUtil;
import com.chatbot.chatbot.security.service.CustomUserDetailsService;
import com.chatbot.chatbot.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * MessageController REST API Tests
 *
 * Uses @WebMvcTest to test only the HTTP layer.
 * MockMvc sends HTTP requests without a real server.
 * MessageService is mocked with @MockBean.
 */
@WebMvcTest(MessageController.class)
@DisplayName("MessageController REST Tests")
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // ─────────────────────────────────────────────────
    // GET /api/messages/{sessionId}
    // ─────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    @DisplayName("GET /api/messages/{sessionId} - should return paginated history")
    void getMessageHistory_ReturnsPage() throws Exception {

        ChatMessageResponse msg = new ChatMessageResponse();
        msg.setMessageId(1L);
        msg.setContent("Hello, I need help!");
        msg.setStatus(MessageStatus.SENT);
        msg.setSessionId(1L);

        Pageable pageable = PageRequest.of(0, 20);
        Page<ChatMessageResponse> page = new PageImpl<>(List.of(msg), pageable, 1);

        when(messageService.getMessageHistory(anyLong(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/messages/1")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Message history retrieved"))
                .andExpect(jsonPath("$.data.content[0].messageId").value(1))
                .andExpect(jsonPath("$.data.content[0].content").value("Hello, I need help!"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    @DisplayName("GET /api/messages/{sessionId} - default pagination params")
    void getMessageHistory_DefaultPagination() throws Exception {

        Page<ChatMessageResponse> emptyPage = new PageImpl<>(List.of());
        when(messageService.getMessageHistory(anyLong(), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    @DisplayName("GET /api/messages/{sessionId} - session not found returns 404")
    void getMessageHistory_SessionNotFound_Returns404() throws Exception {

        when(messageService.getMessageHistory(anyLong(), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("Chat session not found with id: 999"));

        mockMvc.perform(get("/api/messages/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Chat session not found with id: 999"));
    }

    @Test
    @DisplayName("GET /api/messages/{sessionId} - unauthenticated returns 401")
    void getMessageHistory_Unauthenticated_Returns401() throws Exception {

        mockMvc.perform(get("/api/messages/1"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────
    // GET /api/messages/{sessionId}/unread
    // ─────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    @DisplayName("GET /api/messages/{sessionId}/unread - returns unread count")
    void getUnreadCount_ReturnsCount() throws Exception {

        when(messageService.getUnreadCount(anyLong(), anyString())).thenReturn(5L);

        mockMvc.perform(get("/api/messages/1/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Unread count retrieved"))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    @DisplayName("GET /api/messages/{sessionId}/unread - returns 0 when all read")
    void getUnreadCount_AllRead_ReturnsZero() throws Exception {

        when(messageService.getUnreadCount(anyLong(), anyString())).thenReturn(0L);

        mockMvc.perform(get("/api/messages/1/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
    }

    // ─────────────────────────────────────────────────
    // PUT /api/messages/{messageId}/status
    // ─────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "agent@test.com", roles = {"AGENT"})
    @DisplayName("PUT /api/messages/{messageId}/status - update to DELIVERED")
    void updateStatus_ToDelivered_ReturnsOk() throws Exception {

        MessageStatusResponse statusResponse =
                new MessageStatusResponse(1L, 1L, MessageStatus.DELIVERED);

        when(messageService.updateStatusAndBroadcast(any())).thenReturn(statusResponse);

        String body = """
                {
                    "messageId": 1,
                    "sessionId": 1,
                    "status": "DELIVERED"
                }
                """;

        mockMvc.perform(put("/api/messages/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DELIVERED"))
                .andExpect(jsonPath("$.data.messageId").value(1));
    }

    @Test
    @WithMockUser(username = "agent@test.com", roles = {"AGENT"})
    @DisplayName("PUT /api/messages/{messageId}/status - update to READ")
    void updateStatus_ToRead_ReturnsOk() throws Exception {

        MessageStatusResponse statusResponse =
                new MessageStatusResponse(1L, 1L, MessageStatus.READ);

        when(messageService.updateStatusAndBroadcast(any())).thenReturn(statusResponse);

        String body = """
                {
                    "messageId": 1,
                    "sessionId": 1,
                    "status": "READ"
                }
                """;

        mockMvc.perform(put("/api/messages/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READ"));
    }

    @Test
    @WithMockUser(username = "agent@test.com", roles = {"AGENT"})
    @DisplayName("PUT /api/messages/{messageId}/status - message not found returns 404")
    void updateStatus_MessageNotFound_Returns404() throws Exception {

        when(messageService.updateStatusAndBroadcast(any()))
                .thenThrow(new ResourceNotFoundException("Message not found with id: 999"));

        String body = """
                {
                    "messageId": 999,
                    "sessionId": 1,
                    "status": "DELIVERED"
                }
                """;

        mockMvc.perform(put("/api/messages/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
