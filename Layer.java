import java.util.*;
import java.io.*;

/**
 * Represents a single layer in the neural network
 */
public record Layer(int length, ActivationFunction_I activationFunction) {}