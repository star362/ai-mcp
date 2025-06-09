package com.star.ai.test.aimcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
class AiMcpApplicationTests {

    @Value("${gdmcpKey:111}")
    private String gdMcpKey;

    @Test
    void contextLoads() {
        // 初始化 SSE Client
        HttpClientSseClientTransport build = HttpClientSseClientTransport
                .builder("https://mcp.amap.com")
                .sseEndpoint("/sse?key=" + gdMcpKey)
                .build();

        var client = McpClient.async(build)
                .initializationTimeout(Duration.ofSeconds(20))
                .build();
        client.initialize().block();

//        Mono<McpSchema.ListToolsResult> listToolsResultMono = client.listTools();

        // 发起请求
        Mono<McpSchema.CallToolResult> callToolResultMono = client
                .callTool(new McpSchema.CallToolRequest("maps_weather", Map.of("city", "杭州")));
        McpSchema.CallToolResult result = callToolResultMono.block();

        System.out.println("返回的消息：" + result.content());
    }


    @Test
    void mcpToolsTest() throws InterruptedException {
        // 初始化 SSE Client
        HttpClientSseClientTransport build = HttpClientSseClientTransport
                .builder("https://mcp.amap.com")
                .sseEndpoint("/sse?key=" + gdMcpKey)
                .build();

        var client = McpClient.async(build)
                .initializationTimeout(Duration.ofSeconds(20))
                .build();
//        client.initialize().block();

//        Mono<McpSchema.ListResourcesResult> listResourcesResultMono = client.listResources();

//        client.listTools().subscribe(a->{
//            a.tools().stream().forEach(t->{
//                System.out.println(t.name());
//                System.out.println(t.description());
//                System.out.println(t.inputSchema());
//            });
//        });

//        client.closeGracefully();

        var latch = new CountDownLatch(1);

        Mono.using(() -> client,// 创建资源
                c -> {
                    // 使用资源
                    client.initialize().block();
                    return client.listTools(); // 返回要处理的 Mono
                },
                c -> client.closeGracefully()// 释放资源
        ).subscribe(a -> {
            a.tools().stream().forEach(t -> {
                System.out.println(t.name());
                System.out.println(t.description());
                System.out.println(t.inputSchema());
            });
            latch.countDown();
        }, throwable -> {
            System.err.println("Error occurred: " + throwable.getMessage());
        });

        try {
            latch.await(); // 阻塞直到 countDown 被调用
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 重新设置中断状态
        }
    }

}
