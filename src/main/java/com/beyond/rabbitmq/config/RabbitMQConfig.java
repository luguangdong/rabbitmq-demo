package com.beyond.rabbitmq.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: 队列模版配置
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName RabbitMQConfig
 * @date 2020/6/15 21:17
 * @company https://www.beyond.com/
 */
@Configuration
public class RabbitMQConfig {
    //交换机名称
    public static final String ITEM_TOPIC_EXCHANGE = "item_topic_exchange";
    //队列名称
    public static final String ITEM_QUEUE = "item_queue";

    //声明交换机
    @Bean("itemTopicExchange")
    public Exchange topicExchange(){
        return ExchangeBuilder.topicExchange(ITEM_TOPIC_EXCHANGE).durable(true).build();
    }

    //声明队列
    @Bean("itemQueue")
    public Queue itemQueue(){
        return QueueBuilder.durable(ITEM_QUEUE).build();
    }

    //绑定队列和交换机
    @Bean
    public Binding itemQueueExchange(@Qualifier("itemQueue") Queue queue,
                                     @Qualifier("itemTopicExchange") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("item.#").noargs();
    }



    /**
     * 死信队列 交换机标识符
     */
    private static final String DEAD_LETTER_QUEUE_KEY = "x-dead-letter-exchange";
    /**
     * 死信队列交换机绑定键标识符
     */
    private static final String DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    /**
     * 声明Direct交换机 支持持久化.
     *
     * @return the exchange
     */
    @Bean("directExchange")
    public Exchange directExchange() {
        return ExchangeBuilder.directExchange("test-direct").durable(true).build();
    }

    /**
     * 声明一个队列 支持持久化.
     *
     * @return the queue
     */
    @Bean("directQueue")
    public Queue directQueue() {
        return QueueBuilder.durable("test-direct").build();
    }

    /**
     * 通过绑定键 将指定队列绑定到一个指定的交换机 .
     *
     * @param queue    the queue
     * @param exchange the exchange
     * @return the binding
     */
    @Bean
    public Binding directBinding(@Qualifier("directQueue") Queue queue, @Qualifier("directExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("test-direct").noargs();
    }

    /**
     * 声明 fanout 交换机.
     *
     * @return the exchange
     */
    @Bean("fanoutExchange")
    public FanoutExchange fanoutExchange() {
        return (FanoutExchange) ExchangeBuilder.fanoutExchange("fanout-test").durable(true).build();
    }

    /**
     * Fanout queue A.
     *
     * @return the queue
     */
    @Bean("fanoutQueueA")
    public Queue fanoutQueueA() {
        return QueueBuilder.durable("Fanout-A").build();
    }

    /**
     * Fanout queue B .
     *
     * @return the queue
     */
    @Bean("fanoutQueueB")
    public Queue fanoutQueueB() {
        return QueueBuilder.durable("Fanout-B").build();
    }

    /**
     * 绑定队列A 到Fanout 交换机.
     *
     * @param queue          the queue
     * @param fanoutExchange the fanout exchange
     * @return the binding
     */
    @Bean
    public Binding bindingA(@Qualifier("fanoutQueueA") Queue queue, @Qualifier("fanoutExchange") FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }

    /**
     * 绑定队列B 到Fanout 交换机.
     *
     * @param queue          the queue
     * @param fanoutExchange the fanout exchange
     * @return the binding
     */
    @Bean
    public Binding bindingB(@Qualifier("fanoutQueueB") Queue queue, @Qualifier("fanoutExchange") FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }

    /**
     * 定义死信交换机
     * @return
     */

    @Bean("myDlxExchange")
    public Exchange myDlxExchange() {
        return ExchangeBuilder.directExchange("my_dlx_exchange").durable(true).build();
    }

    /**
     * 定义死信队列
     * @return
     */
    @Bean("myDlxQueue")
    public Queue myDlxQueue() {
        return QueueBuilder.durable("my_dlx_queue").build();
    }

    /**
     * 将死信队列根据指定的路由key绑定到死信交换机上
     * @return
     */

    @Bean
    public Binding myDlxQueueBinding() {
        return new Binding("my_dlx_queue", Binding.DestinationType.QUEUE, "my_dlx_exchange", "EX", null);

    }
    /**
     * 定义正常队列，设置死信交换机属性
     * @return
     */
    @Bean("myNormalQueue")
    public Queue myNormalQueue() {
        Map<String, Object> args = new HashMap<>(1);
        //x-dead-letter-exchange    声明  死信交换机
        args.put("x-dead-letter-exchange", "my_dlx_exchange");
        //x-dead-letter-routing-key    声明 死信路由键
        //args.put("x-dead-letter-routing-key", "EX");
        return QueueBuilder.durable("my_normal_queue").withArguments(args).build();
    }


    /**
     * 定义正常交换机
     * @return
     */
    @Bean("myNormalExchange")
    public Exchange myNormalExchange() {
        return ExchangeBuilder.directExchange("my_normal_exchange").durable(true).build();
    }

    /**
     * 将正常队列根据指定的路由key绑定到正常交换机上
     * @return
     */
    @Bean
    public Binding myNormalQueueBinding() {
        return new Binding("my_normal_queue", Binding.DestinationType.QUEUE, "my_normal_exchange", "EX", null);

    }



    /**
     * 死信队列跟交换机类型没有关系 不一定为directExchange  不影响该类型交换机的特性.
     *
     * @return the exchange
     */
    @Bean("deadLetterExchange")
    public Exchange deadLetterExchange() {
        return ExchangeBuilder.directExchange("DL_EXCHANGE").durable(true).build();
    }

    /**
     * 声明一个死信队列.
     * x-dead-letter-exchange   对应  死信交换机
     * x-dead-letter-routing-key  对应 死信队列
     *
     * @return the queue
     */
    @Bean("deadLetterQueue")
    public Queue deadLetterQueue() {
        Map<String, Object> args = new HashMap<>(2);
        //x-dead-letter-exchange    声明  死信交换机
        args.put(DEAD_LETTER_QUEUE_KEY, "DL_EXCHANGE");
        //x-dead-letter-routing-key    声明 死信路由键
        args.put(DEAD_LETTER_ROUTING_KEY, "KEY_R");
        return QueueBuilder.durable("DL_QUEUE").withArguments(args).build();
    }

    /**
     * 定义死信队列转发队列.
     *
     * @return the queue
     */
    @Bean("redirectQueue")
    public Queue redirectQueue() {
        return QueueBuilder.durable("REDIRECT_QUEUE").build();
    }

    /**
     * 死信路由通过 DL_KEY 绑定键绑定到死信队列上.
     *
     * @return the binding
     */
    @Bean
    public Binding deadLetterBinding() {
        return new Binding("DL_QUEUE", Binding.DestinationType.QUEUE, "DL_EXCHANGE", "DL_KEY", null);

    }

    /**
     * 死信路由通过 KEY_R 绑定键绑定到死信队列上.
     *
     * @return the binding
     */
    @Bean
    public Binding redirectBinding() {
        return new Binding("REDIRECT_QUEUE", Binding.DestinationType.QUEUE, "DL_EXCHANGE", "KEY_R", null);
    }


    /**
     * 配置启用rabbitmq事务
     * @param connectionFactory
     * @return
     */
   /* @Bean("rabbitTransactionManager")
    public RabbitTransactionManager rabbitTransactionManager(CachingConnectionFactory connectionFactory) {
        return new RabbitTransactionManager(connectionFactory);
    }*/

}
