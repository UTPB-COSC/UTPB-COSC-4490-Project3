import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PirateBattleshipGame extends JFrame implements Runnable {

    private GameCanvas gameCanvas;
    private volatile boolean running = true; // Allows for a graceful exit from the game loop
    private GameClient client; // Networking client
    private Thread networkThread; // Separate thread for handling networking

    public PirateBattleshipGame() {
        setTitle("Pirate Battleship Game");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gameCanvas = new GameCanvas();
        add(gameCanvas);

        // Add key listener for movement and actions
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                gameCanvas.handleKeyPress(e.getKeyCode());

                // Example: Send player actions to the server
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    sendPlayerAction("MOVE,player1,UP");
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    sendPlayerAction("FIRE,player1");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                gameCanvas.handleKeyRelease(e.getKeyCode());
            }
        });

        setVisible(true);

        // Initialize networking
        try {
            client = new GameClient();
        } catch (Exception e) {
            System.err.println("Failed to initialize networking: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Start the networking thread
        networkThread = new Thread(this::networkHandler);
        networkThread.start();

        // Start the game loop in a new thread
        new Thread(this).start();
    }

    private void sendPlayerAction(String action) {
        try {
            client.send(action);
        } catch (Exception e) {
            System.err.println("Failed to send action: " + e.getMessage());
        }
    }

    private void networkHandler() {
        while (running) {
            try {
                // Receive game state updates from the server
                String gameState = client.receive();
                // Parse and update game canvas state
                gameCanvas.updateFromServer(gameState);
            } catch (Exception e) {
                System.err.println("Networking error: " + e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        final int targetFPS = 60;
        final long optimalTime = 1_000_000_000 / targetFPS; // Optimal time per frame in nanoseconds

        while (running) {
            long startTime = System.nanoTime();

            // Update game state and repaint the canvas
            gameCanvas.updateGame();
            gameCanvas.repaint();

            // Calculate elapsed time and sleep to maintain target frame rate
            long elapsedTime = System.nanoTime() - startTime;
            long sleepTime = (optimalTime - elapsedTime) / 1_000_000; // Convert to milliseconds

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        new PirateBattleshipGame();
    }
}
