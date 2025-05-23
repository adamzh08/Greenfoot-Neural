package de.adamyan.antsimulation;

import de.adamyan.antsimulation.NN.Network;
import de.adamyan.antsimulation.Physics.CircleWall;
import de.adamyan.antsimulation.Physics.LineSegmentWall;
import de.adamyan.antsimulation.Physics.Vector2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;

import java.util.*;
import java.util.stream.IntStream;

/**
 * The whole game logic happens here
 */
public class GameManager {

    private static final Image antImage = new Image("ant.png");


    /// Debug/Test tool to see the rays
    public static boolean shouldDrawRays = false;
    /// ------------------ world settings ------------------
    public static final int POPULATION_SIZE = 1000;
    private static final int STRAIGHT_WALL_AMOUNT = 0;
    private static final int CIRCLE_WALL_AMOUNT = 0;
    private static final double ELITE_PERCENTAGE = 0.10;
    private static final int GENERATION_DURATION_FRAMES = 10 * 60;
    private int genFramesLeft;
    private int genCount;

    private List<Ant> antPopulation;
    private Ant bestAnt;

    /**
     * Collection of all straight straightWalls in the game
     * Great because: Ray interceptions point can be computed fast with a few math operations and no loops
     */
    private List<LineSegmentWall> straightWalls;
    private List<CircleWall> circleWalls;

    public GameManager() {
        initLabyrinth();

        antPopulation = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            antPopulation.add(new Ant(this));
        }
        bestAnt = antPopulation.getFirst();

