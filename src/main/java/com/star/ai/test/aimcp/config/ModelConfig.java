package com.star.ai.test.aimcp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfig {

    @Value("${deepseekKey:111}")
    private String deepseekKey;

    @Bean
    public ChatModel deepseekModel(){
        return  DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .apiKey(deepseekKey)
                        .baseUrl("https://api.deepseek.com/v1")
                        .build())
                .defaultOptions(DeepSeekChatOptions.builder().model("deepseek-chat").build())
                .build();
    }

    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel  deepseekModel){
        return ChatClient.builder(deepseekModel);
    }


}
