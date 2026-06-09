package com.example.carphone.service;

import com.example.carphone.config.AppProperties;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class QrCodeService {
    private final AppProperties properties;
    private final RestClient restClient;

    public QrCodeService(AppProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder.build();
    }

    public byte[] createCodeForVehicle(String vehicleId) {
        if (properties.getWechat().enabled()) {
            return createWechatMiniProgramCode(vehicleId);
        }
        return createPlainQrCode(publicMoveCarUrl(vehicleId));
    }

    public String publicMoveCarUrl(String vehicleId) {
        return properties.getPublicBaseUrl().replaceAll("/+$", "") + "/move-car?id=" + vehicleId;
    }

    private byte[] createWechatMiniProgramCode(String vehicleId) {
        String token = fetchWechatAccessToken();
        return restClient.post()
                .uri("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token={token}", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "scene", "id=" + vehicleId,
                        "page", "pages/call/index",
                        "check_path", false,
                        "width", 430
                ))
                .retrieve()
                .body(byte[].class);
    }

    private String fetchWechatAccessToken() {
        Map<?, ?> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.weixin.qq.com")
                        .path("/cgi-bin/token")
                        .queryParam("grant_type", "client_credential")
                        .queryParam("appid", properties.getWechat().getAppId())
                        .queryParam("secret", properties.getWechat().getAppSecret())
                        .build())
                .retrieve()
                .body(Map.class);
        Object accessToken = response == null ? null : response.get("access_token");
        if (accessToken == null || accessToken.toString().isBlank()) {
            throw new IllegalStateException("获取微信 access_token 失败");
        }
        return accessToken.toString();
    }

    private byte[] createPlainQrCode(String content) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    430,
                    430,
                    Map.of(EncodeHintType.CHARACTER_SET, "UTF-8", EncodeHintType.MARGIN, 1)
            );
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("生成二维码失败", ex);
        }
    }
}
