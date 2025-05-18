package de.adamyan.antsimulation;

import de.adamyan.antsimulation.NN.Network;
import de.adamyan.antsimulation.Physics.LineSegmentWall;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameManager {

    private static final Image antImage = new Image("ant.png");

    private static final int initialPopulationSize = 5000;
    private static final int wallAmount = 10;

    private static final int generationDuration = 30 * 1000;

    private List<Ant> antPopulation;

    private List<LineSegmentWall> walls;

    private double genTimeLeft;

    private int genCount;

    /// map dimensions
    private final double width;
    private final double height;

    public GameManager(double width, double height) {
        this.width = width;
        this.height = height;

        walls = new ArrayList<>();
        for (int i = 0; i < wallAmount; i++) {
            walls.add(new LineSegmentWall(Math.random() * 1000, Math.random() * 800, Math.random() * 1000, Math.random() * 800));
        }
        walls.add(new LineSegmentWall(0, 0, 0, 800));
        walls.add(new LineSegmentWall(0, 800, 1000, 800));
        walls.add(new LineSegmentWall(1000, 800, 1000, 0));
        walls.add(new LineSegmentWall(1000, 0, 0, 0));

        antPopulation = new ArrayList<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            antPopulation.add(new Ant(this));
        }

        genCount = 0;
        genTimeLeft = generationDuration;
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


        for (Ant ant : antPopulation) {
            ant.move(gc, genTimeLeft / generationDuration);
        }

        drawAnts(gc);

        gc.setFont(new Font(50));
        gc.fillText((int)(genTimeLeft / 1000) + "", 50, 50);


        genTimeLeft -= deltaTime;
        if (genTimeLeft <= 0) {
            finishGen();
            genCount++;
            genTimeLeft = generationDuration;
            return true;
        }
        return false;
    }

    private void finishGen() {
        antPopulation.sort((ant1, ant2) -> Double.compare(ant2.getReward(), ant1.getReward()));

        // best 5%
        int bestIdx = (int) (antPopulation.size() * 0.05);

        // half of the population dies
        int deadIdx = (int) (antPopulation.size() * 0.5);


        for (int i = 0; i < bestIdx; i++) {
            // do nothing, these individuals remain as they are
        }

        Ant[] newChildren = new Ant[antPopulation.size() - bestIdx];

        for (int i = bestIdx; i < antPopulation.size(); i++) {

            Ant mamaAnt = antPopulation.get((int) (
                    Math.random() * deadIdx
            ));
            Ant papaAnt = antPopulation.get((int) (
                    Math.random() * deadIdx
            ));

            double papaRewardPositive = papaAnt.getReward() - Math.min(papaAnt.getReward(), mamaAnt.getReward());
            double mamaRewardPositive = mamaAnt.getReward() - Math.min(papaAnt.getReward(), mamaAnt.getReward());

            double papaProbability = papaRewardPositive / (papaRewardPositive + mamaRewardPositive);

            Ant childAnt = new Ant(this);
            childAnt.setNetwork(new Network(mamaAnt.getNetwork(), papaAnt.getNetwork(), papaProbability));

            childAnt.getNetwork().mutate(0.05, 0.4);

            newChildren[i - bestIdx] = childAnt;
        }

        for (int i = 0; i < newChildren.length; i++) {
            antPopulation.set(bestIdx + i, newChildren[i]);
        }

        for (Ant ant : antPopulation) {
            ant.resetPositionAndRotation();
            ant.resetReward();
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
