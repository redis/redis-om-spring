package com.redis.om.spring.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
  public static boolean isOllamaRunning() {
    try {
      URL url = new URL("http://localhost:11434");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.connect();

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.readLine();
        reader.close();
        return response != null && response.contains("Ollama is running");
      }
    } catch (IOException e) {
      // Handle the exception if needed
    }
    return false;
  }
}
