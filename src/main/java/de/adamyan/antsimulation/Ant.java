package de.adamyan.antsimulation;

import de.adamyan.antsimulation.NN.*;
import de.adamyan.antsimulation.Physics.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Optional;

/**
 * One single agent
 */
public class Ant {

    /// Ant settings
    public static final int RAY_COUNT = 16;
    public static final double MAX_RAY_TRAVEL_DISTANCE = 200;
    public static final int MAX_ANT_SPEED = 3;
    public static final double MAX_DELTA_ANGLE_PER_FRAME = Math.toRadians(10);
    public static final double FIELD_OF_VIEW_PERCENTAGE = 50 / 360.;


    public static final Layer[] LAYERS = {
            // +1 for internal compass (own current rotation)
            // +1 for linear time progress (beginning = 0, end = 1)
            new Layer(RAY_COUNT + 1 + 1, ActivationFunctions::linear), // rays as input
            new Layer(4, ActivationFunctions::relu),
            // +1 for rotation change
            // +1 for velocity
            new Layer(+1 + 1, ActivationFunctions::tanh) // angle as single output
    };

    private final GameManager gameManager;


    /// The ant's brain
    private Network network;

    private Vector2D position;
    private double rotationAngle;

    private int amountOfWallCollisions;

    public Ant(GameManager gameManager) {
        this.gameManager = gameManager;

        network = new Network(LAYERS);

        resetGenerationSpecificFields();
    }

    /**
     * Get the rays from the ant
     *
     * @return Array of distances the rays traveled
     */
    public double[] getRayDistances() {
        double[] distances = new double[RAY_COUNT];

        for (int rayIdx = 0; rayIdx < RAY_COUNT; rayIdx++) {
            RayCast rayCast = castWithIndex(rayIdx);
            Optional<double[]> hitCoordinates = rayCast.getIntersection(gameManager);

            if (hitCoordinates.isEmpty()) {
                hitCoordinates = Optional.of(new double[]{getX() + rayCast.cosAngle() * rayCast.length(), getY() + rayCast.sinAngle() * rayCast.length()});
            }

            distances[rayIdx] = Math.sqrt(
                    (getX() - hitCoordinates.get()[0]) * (getX() - hitCoordinates.get()[0])
                            + (getY() - hitCoordinates.get()[1]) * (getY() - hitCoordinates.get()[1])
            );
        }
        return distances;
    }

    /// Converts a distance in pixels to a NN input in range [0;1]
    private double rayDistanceToNNInput(double rayDistance) {
        return (MAX_RAY_TRAVEL_DISTANCE - rayDistance) / MAX_RAY_TRAVEL_DISTANCE;
    }

    /**
     * @param timeProgress in range [0; 1] where 0 = just born, 1 = dead
     */
    public void act(double timeProgress) {

        double[] rayDistancesTraveled = getRayDistances();

        double[] inputsToNN = new double[network.getLayers()[0].length()];

        // most inputs for rays
        for (int i = 0; i < RAY_COUNT; i++) {
            inputsToNN[i] = rayDistanceToNNInput(rayDistancesTraveled[i]);
        }

        // last for internal compass
        inputsToNN[inputsToNN.length - 2] = rotationAngle; // current rotation
        inputsToNN[inputsToNN.length - 1] = timeProgress;  // time left to live

        double[] networkResults = network.getResult(inputsToNN);

        double deltaAngle = networkResults[0] * MAX_DELTA_ANGLE_PER_FRAME; // first output because there is only 1
        double speed = networkResults[1] * MAX_ANT_SPEED;

        rotationAngle += deltaAngle;

        move(speed);
    }

    /**
     * Moves the ant with wallVector checks
     *
     * @param speed is distance in pixels the ant will travel this frame
     */

    private void move(double speed) {
        Vector2D addedPosition = new Vector2D(
                Math.cos(rotationAngle) * speed,
                Math.sin(rotationAngle) * speed
        );

        Optional<double[]> intersection = new RayCast(
                getX(),
                getY(),
                rotationAngle,
                Math.cos(rotationAngle),
                Math.sin(rotationAngle),
                addedPosition.magnitude()
        ).getIntersection(gameManager);

        if (intersection.isEmpty()) {
            position.add(addedPosition);
        } else {
            gameManager.disableAnt(this);
        }
    }


    public void draw_rays(GraphicsContext gc) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(0.1);

        for (int rayIdx = 0; rayIdx < RAY_COUNT; rayIdx++) {
            RayCast rayCast = castWithIndex(rayIdx);
            Optional<double[]> hitCoordinates = rayCast.getIntersection(gameManager);

            if (hitCoordinates.isEmpty()) {
                hitCoordinates = Optional.of(new double[]{getX() + rayCast.cosAngle() * rayCast.length(), getY() + +rayCast.sinAngle() * rayCast.length()});
            }

            gc.setStroke(Color.RED);
            gc.strokeLine(getX(), getY(), hitCoordinates.get()[0], hitCoordinates.get()[1]);
        }
    }

    public double getReward() {
        double progress = getX() / 1000;

        return progress - 0.3 * amountOfWallCollisions;
    }

    public void resetGenerationSpecificFields() {
        amountOfWallCollisions = 0;
        rotationAngle = 0;
        position = new Vector2D(500, 400);
    }

    public RayCast castWithIndex(int rayIdx) {
        double rayAngle = getAngleOfRay(rayIdx);
        return new RayCast(
                getX(),
                getY(),
                rayAngle,
                Math.cos(rayAngle),
                Math.sin(rayAngle),
                Ant.MAX_RAY_TRAVEL_DISTANCE
        );
    }

    public double getAngleOfRay(int rayIdx) {
        double rayIdx_between0And1 = rayIdx / (double) Ant.RAY_COUNT;
        double offsetAngle = (Ant.FIELD_OF_VIEW_PERCENTAGE * (rayIdx_between0And1 - 0.5)) * Math.TAU;
        return getAngle() + offsetAngle;
    }

    public void setNetwork(Network newNet) {
        network = newNet;
    }

    public Network getNetwork() {
        return network;
    }

    public double getX() {
        return position.x();
    }

    public double getY() {
        return position.y();
    }

    public double getAngle() {
        return rotationAngle;
    }
}
