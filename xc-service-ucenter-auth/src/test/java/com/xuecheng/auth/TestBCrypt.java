package com.xuecheng.auth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestBCrypt {
    @Test
    public void testPassword(){
        //原始密码
        String pass = "111111";
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        //使用BCrypt加密，每次都会采用一个随机盐，因为每个计算出来的hash都不一样
        for(int i=0;i<10;i++){
            String encode = bCryptPasswordEncoder.encode(pass);
            System.out.println(encode);
        }
    }
}
