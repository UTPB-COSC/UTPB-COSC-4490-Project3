package src;

import java.net.*;
import java.io.*;

public class GameServer {
    private static final int PORT = 12345;
    private DatagramSocket socket;
    private byte[] buffer = new byte[1024];

    // Keep track of clients
    private InetAddress clientAddress;
    private int clientPort;

    public GameServer() throws SocketException {
        socket = new DatagramSocket(PORT);
        System.out.println("Server started on port " + PORT);
    }

    public void start() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String receivedData = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received from client: " + receivedData);

                // Update client address and port for sending the response
                clientAddress = packet.getAddress();
                clientPort = packet.getPort();

                // Send a simple acknowledgment or an updated game state
                String response = "Game State Updated: " + receivedData;
                DatagramPacket responsePacket = new DatagramPacket(response.getBytes(),
                        response.length(), clientAddress, clientPort);
                socket.send(responsePacket);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            GameServer server = new GameServer();
            server.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
