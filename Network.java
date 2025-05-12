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
    private float[][][] weights;

    /**
     * Creates and initializes a new neural network
     *
     * @param layers Array of layer configurations
     */
    public Network(Layer[] layers) {
        this.layers = layers;
        this.size = layers.length;

        // Allocate memory for layers
        this.weights = new float[size - 1][][];

        for (int layer = 0; layer < size - 1; layer++) {
            // Add +1 for bias weights
            this.weights[layer] = new float[layers[layer].getLength() + 1][];

            for (int inputNeuron = 0; inputNeuron < layers[layer].getLength() + 1; inputNeuron++) {
                this.weights[layer][inputNeuron] = new float[layers[layer + 1].getLength()];
            }
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
            float scale = (float) Math.sqrt(2.0f / (layers[layer].getLength() + layers[layer + 1].getLength()));

            for (int i = 0; i < layers[layer].getLength() + 1; i++) {
                for (int j = 0; j < layers[layer + 1].getLength(); j++) {
                    float r = (random.nextFloat() * 2.0f - 1.0f);
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
                dos.writeInt(layers[layer].getLength() + 1); // +1 for bias
                dos.writeInt(layers[layer + 1].getLength());

                // Write weights for this layer
                for (int i = 0; i < layers[layer].getLength() + 1; i++) {
                    for (int j = 0; j < layers[layer + 1].getLength(); j++) {
                        dos.writeFloat(weights[layer][i][j]);
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
                if (inputSize != layers[layer].getLength() + 1
                        || outputSize != layers[layer + 1].getLength()) {
                    System.err.println("Saved layer dimensions don't match current network.");
                    return false;
                }

                // Read weights
                for (int i = 0; i < layers[layer].getLength() + 1; i++) {
                    for (int j = 0; j < layers[layer + 1].getLength(); j++) {
                        weights[layer][i][j] = dis.readFloat();
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
    public float[] getResult(float[] input) {
        float[] currentLayerActivations = new float[layers[0].getLength()];
        System.arraycopy(input, 0, currentLayerActivations, 0, layers[0].getLength());

        for (int layerIdx = 0; layerIdx < size - 1; layerIdx++) {
            float[] nextLayerActivations = new float[layers[layerIdx + 1].getLength()];

            // Forward propagation
            for (int inputNeuron = 0; inputNeuron < layers[layerIdx].getLength(); inputNeuron++) {
                for (int outputNeuron = 0; outputNeuron < layers[layerIdx + 1].getLength(); outputNeuron++) {
                    nextLayerActivations[outputNeuron] += currentLayerActivations[inputNeuron]
                            * weights[layerIdx][inputNeuron][outputNeuron];
                }
            }

            // Add bias terms
            for (int outputNeuron = 0; outputNeuron < layers[layerIdx + 1].getLength(); outputNeuron++) {
                nextLayerActivations[outputNeuron] += weights[layerIdx][layers[layerIdx].getLength()][outputNeuron];
            }

            // Special handling for softmax in the output layer
            ActivationFunction_I activation = layers[layerIdx + 1].getActivation();
            // Check if this layer uses softmax activation by comparing it with a reference
            // Since we can't directly compare function references, we'll use a named reference
            boolean isSoftmax = isSoftmaxActivation(activation);

            if (layerIdx == size - 2 && isSoftmax) {
                // Find max for numerical stability
                float maxActivation = nextLayerActivations[0];
                for (int i = 1; i < layers[layerIdx + 1].getLength(); i++) {
                    if (nextLayerActivations[i] > maxActivation) {
                        maxActivation = nextLayerActivations[i];
                    }
                }

                // Calculate exp(x - max) and sum
                float expSum = 0.0f;
                for (int i = 0; i < layers[layerIdx + 1].getLength(); i++) {
                    nextLayerActivations[i] = (float) Math.exp(nextLayerActivations[i] - maxActivation);
                    expSum += nextLayerActivations[i];
                }

                // Normalize
                for (int i = 0; i < layers[layerIdx + 1].getLength(); i++) {
                    nextLayerActivations[i] /= expSum;
                }
            } else {
                // Regular activation for other layers
                for (int outputNeuron = 0; outputNeuron < layers[layerIdx + 1].getLength(); outputNeuron++) {
                    nextLayerActivations[outputNeuron]
                            = activation.apply(nextLayerActivations[outputNeuron]);
                }
            }

            // Swap buffers
            currentLayerActivations = nextLayerActivations;
        }

        return currentLayerActivations;
    }

    /**
     * Helper method for softmax activation function This is used to check if a
     * layer is using softmax activation
     *
     * @param input Input value
     * @return Output value (not actually used by softmax implementation)
     */
    private float softmaxSingle(float input) {
        // This is a placeholder - the actual softmax is computed for the entire layer
        return ActivationFunctions.softmaxSingle(input);
    }

    /**
     * Checks if the given activation function is the softmax function
     *
     * @param func Activation function to check
     * @return true if the function is softmax, false otherwise
     */
    private boolean isSoftmaxActivation(ActivationFunction_I func) {
        // In Java we can't directly compare function references, so we'll use a specific method
        // For this example, we'll check if the function has the same behavior as our softmax function
        // by using a simple test value
        float testValue = 1.0f;
        return Math.abs(func.apply(testValue) - ActivationFunctions.softmaxSingle(testValue)) < 0.0001f;
    }
}