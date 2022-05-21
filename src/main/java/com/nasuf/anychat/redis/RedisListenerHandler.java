package com.nasuf.anychat.redis;

import com.nasuf.anychat.model.ChatMessage;
import com.nasuf.anychat.service.ChatService;
import com.nasuf.anychat.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

/**
 * Redis订阅频道处理类
 * @author yangzhendong01
 */
@Service
public class RedisListenerHandler extends MessageListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisListenerHandler.class);

/*    @Value("${redis.channel.msgToAll}")
    private String msgToAll;*/

    @Value("${redis.channel.userStatus}")
    private String userStatus;

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ChatService chatService;

    /**
     * 收到监听消息
     * @param message
     * @param bytes
     */
    @Override
    public void onMessage(Message message, byte[] bytes) {
        byte[] body = message.getBody();
        byte[] channel = message.getChannel();
        String rawMsg;
        String topic;
        try {
            rawMsg = redisTemplate.getStringSerializer().deserialize(body);
            topic = redisTemplate.getStringSerializer().deserialize(channel);
            LOGGER.info("Received raw message from topic:" + topic + ", raw message content：" + rawMsg);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return;
        }

       if (userStatus.equals(topic)) {
            ChatMessage chatMessage = JsonUtil.parseJsonToObj(rawMsg, ChatMessage.class);
            if (chatMessage != null) {
                chatService.alertUserStatus(chatMessage);
            }
        } else {
            ChatMessage chatMessage = JsonUtil.parseJsonToObj(rawMsg, ChatMessage.class);
            if (chatMessage != null) {
                chatService.sendMsg(chatMessage);
            }
        }
    }
}
