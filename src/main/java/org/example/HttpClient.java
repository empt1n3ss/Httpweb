package org.example;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

public class HttpClient {
    public static void main(String[] args) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://localhost:9999/messages?last=10");
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                System.out.println("GET Response Status: " + response.getCode());
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("GET Response Body: " + responseBody);
            }

            HttpPost httpPost = new HttpPost("http://localhost:9999/messages");
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                System.out.println("POST Response Status: " + response.getCode());
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("POST Response Body: " + responseBody);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}