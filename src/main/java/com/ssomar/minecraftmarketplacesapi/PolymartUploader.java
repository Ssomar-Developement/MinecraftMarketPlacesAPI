package com.ssomar.minecraftmarketplacesapi;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolymartUploader {
    private static final String API_URL = "https://api.polymart.org/v1/postUpdate";
    private final String token;

    public PolymartUploader(String token) {
        this.token = token;
    }


    public void uploadVersion(String resourceId, String filePath, String version, String changelog)  {

        try {
            JSONObject metadata = new JSONObject();
            metadata.put("api_key", token);
            metadata.put("resource_id", resourceId);
            metadata.put("version", version);
            metadata.put("title", version);
            metadata.put("message", changelog);

            //String boundary = "---" + System.currentTimeMillis();
            ByteArrayOutputStream requestBody = new ByteArrayOutputStream();

            //requestBody.write(("--" + boundary + "\r\n").getBytes());
            //requestBody.write("Content-Disposition: form-data; name=\"data\"\r\n".getBytes());
            //requestBody.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".getBytes());
            requestBody.write((metadata.toString() + "\r\n").getBytes());

            System.out.println("FilePath: " + filePath);

           /* File pluginFile = new File(filePath);
            byte[] fileBytes = Files.readAllBytes(pluginFile.toPath());
            requestBody.write(("--" + boundary + "\r\n").getBytes());
            requestBody.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + pluginFile.getName() + "\"\r\n").getBytes());
            requestBody.write("Content-Type: application/java-archive\r\n\r\n".getBytes());
            requestBody.write(fileBytes);
            requestBody.write(("\r\n--" + boundary + "--").getBytes());*/

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    //.header("Authorization", "Bearer " + token)
                    .header("enctype", "multipart/form-data")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody.toByteArray()))
                    .timeout(Duration.ofMinutes(5))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMinutes(5))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Request body length: " + requestBody.size());
            System.out.println("Response Headers: " + response.headers());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() != 200) {
                throw new IOException("Upload failed: Status " + response.statusCode() + ", Body: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    // Example usage
    public static void main(String[] args) {

    }
}