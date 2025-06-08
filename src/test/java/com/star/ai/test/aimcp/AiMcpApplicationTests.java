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

@SpringBootTest
class AiMcpApplicationTests {

    @Value("${gdmcpKey}")
    private String gdMcpKey;

    @Test
    void contextLoads() {
        // 初始化 SSE Client
        HttpClientSseClientTransport build = HttpClientSseClientTransport.builder("https://mcp.amap.com")
                .sseEndpoint("/sse?key="+ gdMcpKey)
                .build();

        var client = McpClient.async(build)
                .initializationTimeout(Duration.ofSeconds(20))
                .build();
        client.initialize().block();



                // 发起请求
        Mono<McpSchema.CallToolResult> callToolResultMono = client
                .callTool(new McpSchema.CallToolRequest("maps_weather", Map.of("city", "杭州")));
        McpSchema.CallToolResult result = callToolResultMono.block();
        
        System.out.println("返回的消息：" + result.content());
    }

}
