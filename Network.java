import java.io.*;
import java.util.Random;

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
    private ActivationFunction activation;

    /**
     * Constructor for a neural network layer
     *
     * @param length Number of neurons in this layer
     * @param activation Activation function used in this layer
     */
    public Layer(int length, ActivationFunction activation) {
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
    public ActivationFunction getActivation() {
        return activation;
    }
}

/**
 * Functional interface for activation functions
 */
@FunctionalInterface
interface ActivationFunction {

    float apply(float input);
}

/**
 * Collection of activation functions for neural networks
 */
class ActivationFunctions {

    public static float linear(float x) {
        return x;
    }

    /**
     * Sigmoid activation function
     *
     * Characteristics: - Smooth, continuous function - Output range: (0,1) -
     * Commonly used in binary classification - Can cause vanishing gradient
     * problems
     *
     * @param x Input value
     * @return Output in range (0,1)
     */
    public static float sigmoid(float x) {
        return 1.0f / (1.0f + (float) Math.exp(-x));
    }

    /**
     * Hyperbolic tangent activation function
     *
     * Characteristics: - Zero-centered output - Output range: (-1,1) - Stronger
     * gradients than sigmoid - Still can have vanishing gradient issues
     *
     * @param x Input value
     * @return Output in range (-1,1)
     */
    public static float tanh(float x) {
        return (float) Math.tanh(x);
    }

    /**
     * Rectified Linear Unit (ReLU) activation function
     *
     * Characteristics: - Simple and computationally efficient - No vanishing
     * gradient for positive values - Can cause "dying ReLU" problem - Most
     * commonly used activation in modern networks
     *
     * @param x Input value
     * @return max(0,x)
     */
    public static float relu(float x) {
        return x > 0.0f ? x : 0.0f;
    }

    /**
     * Leaky ReLU activation function
     *
     * Characteristics: - Prevents dying ReLU problem - Small gradient for
     * negative values - No vanishing gradient
     *
     * @param x Input value
     * @param alpha Slope for negative values (typically small, e.g., 0.01)
     * @return x if x > 0, alpha * x otherwise
     */
    public static float lrelu(float x, float alpha) {
        return x > 0.0f ? x : alpha * x;
    }

    /**
     * Leaky ReLU with default alpha value
     *
     * @param x Input value
     * @return x if x > 0, 0.01 * x otherwise
     */
    public static float lrelu(float x) {
        return lrelu(x, 0.01f);
    }

    /**
     * Parametric ReLU activation function
     *
     * Characteristics: - Similar to Leaky ReLU but with learnable alpha - More
     * flexible than standard ReLU - Requires additional parameter training
     *
     * @param x Input value
     * @param alpha Learnable parameter for negative values
     * @return x if x > 0, alpha * x otherwise
     */
    public static float prelu(float x, float alpha) {
        return x > 0.0f ? x : alpha * x;
    }

    /**
     * Exponential Linear Unit activation function
     *
     * Characteristics: - Smooth function including at x=0 - Can produce
     * negative values - Better handling of noise - Self-regularizing
     *
     * @param x Input value
     * @param alpha Scale for the negative part
     * @return x if x ≥ 0, alpha * (exp(x) - 1) otherwise
     */
    public static float elu(float x, float alpha) {
        return x >= 0.0f ? x : alpha * ((float) Math.exp(x) - 1.0f);
    }

    /**
     * ELU with default alpha value
     *
     * @param x Input value
     * @return x if x ≥ 0, (exp(x) - 1) otherwise
     */
    public static float elu(float x) {
        return elu(x, 1.0f);
    }

    /**
     * Single-input softmax for network structure
     *
     * Note: This is only part of the softmax calculation. Full normalization
     * happens in the network forward pass.
     *
     * @param x Input value
     * @return Exponential of input (partial softmax)
     */
    public static float softmaxSingle(float x) {
        return (float) Math.exp(x);
    }

    /**
     * Softmax activation function for entire layer
     *
     * Characteristics: - Converts inputs to probability distribution - Outputs
     * sum to 1.0 - Commonly used in classification - Numerically stable
     * implementation
     *
     * @param input Array of input values
     * @param output Array to store results
     * @param size Length of input/output arrays
     */
    public static void softmax(float[] input, float[] output, int size) {
        float maxVal = input[0];
        for (int i = 1; i < size; i++) {
            if (input[i] > maxVal) {
                maxVal = input[i];
            }
        }

        float sum = 0.0f;
        for (int i = 0; i < size; i++) {
            output[i] = (float) Math.exp(input[i] - maxVal);
            sum += output[i];
        }

        for (int i = 0; i < size; i++) {
            output[i] /= sum;
        }
    }

    /**
     * Gaussian Error Linear Unit (GELU) activation
     *
     * Characteristics: - Smooth approximation of ReLU - Used in modern
     * transformers - Combines properties of dropout and ReLU - More
     * computationally expensive
     *
     * @param x Input value
     * @return GELU activation value
     */
    public static float gelu(float x) {
        double sqrt2OverPi = Math.sqrt(2 / Math.PI);
        return (float) (0.5 * x * (1 + Math.tanh(sqrt2OverPi * (x + 0.044715 * Math.pow(x, 3)))));
    }
}

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
            ActivationFunction activation = layers[layerIdx + 1].getActivation();
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
    private boolean isSoftmaxActivation(ActivationFunction func) {
        // In Java we can't directly compare function references, so we'll use a specific method
        // For this example, we'll check if the function has the same behavior as our softmax function
        // by using a simple test value
        float testValue = 1.0f;
        return Math.abs(func.apply(testValue) - ActivationFunctions.softmaxSingle(testValue)) < 0.0001f;
    }
}

/**
 * Example usage of the neural network implementation
 */
/*
public class NeuralNetworkExample {
    public static void main(String[] args) {
        // Create a simple network: 3 inputs -> 4 hidden -> 2 outputs
        // Using different activation functions for each layer
        Layer[] layers = {
            new Layer(3, ActivationFunctions::relu),
            new Layer(4, ActivationFunctions::sigmoid),
            new Layer(2, ActivationFunctions::softmaxSingle)
        };
        
        // Initialize network
        // This will automatically:
        // 1. Check if weights.bin exists
        // 2. If it exists - load weights from it
        // 3. If not - randomize weights and save them to weights.bin
        Network network = new Network(layers);
        
        // Example forward pass
        float[] input = {0.5f, 0.3f, 0.7f};
        float[] output = network.getResult(input);
        
        // Print results
        System.out.println("Output:");
        for (float value : output) {
            System.out.println(value);
        }
        
        // Demonstrate other activation functions
        System.out.println("\nActivation Function Examples:");
        float testValue = -0.5f;
        System.out.println("Input value: " + testValue);
        System.out.println("Sigmoid: " + ActivationFunctions.sigmoid(testValue));
        System.out.println("Tanh: " + ActivationFunctions.tanh(testValue));
        System.out.println("ReLU: " + ActivationFunctions.relu(testValue));
        System.out.println("Leaky ReLU: " + ActivationFunctions.lrelu(testValue));
        System.out.println("ELU: " + ActivationFunctions.elu(testValue));
        System.out.println("GELU: " + ActivationFunctions.gelu(testValue));
        
        // No need to save weights here since:
        // 1. If weights.bin didn't exist, they were already saved in the constructor
        // 2. If weights.bin did exist, we don't want to overwrite it
    }
}
*/
