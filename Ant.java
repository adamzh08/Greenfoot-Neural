
import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class ant here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Ant extends Actor {

    private static final int RAY_COUNT = 8;
    private static final double MAX_RAY_TRAVEL_DISTANCE = -1; // @Adam choose
    
    private static final Network DEFAULT_NETWORK = 
        new Network(
            new Layer[]{
                new Layer(2, ActivationFunctions::linear), // rays as input
                new Layer(8, ActivationFunctions::tanh),
                new Layer(1, ActivationFunctions::tanh) // angle as output
            }
        );

    private static final double ANT_SPEED = 1;

    
    /**
     * The ant's brain
     */
    private Network network;

    public double[] position = {0.0f, 0.0f}; // X, Y
    public double currentAngle = 0; // in radiants

    public Ant() {
        network = new Network(DEFAULT_NETWORK);
    }

    private double[] get_moving_vetor(int amount) {
        double[] vector = new double[2];

        for (int i = 0; i < amount; i++) {
            double[] position = {((Ant) getWorld().getObjects(Ant.class).get(i)).getX() - getX(), ((Ant) getWorld().getObjects(Ant.class).get(i)).getY() - getY()};
            double[] vector_temp = network.getResult(position);

            vector[0] += vector_temp[0];
            vector[1] += vector_temp[1];
        }

        return vector;
    }

    public void resetPosition() {
        position[0] = 0.0f;
        position[1] = 0.0f;
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
        if (position[0] == 0.0f && position[1] == 0.0f) {
            // Initialize the vector with the current position
            position[0] = (double) getX();
            position[1] = (double) getY();
        }

        long start = System.nanoTime();

        // FUNCTIONS
        MyWorld.time += System.nanoTime() - start;

        double[] rays = getRays(position[0], position[1]);

        for (int i = 0; i < rays.length; i++) {
            System.out.println((double) rays[i]);
        }

        // TODO @Adam: link rays with NN
        // My Idea: forach ray 1 input neuron. If ray doesnot hit: input 0; else input 1 / rayTravelDistanceBeforeHit
        double[] inputsToNN = null;
        
        double deltaAngle = network.getResult(inputsToNN)[0] * Math.PI; // first output because there is only 1
        
        currentAngle += deltaAngle;
        
        // move ant
        position[0] += Math.cos(currentAngle) * ANT_SPEED;
        position[1] += Math.sin(currentAngle) * ANT_SPEED;
    }
}
