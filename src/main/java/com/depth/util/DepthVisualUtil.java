package com.depth.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DepthVisualUtil {

    private static final String OUTPUT_DIR = "webapp/depthOutput";

    public static String generateDepthVisual(String inputPath, Integer nearThreshold, Integer farThreshold, Double maskOpacity) {
        try {
            File inputFile = new File(inputPath);
            BufferedImage originalImage = ImageIO.read(inputFile);

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            float[][] depthMap = computeDepthMap(originalImage);

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = result.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);

            float opacity = maskOpacity.floatValue();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int depthValue = (int) (depthMap[y][x] * 255);
                    Color maskColor;

                    if (depthValue <= nearThreshold) {
                        maskColor = new Color(255, 100, 100, (int) (opacity * 255));
                    } else if (depthValue <= farThreshold) {
                        maskColor = new Color(100, 255, 100, (int) (opacity * 255));
                    } else {
                        maskColor = new Color(100, 150, 255, (int) (opacity * 255));
                    }

                    int originalRGB = originalImage.getRGB(x, y);
                    Color originalColor = new Color(originalRGB, true);

                    int blendedRGB = blendColors(originalColor, maskColor);
                    result.setRGB(x, y, blendedRGB);
                }
            }

            g2d.dispose();

            File outputDir = new File(OUTPUT_DIR);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            String extension = inputPath.substring(inputPath.lastIndexOf('.') + 1);
            String outputFileName = UUID.randomUUID().toString().replace("-", "") + "_depth." + extension;
            String outputPath = OUTPUT_DIR + File.separator + outputFileName;

            File outputFile = new File(outputPath);
            ImageIO.write(result, extension.toUpperCase(), outputFile);

            return outputPath;
        } catch (IOException e) {
            throw new RuntimeException("生成景深可视化图片失败", e);
        }
    }

    private static float[][] computeDepthMap(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        float[][] depthMap = new float[height][width];
        float[][] edgeMagnitude = new float[height][width];

        float maxEdge = 0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int[] gradX = computeGradientX(image, x, y);
                int[] gradY = computeGradientY(image, x, y);

                float magX = (float) Math.sqrt(gradX[0] * gradX[0] + gradX[1] * gradX[1] + gradX[2] * gradX[2]);
                float magY = (float) Math.sqrt(gradY[0] * gradY[0] + gradY[1] * gradY[1] + gradY[2] * gradY[2]);

                edgeMagnitude[y][x] = (magX + magY) / 2;
                if (edgeMagnitude[y][x] > maxEdge) {
                    maxEdge = edgeMagnitude[y][x];
                }
            }
        }

        int blurRadius = 15;
        float[][] blurred = new float[height][width];

        for (int y = blurRadius; y < height - blurRadius; y++) {
            for (int x = blurRadius; x < width - blurRadius; x++) {
                float sum = 0;
                int count = 0;
                for (int dy = -blurRadius; dy <= blurRadius; dy++) {
                    for (int dx = -blurRadius; dx <= blurRadius; dx++) {
                        float dist = (float) Math.sqrt(dx * dx + dy * dy);
                        if (dist <= blurRadius) {
                            float weight = (float) Math.exp(-dist * dist / (2 * blurRadius * blurRadius));
                            sum += edgeMagnitude[y + dy][x + dx] * weight;
                            count++;
                        }
                    }
                }
                blurred[y][x] = sum / count;
            }
        }

        float maxBlurred = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (blurred[y][x] > maxBlurred) {
                    maxBlurred = blurred[y][x];
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float normalized = blurred[y][x] / maxBlurred;
                depthMap[y][x] = 1 - normalized;
            }
        }

        return depthMap;
    }

    private static int[] computeGradientX(BufferedImage image, int x, int y) {
        int rgb1 = image.getRGB(x - 1, y - 1);
        int rgb2 = image.getRGB(x + 1, y - 1);
        int rgb3 = image.getRGB(x - 1, y);
        int rgb4 = image.getRGB(x + 1, y);
        int rgb5 = image.getRGB(x - 1, y + 1);
        int rgb6 = image.getRGB(x + 1, y + 1);

        int r = (getRed(rgb2) + 2 * getRed(rgb4) + getRed(rgb6)) - (getRed(rgb1) + 2 * getRed(rgb3) + getRed(rgb5));
        int g = (getGreen(rgb2) + 2 * getGreen(rgb4) + getGreen(rgb6)) - (getGreen(rgb1) + 2 * getGreen(rgb3) + getGreen(rgb5));
        int b = (getBlue(rgb2) + 2 * getBlue(rgb4) + getBlue(rgb6)) - (getBlue(rgb1) + 2 * getBlue(rgb3) + getBlue(rgb5));

        return new int[]{r, g, b};
    }

    private static int[] computeGradientY(BufferedImage image, int x, int y) {
        int rgb1 = image.getRGB(x - 1, y - 1);
        int rgb2 = image.getRGB(x, y - 1);
        int rgb3 = image.getRGB(x + 1, y - 1);
        int rgb4 = image.getRGB(x - 1, y + 1);
        int rgb5 = image.getRGB(x, y + 1);
        int rgb6 = image.getRGB(x + 1, y + 1);

        int r = (getRed(rgb4) + 2 * getRed(rgb5) + getRed(rgb6)) - (getRed(rgb1) + 2 * getRed(rgb2) + getRed(rgb3));
        int g = (getGreen(rgb4) + 2 * getGreen(rgb5) + getGreen(rgb6)) - (getGreen(rgb1) + 2 * getGreen(rgb2) + getGreen(rgb3));
        int b = (getBlue(rgb4) + 2 * getBlue(rgb5) + getBlue(rgb6)) - (getBlue(rgb1) + 2 * getBlue(rgb2) + getBlue(rgb3));

        return new int[]{r, g, b};
    }

    private static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    private static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    private static int getBlue(int rgb) {
        return rgb & 0xFF;
    }

    private static int blendColors(Color original, Color mask) {
        float maskAlpha = mask.getAlpha() / 255.0f;
        int r = (int) (original.getRed() * (1 - maskAlpha) + mask.getRed() * maskAlpha);
        int g = (int) (original.getGreen() * (1 - maskAlpha) + mask.getGreen() * maskAlpha);
        int b = (int) (original.getBlue() * (1 - maskAlpha) + mask.getBlue() * maskAlpha);
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }
}