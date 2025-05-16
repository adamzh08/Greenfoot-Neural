package de.adamyan.antsimulation.NN;

/**
 * Functional interface for activation functions
 */
@FunctionalInterface
public interface ActivationFunction_I {

    double apply(double input);
}