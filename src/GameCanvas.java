
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.util.Iterator;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


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




    public GameCanvas() {
        rocks = new ArrayList<>();
        generateRocks();
        boat = new Boat(100, screenHeight / 2);
        gameOver = false;
        setUpMouseListener();
        loadSeaBackground();
        enemyBoat = new EnemyBoat(400, 600, 300, 500, 700, 700); // Customize patrol area as needed
        bgMusic = new AudioPlayer("src/assets/bgMusic.wav");
        boatCrashSound = new AudioPlayer("src/assets/boatcrash.wav");
        bgMusic.playLoop(); // Start music when the game initializes
        winningPoint = new Rectangle(930, 500, 50, 50); 


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

        for (Rock rock : rocks) {
            rock.draw(g);
        }
        boat.draw(g);
        enemyBoat.draw(g);  
        

        g.setColor(Color.GREEN);
g.fillRect(winningPoint.x, winningPoint.y, winningPoint.width, winningPoint.height);

    }

  
    
        public void fireProjectile(boolean isEnemy) {
            int startX = isEnemy ? enemyBoat.getX() : boat.getX();
            int startY = isEnemy ? enemyBoat.getY() : boat.getY();
            int velocityY = isEnemy ? 2 : -2; // Enemy cannonballs go downward, player cannonballs upward
    
            Projectile projectile = new Projectile(startX, startY, 0, velocityY);
            if (isEnemy) {
                enemyBoat.getProjectiles().add(projectile);
            } else {
                boat.getProjectiles().add(projectile);
            }
        }
    }
    

    public void updateGame() {
        if (currentState == GameState.PLAYING) {
            boat.updatePosition();
            boat.stayWithinBounds(getWidth(), getHeight());
            debugOverlay.incrementUpdateCount();
            checkWinCondition(); // Check if the player has reached the destination
            enemyBoat.updatePosition();
            enemyBoat.shootAtPlayer(boat);
            enemyBoat.updateProjectiles();

            checkRockCollisions();
            checkProjectileCollisions();
            checkBoatCollisions();


           

            
            debugOverlay.update();
            repaint();
        }
    }

   
    

    private void endGame() {
        gameOver = true;
        currentState = GameState.GAME_OVER;
        bgMusic.stop(); // Stop background music
        boatCrashSound.playOnce();
    }

    private void checkProjectileCollisions() {
        Iterator<Projectile> iterator = enemyBoat.getProjectiles().iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            projectile.move(); // Update the projectile's position
    
            if (projectile.getBounds().intersects(boat.getBounds())) {
                iterator.remove(); // Remove the projectile
                endGame(); // End the game on collision
            } else if (projectile.isOutOfBounds(canvasWidth, canvasHeight)) {
                iterator.remove(); // Remove projectiles that go off-screen
            }
        }
    }
    
    private void checkRockCollisions() {
        for (Rock rock : rocks) {
            if (boat.getBounds().intersects(rock.getBounds())) {
                endGame(); // End game if the boat hits a rock
            }
        }
    }
    
    private void checkBoatCollisions() {
        if (boat.getBounds().intersects(enemyBoat.getBounds())) {
            endGame(); // End game if boats collide
        }
    }
    

    

    public void resetGame() {
        // Reset the player's boat position
        boat.resetPosition();
    
        // Reset enemy boat and rocks (if applicable)
        if (enemyBoat != null) {
            enemyBoat.updatePosition(); // Reset enemy boat position
        }
       
    
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
        if (gameOver) {
            
            if (keyCode == KeyEvent.VK_R) { 
                resetGame(); // Restart the game
            } else if (playerWon && keyCode == KeyEvent.VK_N) { 
                // Placeholder for "Next Level" feature
                System.out.println("Next Level feature coming soon!");
            } else if (keyCode == KeyEvent.VK_Q) {
                System.exit(0); // Quit game
            }
            else if (keyCode == KeyEvent.VK_SPACE) {
                fireProjectile(false); // Player fires
            }
        } else if (currentState == GameState.PLAYING) {
            if (keyCode == KeyEvent.VK_P) { 
                currentState = GameState.PAUSED; // Pause the game
            } else if (keyCode == KeyEvent.VK_D) { 
                debugOverlay.toggleDebugMode(); // Toggle debug mode
            } else { 
                boat.setDirection(keyCode); // Set boat movement direction
            }
        } else if (currentState == GameState.PAUSED) {
            if (keyCode == KeyEvent.VK_P) { 
                currentState = GameState.PLAYING; // Resume the game
            }
        }
    
          repaint(); // Refresh the game state visually
    }
    

    public void handleKeyRelease(int keyCode) {
        if (!gameOver && !onTitleScreen) {
            boat.stopMoving();
        }}
   
