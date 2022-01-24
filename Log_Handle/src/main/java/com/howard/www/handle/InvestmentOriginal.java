package com.howard.www.handle;


import com.howard.www.util.FileHandleUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class InvestmentOriginal {
    private static final Logger logger = LogManager.getLogger("handleOriginal");
    private static final int CPU_NUM=Runtime.getRuntime().availableProcessors();
    public void splitFileAndRead(List<String> splitFiles) throws Exception {
        if(splitFiles.size()<=0){
            return;
        }
        // 配置等待队列
        BlockingQueue<Runnable> blockingDeque=new ArrayBlockingQueue<Runnable>(100);
        // 核心业务线程池
        // 配置饱和策略
        // IO密集型
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2*CPU_NUM,
                2*CPU_NUM,
                60L,
                TimeUnit.SECONDS,
                blockingDeque,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        //逐个读取文件内容
        for(String splitFile:splitFiles){
            threadPoolExecutor.submit(new ProcessOriginal(splitFile));
        }

        threadPoolExecutor.shutdown();
    }

    public static void main(String[] args) throws Exception {
        InvestmentOriginal handleOriginal = new InvestmentOriginal();
        List<String> logFileItems= FileHandleUtil.obtainRootPathFile("E:\\log\\nginx\\log");
        if(logFileItems!=null){
            handleOriginal.splitFileAndRead(logFileItems);
        }
    }
}
