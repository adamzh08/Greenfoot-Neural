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

    /// Ant settings
    public static final int RAY_COUNT = 16;
    public static final double MAX_RAY_TRAVEL_DISTANCE = 100;
    public static final int MAX_ANT_SPEED = 1;
    public static final double MAX_DELTA_ANGLE_PER_FRAME = Math.toRadians(10);
    public static final double FIELD_OF_VIEW_PERCENTAGE = 150 / 360.;


    public static final Network DEFAULT_NETWORK =
            new Network(
                    new Layer[]{
                            // +1 for internal compass (own current rotation)
                            // +1 for linear time progress (beginning = 0, end = 1)
                            new Layer(RAY_COUNT +1 +1, ActivationFunctions::linear), // rays as input
                            new Layer(4, ActivationFunctions::relu),

                            // +1 for rotation change
                            // +1 for velocity
                            new Layer(+1 +1, ActivationFunctions::tanh) // angle as single output
                    }
            );


    /// Debug/Test tool to see the rays
    public static boolean shouldDrawRays = false;


    private final GameManager gameManager;


    /// The ant's brain
    private Network network;

    private double[] position;
    private double rotationAngle;

    private int amountOfWallCollisions;

    public Ant(GameManager gameManager) {
        this.gameManager = gameManager;

        network = new Network(DEFAULT_NETWORK.getLayers());

        resetGenerationSpecificFields();
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

            distances[rayIdx] = Math.sqrt(
                    (getX() - hitCoordinates[0]) * (getX() - hitCoordinates[0])
                            + (getY() - hitCoordinates[1]) * (getY() - hitCoordinates[1])
            );

            if (shouldDrawRays) {
                gc.setStroke(Color.RED);
                gc.strokeLine(getX(), getY(), hitCoordinates[0], hitCoordinates[1]);
            }
        }
        return distances;
    }

    /// Converts a distance in pixels to a NN input in range [0; 1]

    private double rayDistanceToNNInput(double rayDistance) {
        // to be perfected
        return (MAX_RAY_TRAVEL_DISTANCE - rayDistance) / MAX_RAY_TRAVEL_DISTANCE;
    }

    /**
     * @param gc is the canvas where you can draw stuff
     * @param timeProgress in range [0; 1] where 0 = just born, 1 = dead
     */
    public void act(GraphicsContext gc, double timeProgress) {

        double[] rayDistancesTraveled = getRayDistances(gc);

        double[] inputsToNN = new double[network.getLayers()[0].length()];

        // most inputs for rays
        for (int i = 0; i < RAY_COUNT; i++) {
            inputsToNN[i] = rayDistanceToNNInput(rayDistancesTraveled[i]);
        }

        // last for internal compass
        inputsToNN[inputsToNN.length - 2] = rotationAngle; // current rotation
        inputsToNN[inputsToNN.length - 1] = timeProgress;

        double[] networkResults = network.getResult(inputsToNN);

        double deltaAngle = networkResults[0] * MAX_DELTA_ANGLE_PER_FRAME; // first output because there is only 1
        double speed = networkResults[1] * MAX_ANT_SPEED;

        rotationAngle += deltaAngle;

        move(speed);
    }

    /**
     * Moves the ant with wall checks
     * @param speed is distance in pixels the ant will travel this frame
     */
    private void move(double speed) {
        // default next position
        double[] nextFramePosition = {
                position[0] + Math.cos(rotationAngle) * speed,
                position[1] + Math.sin(rotationAngle) * speed
        };

        LineSegmentWall path = new LineSegmentWall(position[0], position[1], nextFramePosition[0], nextFramePosition[1]);

        for (LineSegmentWall wall : gameManager.getWalls()) {
            double[] pathWallIntersection = Ray.intersectionCoordinates(path, wall);

            if (pathWallIntersection != null) {
                amountOfWallCollisions++;
                rotationAngle += Math.PI; // 180° rotation (bounce), not the most optimal solution

                nextFramePosition = position; // the ant will not move this frame
                break;
            }
        }

        position = nextFramePosition;

        position[0] = Math.max(position[0], 0);
        position[0] = Math.min(position[0], 1000);
        position[1] = Math.max(position[1], 0);
        position[1] = Math.min(position[1], 800);
    }

    public double getReward() {
        double progress = getX() / 1000;

        return progress - 0.3 * amountOfWallCollisions;
    }

    public void resetGenerationSpecificFields() {
        amountOfWallCollisions = 0;
        rotationAngle = 0;
        position = new double[]{50, 400};
    }

    public void setNetwork(Network newNet) {
        network = newNet;
    }
    public Network getNetwork() {
        return network;
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
