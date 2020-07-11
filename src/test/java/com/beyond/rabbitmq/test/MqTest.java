package com.beyond.rabbitmq.test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName MqTest
 * @date 2020/6/15 21:14
 * @company https://www.beyond.com/
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MqTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testDirectSend(){
        /**
         * 点对点模式,生产者往同一个队列中发送,消费者从同一个队列中消费
         */
        rabbitTemplate.convertAndSend("test-direct","给direct模式的队列发送消息");
    }

    @Test
    public void testFanoutSend(){
        /**
         * 一对多模式,生产者往一个交换机中发送消息,交换机将消息发送到它绑定的所有队列中
         */
        rabbitTemplate.convertAndSend("fanout-test","","给fanout模式下发送消息");
    }

    @Test
    public void testTopicSend(){
        /**
         * 匹配模式,生产者根据routingKey将消息发送到交换机,交换机routingKey将消息发送到匹配routingKey的队列中
         */
        rabbitTemplate.convertAndSend("topic-test","business.xxx.yyy","给topic模式下发送消息");
    }

    @Test
    public void testDirectSendAck(){
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("test-direct", "test-direct", "给direct模式的队列发送ACK消息", correlationData);
    }


    @Test
    public void testFanoutSendAck(){
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("fanout-test", "fanout-test", "给fanout模式的队列发送ACK消息", correlationData);
    }


    @Test
    public void testDLQ(){
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        //声明消息处理器  这个对消息进行处理  可以设置一些参数   对消息进行一些定制化处理   我们这里  来设置消息的编码  以及消息的过期时间  因为在.net 以及其他版本过期时间不一致   这里的时间毫秒值 为字符串
        MessagePostProcessor messagePostProcessor = message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            //设置编码
            messageProperties.setContentEncoding("utf-8");
            //设置过期时间10*1000毫秒
            messageProperties.setExpiration("10000");
            return message;
        };
        //向DL_QUEUE 发送消息  10*1000毫秒后过期 形成死信
        rabbitTemplate.convertAndSend("DL_EXCHANGE", "DL_KEY", "给死信队列中发送消息", messagePostProcessor, correlationData);
    }


    private static String NORMAL_EXCHANGE = "exchange@normal";
    private static String RETRY_EXCHANGE = "exchange@retry";
    private static String FILED_EXCHANGE = "exchange@filed";

    @Test
    public void testRetryPublisher(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost("192.168.137.109");
        factory.setPort(AMQP.PROTOCOL.PORT);
        factory.setUsername("rabbit");
        factory.setPassword("123456");

        Connection connection=null;
        Channel channel=null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            //声明正常的转发器
            channel.exchangeDeclare(NORMAL_EXCHANGE,"topic");
            //声明重试的转发器
            channel.exchangeDeclare(RETRY_EXCHANGE,"topic");
            //声明失败的转发器
            channel.exchangeDeclare(FILED_EXCHANGE,"topic");

            //发送5条消息到正常的转发器，路由密匙为normal
            for(int i=0;i<5;i++){
                String message = "retry..."+i;
                channel.basicPublish(NORMAL_EXCHANGE,"normal", null,message.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }


}
