package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * 发布订阅的的消费者
 * 监听类
 */
@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            System.out.println("收到了生成静态页面的消息: " + textMessage.getText());
            itemPageService.genIteHtml(Long.parseLong(textMessage.getText()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
