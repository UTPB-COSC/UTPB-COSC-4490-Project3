package src;

import java.net.*;
import java.io.*;

public class GameClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";  
    private static final int PORT = 12345;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private byte[] buffer;

    public GameClient() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        serverAddress = InetAddress.getByName(SERVER_ADDRESS);
    }

    public void sendGameState(String gameState) {
        try {
            byte[] data = gameState.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, PORT);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Error sending game state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String receiveGameState() {
        try {
            buffer = new byte[2048]; 
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            return new String(packet.getData(), 0, packet.getLength());
        } catch (IOException e) {
            System.err.println("Error receiving game state: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public static void main(String[] args) {
        try {
            GameClient client = new GameClient();
            System.out.println("Client started. Sending game state...");

        
            // Start a thread for receiving game states
            Thread receiverThread = new Thread(() -> {
                while (true) {
                    String gameState = client.receiveGameState();
                    if (gameState != null) {
                        System.out.println("Received game state: " + gameState);
                        
                    }
                }
            });
            receiverThread.start();

            for (int i = 0; i < 10; i++) {
                client.sendGameState("Player1 action " + i);
                Thread.sleep(1000); 
            }

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
