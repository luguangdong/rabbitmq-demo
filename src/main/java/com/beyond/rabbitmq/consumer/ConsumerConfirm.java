package com.beyond.rabbitmq.consumer;


import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
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
public class ConsumerConfirm {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RedisTemplate redisTemplate;

    @RabbitListener(queues = "my_normal_queue")
    public void onMessage(Message message, Channel channel) throws Exception {
        //获取全局唯一id，比如通过消息队列来生成订单，那订单号就是唯一的
        String messageId = message.getMessageProperties().getMessageId();

        /**
         * 采用redis中的 setNXl 来实现重复消费问题
         */
        Boolean exists = (boolean) redisTemplate.execute((RedisCallback) action -> {
            return action.setNX(messageId.getBytes(), messageId.getBytes());
        });
        if (exists) {
            //业务处理
        } else {
            //该条消息已经消费过了,不能重复消费
        }


        /**
         * 采用redisTemplate中的 hasKey() 方法来实现重复消费问题
         */
        if (messageId != null && !redisTemplate.hasKey(messageId)) {
            try {
                //接受消息后处理业务开始
                System.out.println("consumer--:" + message.getMessageProperties() + ":" + new String(message.getBody()));
                //接受消息后处理业务结束

                //将这条消息的id放入redis中
                redisTemplate.opsForValue().set(messageId, "消息正常消费成功");

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
                logger.error("MQ消息处理异常，消息ID：{}，消息体:{}", message.getMessageProperties().getMessageId(), message.getBody(), e);
                try {
                    // TODO 保存消息到数据库
                    // 确认消息已经消费成功

                    //将这条消息的id放入redis中
                    redisTemplate.opsForValue().set(messageId, "消息正常消费成功,是出现异常后将该消息保存到数据库中");

                    //模拟异常
                    int i = 1 / 0;
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                } catch (Exception e1) {
                    message.getMessageProperties().getMessageId();
                    logger.error("保存异常MQ消息到数据库异常，放到死性队列，消息ID：{}", message.getMessageProperties().getMessageId());

                    //将这条消息的id放入redis中
                    redisTemplate.opsForValue().set(messageId, "消息正常消费成功,是出现异常后保存数据库时出现异常后将该消息发送到死信队列");

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

        } else {
            logger.error("消息ID={}已经消费过了", message.getMessageProperties().getMessageId());

            //该条消息已经消费过了,ACK回执给MQ服务器删除该消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

    }
}
