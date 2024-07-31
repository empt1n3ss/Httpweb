package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    private final int port;
    private final List<String> validPaths;
    private final ThreadPoolExecutor threadPool;
    public Server(int port, List<String> validPaths) {
        this.port = port;
        this.validPaths = validPaths;
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(64);
    }
    public void start() {
        try (final var serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен");
            while (true) {
                try (final var socket = serverSocket.accept()) {
                    connectionHandle(socket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void connectionHandle(Socket socket) {
        threadPool.execute(new ConnectionHandler(socket, validPaths));
    }
}
