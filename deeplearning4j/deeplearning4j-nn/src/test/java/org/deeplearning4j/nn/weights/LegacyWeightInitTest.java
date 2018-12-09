package org.deeplearning4j.nn.weights;

import org.deeplearning4j.nn.conf.distribution.*;
import org.deeplearning4j.nn.conf.serde.JsonMappers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.rng.Random;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.RandomFactory;
import org.nd4j.shade.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * Test that {@link WeightInit} is compatible with the corresponding classes which implement {@link IWeightInit}. Mocks
 * Nd4j.randomFactory so that legacy and new implementation can be compared exactly.
 *
 * @author Christian Skarby
 */
public class LegacyWeightInitTest {

    private RandomFactory prevFactory;
    private final static int SEED = 666;

    private final static List<Distribution> distributions = Arrays.asList(
            new LogNormalDistribution(12.3, 4.56),
            new BinomialDistribution(3, 0.3),
            new NormalDistribution(0.666, 0.333),
            new UniformDistribution(-1.23, 4.56),
            new OrthogonalDistribution(3.45),
            new TruncatedNormalDistribution(0.456, 0.123),
            new ConstantDistribution(666));

    @Before
    public void setRandomFactory() {
        prevFactory = Nd4j.randomFactory;
        Nd4j.randomFactory = new FixedSeedRandomFactory(prevFactory);
    }

    @After
    public void resetRandomFactory() {
        Nd4j.randomFactory = prevFactory;
    }

    /**
     * Test that param init is identical to legacy implementation
     */
    @Test
    public void initParams() {
        final long[] shape = {5, 5}; // To make identity happy
        final long fanIn = shape[0];
        final long fanOut = shape[1];

        final INDArray inLegacy = Nd4j.create(fanIn * fanOut);
        final INDArray inTest = inLegacy.dup();
        for (WeightInit legacyWi : WeightInit.values()) {
            if (legacyWi != WeightInit.DISTRIBUTION) {
                Nd4j.getRandom().setSeed(SEED);
                final INDArray expected = WeightInitUtil.initWeights(fanIn, fanOut, shape, legacyWi, null, inLegacy);

                Nd4j.getRandom().setSeed(SEED);
                final INDArray actual = legacyWi.getWeightInitFunction(null)
                        .init(fanIn, fanOut, shape, WeightInitUtil.DEFAULT_WEIGHT_INIT_ORDER, inTest);
                assertArrayEquals("Incorrect shape for " + legacyWi + "!", shape, actual.shape());

                assertEquals("Incorrect weight initialization for " + legacyWi + "!", expected, actual);
            }
        }
    }

    /**
     * Test that param init is identical to legacy implementation
     */
    @Test
    public void initParamsFromDistribution() {
        final long[] shape = {3, 7}; // To make identity happy
        final long fanIn = shape[0];
        final long fanOut = shape[1];

        final INDArray inLegacy = Nd4j.create(fanIn * fanOut);
        final INDArray inTest = inLegacy.dup();

        for (Distribution dist : distributions) {

            Nd4j.getRandom().setSeed(SEED);
            final INDArray expected = WeightInitUtil.initWeights(
                    fanIn,
                    fanOut,
                    shape,
                    WeightInit.DISTRIBUTION,
                    Distributions.createDistribution(dist),
                    inLegacy);

            final INDArray actual = new WeightInitDistribution(dist).init(
                    fanIn,
                    fanOut,
                    shape,
                    WeightInitUtil.DEFAULT_WEIGHT_INIT_ORDER,
                    inTest);
            assertArrayEquals("Incorrect shape for " + dist.getClass().getSimpleName() + "!", shape, actual.shape());

            assertEquals("Incorrect weight initialization for " + dist.getClass().getSimpleName() + "!", expected, actual);
        }
    }

    /**
     * Test that weight inits can be serialized and de-serialized in JSON format
     */
    @Test
    public void serializeDeserializeJson() throws IOException {
        final long[] shape = {5, 5}; // To make identity happy
        final long fanIn = shape[0];
        final long fanOut = shape[1];

        final ObjectMapper mapper = JsonMappers.getMapper();
        final INDArray inBefore = Nd4j.create(fanIn * fanOut);
        final INDArray inAfter = inBefore.dup();

        // Just use to enum to loop over all strategies
        for (WeightInit legacyWi : WeightInit.values()) {
            if (legacyWi != WeightInit.DISTRIBUTION) {
                Nd4j.getRandom().setSeed(SEED);
                final IWeightInit before = legacyWi.getWeightInitFunction(null);
                final INDArray expected = before.init(fanIn, fanOut, shape, inBefore.ordering(), inBefore);

                final String json = mapper.writeValueAsString(before);
                final IWeightInit after = mapper.readValue(json, IWeightInit.class);

                Nd4j.getRandom().setSeed(SEED);
                final INDArray actual = after.init(fanIn, fanOut, shape, inAfter.ordering(), inAfter);

                assertArrayEquals("Incorrect shape for " + legacyWi + "!", shape, actual.shape());
                assertEquals("Incorrect weight initialization for " + legacyWi + "!", expected, actual);
            }
        }
    }

    /**
     * Test that distribution can be serialized and de-serialized in JSON format
     */
    @Test
    public void serializeDeserializeDistributionJson() throws IOException {
        final long[] shape = {3, 7}; // To make identity happy
        final long fanIn = shape[0];
        final long fanOut = shape[1];

        final ObjectMapper mapper = JsonMappers.getMapper();
        final INDArray inBefore = Nd4j.create(fanIn * fanOut);
        final INDArray inAfter = inBefore.dup();

        for (Distribution dist : distributions) {

            Nd4j.getRandom().setSeed(SEED);
            final IWeightInit before = new WeightInitDistribution(dist);
            final INDArray expected = before.init(
                    fanIn,
                    fanOut,
                    shape,
                    inBefore.ordering(),
                    inBefore);

            final String json = mapper.writeValueAsString(before);
            final IWeightInit after = mapper.readValue(json, IWeightInit.class);

            Nd4j.getRandom().setSeed(SEED);
            final INDArray actual = after.init(fanIn, fanOut, shape, inAfter.ordering(), inAfter);

            assertArrayEquals("Incorrect shape for " + dist.getClass().getSimpleName() + "!", shape, actual.shape());

            assertEquals("Incorrect weight initialization for " + dist.getClass().getSimpleName() + "!", expected, actual);
        }
    }

    /**
     * Assumes RandomFactory will only call no-args constructor while this test runs
     */
    private static class FixedSeedRandomFactory extends RandomFactory {
        private final RandomFactory factory;


        private FixedSeedRandomFactory(RandomFactory factory) {
            super(factory.getRandom().getClass());
            this.factory = factory;
        }

        @Override
        public Random getRandom() {
            return getNewRandomInstance(SEED);
        }

        @Override
        public Random getNewRandomInstance() {
            return factory.getNewRandomInstance();
        }

        @Override
        public Random getNewRandomInstance(long seed) {
            return factory.getNewRandomInstance(seed);
        }

        @Override
        public Random getNewRandomInstance(long seed, long size) {
            return factory.getNewRandomInstance(seed, size);
        }
    }
}
