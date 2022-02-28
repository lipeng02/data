package com.sansi.data.service;

import com.alibaba.fastjson.JSONObject;
import com.sansi.data.config.JMSConfig;
import com.sansi.data.config.MqttConfiguration;
import com.sansi.data.utils.DataParser;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * MQTT消费端
 */
@Slf4j
@Configuration
public class MqttConsumer {
    public String topicJ = null;
    //封装dtu数据和topic
    Map map = new HashMap();
    @Resource
    private JMSConfig jmsConfig;
    @Resource
    private JMSProducer jmsProducer;
    @Autowired
    private DataParser dataParser;
    @Autowired
    private MqttConfiguration mqttProperties;

    @Bean
    public boolean inbound(String[] args) {
        String broker = mqttProperties.getUrl();
        String clientId = mqttProperties.getClientId();
        //Use the memory persistence
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            //接收离线消息
            connOpts.setCleanSession(true);
            connOpts.setUserName(mqttProperties.getUsername());
            connOpts.setPassword(mqttProperties.getPassword().toCharArray());
            sampleClient.connect(connOpts);
            log.info("Connected to broker: {}" ,broker);

            String[] topics = mqttProperties.getTopics();
            sampleClient.subscribe(topics);
            sampleClient.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    //昆仑通泰
                    if (topic.startsWith("j")) {
                        topicJ = topic;
                        JSONObject jsonObject = JSONObject.parseObject(message.toString());
                        dataParser.parseData(jsonObject);
                    } else {
                        byte[] payload = message.getPayload();
                        map.put("topic", topic);
                        map.put("payload", payload);
                        // 李希文塔机防碰撞210字节的数据截取掉后十个字节
                        if (payload.length == 210) {
                            byte[] bytes = Arrays.copyOf(payload, payload.length - 10);
                            map.put("payload", bytes);
                            jmsProducer.sendMessage(jmsConfig.topic(), map);
                        } else {
                            jmsProducer.sendMessage(jmsConfig.topic(), map);
                        }
                    }
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                }

                public void connectionLost(Throwable throwable) {
                }
            });

        } catch (MqttException me) {
            me.printStackTrace();
        }
        return true;
    }
}

