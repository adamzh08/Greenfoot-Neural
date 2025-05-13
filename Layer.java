import java.util.*;
import java.io.*;

/**
 * Represents a single layer in the neural network
 */
// class -> immutable record (automaticly generated construtor & getters)
record Layer(int length, ActivationFunction_I activationFunction) {}