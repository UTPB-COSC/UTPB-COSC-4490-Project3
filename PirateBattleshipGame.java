import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
public class PirateBattleshipGame extends JFrame implements Runnable {

    private GameCanvas gameCanvas;
    private volatile boolean running = true;
    private GameClient client; // Networking client
    
    private Thread networkThread;

    public PirateBattleshipGame() {
        setTitle("Pirate Battleship Game");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gameCanvas = new GameCanvas(client); // Create game canvas
        add(gameCanvas);
        setVisible(true);

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
                    System.out.println("Sending action: " + action);
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
            client = new GameClient("localhost", 9876); // Initialize the client
            System.out.println("GameClient initialized successfully.");
            gameCanvas.setGameClient(client); // Pass client to GameCanvas
            sendPlayerAction("TEST_CONNECTION"); // Send test connection message
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
            System.out.println("Sending action: " + action);
            client.send(action);
        } catch (Exception e) {
            System.err.println("Failed to send action: " + action);
            e.printStackTrace();
        }
    }

    private void networkHandler() {
        while (running) {
            try {
                String gameState = client.receive();
                if (gameState != null) {
                    System.out.println("Game state received: " + gameState);
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
        final long optimalTime = 1_000_000_000 / targetFPS;

        while (running) {
            long startTime = System.nanoTime();

            gameCanvas.updateGame();
            gameCanvas.repaint();

            long elapsedTime = System.nanoTime() - startTime;
            long sleepTime = (optimalTime - elapsedTime) / 1_000_000;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void dispose() {
        running = false;
        if (client != null) {
            client.close();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        new PirateBattleshipGame();
    }
}
