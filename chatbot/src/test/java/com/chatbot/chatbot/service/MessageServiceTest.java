package com.chatbot.chatbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.chatbot.chatbot.dto.request.MessageStatusRequest;
import com.chatbot.chatbot.dto.request.SendMessageRequest;
import com.chatbot.chatbot.dto.request.TypingRequest;
import com.chatbot.chatbot.dto.response.ChatMessageResponse;
import com.chatbot.chatbot.dto.response.MessageStatusResponse;
import com.chatbot.chatbot.dto.response.TypingResponse;
import com.chatbot.chatbot.entity.ChatSession;
import com.chatbot.chatbot.entity.Message;
import com.chatbot.chatbot.entity.Role;
import com.chatbot.chatbot.entity.User;
import com.chatbot.chatbot.enums.MessageStatus;
import com.chatbot.chatbot.exception.BadRequestException;
import com.chatbot.chatbot.exception.ResourceNotFoundException;
import com.chatbot.chatbot.mapper.EntityMapper;
import com.chatbot.chatbot.repository.ChatSessionRepository;
import com.chatbot.chatbot.repository.MessageRepository;
import com.chatbot.chatbot.repository.UserRepository;
import com.chatbot.chatbot.service.impl.MessageServiceImpl;
import com.chatbot.chatbot.websocket.ChatMessageBroker;

