package com.sansi.data.service;

import com.sansi.data.config.JMSConfig;
import com.sansi.data.config.MqttConfiguration;
import com.sansi.data.utils.DataFormat;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * MQTT消费端
 */
@Configuration
public class MqttConsumer {
    @Resource
    private JMSConfig jmsConfig;

    @Resource
    private JMSProducer jmsProducer;

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
            System.out.println("Connecting to broker:" + broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");

            String[] topics = mqttProperties.getTopics().split(",");
            sampleClient.subscribe(topics);
            sampleClient.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    byte[] payload = message.getPayload();
                    jmsProducer.sendMessage(jmsConfig.topic(), payload);
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                }

                public void connectionLost(Throwable throwable) {
                }
            });

        } catch (MqttException me) {
            System.out.println("reason" + me.getReasonCode());
            System.out.println("msg" + me.getMessage());
            System.out.println("loc" + me.getLocalizedMessage());
            System.out.println("cause" + me.getCause());
            System.out.println("excep" + me);
            me.printStackTrace();
        }
        return true;
    }
}

