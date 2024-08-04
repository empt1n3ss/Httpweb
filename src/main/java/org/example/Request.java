package org.example;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final InputStream body;
    private final Map<String,String> queryParams = new HashMap<>();
    private final Map<String, String> formParams = new HashMap<>();
    private final Map<String, byte[]> fileParams = new HashMap<>();

    public Request(String method, String path, Map<String, String> headers, InputStream body, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.headers.putAll(headers);
        this.body = body;
        this.queryParams.putAll(queryParams);
        if ("POST".equalsIgnoreCase(method) && "multipart/form-data".equalsIgnoreCase(headers.get("Content-Type"))) {
            parseMultipartFormData();
        } else {
            parseFormData();
        }
    }

    public static Request parse(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Неверный запрос");
        }
        String[] parts = requestLine.split(" ");
        String method = parts[0];
        String fullPath = parts[1];

        String path;
        Map<String, String> queryParams;
        try {
            URIBuilder uriBuilder = new URIBuilder(fullPath);
            path = uriBuilder.getPath();
            List<NameValuePair> params = uriBuilder.getQueryParams();
            queryParams = params.stream().collect(Collectors.toMap(NameValuePair::getName,NameValuePair::getValue));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

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

        return new Request(method, path, headers, body, queryParams);
    }
    private void parseFormData() {
        if (body != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(body))) {
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        formParams.put(parts[0], parts[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseMultipartFormData() {
        // todo
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

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }
    public Map<String, String> getFormParams() {
        return formParams;
    }

    public Map<String, byte[]> getFileParams() {
        return fileParams;
    }
}