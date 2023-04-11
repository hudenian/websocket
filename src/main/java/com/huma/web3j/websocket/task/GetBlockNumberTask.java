package com.huma.web3j.websocket.task;

import com.huma.web3j.websocket.service.ClientWeb3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class GetBlockNumberTask {

    public static Map<String, Long> blockNumberMap = new ConcurrentHashMap<>();
    public static Map<String, String> blockHashMap = new ConcurrentHashMap<>();

    static final AtomicLong INDEX = new AtomicLong(0);

    @Resource
    private ClientWeb3Service clientWeb3Service;


    @Scheduled(fixedDelay = 2000L, initialDelay = 1000L)
    public void run() throws Exception {
        log.error("GetBlockNumberTask==========================第{}次启动==========================", INDEX.incrementAndGet());
        while (true) {
            Thread.sleep(2000L);
            long blockNumber = clientWeb3Service.getLastBlock();
            log.error("current blockNumber is:{}", blockNumber);
            blockNumberMap.put("blockNumber", blockNumber);
            String hash = clientWeb3Service.getBlockHash();
            blockHashMap.put("blockNumber", hash);
            log.error("current block hash is:{}", hash);
        }
    }
}
