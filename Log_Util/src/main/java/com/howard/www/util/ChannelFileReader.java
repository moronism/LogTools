package com.howard.www.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChannelFileReader {

    private static final Logger logger = LogManager.getLogger("ChannelFileReader");

    private FileInputStream fileInputStream;
    private ByteBuffer byteBuffer;
    private long fileLength;
    private int arraySize;
    private String legacyPrefix;
    private String dataBlock;
    private int countDataBlock;
    public AtomicInteger atomicInteger = new AtomicInteger();

    public int getCountDataBlock() {
        return countDataBlock;
    }

    public ChannelFileReader(String fileName, int arraySize) throws Exception {
        this.fileInputStream = new FileInputStream(fileName);
        this.fileLength = fileInputStream.getChannel().size();
        this.arraySize = arraySize;
        this.byteBuffer = ByteBuffer.allocate(arraySize);
    }

    public List<String> readDataBlock() throws Exception {
        boolean finalDataComplete;
        FileChannel fileChannel = fileInputStream.getChannel();
        int bytes = fileChannel.read(byteBuffer);
        List<String> dataBlockItem = null;
        if (bytes != -1) {
            byte[] array = new byte[bytes];
            byteBuffer.flip();
            byteBuffer.get(array);
            if (array != null && array.length > 0) {
                if (array[array.length - 1] == 10) {
                    finalDataComplete = true;
                } else {
                    finalDataComplete = false;
                }
                dataBlock = new String(array);
                dataBlockItem = new ArrayList<String>(Arrays.asList(dataBlock.split("\n")));

                if (dataBlockItem != null && dataBlockItem.size() >= 0) {
                    //logger.info("{} , {}",finalDataComplete,dataBlockItem.get(dataBlockItem.size()-1));
                    if (legacyPrefix != null && !"".equals(legacyPrefix)) {
                        String firstItem = new StringBuffer().append(legacyPrefix).append(dataBlockItem.get(0)).toString();
                        dataBlockItem.set(0, firstItem);
                        //logger.info("incomplete String , need add legacy prefix is {},complete String is {}",legacyPrefix,dataBlockItem.get(0));
                    }
                    //logger.info("legacy prefix is {}, first context is {}, list is Line feed {}",legacyPrefix,dataBlockItem.get(0),finalDataComplete);
                    if (finalDataComplete == false) {
                        int dataBlockItemSize = dataBlockItem.size() - 1;
                        legacyPrefix = dataBlockItem.get(dataBlockItemSize);
                        //logger.info("list is Line feed, legacy prefix is {}",legacyPrefix);
                        dataBlockItem.remove(dataBlockItemSize);
                    }
                    countDataBlock += dataBlockItem.size();
                }
            }
        }
        byteBuffer.clear();
        return dataBlockItem;
    }

    public void close() throws Exception {
        fileInputStream.close();
    }

}
