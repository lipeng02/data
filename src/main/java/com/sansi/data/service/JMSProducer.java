package com.sansi.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import javax.jms.Destination;

/**
 * @Intro 队列消息生产者
 * @Author LeePong
 * @Date 2020/4/24 16:12
 */
@Service
public class JMSProducer {
    @Autowired
    private JmsTemplate jmsTemplate;

    // 发送消息，destination是发送到的队列，message是待发送的消息
    public void sendMessage(Destination destination, Object message) {
        jmsTemplate.convertAndSend(destination, message);
    }
}
