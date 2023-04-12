package com.huma.web3j.websocket.service;

import com.huma.web3j.websocket.config.SysConfig;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tx.Contract;
import org.web3j.utils.Async;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author hudenian
 * @date 2023/4/11
 */
@Slf4j
@Service
public class ClientWeb3Service {

    public static final Event EVENT = new Event("ContractDeployed",
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>(true) {
            }, new TypeReference<Utf8String>() {
            }, new TypeReference<Address>(true) {
            }, new TypeReference<Address>() {
            }));

    public static final BlockingQueue<String> eventQueue = new LinkedBlockingQueue<>();

    private WebSocketClient webSocketClient;
    private Web3j web3j;

    @Resource
    private SysConfig sysConfig;

    private Disposable subscription;

    private long scanBeginBlockNumber;


    @PostConstruct
    public void init() throws Exception {
        this.webSocketClient = new WebSocketClient(new URI(sysConfig.getWsUrl()));
        WebSocketService webSocketService = new WebSocketService(webSocketClient, false);
        webSocketService.connect();
        this.web3j = Web3j.build(webSocketService, sysConfig.getPollingInterval(), Async.defaultExecutorService());
        scanBeginBlockNumber = sysConfig.getScanBeginBlockNumber();
        subscribeContractEvent(scanBeginBlockNumber);
    }

    @PreDestroy
    private void close() {
        if (subscription == null) {
            return;
        }
        if (subscription.isDisposed()) {
            return;
        }
        subscription.dispose();
        subscription = null;
        web3j.shutdown();
    }

    public Long getLastBlock() throws Exception {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber().longValue();
        } catch (WebsocketNotConnectedException e) {
            log.error("Error in get last block. Reconnecting");
            webSocketClient.reconnectBlocking();
            //TODO 可以根据自己业务指定起始监听区块
            subscribeContractEvent(scanBeginBlockNumber);
            return getLastBlock();
        }
    }

    public String getBlockHash() throws Exception {
        try {
            return web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock().getHash();
        } catch (WebsocketNotConnectedException e) {
            log.error("Error in get block hash. Reconnecting");
            webSocketClient.reconnectBlocking();
            //TODO 可以根据自己业务指定起始监听区块
            subscribeContractEvent(scanBeginBlockNumber);
            return getBlockHash();
        }
    }

    private void subscribeContractEvent(long beginBlockNumber) {
        EthFilter filter = new EthFilter(
                DefaultBlockParameter.valueOf(BigInteger.valueOf(beginBlockNumber)),
                DefaultBlockParameterName.LATEST,
                sysConfig.getContractAddress());

        filter.addSingleTopic(EventEncoder.encode(EVENT));

        subscription = web3j
                .ethLogFlowable(filter).subscribe(
                        eventLog -> {
                            long blockNumber = eventLog.getBlockNumber().longValue();
                            String txHash = eventLog.getTransactionHash();
                            log.error("监听到事件,交易hash:{}", txHash);
                            EventValues eventValues = Contract.staticExtractEventParameters(EVENT, eventLog);
                            String sb = "blockNumber:" + blockNumber +
                                    ",txHash:" + txHash +
                                    ",address:" + eventValues.getIndexedValues().get(1).toString();
                            eventQueue.add(sb);
                            scanBeginBlockNumber = blockNumber;
                            log.error("current scan begin block is:{}", scanBeginBlockNumber);
                        });
    }

}
