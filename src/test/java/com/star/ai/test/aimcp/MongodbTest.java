package com.star.ai.test.aimcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.star.ai.test.aimcp.enetity.McpToolsTest;
import com.star.ai.test.aimcp.enetity.User;
import com.star.ai.test.aimcp.repository.UserRepository;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MongodbTest extends AiMcpApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void mongoTest() {

        boolean b = mongoTemplate.collectionExists("star_user");
        System.out.println(b);

    }

    @Test
    public void mongoTest2() {

        List<User> users = List.of(
                new User(null, "张三2", "18", "男"),
                new User(null, "李四", "19", "女"),
                new User(null, "王五", "20", "男")

        );
        userRepository.saveAll(users);


        System.out.println("=======");

    }

    @Test
    public void findTest() {
        List<User> alls = userRepository.findAll();
        alls.stream().forEach(System.out::println);
    }

    @Test
    public void updateTest() {
        userRepository.save(new User("6846e301f3f4845798cd8c5d", "张三", "28", "男"));
    }

    @Test
    public void queryTest() {
        userRepository.findById("6846e301f3f4845798cd8c5d").ifPresent(u -> {
            System.out.println(u);
        });
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
        // 初始化 SSE Client
        HttpClientSseClientTransport build = HttpClientSseClientTransport
                .builder(next.getValue().get("url"))
                .sseEndpoint(next.getValue().get("sseEndpoint"))
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
            String sseEndpoint = value.get("sseEndpoint");
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
