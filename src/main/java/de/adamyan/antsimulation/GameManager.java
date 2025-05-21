package de.adamyan.antsimulation;

import de.adamyan.antsimulation.NN.Network;
import de.adamyan.antsimulation.Physics.LineSegmentWall;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * The whole game logic happens here
 */
public class GameManager {

    private static final Image antImage = new Image("ant.png");


    /// Debug/Test tool to see the rays
    public static boolean shouldDrawRays = true;
    /// ------------------ world settings ------------------
    private static final int initialPopulationSize = 10000;
    private static final int wallAmount = 10;
    private static final int GENERATION_DURATION_FRAMES = 20 * 60;
    private int genFramesLeft;
    private int genCount;

    /// ------------------ genetic algorithm settings ------------------
    private static final double percentageOfAgentsThatPassAutomatically = 0.10;

    private List<Ant> antPopulation;

    /**
     * Collection of all straight walls in the game
     * Great because: Ray interceptions point can be computed fast with a few math operations and no loops
     */
    private List<LineSegmentWall> walls;


    public GameManager() {
        walls = new ArrayList<>();
        // generate and add 'wallAmount' random walls
        for (int i = 0; i < wallAmount; i++) {
            walls.add(new LineSegmentWall(Math.random() * 1000, Math.random() * 800, Math.random() * 1000, Math.random() * 800));
        }
        // and the 4 world boundaries
        walls.add(new LineSegmentWall(0, 0, 0, 800));
        walls.add(new LineSegmentWall(0, 800, 1000, 800));
        walls.add(new LineSegmentWall(1000, 800, 1000, 0));
        walls.add(new LineSegmentWall(1000, 0, 0, 0));

        walls.add(new LineSegmentWall(300, 0, 300, 301));
        walls.add(new LineSegmentWall(300, 500, 300, 801));

        antPopulation = new ArrayList<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            antPopulation.add(new Ant(this));
        }

        genCount = 0;
        genFramesLeft = GENERATION_DURATION_FRAMES;
    }

    /**
     * @return generation finished
     */
    public boolean frame_logic() {
        antPopulation.parallelStream().forEach(ant ->
                ant.act(genFramesLeft / (double) GENERATION_DURATION_FRAMES)
        );

        // decrease time left for generation and make a new if the time is up
        genFramesLeft--;
        if (genFramesLeft == 0) {
            finishGen();
            genCount++;
            genFramesLeft = GENERATION_DURATION_FRAMES;
            return true;
        }
        return false;
    }

    public void draw(Canvas canvas) {
        var gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setStroke(Color.SILVER);
        gc.setLineWidth(10);
        gc.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (LineSegmentWall wall : walls) {
            gc.setLineWidth(3);
            gc.setStroke(Color.BLACK);

            gc.strokeLine(wall.startX(), wall.startY(), wall.endX(), wall.endY());
        }

        drawAnts(gc);
        if (shouldDrawRays) {
            for (Ant ant : antPopulation) {
                ant.draw_rays(canvas.getGraphicsContext2D());
            }
        }
        gc.setFill(new Color(0, 1, 0, 0.5));
        gc.fillOval(antPopulation.getFirst().getX() - 10, antPopulation.getFirst().getY() - 10, 20, 20);

        // display the time left in seconds
        gc.setFont(new Font(50));
        gc.fillText(genFramesLeft / 60 + "", 50, 50);
    }


    /**
     * Here the whole logic for 'natural selection', 'gene combination' and mutation happens
     */
    private void finishGen() {
        antPopulation.sort((ant1, ant2) -> Double.compare(ant2.getReward(), ant1.getReward()));

        // best x%
        int bestIdx = (int) (antPopulation.size() * percentageOfAgentsThatPassAutomatically);

        for (int i = 0; i < bestIdx; i++) {
            // do nothing, these individuals remain as they are
        }

        // to store the newly generated children
        Ant[] newChildren = new Ant[antPopulation.size() - bestIdx];

        for (int i = bestIdx; i < antPopulation.size(); i++) {
            // randomly select parent ants
            Ant momAnt = getBestAntOfKRandom(antPopulation, 50);
            Ant dadAnt = getBestAntOfKRandom(antPopulation, 50);

            Ant childAnt = new Ant(this);
            childAnt.setNetwork(new Network(momAnt.getNetwork(), dadAnt.getNetwork(), momAnt.getReward() > dadAnt.getReward() ? 0.35 : 0.65));

            childAnt.getNetwork().mutate(0.05, 0.4);

            newChildren[i - bestIdx] = childAnt;
        }

        // replace the last generation with the new children (excluding the top x%)
        for (int i = 0; i < newChildren.length; i++) {
            antPopulation.set(bestIdx + i, newChildren[i]);
        }

        // reset the ants to factory settings for the next generation
        for (Ant ant : antPopulation) {
            ant.resetGenerationSpecificFields();
        }
    }

    private Ant getBestAntOfKRandom(List<Ant> antPool, int k) {
        Ant bestAnt = null;
        double largestRewardSoFar = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < k; i++) {
            Ant randomAnt = antPool.get((int) (
                    Math.random() * antPool.size()
            ));
            double reward = randomAnt.getReward();

            if (reward > largestRewardSoFar) {
                largestRewardSoFar = reward;
                bestAnt = randomAnt;
            }
        }
        return bestAnt;
    }

    private void drawAnts(GraphicsContext gc) {
        for (Ant ant : antPopulation) {
            gc.save();
            gc.translate(ant.getX(), ant.getY());
            gc.rotate(Math.toDegrees(ant.getAngle()));
            gc.drawImage(antImage, -antImage.getWidth() / 2, -antImage.getHeight() / 2);
            gc.restore();
        }
    }

    public List<LineSegmentWall> getWalls() {
        return walls;
    }

    public List<Ant> getAntPopulation() {
        return antPopulation;
    }

    public int getGenCount() {
        return genCount;
    }
}