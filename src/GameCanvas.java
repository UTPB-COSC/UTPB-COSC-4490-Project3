package src;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class GameCanvas extends JPanel implements Runnable
{

    private Game game;
    private Graphics graphics;
    Toolkit tk;

    private final double rateTarget = 60.0;
    private double waitTime = 1000.0 / rateTarget;
    private double rate = 1000.0 / waitTime;

    public int cursor = 0;
    public int crosshairSize = 20;

    private long reloadStartTime = 0; // Timestamp for when the reload begins
    private boolean isReloading = false; // Tracks if the reload is in progress
    private final long reloadDuration = 5000; // Reload time in milliseconds    

    public GameCanvas(Game game, Graphics g, Toolkit tk)
    {
        this.game = game;
        graphics = g;
        this.tk = tk;
    }

    @Override
    public void run() {
        while(true)
        {
            long startTime = System.nanoTime();

            int width = tk.getScreenSize().width;
            int height = tk.getScreenSize().height;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setColor(Color.CYAN);
            g2d.fillRect(0, 0, width, height);

            game.tank.drawTank(g2d);
            for (EnemyTank enemy : game.enemyTanks) {
                enemy.draw(g2d);
                enemy.drawProjectiles(g2d);
            }
            game.tank.drawProjectiles(g2d);

            for (int i = 0; i < game.clouds.length; i++)
            {
                if (game.clouds[i] != null && !game.clouds[i].passed)
                    game.clouds[i].drawCloud(g2d);
            }

            g2d.setColor(Color.RED);
            g2d.drawOval(game.mouseX - crosshairSize, game.mouseY - crosshairSize, crosshairSize*2, crosshairSize*2);
            g2d.drawLine(game.mouseX - crosshairSize, game.mouseY, game.mouseX+crosshairSize, game.mouseY);
            g2d.drawLine(game.mouseX, game.mouseY-crosshairSize, game.mouseX, game.mouseY+crosshairSize);

            for (int i = 0; i < game.bombers.length; i++)
            {
                if (game.bombers[i] != null)
                    game.bombers[i].drawBomber(g2d);
            }

            g2d.setColor(Color.BLACK);
            g2d.drawString("Magazine", width/2 - 10, 30);

            int squareSize = 20; // Size of each square
            int spacing = 5;     // Space between squares
            int startX = width/2 - 40;     // Starting x-coordinate
            int startY = 50;     // Starting y-coordinate

            // If reloading, calculate remaining time
            if (isReloading) {
                long elapsed = System.currentTimeMillis() - reloadStartTime;
                if (elapsed >= reloadDuration) {
                    // Reload complete
                    isReloading = false;
                    Tank.shotsFired = 0; // Reset magazine
                        // Despite similar logic existing in Tank.java, removing this breaks the magazine reset.
                } else {
                    // Display countdown
                    int countdown = (int) Math.ceil((reloadDuration - elapsed) / 1000.0);
                    g2d.drawString("Reloading: " + countdown + "s", startX, startY + squareSize + 20);
                }
            }
            
            // Draw magazine squares
            for (int i = 0; i < 5; i++) {
                if (i < 5 - Tank.shotsFired && !isReloading) {
                    g2d.setColor(Color.GREEN); // Filled square
                } else {
                    g2d.setColor(Color.RED); // Empty square
                }
                if (Tank.shotsFired == 5) {
                    // If all shots are fired, start reload
                    if (!isReloading) {
                        isReloading = true;
                        reloadStartTime = System.currentTimeMillis();
                    }
                }
                g2d.fillRect(startX + i * (squareSize + spacing), startY, squareSize, squareSize);
            }

            // Draw original score counter
            g2d.setColor(Color.BLACK);
            if (game.running) {
                g2d.drawString(String.format("Score: %d", game.score), 25, 25);
                g2d.drawString(String.format("High Score: %d", game.highScore), 25, 50);
            } else {
                g2d.drawString(String.format("%s Reset Game", cursor == 0 ? ">" : " "), 25, 25);
                g2d.drawString(String.format("%s Exit Game", cursor == 1 ? ">" : " "), 25, 50);
                String vol = "";
                for (int i = 0; i < 11; i++)
                {
                    if ((int) (game.volume * 10) == i)
                    {
                        vol += "|";
                    } else {
                        vol += "-";
                    }
                }
                g2d.drawString(String.format("%s Volume %s", cursor == 2 ? ">" : " ", vol), 25, 75);
                g2d.drawString(String.format("%s Randomize Gaps %s", cursor == 3 ? ">" : " ", game.randomGaps ? "(ON)" : "(OFF)"), 25, 100);
                String dif = "";
                for (double i = 0.0; i <= 3.0; i+= 0.5)
                {
                    if (game.difficulty == i)
                    {
                        dif += "|";
                    } else {
                        dif += "-";
                    }
                }
                g2d.drawString(String.format("%s Difficulty %s", cursor == 4 ? ">" : " ", dif), 25, 125);
                g2d.drawString(String.format("%s Ramping %s", cursor == 5 ? ">" : " ", game.ramping ? "(ON)" : "(OFF)"), 25, 150);
                g2d.drawString(String.format("%s Debug Mode %s", cursor == 6 ? ">" : " ", game.debug ? "(ON)" : "(OFF)"), 25, 175);
            }
            if (game.debug) {
                g2d.drawString(String.format("FPS = %.1f", rate), 200, 25);
                g2d.drawString(String.format("UPS = %.1f", game.rate), 200, 50);
            }

            graphics.drawImage(image, 0, 0, null);

            long sleep = (long) waitTime - (System.nanoTime() - startTime) / 1000000;
            rate = 1000.0 / Math.max(waitTime - sleep, waitTime);

            try
            {
                Thread.sleep(Math.max(sleep, 0));
            } catch (InterruptedException ex)
            {

            }
        }
    }
}
