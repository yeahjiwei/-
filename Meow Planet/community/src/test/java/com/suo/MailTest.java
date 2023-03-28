package com.suo;

import com.suo.utils.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void sendTest(){
        mailClient.sendMail("2645118890@qq.com","TEST","Hello World!!!");
    }

    @Test
    public void sendHtmlMail(){
        Context context = new Context();
        context.setVariable("username","test");

        String process = templateEngine.process("/mail/demo", context);
        System.out.println(process);
        mailClient.sendMail("2645118890@qq.com","HTML",process);
    }
}
