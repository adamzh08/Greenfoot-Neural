import java.util.*;
import java.io.*;

/**
 * Represents a single layer in the neural network
 */
class Layer {

    /**
     * Number of neurons in this layer
     */
    private int length;

    /**
     * Activation function used in this layer
     */
    private ActivationFunction_I activation;

    /**
     * Constructor for a neural network layer
     *
     * @param length Number of neurons in this layer
     * @param activation Activation function used in this layer
     */
    public Layer(int length, ActivationFunction_I activation) {
        this.length = length;
        this.activation = activation;
    }

    /**
     * @return Number of neurons in this layer
     */
    public int getLength() {
        return length;
    }

    /**
     * @return Activation function used in this layer
     */
    public ActivationFunction_I getActivation() {
        return activation;
    }
}