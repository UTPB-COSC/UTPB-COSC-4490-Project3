package src;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class Game extends JPanel implements Runnable
{
    private final GameCanvas canvas;

    private final double rateTarget = 100.0;
    public double waitTime = 1000.0 / rateTarget;
    public double rate = 1000 / waitTime;

    //public Spaceship spaceship;
    public int mouseX;
    public int mouseY;
    public int fireRate = 10;
    public int fireCounter = 0;
    public boolean firing = false;

    //public Asteroid[] pipes = new Asteroid[5];
    private int pipeCount = 1;

    public int highScore = 0;
    public int score = 0;

    public Toolkit tk;
    public boolean debug = false;
    public boolean running = true;
    public double volume = 0.3;
    public boolean randomGaps = false;
    public double difficulty = 0.0;
    public boolean ramping = false;

    private int pipeWidth;
    private int pipeHeight;
    public BufferedImage pipeImage;
    public BufferedImage flippedPipe;

    public BufferedImage[] cloudImage = new BufferedImage[21];
    private final int cloudCap = 20;
    public Cloud[] clouds = new Cloud[cloudCap];
    private int cloudCount = 0;
    private final double cloudRate = 0.005;
    
    
    
 // Game objects
    public Spaceship spaceship;
    public ArrayList<Asteroid> asteroids;
    public ArrayList<Bullet> bullets;
    public ArrayList<Particle> particles; // List to store all particles
    
 // Scrolling image
    public BufferedImage scrollingImage;
    public int scrollingX; // X position for scrolling
    public int scrollingY; // Y position for scrolling
    public boolean isScrolling; // Flag to check if the image is currently scrolling
    private Random random;

    
// Game state
    
    private Timer gameTimer;
    private Timer asteroidSpawnTimer;

    public Game()
    {
        JFrame frame = new JFrame("Game");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.setUndecorated(true);

        tk = Toolkit.getDefaultToolkit();

        frame.setVisible(true);
        frame.requestFocus();

        try {
            File scoreFile = new File("score.txt");
            if(!scoreFile.exists())
            {
                highScore = 0;
            } else {
                try {
                    FileReader fr = new FileReader(scoreFile);
                    BufferedReader br = new BufferedReader(fr);
                    highScore = Integer.parseInt(br.readLine());
                    br.close();
                    fr.close();
                } catch (Exception ex) {
                    highScore = 0;
                }
            }

         // Load scrolling PNG image
            try {
                scrollingImage = ImageIO.read(new File("alien.png")); // Replace with your image file path
            } catch (IOException e) {
                System.err.println("Alien.");
                e.printStackTrace();
            }

            scrollingX = tk.getScreenSize().width; // Start off-screen
            scrollingY = 0;
            isScrolling = false;
            random = new Random();
            
            spaceship = new Spaceship(this, tk, tk.getScreenSize().width/2, tk.getScreenSize().height/2);
            asteroids = new ArrayList<>();
            bullets = new ArrayList<>();
            particles = new ArrayList<>();
         // Timer for spawning new asteroids every 3 seconds
            //asteroidSpawnTimer = new Timer(3000, e -> spawnAsteroid());
            
         // Spawn initial asteroids
            for (int i = 0; i < 5; i++) {
                spawnAsteroid();
            }
         // Timer for spawning new asteroids every 3 seconds
            Timer asteroidSpawnTimer = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    spawnAsteroid();
                }
            });
            asteroidSpawnTimer.start(); // Start spawning asteroids
            
            
            
