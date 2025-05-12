/**
 * Collection of activation functions for neural networks
 */
public final class ActivationFunctions {
    private ActivationFunctions() {
    }

    public static double linear(double x) {
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
    public static double sigmoid(double x) {
        return 1.0f / (1.0f + (double) Math.exp(-x));
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
    public static double tanh(double x) {
        return (double) Math.tanh(x);
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
    public static double relu(double x) {
        return x > 0.0f ? x : 0.0f;
    }

    /**
     * Leaky ReLU activation function
     *
     * Characteristics: - Prevents dying ReLU problem - Small gradient for
     * negative values - No vanishing gradient
     *
     * @param x     Input value
     * @param alpha Slope for negative values (typically small, e.g., 0.01)
     * @return x if x > 0, alpha * x otherwise
     */
    public static double lrelu(double x, double alpha) {
        return x > 0.0f ? x : alpha * x;
    }

    /**
     * Leaky ReLU with default alpha value
     *
     * @param x Input value
     * @return x if x > 0, 0.01 * x otherwise
     */
    public static double lrelu(double x) {
        return lrelu(x, 0.01f);
    }

    /**
     * Parametric ReLU activation function
     *
     * Characteristics: - Similar to Leaky ReLU but with learnable alpha - More
     * flexible than standard ReLU - Requires additional parameter training
     *
     * @param x     Input value
     * @param alpha Learnable parameter for negative values
     * @return x if x > 0, alpha * x otherwise
     */
    public static double prelu(double x, double alpha) {
        return x > 0.0f ? x : alpha * x;
    }

    /**
     * Exponential Linear Unit activation function
     *
     * Characteristics: - Smooth function including at x=0 - Can produce
     * negative values - Better handling of noise - Self-regularizing
     *
     * @param x     Input value
     * @param alpha Scale for the negative part
     * @return x if x ≥ 0, alpha * (exp(x) - 1) otherwise
     */
    public static double elu(double x, double alpha) {
        return x >= 0.0f ? x : alpha * ((double) Math.exp(x) - 1.0f);
    }

    /**
     * ELU with default alpha value
     *
     * @param x Input value
     * @return x if x ≥ 0, (exp(x) - 1) otherwise
     */
    public static double elu(double x) {
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
    public static double softmaxSingle(double x) {
        return (double) Math.exp(x);
    }

    /**
     * Softmax activation function for entire layer
     *
     * Characteristics: - Converts inputs to probability distribution - Outputs
     * sum to 1.0 - Commonly used in classification - Numerically stable
     * implementation
     *
     * @param input  Array of input values
     * @param output Array to store results
     * @param size   Length of input/output arrays
     */
    public static void softmax(double[] input, double[] output, int size) {
        double maxVal = input[0];
        for (int i = 1; i < size; i++) {
            if (input[i] > maxVal) {
                maxVal = input[i];
            }
        }

        double sum = 0.0f;
        for (int i = 0; i < size; i++) {
            output[i] = (double) Math.exp(input[i] - maxVal);
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
    public static double gelu(double x) {
        double sqrt2OverPi = Math.sqrt(2 / Math.PI);
        return (double) (0.5 * x * (1 + Math.tanh(sqrt2OverPi * (x + 0.044715 * Math.pow(x, 3)))));
    }
}
