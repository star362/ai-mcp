package com.star.ai.test.aimcp.controller;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class ChatTestController {

    @Value("${gdmcpKey:111}")
    private String gdMcpKey;
    private ChatClient client;

    public ChatTestController(ChatClient.Builder clientBuilder) {
        InMemoryChatMemoryRepository inMemoryChatMemoryRepository = new InMemoryChatMemoryRepository();

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(inMemoryChatMemoryRepository)
                .maxMessages(10)
                .build();
        this.client = clientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor())
                .build();
    }


    @GetMapping("/chat")
    public String chat(String message) {
        return client.prompt()
                .system("你是问答助手帮助回答问题.")
                .user(message)
                .call().content();
    }

    @GetMapping(value = "/tools", produces = "text/html;charset=utf-8")
    public Flux<String> tools(String message) {
        // 初始化 SSE Client
        HttpClientSseClientTransport build = HttpClientSseClientTransport
                .builder("https://mcp.amap.com")
                .sseEndpoint("/sse?key="+ gdMcpKey)
                .build();

        McpClient.AsyncSpec spec = McpClient.async(build)
//                .clientInfo(clientInfo)
                .requestTimeout(Duration.ofSeconds(20));

        McpAsyncClient mcpclient = spec.build();

        mcpclient.initialize().block();

        return client.prompt()
                .system("你是问答助手帮助回答问题.")
                .user(message)
                .toolCallbacks(new AsyncMcpToolCallbackProvider(mcpclient))
                .stream()
                .content();
    }

}
