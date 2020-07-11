package com.beyond.rabbitmq.test.simple;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * Description: 通过原生rabbitmq.client来实现消息发送
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName ProducerTest
 * @date 2020/7/4 15:59
 * @company https://www.beyond.com/
 */
public class ProducerTest {
    public static final String QUEUE_NAME = "simple_queue";

    private ConnectionFactory connectionFactory = null;
    private Connection connection = null;
    private Channel channel = null;

    @Before
    public void before() throws IOException, TimeoutException {
        //创建连接工厂
        connectionFactory = new ConnectionFactory();
        //主机地址;默认为 localhost
        connectionFactory.setHost("192.168.137.109");
        //连接端口;默认为 5672
        connectionFactory.setPort(5672);
        //虚拟主机名称;默认为 /
        connectionFactory.setVirtualHost("/beyond");
        //连接用户名；默认为guest
        connectionFactory.setUsername("rabbit");
        //连接密码；默认为guest
        connectionFactory.setPassword("123456");

        //创建连接
        connection = connectionFactory.newConnection();

        // 创建频道
        channel = connection.createChannel();

    }

    /**
     * 通过原生rabbitmq.client来实现消息发送
     *
     * @throws IOException
     */
    @Test
    public void testProducer() throws IOException, TimeoutException {
        // 声明（创建）队列
        /**
         * 参数1：队列名称
         * 参数2：是否定义持久化队列
         * 参数3：是否独占本次连接
         * 参数4：是否在不使用的时候自动删除队列
         * 参数5：队列其它参数
         */
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        // 要发送的信息
        String message = "你好；小兔子！";
        /**
         * 参数1：交换机名称，如果没有指定则使用默认Default Exchage
         * 参数2：路由key,简单模式可以传递队列名称
         * 参数3：消息其它属性
         * 参数4：消息内容
         */
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println("已发送消息：" + message);


    }


    @After
    public void after() throws IOException, TimeoutException {
        // 关闭资源
        channel.close();
        connection.close();
    }

}
