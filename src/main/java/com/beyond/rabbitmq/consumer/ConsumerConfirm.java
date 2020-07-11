package com.beyond.rabbitmq.consumer;

import com.beyond.rabbitmq.callback.CorrelationData;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName ConsumerConfirm
 * @date 2020/6/27 19:31
 * @company https://www.beyond.com/
 */
@Component
@RabbitListener(queues = "my_normal_queue")
public class ConsumerConfirm {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @RabbitListener(queues = "my_normal_queue")
    public void onMessage(Message message, Channel channel, CorrelationData correlationData) throws Exception {
        try {
            //接受消息后处理业务开始
            System.out.println("consumer--:" + message.getMessageProperties() + ":" + new String(message.getBody()));
            //接受消息后处理业务结束

            //模拟异常
            int i = 1 / 0;

            /**
             *  处理业务结束后，ACK回执给MQ服务器删除该消息。
             *  multiple:false 是指ACK回执给MQ服务器删除当前处理的一条消息
             *  multiple:true  是指ACK回执给MQ服务器删除deliveryTag小于等于传入值的所有消息
             */
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {

            /**
             *  出现异常后，ACK回执给MQ服务器是否删除该消息。
             *  multiple:false 是指ACK回执给MQ服务器删除当前处理的一条消息
             *  multiple:true  是指ACK回执给MQ服务器删除deliveryTag小于等于传入值的所有消息
             *  requeue:false 不会重新入队列，ACK回执给MQ服务器删除消息
             *  requeue:true 会重新入队列，会添加在队列的末端
             */
            logger.error("MQ消息处理异常，消息ID：{}，消息体:{}", message.getMessageProperties().getCorrelationId(), message, e);
            try {
                // TODO 保存消息到数据库
                // 确认消息已经消费成功

                //模拟异常
                int i = 1 / 0;
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception e1) {
                logger.error("保存异常MQ消息到数据库异常，放到死性队列，消息ID：{}", message.getMessageProperties().getCorrelationId());
                /**
                 * 此处 requeue属性 可以为 false 也可以为true
                 * 当requeue: true时,消费端如果出现异常后则会将这条消息 一直 给MQ服务器发送,消息会重新入队列,会添加在队列的末端。
                 * (使用true时,此处出现的异常次数少,比如网络波动,如果这个异常每次都出现的话,那么就会造成 死循环 )。
                 *
                 * 当requeue: false时,消费端如果出现异常后则会将这条消息会发送到死信队列中
                 */
                //确认消息将消息放到死信队列
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            }

        }


    }
}
