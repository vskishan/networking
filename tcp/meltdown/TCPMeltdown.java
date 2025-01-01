package tcp.meltdown;

import java.io.*;
import java.net.*;

public class TCPMeltdown {

    public static void main(String[] args) throws Exception {
        // Start the App Server and VPN Server
        Thread appServer = new Thread(() -> startAppServer(8081));
        Thread vpnServer = new Thread(() -> startVPNServer(8082));
        appServer.start();
        vpnServer.start();

        Thread.sleep(1000); // Wait for servers to start

        // Start the App Client
        startClient();
    }

    // VPN Server (Middle Layer b/w App Client and Server, delays messages)
    public static void startVPNServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("VPN Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Got a new connection on the VPN Server");
                new Thread(() -> handleVPNConnection(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleVPNConnection(Socket clientSocket) {
        try (Socket targetSocket = new Socket("localhost", 8081); // Connect to App Server
                 InputStream clientIn = clientSocket.getInputStream(); OutputStream clientOut = clientSocket.getOutputStream(); 
                 InputStream targetIn = targetSocket.getInputStream(); OutputStream targetOut = targetSocket.getOutputStream()) {

            System.out.println("VPN Server forwarding data to App Server...");

            // Relays data with a delay
            new Thread(() -> relayDataWithDelay(clientIn, targetOut, "Client to App Server")).start();
            relayDataWithDelay(targetIn, clientOut, "App Server to Client");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void relayDataWithDelay(InputStream in, OutputStream out, String direction) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in)); 
             PrintWriter writer = new PrintWriter(out, true)) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(direction + ": " + line.trim());
                writer.println(line); // Write the line to the output stream
                Thread.sleep(1000);  // Simulate delay
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Application Server
    public static void startAppServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("App Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Got a new connection on the App Server");
                new Thread(() -> handleAppConnection(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleAppConnection(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String input;
            while ((input = in.readLine()) != null) {
                System.out.println("App Server received: " + input);
                out.println("Echo: " + input); // Echo back to the VPN server
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Client
    public static void startClient() {
        try (Socket vpnSocket = new Socket("localhost", 8082); 
             BufferedReader in = new BufferedReader(new InputStreamReader(vpnSocket.getInputStream())); 
             PrintWriter out = new PrintWriter(vpnSocket.getOutputStream(), true)) {

            System.out.println("Client connected to VPN Server");
            for (int i = 1; i <= 5; i++) {
                String message = "Hello " + i;
                out.println(message);
                System.out.println("Client sent: " + message);

                // Wait for response from VPN Server (App Server Echo)
                String response = in.readLine();
                if (response != null) {
                    System.out.println("Client received: " + response);
                } else {
                    System.out.println("No response received.");
                }
                Thread.sleep(200); // Simulate client activity
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
