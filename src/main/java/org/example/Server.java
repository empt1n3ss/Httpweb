package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    private final int port;
    private final List<String> validPaths;
    private final ThreadPoolExecutor threadPool;
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    public Server(int port, List<String> validPaths) {
        this.port = port;
        this.validPaths = validPaths;
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(64);
    }
    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }
    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            System.out.println("Server on port: " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                connectionHandle(socket);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void connectionHandle(Socket socket) {
        threadPool.execute(new ConnectionHandler(socket, validPaths, handlers));
    }
}
