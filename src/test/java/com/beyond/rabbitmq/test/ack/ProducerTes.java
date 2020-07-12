package com.beyond.rabbitmq.test.ack;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName ProducerTes
 * @date 2020/7/5 17:13
 * @company https://www.beyond.com/
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProducerTes {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Test
    public void queueTest(){
        //路由键与队列同名
        rabbitTemplate.convertAndSend("simple_queue", "只发队列simple_queue的消息。");
    }

    @Test
    public void testFailQueueTest() throws InterruptedException {
        //exchange 正确,queue 错误 ,confirm被回调, ack=true; return被回调 replyText:NO_ROUTE
        rabbitTemplate.convertAndSend("test-direct", "3333", "测试消息发送失败进行确认应答。");
    }


    @Test
    @Transactional
    public void queueTest2() throws InterruptedException {
        //路由键与队列同名
        rabbitTemplate.convertAndSend("simple_queue", "只发队列simple_queue的消息--01。");
        System.out.println("----------------dosoming:可以是数据库的操作，也可以是其他业务类型的操作---------------");
        //模拟业务处理失败
        //System.out.println(1/0);
        rabbitTemplate.convertAndSend("simple_queue", "只发队列simple_queue的消息--02。");
    }

    @Test
    public void dlxMessageTest() throws InterruptedException {
        //路由键与队列同名
        rabbitTemplate.convertAndSend("my_normal_exchange", "EX","只发队列simple_queue的消息--01。");
    }


    @Test
    public void repeatMessageTest() throws InterruptedException, UnsupportedEncodingException {
        byte[] body = "使用redis解决rabbbitmq重复消费问题".getBytes("UTF-8");
        MessageProperties messageProperties = new MessageProperties();
        //全局唯一id，比如通过消息队列来生成订单，那订单号就是唯一的
        messageProperties.setMessageId("123");
        Message message = new Message(body,messageProperties);
        rabbitTemplate.convertAndSend("my_normal_exchange", "EX",message);
    }
}
