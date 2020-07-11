package com.beyond.rabbitmq.callback;

import lombok.Data;
/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName CorrelationData
 * @date 2020/6/27 15:53
 * @company https://www.beyond.com/
 */
@Data
public class CorrelationData extends org.springframework.amqp.rabbit.connection.CorrelationData {
    //消息体
    private volatile Object message;
    //交换机
    private String exchange;
    //队列名称
    private String queue;
    //路由键
    private String routingKey;
    //重试次数
    private int retryCount = 0;

    public CorrelationData() {
        super();
    }

    public CorrelationData(String id) {
        super(id);
    }

    public CorrelationData(String id, Object data) {
        this(id);
        this.message = data;
    }

}
