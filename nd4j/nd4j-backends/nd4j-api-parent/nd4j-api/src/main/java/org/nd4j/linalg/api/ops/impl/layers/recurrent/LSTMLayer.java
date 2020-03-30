/* ******************************************************************************
 * Copyright (c) 2020 Konduit K.K.
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
package org.nd4j.linalg.api.ops.impl.layers.recurrent;

import lombok.Getter;
import lombok.NonNull;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.base.Preconditions;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.DynamicCustomOp;
import org.nd4j.linalg.api.ops.impl.layers.recurrent.config.LSTMLayerConfig;
import org.nd4j.linalg.api.ops.impl.layers.recurrent.weights.LSTMLayerWeights;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * LSTM layer implemented as a single operation.
 * Implementation of operation for LSTM layer with optional peep hole connections.<br>
 * S. Hochreiter and J. Schmidhuber. "Long Short-Term Memory". Neural Computation and <a href="https://research.google.com/pubs/archive/43905.pdf">https://research.google.com/pubs/archive/43905.pdf</a><br>
 * Hasim Sak, Andrew Senior, and Francoise Beaufays. "Long short-term memory recurrent neural network architectures for large scale acoustic modeling." INTERSPEECH, 2014.<br>
 * See also: <a href="https://arxiv.org/pdf/1503.04069.pdf">https://arxiv.org/pdf/1503.04069.pdf</a><br>
 * Input arrays:<br>
 * 0: input <br>
 * [sL, bS, nIn]  when dataFormat - TNS <br>
 * [bS, sL, nIn]  when dataFormat - NST <br>
 * [bS, nIn, sL]  when dataFormat - NST <br>
 * 1: previous/initial cell state<br>
 * shapes [nIn, 4*nOut] for FWD, BWD  Direction Mode <br>
 * shapes [2, nIn, 4*nOut] BIDIR_SUM, BIDIR_CONCAT and BIDIR_EXTRA_DIM  Direction Mode <br>
 * 2: previous/initial output [bS, numUnits]<br>
 * * shapes [nIn, 4*nOut] for FWD, BWD  Direction Mode <br>
 * * shapes [2, nIn, 4*nOut] BIDIR_SUM, BIDIR_CONCAT and BIDIR_EXTRA_DIM  Direction Mode <br>
 * 3  max sequence length [bS] <br>
 * 4: LSTMLayerWeights - {@link LSTMLayerWeights} <br>
 * 5: LSTMLayerConfig - {@link LSTMLayerConfig}<br>
 * <p>
 * Output arrays:<br>
 * 0: output h  - rank 3 or 4, depends on DirectionMode and dataFormat<br>
 * 1: output at last step hL - rank 3 or 4, depends on DirectionMode and dataFormat<<br>
 * 2: cell state at last step cL  - same shape as in hL<br>
 */
public class LSTMLayer extends DynamicCustomOp {

    @Getter
    private LSTMLayerConfig configuration;

    @Getter
    private LSTMLayerWeights weights;


    public LSTMLayer() {
    }

    public LSTMLayer(@NonNull SameDiff sameDiff, SDVariable maxTSLength, SDVariable x, SDVariable cLast, SDVariable yLast, LSTMLayerWeights weights, LSTMLayerConfig configuration) {
        super(null, sameDiff, weights.argsWithInputs(x, maxTSLength, cLast, yLast));
        this.configuration = configuration;
        this.weights = weights;
        addIArgument(iArgs());
        addTArgument(tArgs());
        addBArgument(bArgs(weights, maxTSLength, yLast, cLast));


    }

    public LSTMLayer(INDArray x, INDArray cLast, INDArray yLast, INDArray maxTSLength, LSTMLayerWeights lstmWeights, LSTMLayerConfig LSTMLayerConfig) {
        super(null, null, lstmWeights.argsWithInputs(maxTSLength, x, cLast, yLast));
        this.configuration = LSTMLayerConfig;
        this.weights = lstmWeights;
        addIArgument(iArgs());
        addTArgument(tArgs());
        addBArgument(bArgs(weights, maxTSLength, yLast, cLast));
    }

    @Override
    public List<DataType> calculateOutputDataTypes(List<DataType> inputDataTypes) {
        Preconditions.checkState(inputDataTypes != null && 3 <= inputDataTypes.size() && inputDataTypes.size() <= 8, "Expected amount of inputs to LSTMLayer between 3 inputs minimum (input, Wx, Wr only) or 8 maximum, got %s", inputDataTypes);
        //7 outputs, all of same type as input. Note that input 0 is max sequence length (int64), input 1 is actual input
        DataType dt = inputDataTypes.get(1);
        Preconditions.checkState(dt.isFPType(), "Input type 1 must be a floating point type, got %s", dt);
        return Arrays.asList(dt, dt, dt, dt, dt, dt, dt);
    }

    @Override
    public List<SDVariable> doDiff(List<SDVariable> grads) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public String opName() {
        return "lstmLayer";
    }

    @Override
    public Map<String, Object> propertiesForFunction() {
        return configuration.toProperties(true, true);
    }


    public long[] iArgs() {
        return new long[]{
                configuration.getLstmdataformat().ordinal(),// INT_ARG(0)
                configuration.getDirectionMode().ordinal(), // INT_ARG(1)
                configuration.getGateAct().ordinal(),  // INT_ARG(2)
                configuration.getOutAct().ordinal(), // INT_ARG(3)
                configuration.getCellAct().ordinal()  // INT_ARG(4)

        };
    }

    public double[] tArgs() {
        return new double[]{this.configuration.getCellClip()}; // T_ARG(0)
    }


    public <T> boolean[] bArgs(LSTMLayerWeights weights, T maxTSLength, T yLast, T cLast) {
        return new boolean[]{
                weights.hasBias(),         // hasBiases: B_ARG(0)
                maxTSLength != null,         // hasSeqLen: B_ARG(1)
                yLast != null,               // hasInitH: B_ARG(2)
                cLast != null,              // hasInitC: B_ARG(3)
                weights.hasPH(),          // hasPH: B_ARG(4)
                configuration.isRetFullSequence(), //retFullSequence: B_ARG(5)
                configuration.isRetLastH(),  //  retLastH: B_ARG(6)
                configuration.isRetLastC()   // retLastC: B_ARG(7)
        };

    }


}


