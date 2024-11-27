import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParticleSystem {
    private final List<Particle> particles; // List to store all particles

    public ParticleSystem() {
        this.particles = new ArrayList<>();
    }

    // Method to add a particle to the system
    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    // Update method to update each particle and remove expired ones
    public void update() {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.update(); // Update the position and lifetime of the particle
            if (!particle.isAlive()) {
                iterator.remove(); // Remove the particle if it's no longer alive
            }
        }
    }

    // Method to draw all the particles
    public void draw(Graphics g) {
        for (Particle particle : particles) {
            particle.draw(g); // Draw each particle
        }
    }
}

class Particle {
    private int x, y;              // Position of the particle
    private int dx, dy;            // Velocity of the particle (change in x and y per update)
    private int lifetime;          // How long the particle lasts before it disappears
    private BufferedImage image;   // The image representing the particle (explosion, debris, etc.)

    // Constructor to initialize the particle's position, velocity, lifetime, and image
    public Particle(int x, int y, int dx, int dy, int lifetime, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.lifetime = lifetime;
        this.image = image;
    }

    // Method to update the particle's position and decrease its lifetime
    public void update() {
        x += dx; // Move particle along x-axis by its velocity
        y += dy; // Move particle along y-axis by its velocity
        lifetime--;  // Reduce lifetime by 1 with each update
    }

    // Method to check if the particle is still alive (lifetime > 0)
    public boolean isAlive() {
        return lifetime > 0;
    }

    // Method to draw the particle at its current position
    public void draw(Graphics g) {
        if (isAlive()) {
            g.drawImage(image, x, y, null); // Draw the particle image at the current position
        }
    }
}
