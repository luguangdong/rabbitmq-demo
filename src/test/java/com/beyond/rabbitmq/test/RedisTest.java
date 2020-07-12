package com.beyond.rabbitmq.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <p>
 * Description:
 * </p>
 *
 * @author luguangdong
 * @version 1.0.0
 * @ClassName RedisTest
 * @date 2020/7/12 15:50
 * @company https://www.beyond.com/
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testSet(){
        redisTemplate.opsForValue().set("hello","redis");
    }


    @Test
    public void testGet(){
        Object hello = redisTemplate.opsForValue().get("123");
        System.out.println(hello);
    }

    @Test
    public void testSetNX(){
        Boolean exists = (boolean)redisTemplate.execute((RedisCallback) action -> {
            return action.setNX("hello".getBytes(), "hello".getBytes());
        });
        System.out.println(exists);
    }


    @Test
    public void testHasKey(){
        Boolean hasKey = redisTemplate.hasKey("hello");
        System.out.println(hasKey);
    }

    @Test
    public void testDeleteKey(){
        Boolean delete = redisTemplate.delete("hello");
        System.out.println(delete);
    }


}
