package com.depth.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DepthApiClient {

    private static final String API_BASE_URL = "http://localhost:5001";

    public static byte[] predictDepthImage(String imagePath) throws IOException {
        URL url = new URL(API_BASE_URL + "/predict");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream os = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);

        writer.println("--" + boundary);
        writer.println("Content-Disposition: form-data; name=\"image\"; filename=\"" + new File(imagePath).getName() + "\"");
        writer.println("Content-Type: image/jpeg");
        writer.println();
        writer.flush();

        Path path = Paths.get(imagePath);
        byte[] imageBytes = Files.readAllBytes(path);
        os.write(imageBytes);
        os.flush();

        writer.println();
        writer.println("--" + boundary + "--");
        writer.flush();

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            reader.close();
            throw new IOException("API request failed: " + responseCode + " - " + error);
        }

        InputStream is = conn.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();
        conn.disconnect();

        return baos.toByteArray();
    }

    public static String predictDepthData(String imagePath) throws IOException {
        URL url = new URL(API_BASE_URL + "/predict_data");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream os = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);

        writer.println("--" + boundary);
        writer.println("Content-Disposition: form-data; name=\"image\"; filename=\"" + new File(imagePath).getName() + "\"");
        writer.println("Content-Type: image/jpeg");
        writer.println();
        writer.flush();

        Path path = Paths.get(imagePath);
        byte[] imageBytes = Files.readAllBytes(path);
        os.write(imageBytes);
        os.flush();

        writer.println();
        writer.println("--" + boundary + "--");
        writer.flush();

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            reader.close();
            throw new IOException("API request failed: " + responseCode + " - " + error);
        }

        InputStream is = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        is.close();
        os.close();
        conn.disconnect();

        return response.toString();
    }

    public static boolean isServiceAvailable() {
        try {
            URL url = new URL(API_BASE_URL + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        }
    }
}