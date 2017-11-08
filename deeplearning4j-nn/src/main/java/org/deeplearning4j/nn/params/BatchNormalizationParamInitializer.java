package org.deeplearning4j.nn.params;

import org.deeplearning4j.nn.api.ParamInitializer;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.BatchNormalization;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.util.*;

/**
 * Batch normalization variable init
 */

public class BatchNormalizationParamInitializer implements ParamInitializer {

    private static final BatchNormalizationParamInitializer INSTANCE = new BatchNormalizationParamInitializer();

    public static BatchNormalizationParamInitializer getInstance() {
        return INSTANCE;
    }

    public static final String GAMMA = "gamma";
    public static final String BETA = "beta";
    public static final String GLOBAL_MEAN = "mean";
    public static final String GLOBAL_VAR = "var";

    private static final List<String> KEYS = Collections.unmodifiableList(Arrays.asList(GAMMA, BETA, GLOBAL_MEAN, GLOBAL_VAR));

    public static List<String> keys() {
        return KEYS;
    }

    @Override
    public int numParams(Layer l) {
        BatchNormalization layer = (BatchNormalization) l;
        //Parameters in batch norm:
        //gamma, beta, global mean estimate, global variance estimate
        // latter 2 are treated as parameters, which greatly simplifies spark training and model serialization

        if (layer.isLockGammaBeta()) {
            //Special case: gamma and beta are fixed values for all outputs -> no parameters for gamma and  beta in this case
            return 2 * layer.getNOut();
        } else {
            //Standard case: gamma and beta are learned per output; additional 2*nOut for global mean/variance estimate
            return 4 * layer.getNOut();
        }
    }

    @Override
    public List<String> paramKeys(Layer layer) {
        return Arrays.asList(GAMMA, BETA, GLOBAL_MEAN, GLOBAL_VAR);
    }

    @Override
    public List<String> weightKeys(Layer layer) {
        return Collections.emptyList();
    }

    @Override
    public List<String> biasKeys(Layer layer) {
        return Collections.emptyList();
    }

    @Override
    public boolean isWeightParam(String key) {
        return false;
    }

    @Override
    public boolean isBiasParam(String key) {
        return false;
    }

    @Override
    public Map<String, INDArray> init(Layer l, INDArray paramView, boolean initializeParams) {
        Map<String, INDArray> params = Collections.synchronizedMap(new LinkedHashMap<String, INDArray>());
        // TODO setup for RNN
        BatchNormalization layer = (BatchNormalization) l;
        int nOut = layer.getNOut();

        int meanOffset = 0;
        if (!layer.isLockGammaBeta()) { //No gamma/beta parameters when gamma/beta are locked
            INDArray gammaView = paramView.get(NDArrayIndex.point(0), NDArrayIndex.interval(0, nOut));
            INDArray betaView = paramView.get(NDArrayIndex.point(0), NDArrayIndex.interval(nOut, 2 * nOut));

            params.put(GAMMA, createGamma(layer, gammaView, initializeParams));
            params.put(BETA, createBeta(layer, betaView, initializeParams));

            meanOffset = 2 * nOut;
        }

        INDArray globalMeanView =
                        paramView.get(NDArrayIndex.point(0), NDArrayIndex.interval(meanOffset, meanOffset + nOut));
        INDArray globalVarView = paramView.get(NDArrayIndex.point(0),
                        NDArrayIndex.interval(meanOffset + nOut, meanOffset + 2 * nOut));

        if (initializeParams) {
            globalMeanView.assign(0);
            globalVarView.assign(1);
        }

        params.put(GLOBAL_MEAN, globalMeanView);
        params.put(GLOBAL_VAR, globalVarView);

        return params;
    }

    @Override
    public Map<String, INDArray> getGradientsFromFlattened(Layer l, INDArray gradientView) {
        BatchNormalization layer = (BatchNormalization)l;
        int nOut = layer.getNOut();

        Map<String, INDArray> out = new LinkedHashMap<>();
        int meanOffset = 0;
        if (!layer.isLockGammaBeta()) {
            INDArray gammaView = gradientView.get(NDArrayIndex.point(0), NDArrayIndex.interval(0, nOut));
            INDArray betaView = gradientView.get(NDArrayIndex.point(0), NDArrayIndex.interval(nOut, 2 * nOut));
            out.put(GAMMA, gammaView);
            out.put(BETA, betaView);
            meanOffset = 2 * nOut;
        }

        out.put(GLOBAL_MEAN,
                        gradientView.get(NDArrayIndex.point(0), NDArrayIndex.interval(meanOffset, meanOffset + nOut)));
        out.put(GLOBAL_VAR, gradientView.get(NDArrayIndex.point(0),
                        NDArrayIndex.interval(meanOffset + nOut, meanOffset + 2 * nOut)));

        return out;
    }

    private INDArray createBeta(BatchNormalization layer, INDArray betaView, boolean initializeParams) {
        if (initializeParams)
            betaView.assign(layer.getBeta());
        return betaView;
    }

    private INDArray createGamma(BatchNormalization layer, INDArray gammaView, boolean initializeParams) {
        if (initializeParams)
            gammaView.assign(layer.getGamma());
        return gammaView;
    }
}
