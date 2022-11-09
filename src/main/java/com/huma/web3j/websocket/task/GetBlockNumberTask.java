package com.huma.web3j.websocket.task;

import com.huma.web3j.websocket.service.WebSocketClientConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class GetBlockNumberTask {

    public static Map<String,Long> blockNumberMap = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 2L, initialDelay = 10L)
    public void run() throws IOException, InterruptedException {
        WebSocketClientConnector webSocketClientConnector = new WebSocketClientConnector("wss://polygontestapi.terminet.io/ws");
        webSocketClientConnector.start();
        long blockNumber = webSocketClientConnector.getLatestBlockNumber();
        log.error("current blockNumber is:{}",blockNumber);
        blockNumberMap.put("blockNumber",blockNumber);
    }
}
