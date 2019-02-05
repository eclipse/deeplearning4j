/*******************************************************************************
 * Copyright (c) 2015-2018 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.deeplearning4j;

import org.apache.commons.compress.utils.IOUtils;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.layers.BaseLayer;
import org.deeplearning4j.nn.conf.layers.samediff.AbstractSameDiffLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.random.impl.BernoulliDistribution;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.regularization.L1Regularization;
import org.nd4j.linalg.learning.regularization.L2Regularization;
import org.nd4j.linalg.learning.regularization.Regularization;
import org.nd4j.linalg.learning.regularization.WeightDecay;

import java.io.*;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestUtils {

    public static MultiLayerNetwork testModelSerialization(MultiLayerNetwork net){

        MultiLayerNetwork restored;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ModelSerializer.writeModel(net, baos, true);
            byte[] bytes = baos.toByteArray();

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            restored = ModelSerializer.restoreMultiLayerNetwork(bais, true);

            assertEquals(net.getLayerWiseConfigurations(), restored.getLayerWiseConfigurations());
            assertEquals(net.params(), restored.params());
        } catch (IOException e){
            //Should never happen
            throw new RuntimeException(e);
        }

        //Also check the MultiLayerConfiguration is serializable (required by Spark etc)
        MultiLayerConfiguration conf = net.getLayerWiseConfigurations();
        serializeDeserializeJava(conf);

        return restored;
    }

    public static ComputationGraph testModelSerialization(ComputationGraph net){

        ComputationGraph restored;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ModelSerializer.writeModel(net, baos, true);
            byte[] bytes = baos.toByteArray();

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            restored = ModelSerializer.restoreComputationGraph(bais, true);

            assertEquals(net.getConfiguration(), restored.getConfiguration());
            assertEquals(net.params(), restored.params());
        } catch (IOException e){
            //Should never happen
            throw new RuntimeException(e);
        }

        //Also check the ComputationGraphConfiguration is serializable (required by Spark etc)
        ComputationGraphConfiguration conf = net.getConfiguration();
        serializeDeserializeJava(conf);

        return restored;
    }

    private static <T> T serializeDeserializeJava(T object){
        byte[] bytes;
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)){
            oos.writeObject(object);
            oos.close();
            bytes = baos.toByteArray();
        } catch (IOException e){
            //Should never happen
            throw new RuntimeException(e);
        }

        T out;
        try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))){
            out = (T)ois.readObject();
        } catch (IOException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }

        assertEquals(object, out);
        return out;
    }

    public static INDArray randomOneHot(long examples, long nOut){
        return randomOneHot(examples, nOut, new Random(12345));
    }

    public static INDArray randomOneHot(long examples, long nOut, long rngSeed){
        return randomOneHot(examples, nOut, new Random(rngSeed));
    }

    public static INDArray randomOneHot(long examples, long nOut, Random rng){
        INDArray arr = Nd4j.create(examples, nOut);
        for( int i=0; i<examples; i++ ){
            // FIXME: int cast
            arr.putScalar(i, rng.nextInt((int) nOut), 1.0);
        }
        return arr;
    }

    public static INDArray randomOneHotTimeSeries(int minibatch, int outSize, int tsLength){
        return randomOneHotTimeSeries(minibatch, outSize, tsLength, new Random());
    }

    public static INDArray randomOneHotTimeSeries(int minibatch, int outSize, int tsLength, long rngSeed){
        return randomOneHotTimeSeries(minibatch, outSize, tsLength, new Random(rngSeed));
    }

    public static INDArray randomOneHotTimeSeries(int minibatch, int outSize, int tsLength, Random rng){
        INDArray out = Nd4j.create(new int[]{minibatch, outSize, tsLength}, 'f');
        for( int i=0; i<minibatch; i++ ){
            for( int j=0; j<tsLength; j++ ){
                out.putScalar(i, rng.nextInt(outSize), j, 1.0);
            }
        }
        return out;
    }

    public static INDArray randomBernoulli(int... shape) {
        return randomBernoulli(0.5, shape);
    }

    public static INDArray randomBernoulli(double p, int... shape){
        INDArray ret = Nd4j.createUninitialized(shape);
        Nd4j.getExecutioner().exec(new BernoulliDistribution(ret, p));
        return ret;
    }

    public static void writeStreamToFile(File out, InputStream is) throws IOException {
        byte[] b = IOUtils.toByteArray(is);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
            os.write(b);
        }
    }

    public static L1Regularization getL1Reg(List<Regularization> l){
        for(Regularization r : l){
            if(r instanceof L1Regularization){
                return (L1Regularization) r;
            }
        }
        return null;
    }

    public static L2Regularization getL2Reg(BaseLayer baseLayer){
        return getL2Reg(baseLayer.getRegularization());
    }

    public static L2Regularization getL2Reg(List<Regularization> l){
        for(Regularization r : l){
            if(r instanceof L2Regularization){
                return (L2Regularization) r;
            }
        }
        return null;
    }

    public static WeightDecay getWeightDecayReg(BaseLayer bl){
        return getWeightDecayReg(bl.getRegularization());
    }

    public static WeightDecay getWeightDecayReg(List<Regularization> l){
        for(Regularization r : l){
            if(r instanceof WeightDecay){
                return (WeightDecay) r;
            }
        }
        return null;
    }

    public static double getL1(BaseLayer layer) {
        List<Regularization> l = layer.getRegularization();
        return getL1(l);
    }

    public static double getL1(List<Regularization> l){
        L1Regularization l1Reg = null;
        for(Regularization reg : l){
            if(reg instanceof L1Regularization)
                l1Reg = (L1Regularization) reg;
        }
        assertNotNull(l1Reg);
        return l1Reg.getL1().valueAt(0,0);
    }

    public static double getL2(BaseLayer layer) {
        List<Regularization> l = layer.getRegularization();
        return getL2(l);
    }

    public static double getL2(List<Regularization> l){
        L2Regularization l2Reg = null;
        for(Regularization reg : l){
            if(reg instanceof L2Regularization)
                l2Reg = (L2Regularization) reg;
        }
        assertNotNull(l2Reg);
        return l2Reg.getL2().valueAt(0,0);
    }

    public static double getL1(AbstractSameDiffLayer layer){
        return getL1(layer.getRegularization());
    }

    public static double getL2(AbstractSameDiffLayer layer){
        return getL2(layer.getRegularization());
    }

    public static double getWeightDecay(BaseLayer layer) {
        return getWeightDecayReg(layer.getRegularization()).getCoeff().valueAt(0,0);
    }
}
