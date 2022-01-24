package com.howard.www.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ReadFileByLine {
    public static void readLineByChannel(String path) throws Exception {
        long lineNumber = 0;
        FileInputStream fileIn = new FileInputStream(path);
        FileChannel fileChannel = fileIn.getChannel();
        // 开始按行读取
        int bufferSize = 1024 * 1024;  // 每一块的大小
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        byte b;

        while(fileChannel.read(buffer) > 0)
        {
            buffer.flip();
            for (int i = 0; i < buffer.limit(); i++)
            {
                b = buffer.get();
                if(b==10){  // 如果遇到换行
                    lineNumber++;
                }

            }
            buffer.clear(); // 清空buffer
        }
        fileChannel.close();
    }

    public static void readLineByBufferedReader(String path) throws Exception {
        long lineNumber = 0;
        FileInputStream inputStream = new FileInputStream(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line=bufferedReader.readLine()) != null)
        {
            lineNumber++;
        }
        inputStream.close();
        bufferedReader.close();
    }

    public static void main(String[] args) throws Exception {
        String path = "E:\\log\\nginx\\log\\xaa";
        long startTime = System.currentTimeMillis();
        readLineByChannel(path);
        System.out.println("readLineByChannel耗时：" + (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        readLineByBufferedReader(path);
        System.out.println("readLineByBufferedReader耗时：" + (System.currentTimeMillis() - startTime));
    }
}
