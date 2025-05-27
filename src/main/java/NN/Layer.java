package NN;

/**
 * Represents a single layer in the neural network
 */
public record Layer(int length, ActivationFunction_I activationFunction) {
    @Override
    public String toString() {
        return length + "";
    }
}