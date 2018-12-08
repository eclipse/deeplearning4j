package org.deeplearning4j.nn.weights;

import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.distribution.Distributions;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.rng.distribution.impl.OrthogonalDistribution;

/**
 * : Sample weights from a provided distribution<br>
 *
 * @author Adam Gibson
 */
public class WeightInitDistribution implements IWeightInit {

    private final Distribution distribution;

    public WeightInitDistribution(Distribution distribution) {
        this.distribution = distribution;
    }

    @Override
    public void init(double fanIn, double fanOut, long[] shape, char order, INDArray paramView) {
        //org.nd4j.linalg.api.rng.distribution.Distribution not serializable
        org.nd4j.linalg.api.rng.distribution.Distribution dist = Distributions.createDistribution(distribution);
        if (dist instanceof OrthogonalDistribution) {
            dist.sample(paramView.reshape(order, shape));
        } else {
            dist.sample(paramView);
        }
    }
}
