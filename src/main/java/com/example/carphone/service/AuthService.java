package com.example.carphone.service;

import com.example.carphone.config.AppProperties;
import com.example.carphone.dto.AuthDtos.LoginResponse;
import com.example.carphone.dto.AuthDtos.WechatLoginRequest;
import com.example.carphone.model.Owner;
import com.example.carphone.repository.InMemoryStore;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final InMemoryStore store;
    private final AppProperties properties;
    private final RestClient restClient;

    public AuthService(InMemoryStore store, AppProperties properties, RestClient.Builder builder) {
        this.store = store;
        this.properties = properties;
        this.restClient = builder.build();
    }

    public LoginResponse login(WechatLoginRequest request) {
        String openid = resolveOpenid(request.code());
        Owner owner = store.findOwnerByOpenid(openid)
                .orElseGet(() -> store.saveOwner(new Owner(
                        UUID.randomUUID().toString(),
                        openid,
                        request.nickname() == null ? "" : request.nickname(),
                        Instant.now()
                )));
        String token = UUID.randomUUID().toString().replace("-", "");
        store.saveToken(token, owner.id());
        return new LoginResponse(token, owner.id(), owner.openid());
    }

    public Owner requireOwner(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("请先登录");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        return store.findOwnerByToken(token).orElseThrow(() -> new UnauthorizedException("登录已失效"));
    }

    private String resolveOpenid(String code) {
        if (!properties.getWechat().enabled()) {
            return "demo-openid-" + code;
        }

        Map<?, ?> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.weixin.qq.com")
                        .path("/sns/jscode2session")
                        .queryParam("appid", properties.getWechat().getAppId())
                        .queryParam("secret", properties.getWechat().getAppSecret())
                        .queryParam("js_code", code)
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .retrieve()
                .body(Map.class);
        Object openid = response == null ? null : response.get("openid");
        if (openid == null || openid.toString().isBlank()) {
            throw new UnauthorizedException("微信登录失败");
        }
        return openid.toString();
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}
