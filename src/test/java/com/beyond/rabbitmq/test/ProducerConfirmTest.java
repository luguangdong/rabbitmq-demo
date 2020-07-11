package com.beyond.rabbitmq.test;


import com.beyond.rabbitmq.callback.CorrelationData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpConnectException;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.UUID;

/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName ProducerConfirmTest
 * @date 2020/6/27 16:01
 * @company https://www.beyond.com/
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProducerConfirmTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testProducerConfirm() {
        String message = "Hello RabbitMQ:";
        //使用继承扩展的CorrelationData 、id消息流水号
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        correlationData.setExchange("test-direct");
        correlationData.setQueue("test-direct");
        correlationData.setRoutingKey("test-direct");
        correlationData.setMessage(message);

        try {
            rabbitTemplate.convertAndSend("test-direct", "test-direct", message, correlationData);
        } catch (AmqpConnectException e) {
            System.out.println("保存信息编号：" + correlationData);
            // TODO 保存消息到数据库
        }
    }


    @Test
    public void testProducerConfirm2() {
        /**
         * 点对点模式,生产者往同一个队列中发送,消费者从同一个队列中消费
         */
        rabbitTemplate.convertAndSend("my_normal_exchange","my_ttl_dlx","测试过期消息；6秒过期后会被投递到死信交换机");
    }
}
