package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    public void testRedis(){
        //定义key
        String key = "user_token:f0011d50-d707-4ea6-8b46-cf72c8624033";
        //定义value
        Map<String,String> value = new HashMap<>();
        value.put("jwt","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU3MzE0MjQwOCwianRpIjoiZjAwMTFkNTAtZDcwNy00ZWE2LThiNDYtY2Y3MmM4NjI0MDMzIiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.aNHvvComieehxIpatDtu9zBoY4bUnpOleSX7iV6ulU1_iXxr4_r0r1nGYupa01ROahDsq_4sYTPdD5ATQu3XwwcDTzMl-ebRb7TTFtYzN5n-9lbmhnKM1JxHlcpxn8bHLPhxuraI5ZdOm_RoisVx5bvCB4QSd3dkieAQszDvzQegfNUaAm7FmGZSuSUbZsf6_FwD-Uzj8pOH5MYFMNOsXZLEG2iLDsn_6C-tf6GPAQZV0qV5DERsTtcQM6l6Q7xYQRYN3L0l9Yos0ABWVSLoId9xJNOiPOjhFumzykQi3FwvvFV8V-mmnGtEwHkON724GiddGPe5zMKk166PhKS7Sw");
        value.put("refresh_token","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJhdGkiOiJmMDAxMWQ1MC1kNzA3LTRlYTYtOGI0Ni1jZjcyYzg2MjQwMzMiLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU3MzEzOTY3OCwianRpIjoiNzA0MTIyNmEtZGY3OC00M2E1LWIwYjMtMWQ2MjFiOWY5NjM5IiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.MYmksLzXlIUr2tACAhR7KWh_GY1whRjeDc39zhso1RpZHr1abRRXNZjVgbdxymsm2IgFPkBfAG0mWuJga7743Ri9U7AXjfNn3CRFPhMEoRp7bbHn6Yw91_P6ywVaU394VnzZecdBebhO_-UbMu01APqexsVCAt4T--JK6aEoWSNBU7RD2_k2fruPuGCxReiz7u96ts0ZDykhS4PyXPQ-9LlPlVFC1Lpz3NqirYopy18uNTTXAXUdhyevh8PiaSB5j3GC4yg5W4J6ZE-YXr-52f_efXmVy9CZbWHXF---H2t1Jvc6IECyC404MJKt-T03od9m1fQV77Z5SG3nejpkLA");
        //校验key是否存在
        //不存在的时候返回-2
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        System.out.println(expire);
        //存储数据
        redisTemplate.boundValueOps(key).set(JSON.toJSONString(value),60, TimeUnit.SECONDS);
        //获取数据
        String s = redisTemplate.opsForValue().get(key);
        System.out.println(s);
    }
}
