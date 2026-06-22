package com.example.carphone.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class ImageComposerService {

    public byte[] composeSticker(String baseUrl, String ownerName, String plateNo,
                                  String maskedPhone, String comfortMessage,
                                  String qrCodeBase64, List<Map<String, Object>> layers,
                                  Integer width, Integer height) throws Exception {
        // 加载背景图
        BufferedImage background = loadImage(baseUrl);
        if (background == null) {
            background = createDefaultBackground(width, height);
        }

        int w = background.getWidth();
        int h = background.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 绘制背景
        g2.drawImage(background, 0, 0, null);

        // 绘制二维码
        if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
            byte[] qrBytes = java.util.Base64.getDecoder().decode(qrCodeBase64);
            BufferedImage qr = ImageIO.read(new ByteArrayInputStream(qrBytes));
            if (qr != null) {
                int qrSize = (int) (w * 0.28);
                int qrX = w - qrSize - (int) (w * 0.04);
                int qrY = h - qrSize - (int) (h * 0.04);
                g2.drawImage(qr, qrX, qrY, qrSize, qrSize, null);

                // 二维码白底
                g2.setColor(Color.WHITE);
                g2.fillRect(qrX - 6, qrY - 6, qrSize + 12, qrSize + 12);
                g2.setColor(new Color(102, 126, 234));
                g2.fillRect(qrX - 6, qrY - 6, qrSize + 12, 4);
            }
        }

        // 绘制车主信息文字
        drawInfoPanel(g2, w, h, ownerName, plateNo, maskedPhone, comfortMessage);

        // 绘制自定义图层元素
        if (layers != null) {
            for (Map<String, Object> layer : layers) {
                drawLayer(g2, layer, w, h);
            }
        }

        g2.dispose();

        // 转成 JPEG 字节
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(result, "JPEG", out);
        return out.toByteArray();
    }

    public byte[] composeStickerFromEditor(byte[] backgroundImage, String ownerName, String plateNo,
                                            String maskedPhone, String qrCodeBase64,
                                            List<Map<String, Object>> layers,
                                            Integer width, Integer height) throws Exception {
        BufferedImage background = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2b = background.createGraphics();
        ByteArrayInputStream bis = new ByteArrayInputStream(backgroundImage);
        g2b.drawImage(ImageIO.read(bis), 0, 0, null);
        bis.close();
        g2b.dispose();

        int w = background.getWidth();
        int h = background.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.drawImage(background, 0, 0, null);

        if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
            byte[] qrBytes = java.util.Base64.getDecoder().decode(qrCodeBase64);
            BufferedImage qr = ImageIO.read(new ByteArrayInputStream(qrBytes));
            if (qr != null) {
                int qrSize = (int) (w * 0.28);
                int qrX = w - qrSize - (int) (w * 0.04);
                int qrY = h - qrSize - (int) (h * 0.04);
                g2.drawImage(qr, qrX, qrY, qrSize, qrSize, null);
                g2.setColor(Color.WHITE);
                g2.fillRect(qrX - 6, qrY - 6, qrSize + 12, qrSize + 12);
                g2.setColor(new Color(102, 126, 234));
                g2.fillRect(qrX - 6, qrY - 6, qrSize + 12, 4);
            }
        }

        drawInfoPanel(g2, w, h, ownerName, plateNo, maskedPhone, "");

        if (layers != null) {
            for (Map<String, Object> layer : layers) {
                String type = (String) layer.get("type");
                if ("text".equals(type) || "rect".equals(type) || "circle".equals(type) || "image".equals(type)) {
                    drawLayer(g2, layer, w, h);
                }
            }
        }

        g2.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(result, "JPEG", out);
        return out.toByteArray();
    }

    private void drawInfoPanel(Graphics2D g2, int w, int h,
                                String ownerName, String plateNo,
                                String maskedPhone, String comfortMessage) {
        int padding = (int) (w * 0.04);
        int panelWidth = w - padding * 2;
        int panelX = padding;

        g2.setFont(new Font("Microsoft YaHei", Font.BOLD, (int) (w * 0.045)));
        int plateHeight = g2.getFontMetrics().getHeight();

        int yOffset = (int) (h * 0.08);

        // 半透明深色背景面板
        g2.setColor(new Color(0, 0, 0, 140));
        roundRect(g2, panelX, yOffset, panelWidth, (int) (h * 0.32), 16);

        int textX = panelX + (int) (w * 0.05);
        int textY = yOffset + (int) (h * 0.06);

        // 车主名
        g2.setColor(new Color(180, 200, 255));
        g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, (int) (w * 0.03)));
        g2.drawString(ownerName != null ? ownerName : "", textX, textY);

        // 车牌号
        textY += plateHeight + (int) (h * 0.03);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Microsoft YaHei", Font.BOLD, (int) (w * 0.06)));
        g2.drawString(plateNo != null ? plateNo : "", textX, textY);

        // 电话
        textY += plateHeight + (int) (h * 0.02);
        g2.setColor(new Color(200, 210, 240));
        g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, (int) (w * 0.032)));
        g2.drawString("电话：" + (maskedPhone != null ? maskedPhone : ""), textX, textY);

        // 问候语
        textY += plateHeight + (int) (h * 0.03);
        g2.setColor(new Color(160, 175, 220));
        g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, (int) (w * 0.022)));
        if (comfortMessage != null && !comfortMessage.isEmpty()) {
            wrapText(g2, comfortMessage, textX, textY, panelWidth - (int) (w * 0.05), (int) (w * 0.026));
        }
    }

    private void wrapText(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight) {
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();
        String line = "";
        int currentY = y;

        for (int i = 0; i < text.length(); i++) {
            String charStr = text.substring(i, i + 1);
            String testLine = line + charStr;
            int lineWidth = (int) font.getStringBounds(testLine, frc).getWidth();

            if (lineWidth < maxWidth && i < text.length() - 1) {
                line = testLine;
            } else {
                g2.drawString(line, x, currentY);
                currentY += lineHeight;
                line = charStr;
            }
        }
        if (!line.isEmpty()) {
            g2.drawString(line, x, currentY);
        }
    }

    private void drawLayer(Graphics2D g2, Map<String, Object> layer, int w, int h) {
        String type = (String) layer.get("type");
        if ("image".equals(type)) {
            try {
                String src = (String) layer.get("src");
                if (src != null && !src.isEmpty()) {
                    byte[] bytes = java.util.Base64.getDecoder().decode(src);
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                    if (img != null) {
                        int x = (int) ((double) ((Number) layer.get("x")).doubleValue() / 1000.0 * w);
                        int y = (int) ((double) ((Number) layer.get("y")).doubleValue() / 1000.0 * h);
                        int imgW = (int) ((double) ((Number) layer.get("width")).doubleValue() / 1000.0 * w);
                        int imgH = (int) ((double) ((Number) layer.get("height")).doubleValue() / 1000.0 * h);
                        double rotation = ((Number) layer.get("rotation")).doubleValue();
                        AffineTransform at = AffineTransform.getTranslateInstance(x + imgW / 2.0, y + imgH / 2.0);
                        at.rotate(Math.toRadians(rotation));
                        at.scale((double) imgW / img.getWidth(), (double) imgH / img.getHeight());
                        g2.drawImage(img, at, null);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private BufferedImage loadImage(String url) {
        try {
            if (url == null || url.isEmpty()) return null;
            URI uri = new URI(url);
            if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
                java.io.InputStream is = uri.toURL().openStream();
                try {
                    return ImageIO.read(is);
                } finally {
                    is.close();
                }
            } else if ("data".equals(uri.getScheme())) {
                String data = url.substring(url.indexOf(",") + 1);
                byte[] bytes = java.util.Base64.getDecoder().decode(data);
                return ImageIO.read(new ByteArrayInputStream(bytes));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private BufferedImage createDefaultBackground(Integer width, Integer height) {
        int w = width != null ? width : 800;
        int h = height != null ? height : 1200;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 渐变背景
        GradientPaint gp = new GradientPaint(0, 0, new Color(102, 126, 234), w, h, new Color(118, 75, 162));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
        return img;
    }

    private void roundRect(Graphics2D g2, int x, int y, int w, int h, int r) {
        g2.fillRoundRect(x, y, w, h, r, r);
    }


    public byte[] composeStickerWithQrOverlays(byte[] userImage, String ownerName, String plateNo,
                                                 String maskedPhone, String comfortMessage,
                                                 String qrCodeBase64) throws Exception {
        BufferedImage userBg = ImageIO.read(new ByteArrayInputStream(userImage));
        int w = userBg.getWidth();
        int h = userBg.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.drawImage(userBg, 0, 0, null);

        if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
            byte[] qrBytes = java.util.Base64.getDecoder().decode(qrCodeBase64);
            BufferedImage qr = ImageIO.read(new ByteArrayInputStream(qrBytes));
            if (qr != null) {
                int qrSize = (int) (w * 0.25);
                int qrX = w - qrSize - (int) (w * 0.03);
                int qrY = h - qrSize - (int) (h * 0.03);
                g2.setColor(Color.WHITE);
                g2.fillRect(qrX - 4, qrY - 4, qrSize + 8, qrSize + 8);
                g2.drawImage(qr, qrX, qrY, qrSize, qrSize, null);
            }
        }

        int padding = (int) (w * 0.03);
        int panelWidth = (int) (w * 0.55);
        int panelHeight = (int) (h * 0.22);
        g2.setColor(new Color(0, 0, 0, 160));
        roundRect(g2, padding, padding, panelWidth, panelHeight, 12);

        int textX = padding + (int) (w * 0.03);
        int textY = padding + (int) (h * 0.06);

        g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, (int) (w * 0.025)));
        g2.setColor(new Color(180, 200, 255));
        if (ownerName != null && !ownerName.isEmpty()) {
            g2.drawString(ownerName, textX, textY);
            textY += (int) (h * 0.04);
        }

        g2.setFont(new Font("Microsoft YaHei", Font.BOLD, (int) (w * 0.05)));
        g2.setColor(Color.WHITE);
        if (plateNo != null && !plateNo.isEmpty()) {
            g2.drawString(plateNo, textX, textY);
            textY += (int) (h * 0.04);
        }

        g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, (int) (w * 0.025)));
        g2.setColor(new Color(200, 210, 240));
        if (maskedPhone != null && !maskedPhone.isEmpty()) {
            g2.drawString("电话：" + maskedPhone, textX, textY);
            textY += (int) (h * 0.03);
        }

        if (comfortMessage != null && !comfortMessage.isEmpty()) {
            g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, (int) (w * 0.02)));
            g2.setColor(new Color(160, 175, 220));
            wrapText(g2, comfortMessage, textX, textY, panelWidth - (int) (w * 0.06), (int) (w * 0.022));
        }

        g2.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(result, "JPEG", out);
        return out.toByteArray();
    }
}
