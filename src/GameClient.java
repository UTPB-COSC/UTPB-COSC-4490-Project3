package src;

import java.net.*;
import java.io.*;

public class GameClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";  // Server address
    private static final int PORT = 12345;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private byte[] buffer;

    public GameClient() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        serverAddress = InetAddress.getByName(SERVER_ADDRESS);
    }

    // Send game state to the server
    public void sendGameState(Player player, Bomb bomb, Enemy enemy) {
        try {
            // Prepare the game state string
            StringBuilder gameState = new StringBuilder();

            // Add player location
            gameState.append("Player: ").append(player.x).append(",").append(player.y).append(" ");

            // Add bomb location
            gameState.append("Bomb: ").append(bomb.x).append(",").append(bomb.y).append(" ");

            // Add surrounding tiles (above, below, left, right of bomb)
            gameState.append("Tiles: ");
            gameState.append("Above: ").append(getTile(bomb.x, bomb.y - 1)).append(" ");
            gameState.append("Below: ").append(getTile(bomb.x, bomb.y + 1)).append(" ");
            gameState.append("Left: ").append(getTile(bomb.x - 1, bomb.y)).append(" ");
            gameState.append("Right: ").append(getTile(bomb.x + 1, bomb.y)).append(" ");

            // Add enemy location
            gameState.append("Enemy: ").append(enemy.x).append(",").append(enemy.y);

            // Send the game state to the server
            byte[] data = gameState.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, PORT);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Error sending game state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getTile(int x, int y) {
        // For simplicity, assume the map is accessible through the game object
        if (x >= 0 && y >= 0 && x < Game.map[0].length && y < Game.map.length) {
            return Game.map[y][x].type.toString();  // Return tile type (e.g., EMPTY, WALL, BLOCK)
        }
        return "OUT_OF_BOUNDS";  // If out of bounds, return a placeholder
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public static void main(String[] args) {
        try {
            GameClient client = new GameClient();
            System.out.println("Client started. Sending example game state...");

            // Example of sending data
            for (int i = 0; i < 10; i++) {
                Player player = new Player(5, 5, new Game());  // Example player at (5, 5)
                Bomb bomb = new Bomb(6, 6, new Game());  // Example bomb at (6, 6)
                Enemy enemy = new Enemy(4, 4, new Game());  // Example enemy at (4, 4)

                client.sendGameState(player, bomb, enemy);
                Thread.sleep(1000);  // Simulate 1-second intervals between sending states
            }

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
