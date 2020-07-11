package com.beyond.rabbitmq.test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sun.deploy.panel.ITreeNode;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName MqTest
 * @date 2020/6/15 21:14
 * @company https://www.beyond.com/
 */

public class MqTest2 {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RabbitTemplate rabbitTemplate;





    @Test
    public void testFanoutSendAckByConf(){
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("fanout-test", "fanout-test", "给fanout模式的队列发送ACK消息", correlationData);
    }


//    @Test
//    public void testFanoutSendAckByCustom(){
//        try {
//            // 针对网络原因导致连接断开，利用retryTemplate重连10次
//            RetryTemplate retryTemplate = new RetryTemplate();
//            retryTemplate.setRetryPolicy(new SimpleRetryPolicy(10));
//            rabbitTemplate.setRetryTemplate(retryTemplate);
//
//            // 确认是否发到交换机，若没有则存缓存，用另外的线程重发，直接在里面用rabbitTemplate重发会抛出循环依赖错误
//            rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
//                if (ack) {
//                    logger.debug("消息发送到exchange成功,id: {}", correlationData.getId());
//                }else {
//                    logger.debug("消息发送到exchange失败,原因: {}", cause);
//                }
//            });
//            // 确认是否发到队列，若没有则存缓存，然后检查exchange, routingKey配置，之后重发
//            rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
//                String correlationId = message.getMessageProperties().getCorrelationId();
//                logger.debug("消息：{} 发送失败, 应答码：{} 原因：{} 交换机: {}  路由键: {}", correlationId, replyCode, replyText, exchange, routingKey);
//            });
//            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
//            rabbitTemplate.convertAndSend("fanout-test", "fanout-test", "给fanout模式的队列发送ACK消息", correlationData);
//        } catch (AmqpException e) {
//            e.printStackTrace();
//        }
//
//    }


    @Test
    public void test1(){
        BigDecimal amountReceivable = new BigDecimal(0.0);
        System.out.println(amountReceivable);
        amountReceivable = amountReceivable.add(new BigDecimal(8.8)).add(new BigDecimal(10));
        System.out.println(amountReceivable);
        String discount = amountReceivable.divide(new BigDecimal(20), 3, BigDecimal.ROUND_HALF_DOWN).multiply(BigDecimal.TEN).toString();
        System.out.println(discount);
    }


    public void test2(){


        StringBuffer buffer = new StringBuffer("SHZL");

        //buffer.append(getDate8_Digit());

    }

    @Test
    public  void getDate8_Digit(){

        /** 时间格式 yyMMdd.*/
         String DATE_YYMMDD6DIGIT = "yyMMdd";

        /** 时间格式 yyyyMMdd.*/
       String DATE_YYMMDD8DIGIT = "YYYYMMdd";


        Date date = new Date();
        SimpleDateFormat sdf =new SimpleDateFormat(DATE_YYMMDD6DIGIT);
        String str = sdf.format(date);
        System.out.println(str);

    }

    @Test
    public  void getDa(){
        byte[] bytes = Base64.decodeBase64("5Y+q5Y+R6Zif5YiXc2ltcGxlX3F1ZXVl55qE5raI5oGv44CC");
        String s = new String(bytes);
        System.out.println(s);
    }




}
