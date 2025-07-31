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
    public void mongoTest() {
        HttpClientStreamableHttpTransport streamableHttpTransport = HttpClientStreamableHttpTransport
                .builder("http://localhost:3000")
                .build();

        McpSyncClient client = McpClient.sync(streamableHttpTransport)
                .requestTimeout(Duration.ofSeconds(10))
                .capabilities(McpSchema.ClientCapabilities.builder()
                        .roots(true)      // Enable roots capability
                        .sampling()       // Enable sampling capability
                        .build())
                .build();

        // Initialize connection
        client.initialize();

        // List available tools
        McpSchema.ListToolsResult tools = client.listTools();
        System.out.println("Available tools: " + tools.tools());
        tools.tools().forEach(a-> System.out.println(a.name()));
    }


    @Test
    public void simpleConnectionTest() {


        // 初始化 SSE Client
        HttpClientStreamableHttpTransport build = HttpClientStreamableHttpTransport
                .builder("http://127.0.0.1:3000/mcp")
                .build();

        var client = McpClient.async(build)
                .initializationTimeout(Duration.ofSeconds(20))
                .build();
        client.initialize().block();

        client.listTools().subscribe(a->{
            a.tools().stream().forEach(t->{
                System.out.println(t.name());
                System.out.println(t.description());
                System.out.println(t.inputSchema());
            });
        });



    }







}
