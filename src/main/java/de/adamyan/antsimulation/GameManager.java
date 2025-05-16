package de.adamyan.antsimulation;

import de.adamyan.antsimulation.Physics.LineSegmentWall;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private static final Image antImage = new Image("ant.png");

    private static final int initialPopulationSize = 1;
    private static final int wallAmount = 10;

    private List<Ant> antPopulation;

    private List<LineSegmentWall> walls;

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
    }

    public void act(Canvas canvas) {
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
            ant.move(gc);
        }

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
}
