package com.suo.advice;

import com.alibaba.fastjson.JSON;
import com.suo.pojo.VO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionAdviceTest {

    @Test
    void handleException() {
        VO vo = new VO();
        vo.setCode(1);
        vo.setObj("服务器异常!");
        System.out.println(JSON.toJSONString(vo));
    }
}