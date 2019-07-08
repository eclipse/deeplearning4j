package org.deeplearning4j.rl4j.observation.preprocessor;

import org.deeplearning4j.rl4j.observation.preprocessors.PermuteDataSetPreProcessor;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.Assert.*;

public class PermuteDataSetPreProcessorTest {

    @Test(expected = NullPointerException.class)
    public void when_dataSetIsNull_expect_NullPointerException() {
        // Assemble
        PermuteDataSetPreProcessor sut = new PermuteDataSetPreProcessor(PermuteDataSetPreProcessor.PermutationTypes.NCHWtoNHWC);

        // Act
        sut.preProcess(null);
    }

    @Test
    public void when_emptyDatasetInInputdataSetIsNCHW_expect_emptyDataSet() {
        // Assemble
        PermuteDataSetPreProcessor sut = new PermuteDataSetPreProcessor(PermuteDataSetPreProcessor.PermutationTypes.NCHWtoNHWC);
        DataSet ds = new DataSet(null, null);

        // Act
        sut.preProcess(ds);

        // Assert
        assertTrue(ds.isEmpty());
    }

    @Test
    public void when_dataSetIsNCHW_expect_dataSetTransformedToNHWC() {
        // Assemble
        int numChannels = 3;
        int height = 5;
        int width = 4;
        PermuteDataSetPreProcessor sut = new PermuteDataSetPreProcessor(PermuteDataSetPreProcessor.PermutationTypes.NCHWtoNHWC);
        INDArray input = Nd4j.create(new int[] { 1, numChannels, height, width });
        for(int c = 0; c < numChannels; ++c) {
            for(int h = 0; h < height; ++h) {
                for(int w = 0; w < width; ++w) {
                    input.putScalar(new int[] { 0, c, h, w }, c*100.0 + h*10.0 + w);
                }
            }
        }
        DataSet ds = new DataSet(input, null);

        // Act
        sut.preProcess(ds);

        // Assert
        INDArray result = ds.getFeatures();
        long[] shape = result.shape();
        assertEquals(1, shape[0]);
        assertEquals(height, shape[1]);
        assertEquals(width, shape[2]);
        assertEquals(numChannels, shape[3]);

        assertEquals(0.0, result.getDouble(new int[] { 0, 0, 0, 0 }), 0.0);
        assertEquals(1.0, result.getDouble(new int[] { 0, 0, 1, 0 }), 0.0);
        assertEquals(2.0, result.getDouble(new int[] { 0, 0, 2, 0 }), 0.0);
        assertEquals(3.0, result.getDouble(new int[] { 0, 0, 3, 0 }), 0.0);

        assertEquals(110.0, result.getDouble(new int[] { 0, 1, 0, 1 }), 0.0);
        assertEquals(111.0, result.getDouble(new int[] { 0, 1, 1, 1 }), 0.0);
        assertEquals(112.0, result.getDouble(new int[] { 0, 1, 2, 1 }), 0.0);
        assertEquals(113.0, result.getDouble(new int[] { 0, 1, 3, 1 }), 0.0);

        assertEquals(210.0, result.getDouble(new int[] { 0, 1, 0, 2 }), 0.0);
        assertEquals(211.0, result.getDouble(new int[] { 0, 1, 1, 2 }), 0.0);
        assertEquals(212.0, result.getDouble(new int[] { 0, 1, 2, 2 }), 0.0);
        assertEquals(213.0, result.getDouble(new int[] { 0, 1, 3, 2 }), 0.0);

    }

    @Test
    public void when_dataSetIsNHWC_expect_dataSetTransformedToNCHW() {
        // Assemble
        int numChannels = 3;
        int height = 5;
        int width = 4;
        PermuteDataSetPreProcessor sut = new PermuteDataSetPreProcessor(PermuteDataSetPreProcessor.PermutationTypes.NHWCtoNCHW);
        INDArray input = Nd4j.create(new int[] { 1, height, width, numChannels });
        for(int c = 0; c < numChannels; ++c) {
            for(int h = 0; h < height; ++h) {
                for(int w = 0; w < width; ++w) {
                    input.putScalar(new int[] { 0, h, w, c }, c*100.0 + h*10.0 + w);
                }
            }
        }
        DataSet ds = new DataSet(input, null);

        // Act
        sut.preProcess(ds);

        // Assert
        INDArray result = ds.getFeatures();
        long[] shape = result.shape();
        assertEquals(1, shape[0]);
        assertEquals(numChannels, shape[1]);
        assertEquals(height, shape[2]);
        assertEquals(width, shape[3]);

        assertEquals(0.0, result.getDouble(new int[] { 0, 0, 0, 0 }), 0.0);
        assertEquals(1.0, result.getDouble(new int[] { 0, 0, 0, 1 }), 0.0);
        assertEquals(2.0, result.getDouble(new int[] { 0, 0, 0, 2 }), 0.0);
        assertEquals(3.0, result.getDouble(new int[] { 0, 0, 0, 3 }), 0.0);

        assertEquals(110.0, result.getDouble(new int[] { 0, 1, 1, 0 }), 0.0);
        assertEquals(111.0, result.getDouble(new int[] { 0, 1, 1, 1 }), 0.0);
        assertEquals(112.0, result.getDouble(new int[] { 0, 1, 1, 2 }), 0.0);
        assertEquals(113.0, result.getDouble(new int[] { 0, 1, 1, 3 }), 0.0);

        assertEquals(210.0, result.getDouble(new int[] { 0, 2, 1, 0 }), 0.0);
        assertEquals(211.0, result.getDouble(new int[] { 0, 2, 1, 1 }), 0.0);
        assertEquals(212.0, result.getDouble(new int[] { 0, 2, 1, 2 }), 0.0);
        assertEquals(213.0, result.getDouble(new int[] { 0, 2, 1, 3 }), 0.0);

    }
}
