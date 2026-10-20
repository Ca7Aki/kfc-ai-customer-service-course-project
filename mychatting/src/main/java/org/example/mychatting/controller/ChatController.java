package org.example.mychatting.controller;

import dev.langchain4j.service.TokenStream;
import org.example.mychatting.config.AiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin
public class ChatController {

    @Autowired
    private AiConfig.Assistant assistant;

    // ========== 原流式接口（保留，网页端/浏览器继续用） ==========
    @RequestMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@RequestParam(name = "memoryId", defaultValue = "default", required = false) String memoryId,
                             @RequestParam(defaultValue = "我是谁", name = "message", required = false) String message) {
        System.out.println("in controller");
        TokenStream stream = assistant.stream(memoryId, message);
        return Flux.create(sink -> {
            stream.onPartialResponse(sink::next)
                    .onCompleteResponse(s -> sink.complete())
                    .onError(sink::error)
                    .start(); // 启动流处理
        });
    }

    // ========== 小程序专用的普通 JSON 接口 ==========
    // 请求方式: POST  请求路径: /ai/chat-json
    // 请求体(JSON): {"message":"你好","memoryId":"default"}
    // 返回(JSON):   {"reply":"AI客服的完整回复"}
    @PostMapping("/chat-json")
    public Map<String, String> chatJson(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "");
        String memoryId = body.getOrDefault("memoryId", "default");
        String reply = assistant.chat(memoryId, message);
        return Map.of("reply", reply == null ? "" : reply);
    }
}