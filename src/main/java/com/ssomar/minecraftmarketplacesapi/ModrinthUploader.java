package com.ssomar.minecraftmarketplacesapi;

import java.io.*;
import java.net.http.*;
import java.net.URI;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.json.*;

public class ModrinthUploader {
    private static final String API_URL = "https://api.modrinth.com/v2/version";
    private final String token;
    
    public ModrinthUploader(String token) {
        this.token = token;
    }

    @Nullable
    public String getLastVersionOfDepPosted(String depId) {
        String URL = "https://api.modrinth.com/v2/project/"+depId+"/version";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofMinutes(5))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMinutes(5))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("DEPENDENCY Response Headers: " + response.headers());
            System.out.println("DEPENDENCY Response Body: " + response.body());
            JSONArray json = new JSONArray(response.body());
            return json.getJSONObject(0).getString("id");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void uploadVersion(String projectId, String filePath, String version, String changelog, List<String> gameVersions, List<String> dependencies)  {

        try {
            JSONObject metadata = new JSONObject();
            metadata.put("project_id", projectId);
            metadata.put("version_number", version);
            metadata.put("name", version);
            metadata.put("status", "listed");
            metadata.put("game_versions", new JSONArray().putAll(gameVersions));
            metadata.put("version_type", "release");
            metadata.put("loaders", new JSONArray().putAll(Arrays.asList("paper", "bukkit", "spigot", "purpur", "folia")));
            metadata.put("file_parts", new JSONArray().put("SCore.jar"));
            List<JSONObject> dependenciesList = new ArrayList<>();
            for (String dependency : dependencies) {
                JSONObject dep = new JSONObject();
                dep.put("project_id", dependency);
                dep.put("dependency_type", "required");
                String lastVersion = getLastVersionOfDepPosted(dependency);
                System.out.println("Last version of " + dependency + ": " + lastVersion);
                if (lastVersion != null) {
                    dep.put("version_id", lastVersion);
                }
                dependenciesList.add(dep);
            }
            if (!dependenciesList.isEmpty()) {
                metadata.put("dependencies", new JSONArray(dependenciesList));
            }
            else {
                metadata.put("dependencies", new JSONArray());
            }
            metadata.put("featured", true);
            metadata.put("changelog", changelog);

            String boundary = "---" + System.currentTimeMillis();
            ByteArrayOutputStream requestBody = new ByteArrayOutputStream();

            requestBody.write(("--" + boundary + "\r\n").getBytes());
            requestBody.write("Content-Disposition: form-data; name=\"data\"\r\n".getBytes());
            requestBody.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".getBytes());
            requestBody.write((metadata.toString() + "\r\n").getBytes());

            System.out.println("FilePath: " + filePath);

            File pluginFile = new File(filePath);
            byte[] fileBytes = Files.readAllBytes(pluginFile.toPath());
            requestBody.write(("--" + boundary + "\r\n").getBytes());
            requestBody.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + pluginFile.getName() + "\"\r\n").getBytes());
            requestBody.write("Content-Type: application/java-archive\r\n\r\n".getBytes());
            requestBody.write(fileBytes);
            requestBody.write(("\r\n--" + boundary + "--").getBytes());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
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