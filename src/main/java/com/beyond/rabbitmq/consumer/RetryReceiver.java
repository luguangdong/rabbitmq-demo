package com.beyond.rabbitmq.consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName RetryReceiver
 * @date 2020/6/22 23:20
 * @company https://www.beyond.com/
 */
//@Component
@RabbitListener(queues="test-direct")
public class RetryReceiver implements ChannelAwareMessageListener {
    //转发器
    private static String NORMAL_EXCHANGE = "exchange@normal";
    private static String RETRY_EXCHANGE = "exchange@retry";
    private static String FILED_EXCHANGE = "exchange@filed";
    //队列
    private static String NORMAL_QUEUE = "queue@normal";
    private static String RETRY_QUEUE = "queue@retry";
    private static String FILED_QUEUE = "queue@filed";

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            System.out.println("Fanout-A " + new String(message.getBody()));
            //声明正常队列
            channel.queueDeclare(NORMAL_QUEUE, true, false, false, null);
            //声明重试队列,重试队列比较特殊，需要设置两个参数
            Map<String, Object> arg = new HashMap<String, Object>();
            //参数1：将消息发送到哪一个转发器
            arg.put("x-dead-letter-exchange", NORMAL_EXCHANGE);
            //参数2：多长时间后发送
            arg.put("x-message-ttl", 10000);
            channel.queueDeclare(RETRY_QUEUE, true, false, false, arg);

            //声明失败队列
            channel.queueDeclare(FILED_QUEUE, true, false, false, null);

            //将队列绑定转发器和路由密匙
            channel.queueBind(NORMAL_QUEUE, NORMAL_EXCHANGE, "normal");
            channel.queueBind(RETRY_QUEUE, RETRY_EXCHANGE, "normal");
            channel.queueBind(FILED_QUEUE, FILED_EXCHANGE, "normal");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //此处处理消息
                    try {
                        String message = new String(body, StandardCharsets.UTF_8);
                        System.out.println("消费者接受到的消息：" + message);
                        //模拟处理消息是产生异常
                        int i = 1 / 0;
                    } catch (Exception e) {
                        try {
                            //延迟5s
                            Thread.sleep(5000);
                            //判断失败次数
                            long retryCount = getRetryCount(properties);
                            if (retryCount >= 3) {
                                //如果失败超过三次，则发送到失败队列
                                channel.basicPublish(FILED_EXCHANGE, envelope.getRoutingKey(), MessageProperties.PERSISTENT_BASIC, body);
                                System.out.println("消息失败了...");
                            } else {
                                //发送到重试队列,00
                                channel.basicPublish(RETRY_EXCHANGE, envelope.getRoutingKey(), properties, body);
                                System.out.println("消息重试中...");
                            }
                        } catch (Exception e1) {
                            e.printStackTrace();
                        }

                    }
                }
            };
            //消费消息
            channel.basicConsume(NORMAL_QUEUE, true, consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 获取消息失败次数
     *
     * @param properties
     * @return
     */
    public long getRetryCount(AMQP.BasicProperties properties) {
        long retryCount = 0L;
        Map<String, Object> header = properties.getHeaders();
        if (header != null && header.containsKey("x-death")) {
            List<Map<String, Object>> deaths = (List<Map<String, Object>>) header.get("x-death");
            if (deaths.size() > 0) {
                Map<String, Object> death = deaths.get(0);
                retryCount = (Long) death.get("count");
            }
        }
        return retryCount;
    }



}
