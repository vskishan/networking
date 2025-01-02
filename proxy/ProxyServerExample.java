package proxy;

import java.io.*;
import java.net.*;

public class ProxyServerExample {

    public static void main(String[] args) {
        Thread serverThread = new Thread(() -> runServer(8082));
        Thread proxyThread = new Thread(() -> runProxy(8081, 8082));
        Thread clientThread = new Thread(() -> runClient(8081));

        serverThread.start();
        proxyThread.start();
        clientThread.start();
    }

    // Server listens for requests and responds
    private static void runServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server running on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String message = in.readLine();
                System.out.println("Server received: " + message);
                out.println("Server response: " + message.toUpperCase());
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Proxy forwards requests to the server and relays responses
    private static void runProxy(int proxyPort, int serverPort) {
        try (ServerSocket proxySocket = new ServerSocket(proxyPort)) {
            System.out.println("Proxy running on port " + proxyPort);
            while (true) {
                Socket clientSocket = proxySocket.accept();
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);

                String request = clientIn.readLine();
                System.out.println("Proxy received: " + request);

                // Forward request to server
                try (Socket serverSocket = new Socket("localhost", serverPort)) {
                    PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);
                    BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

                    serverOut.println(request);
                    String response = serverIn.readLine();

                    // Relay response back to the client
                    clientOut.println(response);
                    System.out.println("Proxy forwarded response: " + response);
                }
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Client sends a request to the proxy
    private static void runClient(int proxyPort) {
        try (Socket proxySocket = new Socket("localhost", proxyPort)) {
            PrintWriter out = new PrintWriter(proxySocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(proxySocket.getInputStream()));

            String request = "Hello from Client!";
            out.println(request);
            System.out.println("Client sent: " + request);

            String response = in.readLine();
            System.out.println("Client received: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
