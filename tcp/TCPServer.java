package tcp;

import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) {
        int port = 12345; // Port number the server will listen on
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            //Network socket is created and binded to the port 
            System.out.println("Server is listening on port " + port);

            while (true) {
                // Accept a connection from a client
                // (3-way handshake) is done here
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                // Handle client in a separate thread
                new ClientHandler(socket).start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true)
        ) {
            String text;

            while ((text = reader.readLine()) != null) {
                System.out.println("Received: " + text);

                // Echo the message back to the client
                writer.println("Server: " + text);

                // Exit loop if client sends "bye"
                if ("bye".equalsIgnoreCase(text)) {
                    System.out.println("Client disconnected");
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
