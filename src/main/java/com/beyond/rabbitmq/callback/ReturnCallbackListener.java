package com.beyond.rabbitmq.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * <p>
 * Description: 通过实现 ReturnCallback 接口，启动消息失败返回
 *  用于实现消息发送到RabbitMQ交换器后，但无相应队列与交换器绑定时的回调。
 *  在脑裂的情况下会出现这种情况
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName ReturnCallbackListener
 * @date 2020/6/23 19:54
 * @company https://www.beyond.com/
 */
@Component("returnCallbackListener")
public class ReturnCallbackListener implements RabbitTemplate.ReturnCallback{
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //初始化spring容器时,给rabbitTemplate加载ReturnCallback配置
    @PostConstruct
    public void init(){
        rabbitTemplate.setReturnCallback(this);
        //注意：同时需配置mandatory="true"，否则消息则丢失
        rabbitTemplate.setMandatory(true);
        //给Channel设置为事物
        //rabbitTemplate.setChannelTransacted(true);
    }

    /**
     * 当消息从交换机到队列失败时，该方法被调用。（若成功，则不调用）
     * 需要注意的是：该方法调用后，MsgSendConfirmCallBack中的confirm方法也会被调用，且ack = true
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        String correlationId = message.getMessageProperties().getCorrelationId();
        logger.debug("消息：{} 发送失败, 应答码：{} 原因：{} 交换机: {}  路由键: {}", correlationId, replyCode, replyText, exchange, routingKey);
        // TODO 保存消息到数据库

    }
}
