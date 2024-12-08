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
        setVisible(true);

        // Add key listener for movement and actions
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                gameCanvas.handleKeyPress(e.getKeyCode());
                
                String action = "";
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        action = "MOVE,player1,UP";
                        break;
                    case KeyEvent.VK_DOWN:
                        action = "MOVE,player1,DOWN";
                        break;
                    case KeyEvent.VK_LEFT:
                        action = "MOVE,player1,LEFT";
                        break;
                    case KeyEvent.VK_RIGHT:
                        action = "MOVE,player1,RIGHT";
                        break;
                    case KeyEvent.VK_SPACE:
                        action = "FIRE,player1";
                        break;
                }
                if (!action.isEmpty()) {
                    System.out.println("Sending action: " + action);  // Debugging log
                    sendPlayerAction(action);
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                gameCanvas.handleKeyRelease(e.getKeyCode());
            }
        });

        // Initialize networking after GUI is created
        initializeNetworking();

        // Start the networking thread
        networkThread = new Thread(this::networkHandler);
        networkThread.start();

        // Start the game loop in a new thread
        new Thread(this).start();
    }

    private void initializeNetworking() {
        try {
            client = new GameClient(); // Initialize the client
            System.out.println("GameClient initialized successfully.");
            // Now send the TEST_CONNECTION after client is initialized
            sendPlayerAction("TEST_CONNECTION");
        } catch (Exception e) {
            System.err.println("Failed to initialize GameClient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendPlayerAction(String action) {
        if (client == null) {
            System.err.println("GameClient is not initialized. Cannot send action: " + action);
            return;
        }

        try {
            System.out.println("Sending action: " + action); // Debug log
            client.send(action);
        } catch (Exception e) {
            System.err.println("Failed to send action: " + action);
            e.printStackTrace();
        }
    }

    private void networkHandler() {
        while (running) {
            try {
                // Receive game state updates from the server
                String gameState = client.receive();
                if (gameState != null) {
                    System.out.println("Game state received: " + gameState); // Log response
                    gameCanvas.updateFromServer(gameState);
                } else {
                    System.err.println("No game state received.");
                }
            } catch (Exception e) {
                System.err.println("Networking error: " + e.getMessage());
                e.printStackTrace();
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

    // Gracefully terminate all threads
    @Override
    public void dispose() {
        running = false; // Stop game loop and network handler
        if (client != null) {
            client.close();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        new PirateBattleshipGame();
    }
}
