package com.huma.web3j.websocket.service;

import com.huma.web3j.websocket.config.SysConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CertificatePinner;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.utils.Async;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
/**
 * @author hudenian
 * @date 2023/4/11
 */
@Slf4j
@Service
public class ClientWeb3Service {
    private WebSocketClient webSocketClient;
    private Web3j web3j;

    @Resource
    private SysConfig sysConfig;


    @PostConstruct
    public void init() throws Exception {
        this.webSocketClient = new WebSocketClient(new URI(sysConfig.getWsUrl()));
        WebSocketService webSocketService = new WebSocketService(webSocketClient, false);
        webSocketService.connect();
        this.web3j = Web3j.build(webSocketService, sysConfig.getPollingInterval(), Async.defaultExecutorService());
    }

    public Long getLastBlock() throws Exception {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber().longValue();
        } catch (WebsocketNotConnectedException e) {
            log.error("Error in pending tx subscription. Reconnecting");
            webSocketClient.reconnectBlocking();
            return getLastBlock();
        }
    }

    public String getBlockHash() throws Exception {
        return web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST,false).send().getBlock().getHash();
    }
}