/*
            BufferedImage image = ImageIO.read(new File("pipe.png"));

            pipeWidth = tk.getScreenSize().width / 16;
            pipeHeight = (int)(((double)pipeWidth / (double)image.getWidth()) * image.getHeight());

            Image temp = image.getScaledInstance(pipeWidth, pipeHeight, BufferedImage.SCALE_SMOOTH);
            pipeImage = new BufferedImage(pipeWidth, pipeHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics g = pipeImage.getGraphics();
            g.drawImage(temp, 0, 0, null);
            g.dispose();

            AffineTransform at = new AffineTransform();
            at.rotate(Math.PI, image.getWidth() / 2, image.getHeight() / 2);
            AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            BufferedImage flipped = ato.filter(image, null);

            temp = flipped.getScaledInstance(pipeWidth, pipeHeight, BufferedImage.SCALE_SMOOTH);
            flippedPipe = new BufferedImage(pipeWidth, pipeHeight, BufferedImage.TYPE_INT_ARGB);
            g = flippedPipe.getGraphics();
            g.drawImage(temp, 0, 0, null);
            g.dispose();

            Asteroid pipe = new Asteroid(this, tk, tk.getScreenSize().height / 2, pipeWidth, pipeHeight);
            //pipes[0] = pipe;

            image = ImageIO.read(new File("space.png"));
            int fragHeight = image.getHeight() / 21;
            for (int i = 0; i < cloudImage.length; i++)
            {
                temp = image.getSubimage(0, i * fragHeight, image.getWidth(), fragHeight);
                cloudImage[i] = new BufferedImage(image.getWidth(), fragHeight, BufferedImage.TYPE_INT_ARGB);
                g = cloudImage[i].getGraphics();
                g.drawImage(temp, 0, 0, null);
                g.dispose();
            }
*/
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }

        canvas = new GameCanvas(this, frame.getGraphics(), tk);
        frame.add(canvas);

        Thread drawLoop = new Thread(canvas);
        drawLoop.start();

        frame.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if(running)
                	spaceship.setRotatingLeft(true);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                	if(running)
                	spaceship.setRotatingRight(true);
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                	if(running)
                	spaceship.setAccelerating(true);
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                	if(running)
                	bullets.add(spaceship.shoot());
                }
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    running = !running;
                }
                if(e.getKeyCode() == KeyEvent.VK_UP)
                {
                    if (!running)
                    {
                        canvas.cursor--;
                        canvas.cursor = Math.max(canvas.cursor, 0);
                    }
                }
                if(e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    if (!running)
                    {
                        canvas.cursor++;
                        canvas.cursor = Math.min(canvas.cursor, 6);
                    }
                }
                if(e.getKeyCode() == KeyEvent.VK_RIGHT)
                {
                    if (!running)
                    {
                        if (canvas.cursor == 2) {
                            volume += 0.1;
                            volume = Math.min(volume, 1.0);
                        }
                        if (canvas.cursor == 4) {
                            difficulty += 0.5;
                            difficulty = Math.min(difficulty, 3.0);
                        }
                    }
                }
                if(e.getKeyCode() == KeyEvent.VK_LEFT)
                {
                    if (!running)
                    {
                        if (canvas.cursor == 2) {
                            volume -= 0.1;
                            volume = Math.max(volume, 0.0);
                        }
                        if (canvas.cursor == 4) {
                            difficulty -= 0.5;
                            difficulty = Math.max(difficulty, 0.0);
                        }
                    }
                }
                if(e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    if (!running)
                    {
                        if (canvas.cursor == 0)
                            reset();
                        if (canvas.cursor == 1)
                            System.exit(0);
                        if (canvas.cursor == 3)
                            randomGaps = !randomGaps;
                        if (canvas.cursor == 5)
                            ramping = !ramping;
                        if (canvas.cursor == 6)
                            debug = !debug;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
            	if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            		if(running)
            		spaceship.setRotatingLeft(false);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                	if(running)
                	spaceship.setRotatingRight(false);
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                	if(running)
                	spaceship.setAccelerating(false);
                }
            }
        });

        frame.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    firing = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    firing = false;
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        frame.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        frame.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {

            }
        });
    }

    @Override
    public void run()
    {
        while(true)
        {
            long startTime = System.nanoTime();

            if (running)
            {
            	//{
            	
            	spaceship.update();

                ArrayList<Bullet> tempBullets = (ArrayList<Bullet>)bullets.clone();
        		for (Bullet bullet : tempBullets) {
                    bullet.update();
                    if (!bullet.isOnScreen(tk.getScreenSize().width, tk.getScreenSize().height)) {
                        delBullet(bullet);
                    }
        		}

                ArrayList<Asteroid> tempRoids = (ArrayList<Asteroid>)asteroids.clone();
        		for (Asteroid asteroid : tempRoids) {
                    asteroid.update();
                    if (asteroid.isDestroyed()) {
                        delAsteroid(asteroid);
                    }
        		}
        	//}

            	
            	// Update particles
                ArrayList<Particle> tempParticles = (ArrayList<Particle>)particles.clone();
                for (Particle particle : tempParticles) {
                    particle.update();
                    if (!particle.isAlive()) {
                        delParticle(particle);
                    }
                }

                // Remove expired particles
                //particles.removeIf(particle -> !particle.isAlive());
                
            // Handle scrolling image
            if (isScrolling) {
                    scrollingX -= 2; // Scroll speed
                    if (scrollingX + scrollingImage.getWidth() <= 0) {
                        isScrolling = false; // Reset scrolling when off-screen
                    }
             } else {
                    // Randomly trigger scrolling
                    if (random.nextInt(15000) < 5) { // Adjust probability as needed
                        isScrolling = true;
                        scrollingX = tk.getScreenSize().width; // Reset position to start from the right
                        scrollingY = random.nextInt(tk.getScreenSize().height - scrollingImage.getHeight()); // Random vertical position
                    }
                }
            checkCollisions();

            // Remove off-screen bullets and destroyed asteroids
            //bullets.removeIf(bullet -> !bullet.isOnScreen(tk.getScreenSize().width, tk.getScreenSize().height));
            //asteroids.removeIf(asteroid -> asteroid.isDestroyed());

            // Repaint the game screen
            repaint();
/*            	
                fireCounter -= 1;
                fireCounter = Math.max(fireCounter, 0);
                if (firing && fireCounter == 0)
                {
                    // spawn new bullet and add to bullet array
                    fireCounter = 10;
                }
                // for each bullet within drawable space:
                //   perform update
                //   check for collisions
                //   if collide():
                //     do something

                if (Math.random() < cloudRate)
                {
                    cloudCount++;
                    if (cloudCount >= clouds.length)
                        cloudCount = 0;
                    if (clouds[cloudCount] == null || clouds[cloudCount].passed)
                    {
                        Cloud c = new Cloud(this, tk);
                        clouds[cloudCount] = c;
                    }
                }
                for (int i = 0; i < clouds.length; i++)
                {
                    if(clouds[i] != null)
                        clouds[i].update();
                }
/*
                spaceship.update();
                
                for (int i = 0; i < pipes.length; i++) {
                    if (pipes[i] == null)
                        continue;

                    if (pipes[i].update()) {
                        score += 1;

                        if (ramping && score % 10 == 0)
                        {
                            difficulty += 0.5;
                            difficulty = Math.min(difficulty, 3.0);
                        }

                        if (score > highScore)
                        {
                            new Thread(() -> {
                                highScore = score;
                                running = true;
                                try {
                                    File scoreFile = new File("score.txt");
                                    PrintWriter pw = new PrintWriter(scoreFile);
                                    pw.write(String.format("%d%n", highScore));
                                    pw.close();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        }

                        new Thread(() -> {
                            try {
                                AudioInputStream ais = AudioSystem.getAudioInputStream(new File("score.wav").getAbsoluteFile());
                                Clip clip = AudioSystem.getClip();
                                clip.open(ais);
                                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                                gain.setValue(20f * (float) Math.log10(volume));
                                clip.start();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }).start();
                    }

                    if (pipes[i].spawnable && pipes[i].xPos < 3 * tk.getScreenSize().width / 4) {
                        pipes[i].spawnable = false;
                        int min = tk.getScreenSize().height / 4;
                        int range = min * 2;
                        int y = (int) (Math.random() * range) + min;
                        Pipe pipe = new Pipe(this, tk, y, pipeWidth, pipeHeight);
                        pipes[pipeCount] = pipe;
                        pipeCount++;
                        if (pipeCount >= pipes.length)
                            pipeCount = 0;
                    }

                    if (spaceship.collide(pipes[i])) {
                        running = !running;
                    }
                }
*/                
                
                
            }

            
            
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

    private synchronized void delAsteroid(Asteroid a) {
        if (asteroids.contains(a)) {
            int idx = asteroids.indexOf(a);
            asteroids.remove(idx);
        }
    }

    private synchronized void delBullet(Bullet b) {
        if (bullets.contains(b)) {
            int idx = bullets.indexOf(b);
            bullets.remove(idx);
        }
    }

    private synchronized void delParticle(Particle p) {
        if (particles.contains(p)) {
            int idx = particles.indexOf(p);
            particles.remove(idx);
        }
    }

    
 // Spawn a single asteroid at a random location along the edges
    private void spawnAsteroid() {
        Random rand = new Random();

        // Spawn asteroid on the edges of the screen
        int side = rand.nextInt(4); // 0 = top, 1 = right, 2 = bottom, 3 = left
        int x = 0, y = 0;

        if (side == 0) { // Top
            x = rand.nextInt(tk.getScreenSize().width);
            y = 0;
        } else if (side == 1) { // Right
            x = tk.getScreenSize().width;
            y = rand.nextInt(tk.getScreenSize().height);
        } else if (side == 2) { // Bottom
            x = rand.nextInt(tk.getScreenSize().width);
            y = tk.getScreenSize().height;
        } else if (side == 3) { // Left
            x = 0;
            y = rand.nextInt(tk.getScreenSize().height);
        }

        asteroids.add(new Asteroid(this, tk, x, y, 40 + rand.nextInt(20)));
    }
    
/*    
    //@Override
    public void actionPerformed(ActionEvent e) {
        // Update game objects
    	if (running) {
    		spaceship.update();

    		for (Bullet bullet : bullets) {
            bullet.update();
    		}

    		for (Asteroid asteroid : asteroids) {
            asteroid.update();
    		}
    	}

        checkCollisions();

        // Remove off-screen bullets and destroyed asteroids
        bullets.removeIf(bullet -> !bullet.isOnScreen(tk.getScreenSize().width, tk.getScreenSize().height));
        asteroids.removeIf(asteroid -> asteroid.isDestroyed());

        // Repaint the game screen
        repaint();
    }
*/    
    
    private void checkCollisions() {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            for (Asteroid asteroid : asteroids) {
                if (asteroid.getBounds().intersects(bullet.getBounds())) {
                    asteroid.boom();
                    generateParticles(asteroid.getX(), asteroid.getY());
                	asteroid.setDestroyed(true);
                	score += 1;
                    bulletIterator.remove();
                    break;
                }
            }
        }

        for (Asteroid asteroid : asteroids) {
            if (asteroid.getBounds().intersects(spaceship.getBounds())) {
                spaceship.collide();
            	spaceship.setDestroyed(true);
                //timer.stop();
                //JOptionPane.showMessageDialog(this, "Game Over!");
                running = !running;;
            }
        }
    }
    
 // Method to generate particles at a given position
    private void generateParticles(double x, double y) {
        Random random = new Random();
        int numParticles = 15 + random.nextInt(10); // Generate between 15 and 25 particles
        for (int i = 0; i < numParticles; i++) {
            particles.add(new Particle(x, y));
        }
    }
    
    public void reset()
    {
        if (score > highScore)
        {
            highScore = score;
            running = true;
            try {
                File scoreFile = new File("score.txt");
                PrintWriter pw = new PrintWriter(scoreFile);
                pw.write(String.format("%d%n", highScore));
                pw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        spaceship.reset();
        asteroids = new ArrayList<>();
        bullets = new ArrayList<>();
        particles = new ArrayList<>();
        //pipes = new Asteroid[5];
        Asteroid p = new Asteroid(this, tk, tk.getScreenSize().height / 2, pipeWidth, pipeHeight);
        //pipes[0] = p;
        //pipeCount = 1;
        score = 0;

        //clouds = new Cloud[cloudCap];
        //cloudCount = 0;

        running = true;
    }

    public static void main(String[] args)
    {
        Game game = new Game();

        Thread logicLoop = new Thread(game);
        logicLoop.start();
    }
}
