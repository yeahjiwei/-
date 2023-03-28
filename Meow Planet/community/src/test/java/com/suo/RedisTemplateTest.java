package com.suo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTemplateTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test1(){
        String key = "test:count";
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key,1);
        System.out.println(valueOperations.get(key));
        System.out.println(valueOperations.increment(key));
        System.out.println(valueOperations.get(key));
        System.out.println(valueOperations.decrement(key));
    }

    @Test
    public void test2(){
        String key = "test:user";
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.put(key,"id",1);
        hashOperations.put(key,"username","zhangsan");

        System.out.println(hashOperations.get(key,"id"));
        System.out.println(hashOperations.get(key,"username"));
    }

    @Test
    public void test3(){
        String key = "test:ids";
        ListOperations listOperations = redisTemplate.opsForList();
        listOperations.leftPush(key,101);
        listOperations.leftPush(key,102);
        listOperations.leftPush(key,103);
        listOperations.leftPush(key,104);

        System.out.println(listOperations.size(key));

        System.out.println(listOperations.index(key,0));

        System.out.println(listOperations.range(key,0,2));

        System.out.println(listOperations.leftPop(key));

        System.out.println(listOperations.rightPop(key));
    }

    @Test
    public void test4(){
        String key = "test:teachers";
        SetOperations setOperations = redisTemplate.opsForSet();
        setOperations.add(key,"zhangsan","lisi","刘备");

        System.out.println(setOperations.size(key));
        System.out.println(setOperations.pop(key));
        System.out.println(setOperations.members(key));
    }

    @Test
    public void test5(){
        String key = "test:students";
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add(key,"唐僧",80);
        zSetOperations.add(key,"悟空",90);
        zSetOperations.add(key,"八戒",70);
        zSetOperations.add(key,"沙僧",70);
        zSetOperations.add(key,"白龙马",50);

        System.out.println(zSetOperations.size(key));
        System.out.println(zSetOperations.score(key,"八戒"));
        System.out.println(zSetOperations.rank(key,"八戒"));
        System.out.println(zSetOperations.reverseRank(key,"八戒"));
        System.out.println(zSetOperations.range(key,0,4));
    }

    @Test
    public void test6(){
        System.out.println(redisTemplate.hasKey("test:user"));
        System.out.println(redisTemplate.delete("test:user"));
        System.out.println(redisTemplate.hasKey("test:user"));
        System.out.println(redisTemplate.expire("test:teachers",10, TimeUnit.SECONDS));
    }

    @Test
    public void test7(){
        String key = "test:students";
        BoundZSetOperations boundZSetOperations = redisTemplate.boundZSetOps(key);
        boundZSetOperations.add("唐僧",80);
        boundZSetOperations.add("悟空",90);

    }

    //事务
    @Test
    public void tx(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String key = "test:tx";
                //开启事务
                operations.multi();
                ValueOperations valueOperations = operations.opsForValue();
                valueOperations.set(key,1);
                valueOperations.set(key,2);
                System.out.println(valueOperations.get(key));
                //提交事务
                return operations.exec();
            }
        });
        System.out.println(obj);
    }

    //统计20W个重复数据的独立总数
    @Test
    public void HyperLogLogTest() {
        String key = "test:hll:01";

        for (int i = 0; i < 100000; i++) {
            HyperLogLogOperations hyperLogLogOperations = redisTemplate.opsForHyperLogLog();
            hyperLogLogOperations.add(key,i);
        }
        for (int i = 0; i < 100000; i++) {
            HyperLogLogOperations hyperLogLogOperations = redisTemplate.opsForHyperLogLog();
            hyperLogLogOperations.add(key,(int)(Math.random() * 10000 + 1));
        }
        System.out.println(redisTemplate.opsForHyperLogLog().size(key));
    }

    //将三组数据合并，再统计合并后重复数据的独立总数
    @Test
    public void HyperLogLogUnionTest() {
        String key = "test:hll:02";
        for (int i = 0; i < 10000; i++) {
            HyperLogLogOperations hyperLogLogOperations = redisTemplate.opsForHyperLogLog();
            hyperLogLogOperations.add(key,i);
        }

        String key2 = "test:hll:03";
        for (int i = 0; i < 10000; i++) {
            HyperLogLogOperations hyperLogLogOperations = redisTemplate.opsForHyperLogLog();
            hyperLogLogOperations.add(key2,i + 5000);
        }

        String key3 = "test:hll:04";
        for (int i = 0; i < 10000; i++) {
            HyperLogLogOperations hyperLogLogOperations = redisTemplate.opsForHyperLogLog();
            hyperLogLogOperations.add(key3,i + 10000);
        }

        String unionKey = "test:hll:union";

        System.out.println(redisTemplate.opsForHyperLogLog().union(unionKey,key,key2,key3));
    }

    @Test
    public void BitmapTest() {
        String key = "test:bm:01";

        // 记录
        redisTemplate.opsForValue().setBit(key,1,true);
        redisTemplate.opsForValue().setBit(key,4,false);
        redisTemplate.opsForValue().setBit(key,7,true);

        // 查询
        System.out.println(redisTemplate.opsForValue().getBit(key,1));
        System.out.println(redisTemplate.opsForValue().getBit(key,2));
        System.out.println(redisTemplate.opsForValue().getBit(key,3));

        // 统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(key.getBytes());
            }
        });

        System.out.println(obj);
    }

    // 或运算OR
    @Test
    public void BitmapOperationTest() {
        String key2 = "test:bm:02";
        // 记录
        redisTemplate.opsForValue().setBit(key2,0,true);
        redisTemplate.opsForValue().setBit(key2,1,true);
        redisTemplate.opsForValue().setBit(key2,2,true);

        String key3 = "test:bm:03";
        // 记录
        redisTemplate.opsForValue().setBit(key3,2,true);
        redisTemplate.opsForValue().setBit(key3,3,true);
        redisTemplate.opsForValue().setBit(key3,4,true);

        String key4 = "test:bm:04";
        // 记录
        redisTemplate.opsForValue().setBit(key4,4,true);
        redisTemplate.opsForValue().setBit(key4,5,true);
        redisTemplate.opsForValue().setBit(key4,6,true);

        String key = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,key.getBytes(),key2.getBytes(),key3.getBytes(),key4.getBytes());
                return connection.bitCount(key.getBytes());
            }
        });

        System.out.println(obj);

        for (int i = 0; i < 7; i++) {
            System.out.println(redisTemplate.opsForValue().getBit(key,i));
        }
    }
}
