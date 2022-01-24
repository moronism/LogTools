package com.howard.www.handle;

import com.alibaba.fastjson.JSON;
import com.howard.www.handle.bean.NginxAccessLogRecord;
import com.howard.www.util.ChannelFileReader;
import com.howard.www.util.UniqueConstraintGenerator;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.CountDownLatch2;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class ProcessOriginal implements Runnable {

    private static final Logger logger = LogManager.getLogger("dumpData");

    private String accessLogFormat = "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\"";

    private Parser<NginxAccessLogRecord> httpdLoglineParser = new HttpdLoglineParser<NginxAccessLogRecord>(NginxAccessLogRecord.class, accessLogFormat);

    private String filePath;

    //一个应用尽可能用一个Topic，而消息子类型则可以用tags来标识
    private final String topic = "AccessLog".intern();

    private final String tags = "NginxAccessLog".intern();

    private UniqueConstraintGenerator uniqueConstraintGenerator = new UniqueConstraintGenerator(1, 1, 1);

    // 记录当前线程总共处理的数据条数
    private AtomicInteger atomicInteger = new AtomicInteger();

    public ProcessOriginal(String filePath) {
        this.filePath = filePath;
    }

    public ProcessOriginal(String accessLogFormat, UniqueConstraintGenerator uniqueConstraintGenerator) {
        this.accessLogFormat = accessLogFormat;
        this.uniqueConstraintGenerator = uniqueConstraintGenerator;
    }

    private void batchSendByteMessage(List<byte[]> messageItems) throws Exception {
        int messageCount = messageItems.size();
        // 根据消息数量实例化倒计时计算器
        final CountDownLatch2 countDownLatch = new CountDownLatch2(messageCount);
        for (int i = 0; i < messageItems.size(); i++) {
            //
            final byte[] messageItem = messageItems.get(i);
            // 创建消息，并指定Topic，Tag和消息体
            //每个消息在业务层面的唯一标识码要设置到keys字段，方便将来定位消息丢失问题。
            // 服务器会为每个消息创建索引（哈希索引），应用可以通过topic、key来查询这条消息内容，以及消息被谁消费。
            // 由于是哈希索引，请务必保证key尽可能唯一，这样可以避免潜在的哈希冲突。
            Message message = new Message(topic,
                    tags,
                    uniqueConstraintGenerator.nextId() + "",
                    messageItem);
            // SendCallback接收异步返回结果的回调
            ThoroughTransmit.producer.send(message, new SendCallback() {
                public void onSuccess(SendResult sendResult) {
                    //消息发送成功。要注意的是消息发送成功也不意味着它是可靠的。
                    // 要确保不会丢失任何消息，还应启用同步Master服务器或同步刷盘，
                    // 即SYNC_MASTER或SYNC_FLUSH。
                    countDownLatch.countDown();
                    atomicInteger.incrementAndGet();
                    //logger.info("from {} send message success ", filePath);
                }

                public void onException(Throwable e) {
                    // 发送失败
                    countDownLatch.countDown();
                    logger.error("{}", new String(messageItem));
                }
            });
        }
        // 等待5s
        countDownLatch.await(5, TimeUnit.SECONDS);
        // 如果不再发送消息，关闭Producer实例。

    }

    private void batchSendMessage(List<String> messageItems) throws Exception {
        if (messageItems == null || messageItems.size() <= 0) {
            return;
        }
        int messageCount = messageItems.size();
        String messageBody = null;
        byte[] messageByte = null;
        List<byte[]> items = new ArrayList<byte[]>();
        for (int i = 0; i < messageCount; i++) {
            messageBody = messageItems.get(i);
            if (messageBody != null && !"".equals(messageBody)) {
                NginxAccessLogRecord nginxAccessLogRecord = null;
                String nginxAccessLog = null;
                try {
                    nginxAccessLogRecord = httpdLoglineParser.parse(messageBody);
                    nginxAccessLog = JSON.toJSONString(nginxAccessLogRecord);
                    //logger.info(nginxAccessLog);
                } catch (Exception e) {
                    logger.info(messageBody);
                }
                //1byte=8bit
                //1kb=1024byte=1024*8bit
                if (nginxAccessLog != null && !"".equals(nginxAccessLog)) {
                    messageByte = nginxAccessLog.getBytes(RemotingHelper.DEFAULT_CHARSET);
                    if (messageByte != null && messageByte.length > 0) {
                        items.add(messageByte);
                    }
                } else {

                }
            }
        }
        if (items.size() > 0) {
            batchSendByteMessage(items);
        }
    }

    public void run() {
        if (this.filePath != null) {
            try {
                ChannelFileReader reader = new ChannelFileReader(filePath, 65536);
                List<String> items;
                while ((items = reader.readDataBlock()) != null) {
                    if (items != null && items.size() > 0) {
                        batchSendMessage(items);
                    }
                }
                ;
                reader.close();
                logger.info(" 线程 {}  读取文件 {} 获得 {}条数据需要进行处理 , 通过消息方式发送至消息中间 {} 条 ", Thread.currentThread().getName(), filePath, reader.getCountDataBlock(), atomicInteger.get());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ThoroughTransmit.producer.shutdown();
            }
        }
    }
}
