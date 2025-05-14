import java.util.*;
import java.io.*;

/**
 * Represents the entire neural network structure
 */
public class Network implements Serializable {
    /**
     * Array of layer configurations
     */
    private Layer[] layers;

    /**
     * Total number of layers in the network
     */
    private int size;

    /**
     * 3D array of network weights: - First dimension: layer index - Second
     * dimension: input neuron index (including bias) - Third dimension: output
     * neuron index
     */
    private double[][][] weights;

    /**
     * Makes a deep copy of another Neural network "other"
     */
    public Network(Network other) {
        this.layers = other.layers; // layers dont need to be deep copied
        this.size = other.size;
        this.weights = other.getWeightClone();
    }
    
    /**
     * Creates and initializes a new neural network randomly
     *
     * @param layers Array of layer configurations
     */
    public Network(Layer[] layers) {
        this.layers = layers;
        this.size = layers.length;
        
        this.weights = new double[size - 1][][];
        for (int layer = 0; layer < size - 1; layer++) {
            // Add +1 for bias weights
            this.weights[layer] = new double
            [layers[layer].length() + 1]
            [layers[layer + 1].length()];
        }
        
        randomizeWeights();
    }
    /**
     * Load a NN from a file
     */
    public static Network loadFromFile(String srcFileName) {
        File srcFile = new File(srcFileName);
        
        if (!srcFile.exists()) {
            throw new RuntimeException();
        }
        // loading of state
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(srcFile))) {
            return (Network) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes network weights with random values using Xavier/Glorot
     * initialization
     *
     * Implements Xavier/Glorot initialization which helps with: 1. Preventing
     * vanishing/exploding gradients 2. Maintaining appropriate scale of
     * gradients through the network Scale factor is calculated as sqrt(2 /
     * (fan_in + fan_out))
     */
    public void randomizeWeights() {
        Random random = new Random();

        for (int layer = 0; layer < size - 1; layer++) {
            // Xavier/Glorot initialization
            double scale = (double) Math.sqrt(2.0f / (layers[layer].length() + layers[layer + 1].length()));

            for (int i = 0; i < layers[layer].length() + 1; i++) {
                for (int j = 0; j < layers[layer + 1].length(); j++) {
                    double r = (random.nextDouble() * 2.0f - 1.0f);
                    weights[layer][i][j] = r * scale;
                }
            }
        }
    }
    
    public void mutate(double probabilityOfMutation, double maxMutationStrength) {
        for (double[][] weightArray2D : weights) {
            for (double[] weightArray : weightArray2D) {
                for (int i = 0; i < weightArray.length; i++) {
                    // change each individual weight with a chance of "probabilityOfMutation"
                    if (Math.random() < probabilityOfMutation) {
                        weightArray[i] += (2 * Math.random() - 1) * maxMutationStrength;
                    }
                }
            }
        }
    }

    /**
     * Saves network weights to a file
     *
     * @return true if save was successful, false otherwise
     */
    public boolean writeToFile(String tarFileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tarFileName))) {
            oos.writeObject(this);
            
            System.out.println("Weights saved to " + tarFileName);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving weights: " + e.getMessage());
            return false;
        }
    }

    /**
     * Performs forward propagation through the network
     *
     * This function: 1. Propagates input through each layer 2. Applies weights
     * and biases 3. Handles special case for softmax in output layer 4. Applies
     * activation functions 5. Returns final layer output
     *
     * @param input Array of input values
     * @return Array containing output layer activations
     */
    public double[] getResult(double[] input) {
        // removed clone, since currentLayerActivations is not mutated
        Objects.requireNonNull(input);
        double[] currentLayerActivations = input;

        for (int layerIdx = 0; layerIdx < size - 1; layerIdx++) {
            double[] nextLayerActivations = new double[layers[layerIdx + 1].length()];

            // Forward propagation
            for (int inputNeuron = 0; inputNeuron < layers[layerIdx].length(); inputNeuron++) {
                for (int outputNeuron = 0; outputNeuron < layers[layerIdx + 1].length(); outputNeuron++) {
                    nextLayerActivations[outputNeuron] += currentLayerActivations[inputNeuron]
                    * weights[layerIdx][inputNeuron][outputNeuron];
                }
            }

            // Add bias terms
            for (int outputNeuron = 0; outputNeuron < layers[layerIdx + 1].length(); outputNeuron++) {
                nextLayerActivations[outputNeuron] += weights[layerIdx][layers[layerIdx].length()][outputNeuron];
            }

            // Activation function
            ActivationFunction_I activation = layers[layerIdx + 1].activationFunction();
            
            for (int outputNeuron = 0; outputNeuron < layers[layerIdx + 1].length(); outputNeuron++) {
                nextLayerActivations[outputNeuron]
                = activation.apply(nextLayerActivations[outputNeuron]);
            }

            // Swap buffers
            currentLayerActivations = nextLayerActivations;
        }

        return currentLayerActivations;
    }
    
    public double[][][] getWeightClone() {
        double[][][] weightClone = new double[size][][];
        
        for (int layerIdx = 0; layerIdx < size - 1; layerIdx++) {
            int inputCount = layers[layerIdx].length() + 1;
            int outputCount = layers[layerIdx + 1].length();
            weightClone[layerIdx] = new double[inputCount][]; // alternative: new double[inputCount][outputCount]
            
            for (int inputIdx = 0; inputIdx < inputCount; inputIdx++) {
                weightClone[layerIdx][inputIdx] = weights[layerIdx][inputIdx].clone();
            }
        }
        
        return weightClone;
    }
    
    public Layer[] getLayers() {
        return layers;
    }
}