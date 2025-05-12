
import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class ant here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class ant extends Actor {

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

    public float[] position = {0.0f, 0.0f}; // X, Y

    private float[] get_moving_vetor(int amount) {
        float[] vector = new float[2];

        for (int i = 0; i < amount; i++) {
            float[] position = {((ant) getWorld().getObjects(ant.class).get(i)).getX() - getX(), ((ant) getWorld().getObjects(ant.class).get(i)).getY() - getY()};
            float[] vector_temp = network.getResult(position);

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
            position[0] = (float) getX();
            position[1] = (float) getY();
        }
        
        float[] vector_temp = get_moving_vetor(amount_of_ants*amount_of_ants); // Well Hi

        position[0] += vector_temp[0];
        position[1] += vector_temp[1];

        if (position[0] > getWorld().getWidth()) {
            position[0] = getWorld().getWidth();
        } else if (position[0] < 0) {
            position[0] = 0;
        }

        if (position[1] > getWorld().getHeight()) {
            position[1] = getWorld().getHeight();
        } else if (position[1] < 0) {
            position[1] = 0;
        }

        setLocation((int) position[0], (int) position[1]); // Move ant
    }
}
