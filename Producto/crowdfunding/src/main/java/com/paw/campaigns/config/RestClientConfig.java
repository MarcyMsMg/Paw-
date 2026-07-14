package com.paw.campaigns.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient usersRestClient(@Value("${services.users.base-url}") String usersBaseUrl) {
        return RestClient.builder()
                .baseUrl(usersBaseUrl)
                .build();
    }

    @Bean
    public RestClient notificationsRestClient(@Value("${services.notifications.base-url}") String notificationsBaseUrl) {
        return RestClient.builder()
                .baseUrl(notificationsBaseUrl)
                .build();
    }
}