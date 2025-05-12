
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

    public static int amount_of_ants = MyWorld.amount_of_ants;

    public void act() {
        if (position[0] == 0.0f && position[1] == 0.0f) {
            // Initialize the vector with the current position
            position[0] = (double) getX();
            position[1] = (double) getY();
        }

        long start = System.nanoTime();
        double[] vector_temp = get_moving_vetor(amount_of_ants*amount_of_ants); // Well Hi
        MyWorld.time += System.nanoTime() - start;

        position[0] += vector_temp[0];
        position[1] += vector_temp[1];

        position[0] = Math.max(position[0], 0);
        position[0] = Math.min(position[0], getWorld().getWidth());

        position[1] = Math.max(position[1], 0);
        position[1] = Math.min(position[1], getWorld().getHeight());

        setLocation((int) position[0], (int) position[1]); // Move ant
    }
}
