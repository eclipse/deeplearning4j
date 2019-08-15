package org.deeplearning4j.rl4j.support;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.deeplearning4j.rl4j.learning.IHistoryProcessor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class MockHistoryProcessor implements IHistoryProcessor {

    public int recordCallCount = 0;
    public int addCallCount = 0;
    public int startMonitorCallCount = 0;
    public int stopMonitorCallCount = 0;

    private final Configuration config;
    private final CircularFifoQueue<INDArray> history;

    public MockHistoryProcessor(Configuration config) {

        this.config = config;
        history = new CircularFifoQueue<>(config.getHistoryLength());
    }

    @Override
    public Configuration getConf() {
        return config;
    }

    @Override
    public INDArray[] getHistory() {
        INDArray[] array = new INDArray[getConf().getHistoryLength()];
        for (int i = 0; i < config.getHistoryLength(); i++) {
            array[i] = history.get(i).castTo(Nd4j.dataType());
        }
        return array;
    }

    @Override
    public void record(INDArray image) {
        ++recordCallCount;
    }

    @Override
    public void add(INDArray image) {
        ++addCallCount;
        history.add(image);
    }

    @Override
    public void startMonitor(String filename, int[] shape) {
        ++startMonitorCallCount;
    }

    @Override
    public void stopMonitor() {
        ++stopMonitorCallCount;
    }

    @Override
    public boolean isMonitoring() {
        return false;
    }

    @Override
    public double getScale() {
        return 255.0;
    }
}
