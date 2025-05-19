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

    private static final int initialPopulationSize = 3000;
    private static final int wallAmount = 10;

    private static final int generationDuration_ms = 30 * 1000;
    private double genTimeLeft_ms;
    private int genCount;

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

        antPopulation = new ArrayList<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            antPopulation.add(new Ant(this));
        }

        genCount = 0;
        genTimeLeft_ms = generationDuration_ms;
    }

    /**
     * @return generation finished
     */
    public boolean act(Canvas canvas, double deltaTime) {
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


        // let each ant cook and draw them
        for (Ant ant : antPopulation) {
            ant.act(gc, genTimeLeft_ms / generationDuration_ms);
        }
        drawAnts(gc);

        // display the time left in seconds
        gc.setFont(new Font(50));
        gc.fillText((int)(genTimeLeft_ms / 1000) + "", 50, 50);

        // decrease time left for generation and make a new if the time is up
        genTimeLeft_ms -= deltaTime;
        if (genTimeLeft_ms <= 0) {
            finishGen();
            genCount++;
            genTimeLeft_ms = generationDuration_ms;
            return true;
        }
        return false;
    }


    /**
     * Here the whole logic for 'natural selection', 'gene combination' and mutation happens
     */
    private void finishGen() {
        antPopulation.sort((ant1, ant2) -> Double.compare(ant2.getReward(), ant1.getReward()));

        // best 5%
        int bestIdx = (int) (antPopulation.size() * 0.05);

        // half of the population dies
        int deadIdx = (int) (antPopulation.size() * 0.5);


        for (int i = 0; i < bestIdx; i++) {
            // do nothing, these individuals remain as they are
        }

        // to store the newly generated children
        Ant[] newChildren = new Ant[antPopulation.size() - bestIdx];

        for (int i = bestIdx; i < antPopulation.size(); i++) {

            // randomly select a mom ant
            Ant momAnt = antPopulation.get((int) (
                    Math.random() * deadIdx
            ));
            Ant dadAnt = antPopulation.get((int) (
                    Math.random() * deadIdx
            ));

            // shift the reward to make them positive and comparable between mom and dad
            double dadRewardPositive = dadAnt.getReward() - Math.min(dadAnt.getReward(), momAnt.getReward());
            double momRewardPositive = momAnt.getReward() - Math.min(dadAnt.getReward(), momAnt.getReward());

            double papaProbability = dadRewardPositive / (dadRewardPositive + momRewardPositive);

            Ant childAnt = new Ant(this);
            childAnt.setNetwork(new Network(momAnt.getNetwork(), dadAnt.getNetwork(), papaProbability));

            childAnt.getNetwork().mutate(0.05, 0.4);

            newChildren[i - bestIdx] = childAnt;
        }

        // replace the last generation with the new children (excluding the top 5%)
        for (int i = 0; i < newChildren.length; i++) {
            antPopulation.set(bestIdx + i, newChildren[i]);
        }

        // reset the ants to factory settings for the next generation
        for (Ant ant : antPopulation) {
            ant.resetGenerationSpecificFields();
        }
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