package com.beyond.rabbitmq.test;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName TestByCustom
 * @date 2020/6/22 14:11
 * @company https://www.beyond.com/
 */

public class TestByCustom {
    private ConnectionFactory factory = null;
    private Connection connection = null;
    private Channel channel = null;

    @Before
    public void before() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost("192.168.137.109");
        factory.setPort(AMQP.PROTOCOL.PORT);
        factory.setUsername("rabbit");
        factory.setPassword("123456");

        connection = factory.newConnection();
        channel = connection.createChannel();

    }

    /**
     * 通过AMQP事务机制实现，这也是AMQP协议层面提供的解决方案
     *
     * @throws IOException
     */
    @Test
    public void testBasicPublishByTX() throws IOException {
        String EXCHANGE_NAME = "test-direct";
        String QUEUE_NAME = "test-direct";
        String ROUTING_KEY = "test-direct";
        /**
         * exchange :交换器的名称
         * type : 交换器的类型，常见的有direct,fanout,topic等
         * durable :设置是否持久化。durable设置为true时表示持久化，反之非持久化.持久化可以将交换器存入磁盘，在服务器重启的时候不会丢失相关信息。
         * autoDelete：设置是否自动删除。autoDelete设置为true时，则表示自动删除。
         * internal：设置是否内置的。如果设置为true，则表示是内置的交换器，客户端程序无法直接发送消息到这个交换器中，只能通过交换器路由到交换器这种方式。
         * arguments:其它一些结构化的参数，比如：alternate-exchange

         */
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true, false, false, null);
        /**
         * queue:队列的名称
         * durable：是否持久化
         * exclusive: 设置是否排他。为 true 则设置队列为排他的。如果一个队列被声明为排他队列，该队列仅对首次声明它的连接可见，并在连接断开时自动删除。
         * autoDelete: 设置是否自动删除。为 true 则设置队列为自动删除。自动删除的前提是: 至少有一个消费者连接到这个队列，之后所有与这个队列连接的消费者都断开时，才会 自动删除。
         * 不能把这个参数错误地理解为: "当连接到此队列的所有客户端断开时，这个队列自动删除"，因为生产者客户端创建这个队列，或者没有消费者客户端与这个队 列连接时，都不会自动删除这个队列。
         * arguments:其它一些结构化的参数，比如：alternate-exchange
         */
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY, null);
        String message = "Hello RabbitMQ:";
        try {
            channel.txSelect();
            /**
             * exchange：名称
             * routingKey：路由键
             * mandatory：为true时如果exchange根据自身类型和消息routeKey无法找到一个符合条件的queue，那么会调用basic.return方法将消息返还给生产者。
             * 为false时出现上述情形broker会直接将消息扔掉
             * immediate：为true时如果exchange在将消息route到queue(s)时发现对应的queue上没有消费者，那么这条消息不会放入队列中。
             * 当与消息routeKey关联的所有queue(一个或多个)都没有消费者时，该消息会通过basic.return方法返还给生产者。
             */
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, false, false, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
            int result = 1 / 0;
            channel.txCommit();
        } catch (Exception e) {
            e.printStackTrace();
            channel.txRollback();
        }
    }


    /**
     * 通过将channel设置成confirm模式来实现
     * 普通confirm模式：每发送一条消息后，调用waitForConfirms()方法，等待服务器端confirm。实际上是一种串行confirm了。
     *
     * @throws IOException
     */
    @Test
    public void testBasicPublishByConfirmCommon() throws IOException, InterruptedException {
        String EXCHANGE_NAME = "test-direct";
        String QUEUE_NAME = "test-direct";
        String ROUTING_KEY = "test-direct";

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true, false, false, null);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY, null);
        channel.confirmSelect();
        String message = "Hello RabbitMQ:";
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, false, false, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
        if (channel.waitForConfirms()) {
            System.out.println("send message succeed.");
        }else {
            System.out.println("send message failed.");
            // do something
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, false, false, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
        }

    }


    /**
     * 通过将channel设置成confirm模式来实现
     * 批量confirm模式：每发送一批消息后，调用waitForConfirms()方法，等待服务器端confirm。
     *
     * @throws IOException
     */
    @Test
    public void testBasicPublishByConfirmBatch() throws IOException, InterruptedException {
        String EXCHANGE_NAME = "test-direct";
        String QUEUE_NAME = "test-direct";
        String ROUTING_KEY = "test-direct";

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true, false, false, null);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY, null);
        channel.confirmSelect();
        String message = "Hello RabbitMQ:";
        for (int i = 0; i < 5; i++) {
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, false, false, MessageProperties.PERSISTENT_TEXT_PLAIN, (message + i).getBytes(StandardCharsets.UTF_8));
        }
        if (!channel.waitForConfirms()) {
            System.out.println("send message failed.");
        }

    }


    /**
     * 通过将channel设置成confirm模式来实现
     * 异步confirm模式：提供一个回调方法，服务端confirm了一条或者多条消息后Client端会回调这个方法。
     *
     * @throws IOException
     */
    @Test
    public void testBasicPublishByConfirmAsync() throws IOException, InterruptedException {
        String EXCHANGE_NAME = "test-direct";
        String QUEUE_NAME = "test-direct";
        String ROUTING_KEY = "test-direct";

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true, false, false, null);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY, null);

        SortedSet<Long> confirmSet = Collections.synchronizedSortedSet(new TreeSet<Long>());
        channel.confirmSelect();
        String message = "Hello RabbitMQ:";
        //添加监听器
        channel.addConfirmListener(new ConfirmListener() {
            //没问题的handleAck
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                if (multiple) {
                    //public SortedSet<E> headset(E toElement) 返回从开始到指定元素的集合
                    confirmSet.headSet(deliveryTag + 1).clear();
                } else {
                    confirmSet.remove(deliveryTag);
                }
            }
            //回调/重发重试
            @SneakyThrows
            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("Nack, SeqNo: " + deliveryTag + ", multiple: " + multiple);
                if (multiple) {
                    confirmSet.headSet(deliveryTag + 1).clear();
                } else {
                    confirmSet.remove(deliveryTag);
                }
                //这里添加处理消息重发的场景,可以1s之后再发 10s之后再发
                Thread.sleep(1000);
                channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, false, false, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
            }
        });


        //模拟插入数据
        while (true) {
            long nextSeqNo = channel.getNextPublishSeqNo();
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, false, false, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
            confirmSet.add(nextSeqNo);
        }
    }


    @After
    public void after() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }


    @Test
    public void consumer() throws IOException, InterruptedException {
        String EXCHANGE_NAME = "test-direct";
        String QUEUE_NAME = "test-direct";
        String ROUTING_KEY = "test-direct";
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(message);

                //自动应答
                //channel.basicAck(envelope.getDeliveryTag(), true);
                //手动应答
                channel.basicAck(envelope.getDeliveryTag(), false);

                /*if (message.contains(":3")){
                    // requeue：重新入队列，false：直接丢弃，相当于告诉队列可以直接删除掉
                    channel.basicReject(envelope.getDeliveryTag(), true);
                    channel.basicRecover(true);
                } else {
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }*/
            }
        };


        //自动应答
        //channel.basicConsume(QUEUE_NAME, true, consumer);

        //手动应答
        channel.basicConsume(QUEUE_NAME, false, consumer);
        //Thread.sleep(100000);
    }


    @Test
    public void testSortedSet(){
        SortedSet<Long> confirmSet = Collections.synchronizedSortedSet(new TreeSet<Long>());
        confirmSet.add(1L);
        confirmSet.add(2L);
        confirmSet.add(3L);
        confirmSet.add(4L);
        SortedSet<Long> longSortedSet = confirmSet.headSet(3L + 1L);
        System.out.println(longSortedSet);
        longSortedSet.clear();
        System.out.println(longSortedSet);
        System.out.println(confirmSet);
    }


}
