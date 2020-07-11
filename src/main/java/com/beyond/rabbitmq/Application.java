package com.beyond.rabbitmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName Application
 * @date 2020/6/15 21:12
 * @company https://www.beyond.com/
 */
@SpringBootApplication
@ImportResource("classpath:spring/spring-rabbitmq.xml")
public class Application{
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
