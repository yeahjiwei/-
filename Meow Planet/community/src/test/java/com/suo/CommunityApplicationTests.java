package com.suo;

import com.suo.mapper.UserMapper;
import com.suo.pojo.User;
import com.suo.utils.CommunityConstant;
import com.suo.utils.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;
import java.util.Random;

@SpringBootTest
class CommunityApplicationTests {


    private static final Logger logger = LoggerFactory.getLogger(CommunityApplicationTests.class);


    @Test
    public void test(){
        logger.debug("debug log");
        logger.info("info log");
        logger.warn("warn log");
        logger.error("error log");
    }

    @Test
    public void uuid(){
        System.out.println(CommunityUtil.generateUUID());
    }

    @Test
    public void time(){
        long l = System.currentTimeMillis();
        System.out.println(System.currentTimeMillis());
        Date date = new Date();
        System.out.println(date);
        System.out.println(new Date(System.currentTimeMillis()));
        System.out.println(new Date(System.currentTimeMillis() + 0));
        System.out.println(new Date(System.currentTimeMillis() + 1000 * 3600));
        System.out.println(new Date(System.currentTimeMillis() + 1000 * 3600 * 12));
        System.out.println(new Date(System.currentTimeMillis() + 1000 * 3600 * 24 * 7));
        System.out.println(new Date(System.currentTimeMillis() + 1000 * CommunityConstant.REMEMBER_EXPIRED_SECONDS));
    }

    @Test
    public void url(){
        for(int i = 0; i< 1000; i++)
        System.out.println(String.format("https://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
    }

}
