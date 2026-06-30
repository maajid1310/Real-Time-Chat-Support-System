package com.chatbot.chatbot.mapper;

import org.springframework.stereotype.Component;

import com.chatbot.chatbot.dto.response.ChatMessageResponse;
import com.chatbot.chatbot.entity.Message;

/**
 * Entity Mapper
 *
 * Converts domain entities to DTOs.
 * Uses manual mapping (no ModelMapper) to keep full control
 * over which fields are exposed and avoid lazy-loading surprises.
 */
@Component
public class EntityMapper {

    /**
     * Map Message entity to ChatMessageResponse DTO.
     *
     * @param message the persisted message entity (with sender and chatSession loaded)
     * @return ChatMessageResponse ready for broadcast or REST response
     */
    public ChatMessageResponse toMessageResponse(Message message) {
        ChatMessageResponse response = new ChatMessageResponse();

        response.setMessageId(message.getId());
        response.setContent(message.getContent());
        response.setStatus(message.getStatus());
        response.setSentAt(message.getSentAt());
        response.setDeliveredAt(message.getDeliveredAt());
        response.setReadAt(message.getReadAt());

        // Session
        if (message.getChatSession() != null) {
            response.setSessionId(message.getChatSession().getId());
        }

        // Sender
        if (message.getSender() != null) {
            response.setSenderId(message.getSender().getId());
            response.setSenderEmail(message.getSender().getEmail());
            response.setSenderName(
                    message.getSender().getFirstName()
                    + " " + message.getSender().getLastName()
            );
            if (message.getSender().getRole() != null) {
                response.setSenderRole(message.getSender().getRole().getRoleName());
            }
        }

        return response;
    }
}
