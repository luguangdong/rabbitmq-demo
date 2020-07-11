package com.beyond.rabbitmq.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * <p>
 * Description: 通过实现 ConfirmCallback 接口，消息发送到 Broker 后触发回调，确认消息是否到达 Broker 服务器，也就是只确认是否正确到达 Exchange 中
 *
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName ConfirmCallbackListener
 * @date 2020/6/23 17:34
 * @company https://www.beyond.com/
 */
@Component("confirmCallbackListener")
public class ConfirmCallbackListener implements RabbitTemplate.ConfirmCallback {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //初始化spring容器时,给rabbitTemplate加载ConfirmCallback配置
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
    }


    /**
     * 当消息发送到交换机（exchange）时，该方法被调用.
     * 1.如果消息没有到exchange,则 ack=false
     * 2.如果消息到达exchange,则 ack=true
     * <p>
     * 注意：在confirmCallback中是没有原message的，所以无法在这个函数中调用重发，confirmCallback只有一个通知的作用
     *
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            if (correlationData instanceof com.beyond.rabbitmq.callback.CorrelationData) {
                logger.debug("消息发送到exchange成功,消息ID：{},correlationData: {}", correlationData.getId(), correlationData);
            } else {
                //适配rabbitmq原生的CorrelationData对象
                logger.debug("消息发送到exchange成功");
            }
        } else {
            // 根据业务逻辑实现消息补偿机制
            if (correlationData instanceof com.beyond.rabbitmq.callback.CorrelationData) {
                com.beyond.rabbitmq.callback.CorrelationData messageCorrelationData = (com.beyond.rabbitmq.callback.CorrelationData) correlationData;
                String exchange = messageCorrelationData.getExchange();
                String routingKey = messageCorrelationData.getRoutingKey();
                Object message = messageCorrelationData.getMessage();
                int retryCount = messageCorrelationData.getRetryCount();
                //当重试次数大于3次后，将不再发送
                if (((com.beyond.rabbitmq.callback.CorrelationData) correlationData).getRetryCount() <= 3) {
                    //重试次数+1
                    ((com.beyond.rabbitmq.callback.CorrelationData) correlationData).setRetryCount(retryCount + 1);
                    convertAndSend(exchange, routingKey, message, correlationData);
                } else {
                    //消息重试发送失败,将消息放到数据库等待补发
                    logger.warn("MQ消息重发失败，消息入库，消息ID：{}，消息体:{}", correlationData.getId(), ((com.beyond.rabbitmq.callback.CorrelationData) correlationData).getMessage());
                    // TODO 保存消息到数据库
                }
            } else {
                //适配rabbitmq原生的CorrelationData对象
                logger.debug("消息发送到exchange失败,原因: {}", cause);
            }
        }
    }

    /**
     * 发送消息
     *
     * @param exchange        交换机名称
     * @param routingKey      路由key
     * @param message         消息内容
     * @param correlationData 消息相关数据（消息ID）
     * @throws AmqpException
     */
    private void convertAndSend(String exchange, String routingKey, final Object message, CorrelationData correlationData) throws AmqpException {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
        } catch (Exception e) {
            logger.error("MQ消息发送异常，消息ID：{}，消息体:{}, exchangeName:{}, routingKey:{}",
                    correlationData.getId(), message, exchange, routingKey, e);
            // TODO 保存消息到数据库
        }
    }
}
