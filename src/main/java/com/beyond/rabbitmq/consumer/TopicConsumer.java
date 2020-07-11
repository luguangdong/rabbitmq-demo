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
 * @ClassName TopicConsumer
 * @date 2020/6/19 16:00
 * @company https://www.beyond.com/
 */
//@Component
public class TopicConsumer {
    @Component
    @RabbitListener(queues="topic-a")
    public class TopicConsumerA{
        @RabbitHandler
        public void showMessage(String message){
            System.out.println("topic-a接收到消息："+message);
        }
    }
    @Component
    @RabbitListener(queues="topic-b")
    public class TopicConsumerB{
        @RabbitHandler
        public void showMessage(String message){
            System.out.println("topic-b接收到消息："+message);
        }
    }
    @Component
    @RabbitListener(queues="topic-c")
    public class TopicConsumerC{
        @RabbitHandler
        public void showMessage(String message){
            System.out.println("topic-c接收到消息："+message);
        }
    }

    @Component
    @RabbitListener(queues="topic-d")
    public class TopicConsumerD{
        @RabbitHandler
        public void showMessage(String message){
            System.out.println("topic-d接收到消息："+message);
        }
    }



}
