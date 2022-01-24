package com.howard.www.handle;

import org.apache.rocketmq.client.producer.DefaultMQProducer;

public class ThoroughTransmit {
    static {
        initMqServer();
    }
    public static DefaultMQProducer producer;

    private static void initMqServer() {
        try {
            // 实例化消息生产者Producer
            producer = new DefaultMQProducer("please_rename_unique_group_name");
            // 设置NameServer的地址
            producer.setNamesrvAddr("localhost:9876");
            // 启动Producer实例
            producer.start();
            producer.setRetryTimesWhenSendAsyncFailed(0);
        } catch (Exception e) {

        }
    }


}
