package com.example.carphone.renderer;

import com.example.carphone.dto.VehicleDtos.VehicleResponse;
import com.example.carphone.service.QrCodeService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Service
public class StickerRenderer {

    private final QrCodeService qrCodeService;

    public StickerRenderer(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    public byte[] renderSticker(VehicleResponse vehicle, int width, int height) throws Exception {
        BufferedImage bg = generateGradientBackground(width, height);

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(bg, 0, 0, null);

            byte[] qrBytes = qrCodeService.createCodeForVehicle(vehicle.id());
            BufferedImage qrImage = ImageIO.read(new java.io.ByteArrayInputStream(qrBytes));

            int qrSize = Math.min(width / 2, 260);
            int qrX = (width - qrSize) / 2;
            int qrY = (height / 2 - qrSize / 2) - 20;

            drawGlowEffect(g2d, qrX - 5, qrY - 5, qrSize + 10, qrSize + 10);
            g2d.drawImage(qrImage, qrX, qrY, qrSize, qrSize, null);

            g2d.setColor(new Color(0, 255, 200, 180));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(qrX - 4, qrY - 4, qrSize + 8, qrSize + 8, 12, 12);

            String ownerName = vehicle.ownerName();
            g2d.setColor(new Color(200, 255, 255, 240));
            g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
            FontMetrics fm = g2d.getFontMetrics();
            int nameX = (width - fm.stringWidth(ownerName)) / 2;
            g2d.drawString(ownerName, nameX, qrY + qrSize + 50);

            String plateNo = vehicle.plateNo();
            if (plateNo != null && !plateNo.isEmpty()) {
                g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
                fm = g2d.getFontMetrics();
                int plateX = (width - fm.stringWidth("\u8f66\u724c: " + plateNo)) / 2;
                g2d.drawString("\u8f66\u724c: " + plateNo, plateX, qrY + qrSize + 75);
            }

            g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            fm = g2d.getFontMetrics();
            String phoneText = "\u7535\u8bdd: " + vehicle.maskedPhone();
            int phoneX = (width - fm.stringWidth(phoneText)) / 2;
            g2d.drawString(phoneText, phoneX, qrY + qrSize + 100);

            String comfort = vehicle.comfortMessage();
            if (comfort != null && !comfort.isEmpty()) {
                drawWrappedText(g2d, comfort, 20, qrY + qrSize + 130, width - 40, 14);
            }

            drawCornerAccents(g2d, width, height);
        } finally {
            g2d.dispose();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(canvas, "PNG", out);
        return out.toByteArray();
    }

    private void drawGlowEffect(Graphics2D g2d, int x, int y, int w, int h) {
        g2d.setColor(new Color(0, 255, 200, 30));
        g2d.fillRoundRect(x - 8, y - 8, w + 16, h + 16, 20, 20);
        g2d.setColor(new Color(0, 255, 200, 15));
        g2d.fillRoundRect(x - 16, y - 16, w + 32, h + 32, 28, 28);
    }

    private void drawCornerAccents(Graphics2D g2d, int w, int h) {
        g2d.setColor(new Color(0, 255, 200, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(20, 40, 20, 20);
        g2d.drawLine(20, 20, 40, 20);
        g2d.drawLine(w - 40, 20, w - 20, 20);
        g2d.drawLine(w - 20, 20, w - 20, 40);
        g2d.drawLine(20, h - 40, 20, h - 20);
        g2d.drawLine(20, h - 20, 40, h - 20);
        g2d.drawLine(w - 40, h - 20, w - 20, h - 20);
        g2d.drawLine(w - 20, h - 40, w - 20, h - 20);
    }

    private void drawWrappedText(Graphics2D g2d, String text, int x, int y, int maxWidth, int fontSize) {
        g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, fontSize));
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight() + 4;
        String[] chars = text.split("");
        StringBuilder line = new StringBuilder();
        int currentY = y;

        for (String c : chars) {
            StringBuilder testLine = new StringBuilder(line);
            testLine.append(c);
            if (fm.stringWidth(testLine.toString()) > maxWidth && line.length() > 0) {
                g2d.drawString(line.toString(), x, currentY);
                line = new StringBuilder(c);
                currentY += lineHeight;
            } else {
                line = testLine;
            }
        }
        if (line.length() > 0) {
            g2d.drawString(line.toString(), x, currentY);
        }
    }

    private BufferedImage generateGradientBackground(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(10, 15, 30),
                    0, height, new Color(20, 30, 50)
            );
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);

            g2d.setColor(new Color(0, 255, 200, 15));
            g2d.setStroke(new BasicStroke(1));
            for (int i = 0; i < width; i += 30) {
                g2d.drawLine(i, 0, i, height);
            }
            for (int i = 0; i < height; i += 30) {
                g2d.drawLine(0, i, width, i);
            }

            g2d.setColor(new Color(0, 255, 200, 25));
            drawHexagon(g2d, 60, 60, 25);
            drawHexagon(g2d, width - 60, 60, 25);
            drawHexagon(g2d, 60, height - 60, 25);
            drawHexagon(g2d, width - 60, height - 60, 25);

            g2d.setColor(new Color(0, 255, 200, 80));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(8, 8, width - 16, height - 16, 20, 20);
        } finally {
            g2d.dispose();
        }
        return img;
    }

    private void drawHexagon(Graphics2D g2d, int cx, int cy, int r) {
        java.awt.geom.Path2D.Double hex = new java.awt.geom.Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 30);
            double x = cx + r * Math.cos(angle);
            double y = cy + r * Math.sin(angle);
            if (i == 0) {
                hex.moveTo(x, y);
            } else {
                hex.lineTo(x, y);
            }
        }
        hex.closePath();
        g2d.draw(hex);
    }
}