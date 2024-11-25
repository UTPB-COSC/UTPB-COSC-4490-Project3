public class Level {
    private int levelNumber;
    private int enemySpeed;
    private int numRocks;
    private int projectileSpeed;

    public Level(int levelNumber, int enemySpeed, int numRocks, int projectileSpeed) {
        this.levelNumber = levelNumber;
        this.enemySpeed = enemySpeed;
        this.numRocks = numRocks;
        this.projectileSpeed = projectileSpeed;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getEnemySpeed() {
        return enemySpeed;
    }

    public int getNumRocks() {
        return numRocks;
    }

    public int getProjectileSpeed() {
        return projectileSpeed;
    }
}
