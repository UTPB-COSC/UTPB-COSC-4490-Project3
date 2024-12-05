package src;

import java.net.*;
import java.io.*;

public class GameServer {
    private static final int PORT = 12345;
    private DatagramSocket socket;
    private byte[] buffer = new byte[1024];

    public GameServer() throws SocketException {
        socket = new DatagramSocket(PORT);
        System.out.println("Server started on port " + PORT);
    }

    public void start() {
        while (true) {
            try {
                // Receive data from client
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Process the received data (game state update from client)
                String receivedData = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received from client: " + receivedData);

                // Respond back to client with game state (send to all connected clients)
                String response = "Game State: " + receivedData;  // Here, you'd send updated game state
                DatagramPacket responsePacket = new DatagramPacket(response.getBytes(),
                        response.length(), packet.getAddress(), packet.getPort());
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
