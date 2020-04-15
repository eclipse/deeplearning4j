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
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.base.Preconditions;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.DynamicCustomOp;
import org.nd4j.linalg.api.ops.impl.layers.recurrent.config.LSTMLayerConfig;
import org.nd4j.linalg.api.ops.impl.layers.recurrent.weights.LSTMLayerWeights;
import org.nd4j.shade.guava.primitives.Booleans;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * LSTM layer implemented as a single operation.
 */
@NoArgsConstructor
public class LSTMLayerBp extends DynamicCustomOp {

    @Getter
    private LSTMLayerConfig configuration;

    @Getter
    private LSTMLayerWeights weights;

    private SDVariable cLast;
    private SDVariable yLast;
    private SDVariable maxTSLength;



//  Input base on this cpp code part for lstmLayer_bp
//    const auto x  = INPUT_VARIABLE(0);          // input
//    const auto Wx = INPUT_VARIABLE(1);          // input weights
//    const auto Wr = INPUT_VARIABLE(2);          // recurrent weights
//    count = 3;
//    const auto b      = hasBiases  ? INPUT_VARIABLE(count++) : nullptr;  // biases
//    const auto seqLen = hasSeqLen  ? INPUT_VARIABLE(count++) : nullptr;  // seqLen vector
//    const auto hI     = hasInitH   ? INPUT_VARIABLE(count++) : nullptr;  // initial output
//    const auto cI     = hasInitC   ? INPUT_VARIABLE(count++) : nullptr;  // initial cell state
//    const auto Wp     = hasPH      ? INPUT_VARIABLE(count++) : nullptr;  // peephole weights
//    const auto dLdh   = retFullSeq ? INPUT_VARIABLE(count++) : nullptr;  // gradient vs. output
//    const auto dLdhL  = retLastH   ? INPUT_VARIABLE(count++) : nullptr;  // gradient vs. output at last time step
//    const auto dLdcL  = retLastC   ? INPUT_VARIABLE(count++) : nullptr;  // gradient vs. cell state at last time step

    public LSTMLayerBp(@NonNull SameDiff sameDiff, SDVariable x, SDVariable cLast, SDVariable yLast, SDVariable maxTSLength, LSTMLayerWeights weights, LSTMLayerConfig configuration,
                       SDVariable dLdh, SDVariable dLdhL, SDVariable dLdcL) {
//        super("lstmLayer_bp", sameDiff, weights.argsWithInputs(x, maxTSLength, cLast, yLast));
        super("lstmLayer_bp", sameDiff, wrapFilterNull(x, weights.getWeights(), weights.getRWeights(),weights.getBias(),
                maxTSLength, yLast, cLast, weights.getPeepholeWeights(), dLdh, dLdhL, dLdcL));
        this.configuration = configuration;
        this.weights = weights;
        this.cLast = cLast;
        this.yLast = yLast;
        this.maxTSLength = maxTSLength;
        addIArgument(iArgs());
        addTArgument(tArgs());
        addBArgument(bArgs(weights, maxTSLength, yLast, cLast));


//         for (int i=0; i<args().length;i++){System.out.println(arg(i));}
        Preconditions.checkState(this.configuration.isRetLastH() || this.configuration.isRetLastC() || this.configuration.isRetFullSequence(),
                "You have to specify at least one output you want to return. Use isRetLastC, isRetLast and isRetFullSequence  methods  in LSTMLayerConfig builder to specify them");


    }


    @Override
    public List<DataType> calculateOutputDataTypes(List<DataType> inputDataTypes) {
        DataType dt = inputDataTypes.get(1);
        ArrayList<DataType> list = new ArrayList<>();
        if (configuration.isRetFullSequence()) {

            list.add(dt);
        }

        if (configuration.isRetLastC()) {

            list.add(dt);
        }
        if (configuration.isRetLastH()){

            list.add(dt);
        }
        list.add(dt);
        list.add(dt);
        list.add(dt);
        list.add(dt);



        Preconditions.checkState(dt.isFPType(), "Input type 1 must be a floating point type, got %s", dt);
        return list;
    }


    @Override
    public String opName() {
        return "lstmLayer_bp";
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


    @Override
    public int getNumOutputs(){
//        C++ outputs
//        auto dLdx  = OUTPUT_VARIABLE(0);                                 // gradient vs. input
//        auto dLdWx = OUTPUT_NULLIFIED(1);                                // gradient vs. input weights
//        auto dLdWr = OUTPUT_NULLIFIED(2);                                // gradient vs. recurrent weights
//        auto dLdb  = hasBiases ? OUTPUT_NULLIFIED(count++) : nullptr;    // gradient vs. biases
//        auto dLdsL = hasSeqLen ? INPUT_VARIABLE(count++)   : nullptr;    // gradient vs. seqLen vector, we don't calculate it !!!
//        auto dLdhI = hasInitH  ? OUTPUT_NULLIFIED(count++) : nullptr;    // gradient vs. initial output
//        auto dLdcI = hasInitC  ? OUTPUT_NULLIFIED(count++) : nullptr;    // gradient vs. initial cell state
//        auto dLdWp = hasPH     ? OUTPUT_NULLIFIED(count)   : nullptr;    // gradient vs. peephole weights

        return Booleans.countTrue(
                configuration.isRetFullSequence(),
                configuration.isRetLastH(),
                configuration.isRetLastC(),
                weights.hasBias(),
                this.maxTSLength != null,
                this.yLast != null,
                this.cLast != null,
                weights.hasPH()
                );
    }




}

