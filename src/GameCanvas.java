
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class GameCanvas extends JPanel {
    private int screenWidth = 1000;
    private int screenHeight = 800;
    private ArrayList<Rock> rocks;
    private Boat boat;
    private boolean gameOver;
    private boolean onTitleScreen = true;
    private Image seaBackground;
    private EnemyBoat enemyBoat;
    private enum GameState { PLAYING, PAUSED, GAME_OVER, MENU }
    private GameState currentState = GameState.MENU;
    private DebugOverlay debugOverlay = new DebugOverlay();
    private AudioPlayer bgMusic;
    private AudioPlayer boatCrashSound;
    private Rectangle winningPoint;
    private boolean playerWon = false; // Track if the player won
    private List<Projectile> projectiles;
    private List<Projectile> enemyProjectiles; // For enemy boat projectiles
    private long lastShotTime = 0; // Track the last shot time
    private long enemyLastShotTime = 0; // Last shot timestamp for enemy boat
    private ParticleSystem particleSystem = new ParticleSystem();
    
    






    public GameCanvas() {
        rocks = new ArrayList<>();
        generateRocks();
        boat = new Boat(100, screenHeight / 2);
        gameOver = false;
        setUpMouseListener();
        loadSeaBackground();
        enemyBoat = new EnemyBoat(300, 500, 300, 500, 300, 900);
        bgMusic = new AudioPlayer("src/assets/bgMusic.wav");
        boatCrashSound = new AudioPlayer("src/assets/boatcrash.wav");
        bgMusic.playLoop(); // Start music when the game initializes
        winningPoint = new Rectangle(930, 500, 50, 50); 
        projectiles = new CopyOnWriteArrayList<>(); // Thread-safe for iteration during update
        enemyProjectiles = new CopyOnWriteArrayList<>();




        // Set up key listener for handling key events
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e.getKeyCode());
            }
        });
    }

    public void generateRocks() {
        // Adding rocks of different sizes at various positions
        rocks.add(new Rock(200, 200, 50, 50, "src/assets/rock1.png"));
        rocks.add(new Rock(500, 150, 60, 60, "src/assets/rock2.png"));
        rocks.add(new Rock(650, 400, 55, 55, "src/assets/rock3.png"));
        rocks.add(new Rock(300, 300, 100, 100, "src/assets/rock1.png"));
        rocks.add(new Rock(600, 250, 110, 110, "src/assets/rock2.png"));
        rocks.add(new Rock(700, 400, 90, 90, "src/assets/rock3.png"));
        rocks.add(new Rock(800, 650, 150, 150, "src/assets/rock1.png"));
        rocks.add(new Rock(600, 100, 120, 130, "src/assets/rock2.png"));
        rocks.add(new Rock(750, 750, 140, 140, "src/assets/rock3.png"));
        rocks.add(new Rock(920, 100, 170, 200, "src/assets/rock3.png"));
        rocks.add(new Rock(890, 200, 120, 130, "src/assets/rock2.png"));
        rocks.add(new Rock(800, 300, 70, 80, "src/assets/rock1.png"));



    }

    private void setUpMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onTitleScreen) {
                    int mouseX = e.getX();
                    int mouseY = e.getY();
                    int buttonX = screenWidth / 2 - 100;
                    int buttonY = screenHeight / 2;
                    int buttonWidth = 200;
                    int buttonHeight = 50;

                    if (mouseX >= buttonX && mouseY >= buttonY && mouseX <= buttonX + buttonWidth && mouseY <= buttonY + buttonHeight) {
                        onTitleScreen = false;
                        resetGame();
                        currentState = GameState.PLAYING;
                    }
                }
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    
        if (onTitleScreen) {
            drawTitleScreen(g);
            if (bgMusic != null && !bgMusic.isPlaying()) {
                bgMusic.playLoop(); // Play music on title screen
            }
        } else if (gameOver) {
            if (bgMusic != null) {
                bgMusic.stop(); // Stop music on game over
            }
            if (playerWon) {
                drawWinScreen(g); // Show "You Win" screen
            } else {
                drawGameOverScreen(g); // Show "Game Over" screen
            }
        } else if (currentState == GameState.PAUSED) {
            if (bgMusic != null) {
                bgMusic.pause(); // Pause audio
            }
            drawGameElements(g);
            drawPauseMenu(g);
        } else { // Game is running
            if (bgMusic != null && !bgMusic.isPlaying()) {
                bgMusic.resume(); // Resume audio
            }
            for (Projectile projectile : projectiles) {
                projectile.draw(g);
            }
            drawGameElements(g);
            debugOverlay.draw(g, boat, enemyBoat, rocks);
        }
    }
    
    

    private void drawTitleScreen(Graphics g) {
        // Background color
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, screenWidth, screenHeight);
    
        // Title
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("Pirate Battleship", screenWidth / 2 - 180, screenHeight / 3);
    
        // Start Game button
        g.setColor(Color.BLACK);
        g.fillRect(screenWidth / 2 - 100, screenHeight / 2, 200, 50);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Start Game", screenWidth / 2 - 60, screenHeight / 2 + 35);
    
        // Controls and Instructions
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.BLACK);
        int instructionsY = screenHeight / 2 + 100;  // Start y-position for instructions
    
        g.drawString("Controls:", screenWidth / 2 - 100, instructionsY);
        g.drawString("Arrow Keys: Move Boat", screenWidth / 2 - 100, instructionsY + 30);
        g.drawString("P: Pause/Resume Game", screenWidth / 2 - 100, instructionsY + 60);
        g.drawString("R: Reset Game", screenWidth / 2 - 100, instructionsY + 90);
        g.drawString("Q: Quit Game", screenWidth / 2 - 100, instructionsY + 120);
        g.drawString("D: Toggle Debug Mode", screenWidth / 2 - 100, instructionsY + 150);
    }
    

    private void drawGameOverScreen(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, screenWidth, screenHeight);

        for (Rock rock : rocks) {
            rock.draw(g);
        }
        boat.draw(g);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("GAME OVER", screenWidth / 2 - 120, screenHeight / 2 - 20);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press 'R' to Restart", screenWidth / 2 - 100, screenHeight / 2 + 20);
    }

    private void loadSeaBackground() {
        try {
            seaBackground = ImageIO.read(new File("src/assets/sea.gif"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void drawGameElements(Graphics g) {
        if (seaBackground != null) {
            g.drawImage(seaBackground, 0, 0, screenWidth, screenHeight, null);
        }

        for (Projectile p : projectiles) {
            p.draw(g);
        }

        for (Projectile p : enemyProjectiles) {
            p.draw(g);
        }
        
        for (Rock rock : rocks) {
            rock.draw(g);
        }
        boat.draw(g);
        if (!enemyBoat.isDestroyed()) {
            enemyBoat.draw(g); // Only draw if the enemy boat isn't destroyed
        }

        // Draw particles
        particleSystem.draw(g);

        g.setColor(Color.GREEN);
g.fillRect(winningPoint.x, winningPoint.y, winningPoint.width, winningPoint.height);

    }


    public void updateGame() {
        if (currentState == GameState.PLAYING) {
            boat.updatePosition();
            boat.stayWithinBounds(getWidth(), getHeight());
            enemyBoat.updatePosition();  // Update enemy position
            debugOverlay.incrementUpdateCount();
            checkWinCondition(); // Check if the player has reached the destination
            particleSystem.update(); // Update particle effects

            updateProjectiles();
            enemyFireProjectile(boat);


            // Check for collisions
            for (Rock rock : rocks) {
                if (boat.getBounds().intersects(rock.getBounds())) {
                    endGame();
                }
            }

                // Update projectiles and check for collisions
                for (Projectile projectile : projectiles) {
                    if (projectile.isActive()) {
                        projectile.update();
            
                        // Check collision with enemy boat
                        if (!enemyBoat.isDestroyed() && projectile.getBounds().intersects(enemyBoat.getBounds())) {
                            projectile.setActive(false); // Deactivate the projectile
                            enemyBoat.destroy();        // Destroy the enemy boat
                            System.out.println("Enemy boat destroyed!");
                        }
                    }
                }
                    // Update enemy projectiles
    for (Projectile projectile : enemyProjectiles) {
        if (projectile.isActive()) {
            projectile.update();

            // Check collision with player boat
            if (projectile.getBounds().intersects(boat.getBounds())) {
                projectile.setActive(false); // Deactivate projectile
                endGame();                  // Player loses
            }
        }
}

            // Remove inactive projectiles
            projectiles.removeIf(projectile -> !projectile.isActive());
            enemyProjectiles.removeIf(projectile -> !projectile.isActive());



            if (boat.getBounds().intersects(enemyBoat.getBounds())) {
                endGame();
            }

            debugOverlay.update();
            repaint();
        }
    }

    private void updateProjectiles() {
        for (Projectile projectile : projectiles) {
            if (projectile.isActive()) {
                projectile.update();
    
                // Check collision with enemy boat
                if (!enemyBoat.isDestroyed() && projectile.getBounds().intersects(enemyBoat.getBounds())) {
                    projectile.setActive(false); // Deactivate the projectile
                    enemyBoat.destroy();        // Destroy the enemy boat
                    System.out.println("Enemy boat destroyed!");
    
                    // Trigger fragmented explosion
                    createBoatExplosion(
                        enemyBoat.getImage(),                   // Enemy boat image
                        enemyBoat.getX(), enemyBoat.getY(),     // Enemy boat position
                        4                                       // Number of fragments per row/column
                    );
                }
            }
        }
    }

    private void enemyFireProjectile(Boat playerBoat) {
        long currentTime = System.currentTimeMillis();
    
        // Check if enough time has passed and if the player is in range
        if (currentTime - enemyLastShotTime >= 1500 && enemyBoat.isPlayerInRange(playerBoat)) { 
            int enemyX = enemyBoat.getX();
            int enemyY = enemyBoat.getY();
            
            // Create and add a new projectile to the enemy's projectiles list
            enemyProjectiles.add(
                new Projectile(enemyX - 20, enemyY + enemyBoat.getHeight() / 2 - 15, -1, 0, "src/assets/fireball.png")
            ); // Shoots leftward
            
            enemyLastShotTime = currentTime; // Update last shot time
        }
    }
    
    
    private void fireProjectile() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= 500) { // 0.5-second delay
            int boatX = boat.getX();
            int boatY = boat.getY();
            projectiles.add(
                new Projectile(boatX + boat.getWidth(), boatY + boat.getHeight() / 2 - 15, 1, 0, "src/assets/fireball.png")
            ); // Shoots rightward
            lastShotTime = currentTime;
        }
    }
    private void createBoatExplosion(BufferedImage enemyBoatImage, int x, int y, int numFragments) {
        // Use the resized width and height from the EnemyBoat instance
        int fragmentWidth = enemyBoatImage.getWidth() / numFragments;
        int fragmentHeight = enemyBoatImage.getHeight() / numFragments;
    
        for (int i = 0; i < numFragments; i++) {
            for (int j = 0; j < numFragments; j++) {
                // Extract a fragment of the enemy boat image
                BufferedImage fragment = enemyBoatImage.getSubimage(
                    i * fragmentWidth, j * fragmentHeight, fragmentWidth, fragmentHeight);
    
                // Randomize movement speed
                int dx = (int) (Math.random() * 10 - 5); // Random horizontal velocity
                int dy = (int) (Math.random() * 10 - 5); // Random vertical velocity
                int lifetime = (int) (Math.random() * 30 + 20); // Random lifetime
    
                // Add particle to the particle system
                particleSystem.addParticle(new Particle(
                    x + i * fragmentWidth, // Initial x position of the fragment
                    y + j * fragmentHeight, // Initial y position of the fragment
                    dx, dy, lifetime, fragment
                ));
            }
        }
    }
    
    

    private void endGame() {
        gameOver = true;
        currentState = GameState.GAME_OVER;
        bgMusic.stop(); // Stop background music
        boatCrashSound.playOnce();
    }

    public void resetGame() {
        // Reset the player's boat position
        boat.resetPosition();
        enemyBoat = new EnemyBoat(400, 600, 300, 500, 700, 700);

        // Reset enemy boat and rocks (if applicable)
        if (enemyBoat != null) {
            enemyBoat.updatePosition(); // Reset enemy boat position
        }
        
        projectiles.clear(); // Remove any existing projectiles

    
        // Reset game state
        gameOver = false;
        playerWon = false; // Reset win condition
        currentState = GameState.PLAYING;
    
        // Restart the background music
        if (bgMusic != null) {
            bgMusic.stop();  // Stop current music
            bgMusic.playLoop();  // Start music again in a loop
        }
    
        // Redraw the game state
        repaint();
    }
    



public void checkWinCondition() {
    if (boat.getBounds().intersects(winningPoint)) {
        winGame();
    }
}
private void drawWinScreen(Graphics g) {
    // Set background color
    g.setColor(Color.CYAN);
    g.fillRect(0, 0, screenWidth, screenHeight);

    // Draw other game elements like rocks and boat
    for (Rock rock : rocks) {
        rock.draw(g);
    }
    boat.draw(g);

    // Display "You Win" message
    g.setColor(Color.BLACK);
    g.setFont(new Font("Arial", Font.BOLD, 36));
    g.drawString("YOU WIN!", screenWidth / 2 - 100, screenHeight / 2 - 40);

    // Display options
    g.setFont(new Font("Arial", Font.PLAIN, 20));
    g.drawString("Press 'R' to Restart", screenWidth / 2 - 100, screenHeight / 2 + 20);
    g.drawString("Press 'N' for Next Level", screenWidth / 2 - 120, screenHeight / 2 + 50);
}

public void winGame() {
    if (!gameOver) {
        gameOver = true;
        playerWon = true; // Mark as a win
        bgMusic.stop();
        repaint();
    }
}
    private void drawPauseMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, screenWidth, screenHeight);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("PAUSED", screenWidth / 2 - 40, screenHeight / 2 - 20);
        g.drawString("Press 'P' to Resume", screenWidth / 2 - 80, screenHeight / 2 + 20);
        g.drawString("Press 'R' to Reset", screenWidth / 2 - 80, screenHeight / 2 + 60);
        g.drawString("Press 'Q' to Quit", screenWidth / 2 - 80, screenHeight / 2 + 100);
        g.drawString("Press 'D' to open Debug mode", screenWidth / 2 - 80, screenHeight / 2 + 140);
    }

    public void handleKeyPress(int keyCode) {
        // Global actions (work in all states)
        if (keyCode == KeyEvent.VK_Q) {
            System.exit(0); // Quit the game
        } else if (keyCode == KeyEvent.VK_R) {
            resetGame(); // Reset the game
            return; // Prevent further state-specific processing
        }
    
        // Handle state-specific actions
        if (currentState == GameState.PLAYING) {
            if (keyCode == KeyEvent.VK_P) {
                currentState = GameState.PAUSED; // Pause the game
            } else if (keyCode == KeyEvent.VK_D) {
                debugOverlay.toggleDebugMode(); // Toggle debug mode
            } else if (keyCode == KeyEvent.VK_SPACE) {
                fireProjectile(); // Fire a projectile
            } else {
                boat.setDirection(keyCode); // Move the boat
            }
        } else if (currentState == GameState.PAUSED) {
            if (keyCode == KeyEvent.VK_P) {
                currentState = GameState.PLAYING; // Resume the game
            }
        } else if (gameOver) {
            if (playerWon && keyCode == KeyEvent.VK_N) {
                // Placeholder for "Next Level" feature
                System.out.println("Next Level feature coming soon!");
            }
        }
    
        // Refresh the game state visually
        repaint();
    }
    
    

    public void handleKeyRelease(int keyCode) {
        if (!gameOver && !onTitleScreen) {
            boat.stopMoving();
        }
    }
}