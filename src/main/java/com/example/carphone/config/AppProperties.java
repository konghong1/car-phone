package com.example.carphone.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String publicBaseUrl = "http://localhost:8080";
    private final Wechat wechat = new Wechat();

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public Wechat getWechat() {
        return wechat;
    }

    public static class Wechat {
        private String appId = "";
        private String appSecret = "";

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public boolean enabled() {
            return appId != null && !appId.isBlank() && appSecret != null && !appSecret.isBlank();
        }
    }
}
