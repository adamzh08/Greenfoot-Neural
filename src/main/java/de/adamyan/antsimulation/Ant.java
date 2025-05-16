package de.adamyan.antsimulation;

import de.adamyan.antsimulation.NN.*;
import de.adamyan.antsimulation.Physics.LineSegmentWall;
import de.adamyan.antsimulation.Physics.Ray;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * One single agent
 */
public class Ant {

    // Ant settings

    public static final int RAY_COUNT = 16;
    public static final double MAX_RAY_TRAVEL_DISTANCE = 50;
    public static final int ANT_SPEED = 1;
    public static final double MAX_DELTA_ANGLE_PER_FRAME = Math.toRadians(10);
    public static final double FIELD_OF_VIEW_PERCENTAGE = 150 / 360.;


    public static final Network DEFAULT_NETWORK =
            new Network(
                    new Layer[]{
                            new Layer(RAY_COUNT, ActivationFunctions::linear), // rays as input
                            new Layer(8, ActivationFunctions::relu),
                            new Layer(8, ActivationFunctions::relu),
                            new Layer(1, ActivationFunctions::tanh) // angle as single output
                    }
            );



    /// Debug/Test tool to see the rays
    private static final boolean shouldDrawRays = true;


    private final GameManager gameManager;

    /// The ant's brain
    private Network network;

    private double[] position;
    private double rotationAngle;

    public Ant(GameManager gameManager) {
        this.gameManager = gameManager;

        network = new Network(DEFAULT_NETWORK.getLayers());

        position = new double[]{400, 400};
        rotationAngle = 0;
    }

    /**
     * Get the rays from the ant
     *
     * @return Array of distances the rays traveled
     */
    public double[] getRayDistances(GraphicsContext gc) {
        double[] distances = new double[RAY_COUNT];

        gc.setLineWidth(0.1);

        for (int rayIdx = 0; rayIdx < RAY_COUNT; rayIdx++) {
            double[] hitCoordinates = Ray.cast(this, rayIdx).getIntersection(gameManager);

            if (hitCoordinates != null) {
                distances[rayIdx] = Math.sqrt(
                        (getX() - hitCoordinates[0]) * (getX() - hitCoordinates[0])
                        + (getY() - hitCoordinates[1]) * (getY() - hitCoordinates[1])
                );

                if (shouldDrawRays) {
                    gc.setStroke(Color.RED);
                    gc.strokeLine(getX(), getY(), hitCoordinates[0], hitCoordinates[1]);
                }
            }
        }
        return distances;
    }

    /// Converts a distance in pixels to a NN input in range [0; 1]

    private double rayDistanceToNNInput(double rayDistance) {
        // to be perfected
        return (MAX_RAY_TRAVEL_DISTANCE - rayDistance) / MAX_RAY_TRAVEL_DISTANCE;
    }

    public void move(GraphicsContext gc) {

        double[] rayDistancesTraveled = getRayDistances(gc);

        double[] inputsToNN = new double[RAY_COUNT];
        for (int i = 0; i < RAY_COUNT; i++) {
            inputsToNN[i] = rayDistanceToNNInput(rayDistancesTraveled[i]);
        }

        double deltaAngle = network.getResult(inputsToNN)[0] * MAX_DELTA_ANGLE_PER_FRAME; // first output because there is only 1

        rotationAngle += deltaAngle;
        move();
    }

    private void move() {
        double[] nextFramePosition = {
                position[0] + Math.cos(rotationAngle) * ANT_SPEED,
                position[1] + Math.sin(rotationAngle) * ANT_SPEED
        };

        LineSegmentWall path = new LineSegmentWall(position[0], position[1], nextFramePosition[0], nextFramePosition[1]);

        boolean crossedAny = false;
        for (LineSegmentWall wall : gameManager.getWalls()) {
            double[] pathWallIntersection = Ray.intersectionCoordinates(path, wall);

            if (pathWallIntersection != null) {
                rotationAngle += Math.PI; // 180° rotation (bounce)
                crossedAny = true;
                break;
            }
        }

        if (!crossedAny) {
            position = nextFramePosition;
        }

        position[0] = Math.max(position[0], 0);
        position[0] = Math.min(position[0], 1000);
        position[1] = Math.max(position[1], 0);
        position[1] = Math.min(position[1], 800);
    }

    public double getX() {
        return position[0];
    }
    public double getY() {
        return position[1];
    }
    public double getAngle() {
        return rotationAngle;
    }
}