        genCount = 0;
        genFramesLeft = GENERATION_DURATION_FRAMES;
    }

    private void initLabyrinth() {
        straightWalls = new ArrayList<>();
        circleWalls = new ArrayList<>();
        // generate and add 'STRAIGHT_WALL_AMOUNT' random straightWalls
        for (int i = 0; i < STRAIGHT_WALL_AMOUNT; i++) {
            straightWalls.add(new LineSegmentWall(
                    new Vector2D(
                            Math.random() * 1000,
                            Math.random() * 800
                    ),
                    new Vector2D(
                            Math.random() * 1000,
                            Math.random() * 800
                    )
            ));
        }
        // and the 4 world boundaries
        straightWalls.add(new LineSegmentWall(new Vector2D(0, 0), new Vector2D(1, 800)));
        straightWalls.add(new LineSegmentWall(new Vector2D(0, 800), new Vector2D(1000, 0)));
        straightWalls.add(new LineSegmentWall(new Vector2D(1000, 800), new Vector2D(1, -800)));
        straightWalls.add(new LineSegmentWall(new Vector2D(1000, 0), new Vector2D(-1000, 0)));

        straightWalls.add(new LineSegmentWall(new Vector2D(300, 0), new Vector2D(1, 300)));
        straightWalls.add(new LineSegmentWall(new Vector2D(300, 500), new Vector2D(1, 300)));


        for (int i = 0; i < CIRCLE_WALL_AMOUNT; i++) {
            circleWalls.add(getRandomCircleWall());
        }
    }

    private CircleWall getRandomCircleWall() {
        List<Double> allAngles = IntStream.rangeClosed(1, 35)
                .map(i -> i * 10)
                .mapToDouble(Math::toRadians)
                .boxed()
                .toList();

        List<Double> randomAngles = new ArrayList<>(allAngles);
        Collections.shuffle(randomAngles);

        int segmentCount = 2 + (int)(Math.random() * 34);
        randomAngles = randomAngles.subList(0, segmentCount);

        randomAngles.addFirst(0.);
        randomAngles.addLast(Math.TAU);

        return new CircleWall(
                500,
                400,
                50 + Math.random() * 350,
                randomAngles.stream().sorted().toList()
        );
    }

    /**
     * @return generation finished
     */
    public boolean frame_logic() {
        antPopulation.parallelStream()
                .filter(Objects::nonNull)
                .forEach(ant -> ant.act(genFramesLeft / (double) GENERATION_DURATION_FRAMES));

        if (!antPopulation.contains(bestAnt)) {
            bestAnt = antPopulation.stream()
                    .filter(Objects::nonNull)
                    .findFirst()
                    .get();
        }

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

        drawWalls(gc);
        drawAnts(gc);
        if (shouldDrawRays) {
            for (Ant ant : antPopulation) {
                if (ant != null) {
                    ant.draw_rays(canvas.getGraphicsContext2D());
                }
            }
        }
        gc.setFill(new Color(0, 1, 0, 0.5));
        gc.fillOval(bestAnt.getX() - 10, bestAnt.getY() - 10, 20, 20);

        // display the time left in seconds
        gc.setFont(new Font(50));
        gc.fillText(genFramesLeft / 60 + "", 50, 50);
    }

    private void drawWalls(GraphicsContext gc) {
        gc.setLineWidth(3);
        gc.setStroke(Color.BLACK);
        for (LineSegmentWall wall : straightWalls) {
            gc.strokeLine(wall.startPos().x(), wall.startPos().y(), wall.startPos().x() + wall.wallVector().x(), wall.startPos().y() + wall.wallVector().y());
        }
        for (CircleWall wall : circleWalls) {
            List<Double> angles = wall.wallSpaceChangingAngles();
            double radius = wall.radius();
            double diameter = radius * 2;
            double arcX = wall.centerX() - radius;
            double arcY = wall.centerY() - radius;

            for (int i = 0; i < angles.size() - 1; i++) {
                boolean isWallSegment = i % 2 == 0;
                if (isWallSegment) {
                    double startRad = angles.get(i);
                    double endRad = angles.get(i + 1);

                    double startDeg = Math.toDegrees(-startRad); // Negative for clockwise
                    double extentDeg = Math.toDegrees(-(endRad - startRad));

                    gc.strokeArc(
                            arcX,
                            arcY,
                            diameter,
                            diameter,
                            startDeg,
                            extentDeg,
                            ArcType.OPEN
                    );
                }
            }
        }
    }

    /**
     * Here the whole logic for 'natural selection', 'gene combination' and mutation happens
     */
    private void finishGen() {
        List<Ant> antsAlive = antPopulation.stream().filter(Objects::nonNull).sorted((ant1, ant2) -> Double.compare(ant2.getReward(), ant1.getReward())).toList();

        int firstNonEliteIndex = (int) (POPULATION_SIZE * ELITE_PERCENTAGE);
        firstNonEliteIndex = antsAlive.size() < firstNonEliteIndex? antPopulation.size() : firstNonEliteIndex;

        for (int i = 0; i < firstNonEliteIndex; i++) {
            antPopulation.set(i, antsAlive.get(i));
        }
        for (int i = firstNonEliteIndex; i < POPULATION_SIZE; i++) {
            // randomly select parent ants
            Ant momAnt = getBestAntOfKRandom(antsAlive, 20);
            Ant dadAnt = getBestAntOfKRandom(antsAlive, 20);

            Ant childAnt = new Ant(this);
            childAnt.setNetwork(new Network(momAnt.getNetwork(), dadAnt.getNetwork(), momAnt.getReward() > dadAnt.getReward() ? 0.35 : 0.65));
            childAnt.getNetwork().mutate(0.05, 0.4);

            antPopulation.set(i, childAnt);
        }
        antPopulation.forEach(Ant::resetGenerationSpecificFields);
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
            if (ant != null) {
                gc.save();
                gc.translate(ant.getX(), ant.getY());
                gc.rotate(Math.toDegrees(ant.getAngle()));
                gc.drawImage(antImage, -antImage.getWidth() / 2, -antImage.getHeight() / 2);
                gc.restore();
            }
        }
    }

    public void disableAnt(Ant ant) {
        antPopulation.set(antPopulation.indexOf(ant), null);
    }

    public List<LineSegmentWall> getStraightWalls() {
        return straightWalls;
    }

    public List<CircleWall> getCircleWalls() {
        return circleWalls;
    }

    public List<Ant> getAntPopulation() {
        return antPopulation;
    }

    public int getGenCount() {
        return genCount;
    }

    public Ant getBestAnt() {
        return bestAnt;
    }
}