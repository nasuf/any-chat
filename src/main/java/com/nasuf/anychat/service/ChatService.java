package com.nasuf.anychat.service;

import com.nasuf.anychat.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private SimpMessageSendingOperations simpMessageSendingOperations;

    public void sendMsg(@Payload ChatMessage chatMessage) {
        LOGGER.info(chatMessage.toString());
        simpMessageSendingOperations.convertAndSend(String.format("/topic/%s", chatMessage.getChatRoom()), chatMessage);
    }

    public void alertUserStatus(@Payload ChatMessage chatMessage) {
        simpMessageSendingOperations.convertAndSend(String.format("/topic/%s", chatMessage.getChatRoom()), chatMessage);
    }
}
