package org.example;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final InputStream body;

    public Request(String method, String path, Map<String, String> headers, InputStream body) {
        this.method = method;
        this.path = path;
        this.headers.putAll(headers);
        this.body = body;
    }

    public static Request parse(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Неверный запрос");
        }
        String[] parts = requestLine.split(" ");
        String method = parts[0];
        String path = parts[1];

        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            String[] headerParts = line.split(": ");
            headers.put(headerParts[0], headerParts[1]);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        while (in.ready()) {
            bodyBuilder.append((char) in.read());
        }
        InputStream body = new ByteArrayInputStream(bodyBuilder.toString().getBytes());

        return new Request(method, path, headers, body);
    }
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return body;
    }
}

