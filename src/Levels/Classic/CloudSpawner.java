package src.Levels.Classic;

import src.Objects.Spawner;
import src.Rand;
import src.Threads.Engine;

import java.awt.image.BufferedImage;

public class CloudSpawner extends Spawner {
    final ClassicMode parent;
    final Engine engine;
    BufferedImage[] clouds;

    public CloudSpawner(ClassicMode p, Engine e) {
        parent = p;
        engine = e;
    }

    public void addArt(BufferedImage[] clouds) {
        this.clouds = clouds;
    }

    @Override
    public boolean spawnCondition() {
        if (parent.cloudCount < parent.maxClouds)
        {
            return Rand.pct() < parent.cloudSpawnChance;
        }
        return false;
    }

    @Override
    public void spawn() {
        new Thread (() -> {
            if (spawnCondition()) {
                int cloudNum = Rand.range(21);
                while (parent.clouds.containsKey(cloudNum)) {
                    cloudNum = Rand.range(21);
                }
                parent.addCloud(cloudNum, new Cloud(parent, clouds[cloudNum], cloudNum));
            }
        }).start();
    }
}
