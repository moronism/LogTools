package com.howard.www.handle;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.howard.www.db.DuridDBUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.ArrayList;
import java.util.List;

public class PersistenceOriginal {
    private static final Logger logger = LogManager.getLogger("persistenceOriginal");

    private void consumptionOriginal() throws Exception {
        DeliveryReceiver.consumer.registerMessageListener(new MessageListenerConcurrently() {
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                //logger.info("{} receive new messages subtotal {} strip", Thread.currentThread().getId(), msgs.size());
                String insertSqlPrefix="INSERT INTO `access_log` (`original`) VALUES ('";
                String insertSqlSuffix="');";
                StringBuffer stringBuffer=new StringBuffer();
                List<String> batchSql=new ArrayList<String>();
                String parameter;
                for (MessageExt messageExt : msgs) {
                    if (messageExt.getBody() != null && messageExt.getBody().length > 0) {
                        parameter=new String(messageExt.getBody());
                        if(parameter!=null&&!"".equals(parameter)){
                            stringBuffer.append(insertSqlPrefix).append(parameter).append(insertSqlSuffix);
                            batchSql.add(stringBuffer.toString());
                            stringBuffer.delete(0,stringBuffer.length());
                        }
                    }
                }
                if(batchSql.size()>0){
                    try {
                        DuridDBUtil.insertBatch(batchSql);
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }catch (Exception e){
                        logger.error(msgs);
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        DeliveryReceiver.consumer.start();
    }

    public static void main(String[] args) throws Exception {
        PersistenceOriginal persistenceOriginal = new PersistenceOriginal();
        persistenceOriginal.consumptionOriginal();

    }
}
