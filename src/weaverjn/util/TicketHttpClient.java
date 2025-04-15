package weaverjn.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TicketHttpClient {

    // 默认的JSON请求体  
    private static final String DEFAULT_JSON_BODY = "[{" +
            "\"ticketType\": 0," + // 火车票  
            "\"files\": []" +
            "},{" +
            "\"ticketType\": 1," + // 飞机票  
            "\"files\": []" +
            "}]";

    /**
     * 发送带有票务数据的HTTP POST请求  
     *
     * @param endpoint 请求的URL地址  
     * @return 服务器的响应
     * @throws IOException 如果发生I/O错误  
     */
    public String sendTicketData(String endpoint) throws IOException {
        return sendTicketData(endpoint, DEFAULT_JSON_BODY);
    }

    /**
     * 发送带有自定义票务数据的HTTP POST请求  
     *
     * @param endpoint 请求的URL地址  
     * @param jsonBody 要发送的JSON请求体  
     * @return 服务器的响应
     * @throws IOException 如果发生I/O错误  
     */
    public String sendTicketData(String endpoint, String jsonBody) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置HTTP请求  
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        // 将JSON请求体写入请求  
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取响应  
        StringBuilder response = new StringBuilder();
        int responseCode = connection.getResponseCode();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 200 && responseCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream(),
                        StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        connection.disconnect();
        return response.toString();
    }

    /**
     * GET请求
     */
    public String sendGetRequest(String url) throws Exception {
        java.net.URL obj = new java.net.URL(url);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(999999);
        connection.setRequestProperty("Content-Type", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            return null;
        }
    }
}  