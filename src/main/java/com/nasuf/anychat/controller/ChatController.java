package com.nasuf.anychat.controller;

import java.util.HashSet;
import java.util.Set;

import com.nasuf.anychat.model.ChatMessage;
import com.nasuf.anychat.redis.RedisListenerHandler;
import com.nasuf.anychat.util.JsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    @Value("${redis.channel.userStatus}")
    private String userStatus;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedisListenerHandler redisListenerHandler;

    @Autowired
    private RedisMessageListenerContainer container;
    private Set<String> chatRooms = new HashSet<>();

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        try {
            redisTemplate.convertAndSend(chatMessage.getChatRoom(), JsonUtil.parseObjToJson(chatMessage));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

        LOGGER.info("User [{}] joined room: [{}] !", chatMessage.getSender(), chatMessage.getChatRoom());
        try {
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
            headerAccessor.getSessionAttributes().put("chatroom", chatMessage.getChatRoom());
            if (chatMessage.getType().equals(ChatMessage.MessageType.JOIN)
                    && !chatRooms.contains(chatMessage.getChatRoom())) {
                container.addMessageListener(redisListenerHandler, new PatternTopic(chatMessage.getChatRoom()));
            }
            redisTemplate.opsForSet().add(chatMessage.getChatRoom(), chatMessage.getSender());
            redisTemplate.convertAndSend(userStatus, JsonUtil.parseObjToJson(chatMessage));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
