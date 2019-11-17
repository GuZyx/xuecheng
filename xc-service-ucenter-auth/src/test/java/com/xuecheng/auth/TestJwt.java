package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {

    @Test   //创建jwt令牌
    public void createJwt(){
        //密钥库的文件
        String key_store = "xc.keystore";
        //密钥库的密码
        String key_store_pass = "xuechengkeystore";
        //密钥的别名
        String key_alias = "xckey";
        //密钥的访问密码
        String key_pass = "xuecheng";
        //密钥库文件的路径
        Resource resource = new ClassPathResource(key_store);

        //创建密钥工厂
        KeyStoreKeyFactory keyFactory = new KeyStoreKeyFactory(resource,key_store_pass.toCharArray());
        //密钥对(公钥和私钥)
        KeyPair keyPair = keyFactory.getKeyPair(key_alias, key_pass.toCharArray());
        //拿到私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        //jwt令牌的内容
        Map<String,String> body = new HashMap<>();
        body.put("name","itcast");
        String content = JSON.toJSONString(body);

        //生成jwt令牌
        Jwt jwt = JwtHelper.encode(content, new RsaSigner(privateKey));
        //生成Jwt令牌编码
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

    @Test   //校验jwt令牌
    public void testVerify(){
        //公钥
        String pubKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
        //校验jwt令牌
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJ0ZXN0MDIiLCJ1dHlwZSI6IjEwMTAwMiIsImlkIjoiNDkiLCJleHAiOjE1NzM1NzI3MzgsImF1dGhvcml0aWVzIjpbInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYmFzZSIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfZGVsIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9saXN0IiwiY291cnNlX2dldF9iYXNlaW5mbyIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfcGxhbiIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2UiLCJjb3Vyc2VfZmluZF9saXN0IiwieGNfdGVhY2htYW5hZ2VyIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9tYXJrZXQiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX3B1Ymxpc2giLCJjb3Vyc2VfcGljX2xpc3QiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX2FkZCJdLCJqdGkiOiI3YmY5ODEwNS00MmI3LTQxZWMtYmQ4Zi1lMWQ4ZDNjM2YzMmIiLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.fdut4Cf9-mT6PeNtUvJHtSRAmZxbH-u1HF-S39Reyt9941Bs8HvoDVNUY8RRbwJvzSdtnwxqrQHO2o2j4Rtk7TIneOMYERMXsF3AsXta6IzI2uPtl1KIELcRqpYfsjNO_08FUdXwXCRm8x38oTGVoAbt7RkS48lLgBdzTaUrtg7bQGsrM-0R7G-427XCn5j9zswPctPLWs3P9rcaU3QCBfXZ3YoTwdUXuMutxhdceB-vPTZVSdgZQ6XLv5Dw23d9OEXdWGIlnBVyc4Ae-IDU-ijcJSgov1kN0d2f_YAub5I_JiKXpnrAQ0B_hzrKhpOLHEPXR52aQ51HdMq4UudmBQ";
        Jwt verify = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(pubKey));
        //拿到jwt令牌自定义的内容
        String claims = verify.getClaims();
        System.out.println(claims);
    }
}