/**
 * Unit Tests for MessageServiceImpl
 *
 * Tests all service methods using Mockito.
 * No database or Spring context required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService Unit Tests")
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageBroker chatMessageBroker;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User customer;
    private User agent;
    private ChatSession chatSession;
    private Message message;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = new Role();
        customerRole.setRoleName("CUSTOMER");

        customer = new User();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@test.com");
        customer.setRole(customerRole);

        agent = new User();
        agent.setId(2L);
        agent.setFirstName("Agent");
        agent.setLastName("Smith");
        agent.setEmail("agent@test.com");

        chatSession = new ChatSession();
        chatSession.setStatus(com.chatbot.chatbot.enums.ChatStatus.ACTIVE);

        message = new Message();
        message.setContent("Hello, I need help!");
        message.setStatus(MessageStatus.SENT);
        message.setSender(customer);
        message.setChatSession(chatSession);
    }

    // ─────────────────────────────────────────────────
    // saveAndBroadcast Tests
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("saveAndBroadcast: should save message and broadcast successfully")
    void saveAndBroadcast_Success() {
        // Arrange
        SendMessageRequest request = new SendMessageRequest(1L, "Hello, I need help!");

        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(chatSession));
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(customer));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        ChatMessageResponse expectedResponse = new ChatMessageResponse();
        expectedResponse.setContent("Hello, I need help!");
        expectedResponse.setStatus(MessageStatus.SENT);
        when(entityMapper.toMessageResponse(message)).thenReturn(expectedResponse);

        // Act
        ChatMessageResponse result = messageService.saveAndBroadcast(request, "john@test.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hello, I need help!");
        assertThat(result.getStatus()).isEqualTo(MessageStatus.SENT);

        verify(messageRepository, times(1)).save(any(Message.class));
        verify(chatMessageBroker, times(1)).broadcastMessage(anyLong(), any(ChatMessageResponse.class));
    }

    @Test
    @DisplayName("saveAndBroadcast: should throw ResourceNotFoundException for invalid session")
    void saveAndBroadcast_InvalidSession_ThrowsException() {
        // Arrange
        SendMessageRequest request = new SendMessageRequest(999L, "Hello");
        when(chatSessionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> messageService.saveAndBroadcast(request, "john@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Chat session not found with id: 999");

        verify(messageRepository, never()).save(any());
        verify(chatMessageBroker, never()).broadcastMessage(anyLong(), any());
    }

    @Test
    @DisplayName("saveAndBroadcast: should throw ResourceNotFoundException for invalid user")
    void saveAndBroadcast_InvalidUser_ThrowsException() {
        // Arrange
        SendMessageRequest request = new SendMessageRequest(1L, "Hello");
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(chatSession));
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> messageService.saveAndBroadcast(request, "unknown@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveAndBroadcast: should throw BadRequestException for blank content")
    void saveAndBroadcast_BlankContent_ThrowsException() {
        // Arrange
        SendMessageRequest request = new SendMessageRequest(1L, "   ");
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(chatSession));
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(customer));

        // Act & Assert
        assertThatThrownBy(() -> messageService.saveAndBroadcast(request, "john@test.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Message content cannot be blank");
    }

    // ─────────────────────────────────────────────────
    // updateStatusAndBroadcast Tests
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus: SENT → DELIVERED should succeed")
    void updateStatus_SentToDelivered_Success() {
        // Arrange
        message.setStatus(MessageStatus.SENT);
        MessageStatusRequest request = new MessageStatusRequest(1L, 1L, MessageStatus.DELIVERED);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        // Act
        MessageStatusResponse response = messageService.updateStatusAndBroadcast(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(MessageStatus.DELIVERED);
        verify(chatMessageBroker, times(1)).broadcastStatus(anyLong(), any());
    }

    @Test
    @DisplayName("updateStatus: DELIVERED → READ should succeed")
    void updateStatus_DeliveredToRead_Success() {
        // Arrange
        message.setStatus(MessageStatus.DELIVERED);
        MessageStatusRequest request = new MessageStatusRequest(1L, 1L, MessageStatus.READ);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        // Act
        MessageStatusResponse response = messageService.updateStatusAndBroadcast(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(MessageStatus.READ);
        verify(chatMessageBroker, times(1)).broadcastStatus(anyLong(), any());
    }

    @Test
    @DisplayName("updateStatus: READ → SENT should throw BadRequestException (backward transition)")
    void updateStatus_BackwardTransition_ThrowsException() {
        // Arrange
        message.setStatus(MessageStatus.READ);
        MessageStatusRequest request = new MessageStatusRequest(1L, 1L, MessageStatus.SENT);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        // Act & Assert
        assertThatThrownBy(() -> messageService.updateStatusAndBroadcast(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status transition");

        verify(chatMessageBroker, never()).broadcastStatus(anyLong(), any());
    }

    @Test
    @DisplayName("updateStatus: DELIVERED → SENT should throw BadRequestException")
    void updateStatus_DeliveredToSent_ThrowsException() {
        // Arrange
        message.setStatus(MessageStatus.DELIVERED);
        MessageStatusRequest request = new MessageStatusRequest(1L, 1L, MessageStatus.SENT);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        // Act & Assert
        assertThatThrownBy(() -> messageService.updateStatusAndBroadcast(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("updateStatus: should throw ResourceNotFoundException for invalid messageId")
    void updateStatus_InvalidMessage_ThrowsException() {
        // Arrange
        MessageStatusRequest request = new MessageStatusRequest(999L, 1L, MessageStatus.DELIVERED);
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> messageService.updateStatusAndBroadcast(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Message not found with id: 999");
    }

    // ─────────────────────────────────────────────────
    // broadcastTyping Tests
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("broadcastTyping: should build and broadcast typing response")
    void broadcastTyping_Success() {
        // Arrange
        TypingRequest request = new TypingRequest(1L, true);
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(customer));

        // Act
        TypingResponse response = messageService.broadcastTyping(request, "john@test.com");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isTyping()).isTrue();
        assertThat(response.getSessionId()).isEqualTo(1L);
        assertThat(response.getDisplayName()).isEqualTo("John Doe");
        verify(chatMessageBroker, times(1)).broadcastTyping(anyLong(), any(TypingResponse.class));
    }

    @Test
    @DisplayName("broadcastTyping: typing=false should broadcast stop event")
    void broadcastTyping_StopTyping_Success() {
        // Arrange
        TypingRequest request = new TypingRequest(1L, false);
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(customer));

        // Act
        TypingResponse response = messageService.broadcastTyping(request, "john@test.com");

        // Assert
        assertThat(response.isTyping()).isFalse();
        verify(chatMessageBroker, times(1)).broadcastTyping(anyLong(), any());
    }

    // ─────────────────────────────────────────────────
    // getMessageHistory (Pagination) Tests
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("getMessageHistory: should return paginated messages for valid session")
    void getMessageHistory_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Message> messagePage = new PageImpl<>(List.of(message), pageable, 1);

        when(chatSessionRepository.existsById(1L)).thenReturn(true);
        when(messageRepository.findByChatSessionId(1L, pageable)).thenReturn(messagePage);

        ChatMessageResponse mappedResponse = new ChatMessageResponse();
        mappedResponse.setContent("Hello, I need help!");
        when(entityMapper.toMessageResponse(message)).thenReturn(mappedResponse);

        // Act
        Page<ChatMessageResponse> result = messageService.getMessageHistory(1L, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Hello, I need help!");
        verify(messageRepository, times(1)).findByChatSessionId(1L, pageable);
    }

    @Test
    @DisplayName("getMessageHistory: should throw ResourceNotFoundException for invalid session")
    void getMessageHistory_InvalidSession_ThrowsException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(chatSessionRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> messageService.getMessageHistory(999L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Chat session not found with id: 999");

        verify(messageRepository, never()).findByChatSessionId(anyLong(), any());
    }

    @Test
    @DisplayName("getMessageHistory: should return empty page when no messages exist")
    void getMessageHistory_EmptySession_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Message> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(chatSessionRepository.existsById(1L)).thenReturn(true);
        when(messageRepository.findByChatSessionId(1L, pageable)).thenReturn(emptyPage);

        // Act
        Page<ChatMessageResponse> result = messageService.getMessageHistory(1L, pageable);

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    // ─────────────────────────────────────────────────
    // getUnreadCount Tests
    // ─────────────────────────────────────────────────

    @Test
    @DisplayName("getUnreadCount: should return correct unread message count")
    void getUnreadCount_Success() {
        // Arrange
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(customer));
        when(messageRepository.countUnreadMessages(1L, 1L)).thenReturn(3L);

        // Act
        Long count = messageService.getUnreadCount(1L, "john@test.com");

        // Assert
        assertThat(count).isEqualTo(3L);
        verify(messageRepository, times(1)).countUnreadMessages(1L, 1L);
    }

    @Test
    @DisplayName("getUnreadCount: should return 0 when no unread messages")
    void getUnreadCount_NoUnread_ReturnsZero() {
        // Arrange
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(customer));
        when(messageRepository.countUnreadMessages(1L, 1L)).thenReturn(0L);

        // Act
        Long count = messageService.getUnreadCount(1L, "john@test.com");

        // Assert
        assertThat(count).isEqualTo(0L);
    }
}
