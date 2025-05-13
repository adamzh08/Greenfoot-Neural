
import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class ant here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Ant extends Actor {

    /**
     * Act - do whatever the ant wants to do. This method is called whenever the
     * 'Act' or 'Run' button gets pressed in the environment.
     */
    private static Layer[] layers = {
        new Layer(2, ActivationFunctions::linear),
        new Layer(8, ActivationFunctions::tanh),
        new Layer(2, ActivationFunctions::tanh)
    };

    private static Network network = new Network(layers);

    public double[] position = {0.0f, 0.0f}; // X, Y

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
     * @param RADIUS Radius of the rays
     * @param RAYS   Number of rays
     * @return Array of rays
     
     */
    public double[] getRays(double x, double y, int RADIUS, int RAYS) {
        double[] array = new double[RAYS];

        for (int ray = 0; ray < RAYS; ray++) {
            double angle = (double) ray / (double) RAYS * Math.PI * 2.0f;
            double x1 = (double) Math.cos(angle);
            double y1 = (double) Math.sin(angle);

            Color color = Color.WHITE;

            for (int radius = 0; radius < RADIUS; radius++) {
                if (x + x1 * radius > 0 && x + x1 * radius < getWorld().getWidth() && y + y1 * radius > 0 && y + y1 * radius < getWorld().getHeight()) {
                    if (!getWorld().getColorAt((int) (x + x1 * radius), (int) (y + y1 * radius)).equals(color)) {
                        array[ray] = RADIUS - radius;
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
        //setLocation((int) position[0], (int) position[1]); // Move ant
    }
}
