
import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * One single agent
 */
public class Ant extends Actor {

    private static final int RAY_COUNT = 8;
    private static final double MAX_RAY_TRAVEL_DISTANCE = 300; // @Adam choose

    private static final Network DEFAULT_NETWORK = 
        new Network(
            new Layer[]{
                new Layer(RAY_COUNT, ActivationFunctions::linear), // rays as input
                new Layer(8, ActivationFunctions::tanh),
                new Layer(1, ActivationFunctions::tanh) // angle as single output
            }
        );

    /**
     * The ant's (constant) speed in pixels/frame
     */
    private static final int ANT_SPEED = 1;

    private static final int MAX_DELTA_ANGLE_PER_FRAME_DEGREES = 10;

    /**
     * Debug/Test tool to see the rays
     */
    private static final boolean shouldDrawRays = true;

    /**
     * The ant's brain
     */
    private Network network;

    public Ant() {
        network = new Network(DEFAULT_NETWORK.getLayers());
    }

    /**
     * Get the rays from the ant
     * @return Array of distances the rays traveled
     */
    public double[] getRayDistances() {

        GreenfootImage bg = getWorld().getBackground();
        double[] array = new double[RAY_COUNT];
        for (int ray = 0; ray < RAY_COUNT; ray++) {
            double angle = ray / (double) RAY_COUNT * Math.PI * 2.0f;
            double x1 = Math.cos(angle);
            double y1 = Math.sin(angle);

            Color wallColor = Color.BLACK;

            for (int radius = 0; radius < MAX_RAY_TRAVEL_DISTANCE; radius++) {

                int[] coordinates = {
                        (int)(getX() + x1 * radius),
                        (int)(getY() + y1 * radius)
                    };

                // check if is in world
                if (coordinates[0] > 0 && coordinates[0] < getWorld().getWidth() && coordinates[1] > 0 && coordinates[1] < getWorld().getHeight()) {

                    if (getWorld().getColorAt(coordinates[0], coordinates[1]).equals(wallColor)) {
                        array[ray] = radius; // CHANGED: removed 'MAX_RAY_TRAVEL_DISTANCE - radius'

                        if (shouldDrawRays) {
                            bg.setColor(Color.RED);
                            bg.drawLine(
                                getX(),
                                getY(),
                                (int)(getX() + x1 * (radius - 1)), // -1 to not cover wall
                                (int)(getY() + y1 * (radius - 1))
                            );
                        }
                        break;
                    }
                }
            }

        }
        return array;
    }

    /**
     * Converts a distance in pixels to a NN input in range [0; 1]
     */
    private double rayDistanceToNNInput(double rayDistance) {
        // to be perfected
        return (MAX_RAY_TRAVEL_DISTANCE - rayDistance) / MAX_RAY_TRAVEL_DISTANCE;

    }
    public void act() {
        long start = System.nanoTime();
        // FUNCTIONS
        MyWorld.time += System.nanoTime() - start;

        double[] rayDistancesTraveled = getRayDistances();

        for (int i = 0; i < RAY_COUNT; i++) {
            System.out.println("Ray #" + i + " distance traveled: " + rayDistancesTraveled[i]);
        }

        double[] inputsToNN = new double[RAY_COUNT];
        for (int i = 0; i < RAY_COUNT; i++) {
            inputsToNN[i] = rayDistanceToNNInput(rayDistancesTraveled[i]);
        }

        double deltaAngle = network.getResult(inputsToNN)[0] * MAX_DELTA_ANGLE_PER_FRAME_DEGREES; // first output because there is only 1

        System.out.println("deltaAngle: " + deltaAngle);

        // move ant, greenfoot takes care of angles when move()
        setRotation(getRotation() + (int) deltaAngle);
        move(ANT_SPEED);
    }
}
