package com.suo.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.suo.pojo.DiscussPost;
import com.suo.pojo.Event;
import com.suo.pojo.Message;
import com.suo.service.DiscussPostService;
import com.suo.service.ElasticsearchService;
import com.suo.service.MessageService;
import com.suo.utils.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {CommunityConstant.TOPIC_COMMENT,CommunityConstant.TOPIC_LIKE,CommunityConstant.TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {

        if (record == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSON.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        Message message = new Message();
        message.setToId(event.getEntityUserId());
        message.setFromId(CommunityConstant.SYSTEM_USER_ID);
        message.setCreateTime(new Date());
        message.setConversationId(event.getTopic());

        Map<String,Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSON.toJSONString(content));
        messageService.addMessage(message);
    }

    // 消费发帖事件
    @KafkaListener(topics = {CommunityConstant.TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            logger.error("消息内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("消息格式错误！");
            return;
        }
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }


}
