import java.util.*;
import java.io.*;

/**
 * Represents the entire neural network structure
 */
class Network {
    /**
     * Array of layer configurations
     */
    private Layer[] layers;

    /**
     * Total number of layers in the network
     */
    private int size;

    /**
     * Filename for storing/loading weights
     */
    private static final String WEIGHTS_FILENAME = "weights.bin";

    /**
     * 3D array of network weights: - First dimension: layer index - Second
     * dimension: input neuron index (including bias) - Third dimension: output
     * neuron index
     */
    private double[][][] weights;

    /**
     * Creates and initializes a new neural network
     *
     * @param layers Array of layer configurations
     */
    public Network(Layer[] layers) {
        this.layers = layers;
        this.size = layers.length;

        // Allocate memory for layers
        this.weights = new double[size - 1][][];

        for (int layer = 0; layer < size - 1; layer++) {
            // Add +1 for bias weights
            this.weights[layer] = new double
            [layers[layer].length() + 1]
            [layers[layer + 1].length()];
        }

        // Check if weights file exists, if it does - load weights
        // If not - randomize and save
        if (!loadWeights()) {
            randomizeWeights();
            saveWeights();
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

    /**
     * Saves network weights to a file
     *
     * @return true if save was successful, false otherwise
     */
    public boolean saveWeights() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(WEIGHTS_FILENAME))) {
            // Write network size
            dos.writeInt(size);

            // For each layer, write dimensions
            for (int layer = 0; layer < size - 1; layer++) {
                dos.writeInt(layers[layer].length() + 1); // +1 for bias
                dos.writeInt(layers[layer + 1].length());

                // Write weights for this layer
                for (int i = 0; i < layers[layer].length() + 1; i++) {
                    for (int j = 0; j < layers[layer + 1].length(); j++) {
                        dos.writeDouble(weights[layer][i][j]);
                    }
                }
            }

            System.out.println("Weights saved to " + WEIGHTS_FILENAME);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving weights: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads network weights from a file
     *
     * @return true if load was successful, false otherwise
     */
    public boolean loadWeights() {
        File file = new File(WEIGHTS_FILENAME);
        if (!file.exists()) {
            System.out.println("Weights file not found. Will randomize weights.");
            return false;
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(WEIGHTS_FILENAME))) {
            // Read network size and verify
            int savedSize = dis.readInt();
            if (savedSize != size) {
                System.err.println("Saved network size doesn't match current network.");
                return false;
            }

            // Read each layer
            for (int layer = 0; layer < size - 1; layer++) {
                int inputSize = dis.readInt();
                int outputSize = dis.readInt();

                // Verify dimensions
                if (inputSize != layers[layer].length() + 1
                || outputSize != layers[layer + 1].length()) {
                    System.err.println("Saved layer dimensions don't match current network.");
                    return false;
                }

                // Read weights
                for (int i = 0; i < layers[layer].length() + 1; i++) {
                    for (int j = 0; j < layers[layer + 1].length(); j++) {
                        weights[layer][i][j] = dis.readDouble();
                    }
                }
            }

            System.out.println("Weights loaded from " + WEIGHTS_FILENAME);
            return true;
        } catch (IOException e) {
            System.err.println("Error loading weights: " + e.getMessage());
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
}