
import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * One single agent
 */
public class Ant extends Actor {

    private static final int RAY_COUNT = 8;
    private static final double MAX_RAY_TRAVEL_DISTANCE = 100; // @Adam choose

    private static final Network DEFAULT_NETWORK = 
        new Network(
            new Layer[]{
                new Layer(RAY_COUNT, ActivationFunctions::linear), // rays as input
                new Layer(8, ActivationFunctions::tanh),
                new Layer(1, ActivationFunctions::tanh) // angle as output
            }
        );

    /**
     * The ant's (constant) speed in pixel/frame
     */
    private static final int ANT_SPEED = 1;

    /**
     * The ant's brain
     */
    private Network network;

    public Ant() {
        network = new Network(DEFAULT_NETWORK.getLayers());
    }

    /**
     * Get the rays from the ant
     *
     * @param x      X position of the ant
     * @param y      Y position of the ant
     * @return Array of rays

     */
    // removed "int RADIUS, int RAYS" and made them "static final"
    public double[] getRays(double x, double y) {
        double[] array = new double[RAY_COUNT];

        for (int ray = 0; ray < RAY_COUNT; ray++) {
            double angle = ray / (double) RAY_COUNT * Math.PI * 2.0f;
            double x1 = Math.cos(angle);
            double y1 = Math.sin(angle);

            Color color = Color.WHITE;

            for (int radius = 0; radius < MAX_RAY_TRAVEL_DISTANCE; radius++) {
                if (x + x1 * radius > 0 && x + x1 * radius < getWorld().getWidth() && y + y1 * radius > 0 && y + y1 * radius < getWorld().getHeight()) {
                    if (!getWorld().getColorAt((int) (x + x1 * radius), (int) (y + y1 * radius)).equals(color)) {
                        array[ray] = MAX_RAY_TRAVEL_DISTANCE - radius;
                        break;
                    }
                }
            }
        }
        return array;
    }

    public void act() {
        long start = System.nanoTime();
        // FUNCTIONS
        MyWorld.time += System.nanoTime() - start;

        double[] rays = getRays(getX(), getY());

        for (int i = 0; i < rays.length; i++) {
            System.out.println((double) rays[i]);
        }

        // TODO @Adam: link rays with NN
        // My Idea: forach ray 1 input neuron. If ray doesnot hit: input 0; else input 1 / rayTravelDistanceBeforeHit
        double[] inputsToNN = rays;

        // inputsToNN = new double[RAY_COUNT]; // test with {0, 0, 0, ...}

        double deltaAngle = network.getResult(inputsToNN)[0] * Math.PI; // first output because there is only 1

        setRotation(getRotation() + (int) deltaAngle);
        
        System.out.println(deltaAngle);

        
        // move ant, greenfoot takes care of x += Math.sin()
        setRotation(getRotation() + (int) deltaAngle);
        move(ANT_SPEED);
    }
}
