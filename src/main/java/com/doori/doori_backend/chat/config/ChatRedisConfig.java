package com.doori.doori_backend.chat.config;

import com.doori.doori_backend.chat.redis.ChatSystemSubscriber;
import com.doori.doori_backend.chat.redis.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class ChatRedisConfig {

    @Bean
    public RedisTemplate<String, Object> chatRedisTemplate(RedisConnectionFactory factory) {
        GenericJacksonJsonRedisSerializer serializer =
            GenericJacksonJsonRedisSerializer.builder().build();

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        // autoStartup=true(기본값) — Spring이 start()를 호출하여 컨테이너를 RUNNING 상태로 만듦
        // 이후 첫 addMessageListener() 호출 시 실제 Redis SUBSCRIBE 전송
        return container;
    }

    // 채팅 메시지 구독용 어댑터
    @Bean
    public MessageListenerAdapter chatMessageListenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    // 다중 인스턴스 신규 방 알림용 어댑터
    // StringRedisSerializer: 시스템 채널은 roomId 숫자 문자열만 전달 — JSON 직렬화 불필요
    @Bean
    public MessageListenerAdapter chatSystemListenerAdapter(ChatSystemSubscriber subscriber) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(subscriber, "onNewRoom");
        adapter.setSerializer(new StringRedisSerializer());
        return adapter;
    }
}
