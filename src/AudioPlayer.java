import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioPlayer {
    private Clip clip;
    private long clipPosition = 0; // Position of the clip in microseconds
    private boolean isPaused = false; // Track pause state


    public AudioPlayer(String filePath) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filePath));
            clip = AudioSystem.getClip();
            clip.open(audioStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void playLoop() {
        if (clip != null) {
            clip.setMicrosecondPosition(0); // Start from the beginning
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }
    public void playOnce() {
        if (clip != null) {
            clip.setMicrosecondPosition(0); // Start from the beginning
            clip.start(); // Play once
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void pause() {
        if (clip != null && clip.isRunning()) {
            clipPosition = clip.getMicrosecondPosition(); // Save current position
            clip.stop(); // Stop playback
        }
    }

    public void resume() {
        if (clip != null && !clip.isRunning()) {
            clip.setMicrosecondPosition(clipPosition); // Resume from saved position
            clip.start();
        }
    }

    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }
}
