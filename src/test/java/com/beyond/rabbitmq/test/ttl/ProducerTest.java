package com.beyond.rabbitmq.test.ttl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName ProducerTest
 * @date 2020/7/5 14:05
 * @company https://www.beyond.com/
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProducerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 过期队列消息
     * 投递到该队列的消息如果没有消费都将在6秒之后被删除
     */
    @Test
    public void ttlQueueTest(){
        //路由键与队列同名
        rabbitTemplate.convertAndSend("my_ttl_queue", "发送到过期队列my_ttl_queue，6秒内不消费则不能再被消费。");
    }

    /**
     * 过期消息
     * 该消息投递任何交换机或队列中的时候；如果到了过期时间则将从该队列中删除
     */
    @Test
    public void ttlMessageTest(){
        MessageProperties messageProperties = new MessageProperties();
        //设置消息的过期时间，5秒
        messageProperties.setExpiration("5000");

        Message message = new Message("测试过期消息，5秒钟过期".getBytes(), messageProperties);
        //路由键与队列同名
        rabbitTemplate.convertAndSend("my_ttl_queue", message);
    }


    /**
     * 过期消息投递到死信队列
     * 投递到一个正常的队列，但是该队列有设置过期时间，到过期时间之后消息会被投递到死信交换机（队列）
     */
    @Test
    public void dlxTTLMessageTest(){
        rabbitTemplate.convertAndSend("my_normal_exchange", "my_ttl_dlx", "测试过期消息；6秒过期后会被投递到死信交换机");
    }

    /**
     * 超过队列长度消息投递到死信队列
     * 投递到一个正常的队列，但是该队列有设置最大消息数，到最大消息数之后队列中最早的消息会被投递到死信交换机（队列）
     */
    @Test
    public void dlxMaxMessageTest(){
        rabbitTemplate.convertAndSend("my_normal_exchange", "my_max_dlx",
                "队列my_max_dlx_queue的最大长度为2；消息超过后会被投递到死信交换机；这是第1个消息");
        rabbitTemplate.convertAndSend("my_normal_exchange", "my_max_dlx",
                "队列my_max_dlx_queue的最大长度为2；消息超过后会被投递到死信交换机；这是第2个消息");
        rabbitTemplate.convertAndSend("my_normal_exchange", "my_max_dlx",
                "队列my_max_dlx_queue的最大长度为2；消息超过后会被投递到死信交换机；这是第3个消息");
    }


}
