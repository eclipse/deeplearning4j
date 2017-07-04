package org.deeplearning4j.spark.parameterserver.networking;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.optimize.solvers.accumulation.EncodingHandler;
import org.deeplearning4j.spark.parameterserver.networking.messages.SilentUpdatesMessage;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.parameterserver.distributed.VoidParameterServer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This MessageHandler implementation does the same as EncodingHandler, plus additionally:
 * sends encoded messages over the wire + receives encoded messages from outer parties
 *
 * @author raver119@gmail.com
 */
@Slf4j
public class WiredEncodingHandler extends EncodingHandler {
    protected AtomicLong updatesCounter = new AtomicLong(0);

    public WiredEncodingHandler() {
        super();
    }

    public WiredEncodingHandler(double threshold) {
        super(threshold);
    }

    public WiredEncodingHandler(double threshold, Double boundary) {
        super(threshold, boundary);
    }

    /**
     * This method sends given message to all registered recipients
     *
     * @param message
     */
    @Override
    protected void sendMessage(INDArray message) {
        // here we'll send our stuff to other executores over the wire
        // and let's pray for udp broadcast availability

        // Send this message away
        // FIXME: do something with unsafe duplication, which is bad and used ONLY for local spark
        try (MemoryWorkspace wsO = Nd4j.getMemoryManager().scopeOutOfWorkspaces()) {
            long updateId = updatesCounter.getAndIncrement();

            VoidParameterServer.getInstance().execDistributedImmediately(
                            new SilentUpdatesMessage(message.unsafeDuplication(), updateId));
        }


        // heere we update local queue
        super.sendMessage(message);
    }
}
