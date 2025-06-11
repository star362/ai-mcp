package com.star.ai.test.aimcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.star.ai.test.aimcp.enetity.McpToolsTest;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

        List<McpSchema.Content> content = result.content();
        Map<String, Object> stringObjectMap = content.stream().findFirst().map(c -> {
            String type = c.type();
            return Map.of("type", type, "content", c);
        }).get();
        System.out.println("返回的消息：" + stringObjectMap);
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



    @Test
    public void mcpaddTest() {

        HashMap<String, Map<String, String>> stringMapHashMap = jsonToMapTest();


    }

    public static void main(String[] args) {
        extracted();

    }

    private static void extracted() {
        HashMap<String, Map<String, String>> stringMapHashMap = jsonToMapTest();
        System.out.println(stringMapHashMap);

        Map.Entry<String, Map<String, String>> next = stringMapHashMap.entrySet().stream().iterator().next();

        // 从 Map 中获取原始 URL，建议使用 final 声明，防止后续意外修改
        final String originalUrl = next.getValue().get("url");

// 为最终结果声明变量，并提供有意义的名称
        String baseUrl;
        String ssePath;

// 我们只需要调用一次 indexOf，这样更高效
        final String sseMarker = "/sse";
        final int sseIndex = originalUrl.indexOf(sseMarker);

        if (sseIndex != -1) { // 如果找到了 "/sse" (indexOf返回-1表示未找到)
            // 1. 截取 "/sse" 之前的部分作为基本URL
            baseUrl = originalUrl.substring(0, sseIndex);
            // 2. 截取从 "/sse" 开始到末尾的部分作为SSE路径
            ssePath = originalUrl.substring(sseIndex);
        } else {
            // 如果 URL 中不包含 "/sse"，则baseUrl就是完整的原始URL
            baseUrl = originalUrl;
            // SSE 路径从其他配置中获取
            ssePath = next.getValue().get("sseEndpoint");
        }

        HttpClientSseClientTransport build = HttpClientSseClientTransport
                .builder(baseUrl)
                .sseEndpoint(ssePath)
                .build();

        var client = McpClient.async(build)
                .initializationTimeout(Duration.ofSeconds(20))
                .build();

        Mono<List<McpToolsTest>> map = Mono.using(() -> client,// 创建资源
                c -> {
                    // 使用资源
                    client.initialize().block();
                    return client.listTools(); // 返回要处理的 Mono
                },
                c -> client.closeGracefully()// 释放资源
        ).map(a -> {
            return a.tools().stream().map(t -> {
                return new McpToolsTest(t.name(), t.description(), t.inputSchema());
            }).toList();
        });

        List<McpToolsTest> block = map.block();
        System.out.println(block);
    }

    private static HashMap<String, Map<String, String>> jsonToMapTest() {
        var configJson = """
                {
                   "mcpServers": {
                     "amap-amap-sse": {
                       "url": "https://mcp.amap.com",
                       "sseEndpoint": "/sse?key=您在高德官网上申请的key"
                     }
                   }
                 }
                """;

        HashMap<String, Map<String, String>> stringMapHashMap = null;
        try {
            stringMapHashMap = handelJson(configJson);
            Map.Entry<String, Map<String, String>> next = stringMapHashMap.entrySet().iterator().next();
            String key = next.getKey();

            Map<String, String> value = next.getValue();
            String url = value.get("url");
            Map<String, String> url1 = new HashMap<>();
            url1.put("url", url);
            if(value.containsKey("sseEndpoint")){
                url1.put("sseEndpoint", value.get("sseEndpoint"));
            }
            stringMapHashMap.put(key, url1);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return stringMapHashMap;
    }


    /**
     * 处理json
     *
     * @param configJson
     * @throws JsonProcessingException
     */
    private static HashMap<String, Map<String, String>> handelJson(String configJson) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Map<String, String>> mcpServersMap = new HashMap<>();
        JsonNode jsonNode = mapper.readTree(configJson);
        JsonNode mcpServersNode = jsonNode.path("mcpServers");
        if (mcpServersNode.isObject()) {
            // 遍历mcpServers节点下的所有子节点
            Iterator<Map.Entry<String, JsonNode>> fields = mcpServersNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String serverName = entry.getKey();
                JsonNode serverNode = entry.getValue();

                // 为每个服务器创建一个map来存储其属性
                Map<String, String> serverAttributes = new HashMap<>();
                if (serverNode.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> serverFields = serverNode.fields();
                    while (serverFields.hasNext()) {
                        Map.Entry<String, JsonNode> serverEntry = serverFields.next();
                        String key = serverEntry.getKey();
                        String value = serverEntry.getValue().asText();
                        serverAttributes.put(key, value);
                    }
                }
                // 将服务器及其属性添加到mcpServersMap中
                mcpServersMap.put(serverName, serverAttributes);
            }
        }
        return mcpServersMap;
    }

}
