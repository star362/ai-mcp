package com.star.ai.test.aimcp.controller;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@RequestMapping("mcp")
@RestController
public class McpTestController {

    @Value("${gdmcpKey:111}")
    private String gdMcpKey;

    @GetMapping
    public Flux<Map<String, Object>> test(@RequestParam String message) {
        HttpClientSseClientTransport build = HttpClientSseClientTransport
                .builder("https://mcp.amap.com")
                .sseEndpoint("/sse?key=" + gdMcpKey)
                .build();

        var client = McpClient.async(build)
                .initializationTimeout(Duration.ofSeconds(20))
                .build();

        Flux<Map<String, Object>> map = Mono.fromCallable(() -> {
                    client.initialize().block();
                    return client;
                })
                .flatMapMany(c -> c.callTool(
                        new McpSchema.CallToolRequest("maps_weather", Map.of("city", "杭州"))
                ))
                .map(resulr -> {
                    return resulr.content().stream().findFirst().map(c -> {
                        String type = c.type();
                        return Map.of("type", type, "content", c);
                    }).get();
                });


        return map;
    }


}
