package org.deeplearning4j.rl4j.observation.transform.operation;

import org.deeplearning4j.rl4j.observation.transform.operation.temporalmerge.TemporalMergeAssembler;
import org.deeplearning4j.rl4j.observation.transform.operation.temporalmerge.TemporalMergeElementStore;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.Assert.*;

public class TemporalMergeTransformTest {

    @Test
    public void when_firstDimensionIsNotBatch_expect_observationAddedAsIs() {
        // Arrange
        MockStore store = new MockStore(false);
        TemporalMergeTransform sut = TemporalMergeTransform.builder()
                .isFirstDimenstionBatch(false)
                .elementStore(store)
                .build();
        INDArray input = Nd4j.create(new double[] { 1.0, 2.0, 3.0 });

        // Act
        sut.transform(input);

        // Assert
        assertEquals(1, store.addedObservation.shape().length);
        assertEquals(3, store.addedObservation.shape()[0]);
    }

    @Test
    public void when_firstDimensionIsBatch_expect_observationAddedAsSliced() {
        // Arrange
        MockStore store = new MockStore(false);
        TemporalMergeTransform sut = TemporalMergeTransform.builder()
                .isFirstDimenstionBatch(true)
                .elementStore(store)
                .build();
        INDArray input = Nd4j.create(new double[] { 1.0, 2.0, 3.0 }).reshape(1, 3);

        // Act
        sut.transform(input);

        // Assert
        assertEquals(1, store.addedObservation.shape().length);
        assertEquals(3, store.addedObservation.shape()[0]);
    }

    @Test
    public void when_notReady_expect_resultIsNull() {
        // Arrange
        MockStore store = new MockStore(false);
        TemporalMergeTransform sut = TemporalMergeTransform.builder()
                .isFirstDimenstionBatch(true)
                .elementStore(store)
                .build();
        INDArray input = Nd4j.create(new double[] { 1.0, 2.0, 3.0 });

        // Act
        INDArray result = sut.transform(input);

        // Assert
        assertNull(result);
    }

    @Test
    public void when_notShouldStoreCopy_expect_sameIsStored() {
        // Arrange
        MockStore store = new MockStore(false);
        TemporalMergeTransform sut = TemporalMergeTransform.builder()
                .shouldStoreCopy(false)
                .elementStore(store)
                .build();
        INDArray input = Nd4j.create(new double[] { 1.0, 2.0, 3.0 });

        // Act
        INDArray result = sut.transform(input);

        // Assert
        assertSame(input, store.addedObservation);
    }

    @Test
    public void when_shouldStoreCopy_expect_copyIsStored() {
        // Arrange
        MockStore store = new MockStore(true);
        TemporalMergeTransform sut = TemporalMergeTransform.builder()
                .shouldStoreCopy(true)
                .elementStore(store)
                .build();
        INDArray input = Nd4j.create(new double[] { 1.0, 2.0, 3.0 });

        // Act
        INDArray result = sut.transform(input);

        // Assert
        assertNotSame(input, store.addedObservation);
        assertEquals(1, store.addedObservation.shape().length);
        assertEquals(3, store.addedObservation.shape()[0]);
    }

    @Test
    public void when_transformCalled_expect_storeContentAssembledAndOutputHasCorrectShape() {
        // Arrange
        MockStore store = new MockStore(true);
        MockAssemble assemble = new MockAssemble();
        TemporalMergeTransform sut = TemporalMergeTransform.builder()
                .elementStore(store)
                .assembler(assemble)
                .build();
        INDArray input = Nd4j.create(new double[] { 1.0, 2.0, 3.0 });

        // Act
        INDArray result = sut.transform(input);

        // Assert
        assertEquals(1, assemble.assembleElements.length);
        assertSame(store.addedObservation, assemble.assembleElements[0]);

        assertEquals(2, result.shape().length);
        assertEquals(1, result.shape()[0]);
        assertEquals(3, result.shape()[1]);
    }

    public static class MockStore implements TemporalMergeElementStore {

        private final boolean isReady;
        private INDArray addedObservation;

        public MockStore(boolean isReady) {

            this.isReady = isReady;
        }

        @Override
        public void add(INDArray observation) {
            addedObservation = observation;
        }

        @Override
        public INDArray[] get() {
            return new INDArray[] { addedObservation };
        }

        @Override
        public boolean isReady() {
            return isReady;
        }

        @Override
        public void reset() {

        }
    }

    public static class MockAssemble implements TemporalMergeAssembler {

        private INDArray[] assembleElements;

        @Override
        public INDArray assemble(INDArray[] elements) {
            assembleElements = elements;
            return elements[0];
        }
    }
}
