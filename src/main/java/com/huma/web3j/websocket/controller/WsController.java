package com.huma.web3j.websocket.controller;

import com.huma.web3j.websocket.service.ClientWeb3Service;
import com.huma.web3j.websocket.task.GetBlockNumberTask;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 */
@RestController
@RequestMapping(value = "ws", produces = MediaType.APPLICATION_JSON_VALUE)
public class WsController {


    @GetMapping("/blockNumber")
    public Long getBlock() {
        return GetBlockNumberTask.blockNumberMap.get("blockNumber");
    }

    @GetMapping("/blockHash")
    public String getBlockHash() {
        return GetBlockNumberTask.blockHashMap.get("blockNumber");
    }

    @GetMapping("/events")
    public String getEventQueue() {
        if (!ClientWeb3Service.eventQueue.isEmpty()) {
            return ClientWeb3Service.eventQueue.remove();
        } else {
            return "还未监听到事件";
        }

    }
}
