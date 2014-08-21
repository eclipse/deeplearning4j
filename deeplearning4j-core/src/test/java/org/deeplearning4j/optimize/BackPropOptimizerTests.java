package org.deeplearning4j.optimize;

import org.deeplearning4j.linalg.api.activation.Activations;
import org.deeplearning4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.linalg.factory.NDArrays;
import org.deeplearning4j.nn.BaseMultiLayerNetwork.ParamRange;

import org.deeplearning4j.dbn.DBN;
import org.deeplearning4j.rbm.RBM;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the back prop optimizer
 */
public class BackPropOptimizerTests {

    private static Logger log = LoggerFactory.getLogger(BackPropOptimizerTests.class);


    @Test
    public void testHessianFree() {

    }

    @Test
    public void testBackPropOptimizerIndices() {

        double preTrainLr = 0.01;
        int preTrainEpochs = 10000;
        int k = 1;
        int nIns = 4,nOuts = 3;
        int[] hiddenLayerSizes = new int[] {4,3,2};
        double fineTuneLr = 0.01;
        int fineTuneEpochs = 10000;


        DBN dbn = new DBN.Builder().withHiddenUnits(RBM.HiddenUnit.RECTIFIED)
                .withVisibleUnits(RBM.VisibleUnit.GAUSSIAN)
                .numberOfInputs(nIns).numberOfOutPuts(nOuts).withActivation(Activations.tanh())
                .hiddenLayerSizes(hiddenLayerSizes)
                .build();

        INDArray params = dbn.params();
        assertEquals(1,params.rows());
        assertEquals(params.columns(),params.length());
        dbn.setLabels(NDArrays.create(1, nOuts));


        BackPropOptimizer op = new BackPropOptimizer(dbn,1e-1,1000);
        INDArray layerParams = op.getParameters();

        ParamRange r = dbn.startIndexForLayer(0);
        double firstWeightForParam = (double) layerParams.getScalar(r.getwStart() + 1).element();
        double firstWeightInNetwork = (double) dbn.getLayers()[0].getW().getScalar(1).element();
        assertEquals(0,r.getwStart());
        int len = dbn.getLayers()[0].getW().length();
        assertEquals(len,r.getwEnd());
        assertEquals(dbn.getLayers()[0].gethBias().length(),Math.abs(r.getBiasStart() - r.getBiasEnd()));

        ParamRange r2 = dbn.startIndexForLayer(1);
        assertEquals(dbn.getLayers()[0].getW().length() + dbn.getLayers()[0].gethBias().length(),r2.getwStart());


        double secondWeightForParam = (double) layerParams.getScalar(r2.getwStart() + 1).element();
        double secondWeightInNetwork = (double) dbn.getLayers()[1].getW().getScalar(1).element();


        assertEquals(true,firstWeightForParam == firstWeightInNetwork);
        assertEquals(true,secondWeightForParam == secondWeightInNetwork);

        assertEquals(op.getNumParameters(),op.getParameters().length());
        assertEquals(op.getNumParameters(),op.getValueGradient(0).length());
        assertEquals(op.getParameters().length(),op.getValueGradient(0).length());


        assertEquals(dbn.getLayers()[1].gethBias().length(),Math.abs(r2.getBiasStart() - r2.getBiasEnd()));

    }


}
