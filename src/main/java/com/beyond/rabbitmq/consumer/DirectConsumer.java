package com.beyond.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: 直接模式消费者
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName DirectConsumer
 * @date 2020/6/19 15:48
 * @company https://www.beyond.com/
 */
//@Component
@RabbitListener(queues="test-direct" )
public class DirectConsumer {
    @RabbitHandler
    public void showMessage(String message){
        System.out.println("test-direct接收到消息："+message);
    }
}
