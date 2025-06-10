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





}
