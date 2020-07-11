package com.beyond.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName FanoutConsumer
 * @date 2020/6/19 16:00
 * @company https://www.beyond.com/
 */
//@Component
public class FanoutConsumer {
    //@Component
    @RabbitListener(queues="Fanout-A")
    public class FanoutConsumerA{
        @RabbitHandler
        public void showMessage(String message){
            System.out.println("Fanout-A接收到消息："+message);
        }
    }
    //@Component
    @RabbitListener(queues="Fanout-B")
    public class FanoutConsumerB{
        @RabbitHandler
        public void showMessage(String message){
            System.out.println("Fanout-B接收到消息："+message);
        }
    }
}
