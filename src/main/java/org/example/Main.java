package org.example;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(9999, List.of("/index.html", "/spring.svg", "/spring.png",
                "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html",
                "/events.html", "/events.js"));
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            System.out.println("GET /messages handler");
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: 13\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    "Hello, World!";
            responseStream.write(response.getBytes());
            responseStream.flush();
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {
            System.out.println("POST /messages handler");
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: 7\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    "Posted!";
            responseStream.write(response.getBytes());
            responseStream.flush();
        });

        server.listen(9999);
    }
}
