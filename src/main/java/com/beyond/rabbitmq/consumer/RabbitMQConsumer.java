package com.beyond.rabbitmq.consumer;


import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName RabbitMQConsumer
 * @date 2020/6/15 23:18
 * @company https://www.beyond.com/
 */
//@Component
public class RabbitMQConsumer {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQConsumer.class);

    /**
     * DIRECT模式.
     *
     * @param message the message
     * @param channel the channel
     * @throws IOException the io exception  这里异常需要处理
     */
    @RabbitListener(queues = {"test-direct"})
    public void message(Message message, Channel channel) throws IOException {
        //消息的标识，false只确认当前一个消息收到，true确认所有consumer获得的消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);

        //ack返回false，并重新回到队列
        //channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);

        //拒绝消息
        //channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);

        System.out.println("test-direct " + new String(message.getBody()));
        log.debug("test-direct " + new String(message.getBody()));
    }

    /**
     * FANOUT广播队列监听一.
     *
     * @param message the message
     * @param channel the channel
     * @throws IOException the io exception  这里异常需要处理
     */
    @RabbitListener(queues = {"Fanout-A"})
    public void on(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        System.out.println("Fanout-A " + new String(message.getBody()));
        log.debug("Fanout-A " + new String(message.getBody()));
    }

    /**
     * FANOUT广播队列监听二.
     *
     * @param message the message
     * @param channel the channel
     * @throws IOException the io exception   这里异常需要处理
     */
    @RabbitListener(queues = {"Fanout-B"})
    public void t(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        System.out.println("Fanout-B " + new String(message.getBody()));
        log.debug("Fanout-B " + new String(message.getBody()));
    }


    /**
     * 监听替补队列 来验证死信.
     *
     * @param message the message
     * @param channel the channel
     * @throws IOException the io exception  这里异常需要处理
     */
    @RabbitListener(queues = {"REDIRECT_QUEUE"})
    public void redirect(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        System.out.println("dead message  10s 后 消费消息: " + new String(message.getBody()));
        log.debug("dead message  10s 后 消费消息 {}", new String(message.getBody()));
    }


}
