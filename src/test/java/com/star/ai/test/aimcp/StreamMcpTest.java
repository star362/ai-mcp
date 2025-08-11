package com.star.ai.test.aimcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.star.ai.test.aimcp.enetity.User;
import com.star.ai.test.aimcp.repository.UserRepository;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class StreamMcpTest extends AiMcpApplicationTests {




    @Test
    public void simpleConnectionTest() {

        // 初始化 SSE Client
        HttpClientStreamableHttpTransport build = HttpClientStreamableHttpTransport
                .builder("http://127.0.0.1:3000")
                .endpoint("/mcp/")
                .build();

        var client = McpClient.async(build)
                .initializationTimeout(Duration.ofSeconds(20))
                .build();



        var latch = new CountDownLatch(1);

        Mono.using(() -> client,// 创建资源
                c -> {
                    // 使用资源
                    client.initialize();
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




    public static void main(String[] args) {
        McpClientTransport transport;
        String url = "http://localhost:3000";
        HttpClientStreamableHttpTransport.Builder httpBuilder = HttpClientStreamableHttpTransport.builder(url);
        httpBuilder.endpoint("/mcp/");
        transport = httpBuilder.build();

        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(20)) // Set a reasonable request timeout
                .initializationTimeout(Duration.ofSeconds(21)) // Set a reasonable initialization timeout
                .build();

        try {
            // Initialize the connection
            client.initialize();

            McpSchema.ListToolsResult tools = client.listTools();
//            logger.log(Level.INFO, "Available tools: {0}", tools.tools());
            tools.tools().stream().forEach(t -> {
                System.out.println(t.name());
                System.out.println(t.description());
                System.out.println(t.inputSchema());
            });

        } catch (Exception e) {
//            logger.log(Level.SEVERE, "Failed to initialize MCP client from DebugServlet", e);
        }
    }


}
