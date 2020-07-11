package com.beyond.rabbitmq.test.spring;

import com.beyond.rabbitmq.config.RabbitMQConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
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
 * @ClassName SendMsgTest
 * @date 2020/7/5 0:03
 * @company https://www.beyond.com/
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SendMsgTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Test
    public void testSendMsg(){
        /**
         * 发送消息
         * 参数一：交换机名称
         * 参数二：路由key
         * 参数三：发送的消息
         */
        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE, "item.insert", "商品新增，routing key 为item.insert");
        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE, "item.update", "商品修改，routing key 为item.update");
        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE, "item.delete", "商品删除，routing key 为item.delete");


    }
}
