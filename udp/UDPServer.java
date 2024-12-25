import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {
    public static void main(String[] args) {
        int port = 9876; // Port number to listen on

        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            System.out.println("UDP Server is running on port " + port);

            byte[] receiveBuffer = new byte[1024];

            while (true) {
                // Receive data packet
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Received: " + receivedData);

                // Get client address and port
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                // Prepare response
                String responseData = "Acknowledged: " + receivedData;
                byte[] responseBuffer = responseData.getBytes();

                // Send response to client
                DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length, clientAddress, clientPort);
                serverSocket.send(responsePacket);
                System.out.println("Response sent to client.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
