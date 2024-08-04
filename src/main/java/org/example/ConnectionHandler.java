package org.example;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final List<String> validPaths;
    private final Map<String, Map<String, Handler>> handlers;

    public ConnectionHandler(Socket socket, List<String> validPaths, Map<String, Map<String, Handler>> handlers) {
        this.socket = socket;
        this.validPaths = validPaths;
        this.handlers = handlers;
    }

    @Override
    public void run() {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            Request request = Request.parse(in);

            Handler handler = handlers
                    .getOrDefault(request.getMethod(), Map.of())
                    .get(request.getPath());

            if (handler != null) {
                handler.handle(request, out);
            } else {
                handleFileRequest(request, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                System.out.println("Socket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFileRequest(Request request, BufferedOutputStream out) throws IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            System.out.println("Handling POST request");
            System.out.println("Form parameters:");
            for (Map.Entry<String, String> entry : request.getFormParams().entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            if (!request.getFileParams().isEmpty()) {
                System.out.println("Received files:");
                for (Map.Entry<String, byte[]> entry : request.getFileParams().entrySet()) {
                    System.out.println("File " + entry.getKey() + " with size " + entry.getValue().length + " bytes");
                }
            }
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: 7\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    "Posted!";
            out.write(response.getBytes());
            out.flush();
        } else {
            final var path = request.getPath();
            System.out.println("Handling file request for path: " + path);

            if (!validPaths.contains(path)) {
                send404(out);
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
            } else {
                final var length = Files.size(filePath);
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();
            }
        }
    }

    private void send404(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
        System.out.println("Sent 404 response");
    }

    private void sendResponse(BufferedOutputStream out, String status, String mimeType, byte[] content) throws IOException {
        out.write((status + "\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n").getBytes());
        out.write(content);
        out.flush();
        System.out.println("Sent response: " + status);
    }
}
