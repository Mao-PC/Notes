package com.dongnaoedu.rabbitmq.spring.ps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.handler.annotation.Payload;

@Configuration
@EnableAutoConfiguration
@Import(AppConfiguration.class)
@RabbitListener(queues = "#{queue.name}")
public class ConsumerApp2 {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerApp2.class);

    @RabbitHandler
    public void receive(@Payload String msg) {
        logger.info("收到消息：" + msg);
    }

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ConsumerApp2.class);
        springApplication.setAdditionalProfiles("consumer");
        springApplication.run(args);
    }
}
