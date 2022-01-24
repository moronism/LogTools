package com.howard.www.handle;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;

public class DeliveryReceiver {

    private static int  consumeMessageBatchMaxSize=1000;
    private static int  consumeThreadMin=20;
    private static int  consumeThreadMax=20;
    public static DefaultMQPushConsumer consumer;
    static {
        initDeliveryReceiver();
    }

    private static void initDeliveryReceiver(){
        try{
            consumer=new DefaultMQPushConsumer("please_rename_unique_group_name");
            consumer.setNamesrvAddr("localhost:9876");
            consumer.subscribe("AccessLog", "*");
            //某些业务流程如果支持批量方式消费，则可以很大程度上提高消费吞吐量，例如订单扣款类应用，
            // 一次处理一个订单耗时 1 s，一次处理 10 个订单可能也只耗时 2 s，
            // 这样即可大幅度提高消费的吞吐量，
            // 通过设置 consumer的 consumeMessageBatchMaxSize 返个参数，默认是 1，
            // 即一次只消费一条消息，例如设置为 N，那么每次消费的消息数小于等于 N。
            consumer.setConsumeMessageBatchMaxSize(consumeMessageBatchMaxSize);
            // 提高单个 Consumer 的消费并行线程，
            // 通过修改参数 consumeThreadMin、consumeThreadMax实现
            consumer.setConsumeThreadMin(consumeThreadMin);
            consumer.setConsumeThreadMax(consumeThreadMax);
        }catch (Exception e){

        }
    }

}
