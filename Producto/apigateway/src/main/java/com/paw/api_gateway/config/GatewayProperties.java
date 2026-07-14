package com.paw.api_gateway.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paw.gateway")
public class GatewayProperties {

    private Cors cors = new Cors();
    private Services services = new Services();

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    public static class Cors {

            private List<String> allowedOrigins = new ArrayList<>(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:8080",
                "http://127.0.0.1:8080"
        ));

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            if (allowedOrigins == null) {
                this.allowedOrigins = new ArrayList<>();
                return;
            }
            this.allowedOrigins = allowedOrigins.stream()
                    .map(String::trim)
                    .filter(origin -> !origin.isBlank())
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public static class Services {

        private String usersUrl = "http://localhost:8081";
        private String campaignsUrl = "http://localhost:8082";
        private String donationsUrl = "http://localhost:8083";
        private String adoptionsUrl = "http://localhost:8084";
        private String feedUrl = "http://localhost:8085";
        private String notificationsUrl = "http://localhost:8086";

        public String getUsersUrl() {
            return usersUrl;
        }

        public void setUsersUrl(String usersUrl) {
            this.usersUrl = usersUrl;
        }

        public String getCampaignsUrl() {
            return campaignsUrl;
        }

        public void setCampaignsUrl(String campaignsUrl) {
            this.campaignsUrl = campaignsUrl;
        }

        public String getDonationsUrl() {
            return donationsUrl;
        }

        public void setDonationsUrl(String donationsUrl) {
            this.donationsUrl = donationsUrl;
        }

        public String getAdoptionsUrl() {
            return adoptionsUrl;
        }

        public void setAdoptionsUrl(String adoptionsUrl) {
            this.adoptionsUrl = adoptionsUrl;
        }

        public String getFeedUrl() {
            return feedUrl;
        }

        public void setFeedUrl(String feedUrl) {
            this.feedUrl = feedUrl;
        }

        public String getNotificationsUrl() {
            return notificationsUrl;
        }

        public void setNotificationsUrl(String notificationsUrl) {
            this.notificationsUrl = notificationsUrl;
        }
    }
}
