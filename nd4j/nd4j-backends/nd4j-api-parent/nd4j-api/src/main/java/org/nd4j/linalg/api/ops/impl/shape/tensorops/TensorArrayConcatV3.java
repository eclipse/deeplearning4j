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

package org.nd4j.linalg.api.ops.impl.shape.tensorops;

import lombok.val;
import onnx.OnnxProto3;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.imports.NoOpNameFoundException;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ops.Op;
import org.nd4j.list.compat.TensorList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TensorArrayConcatV3 extends BaseTensorOp {

    public TensorArrayConcatV3(String name, SameDiff sameDiff, SDVariable[] args){
        super(name, sameDiff, args);
    }
    public TensorArrayConcatV3(SameDiff sameDiff, SDVariable[] args){
        super(null, sameDiff, args);
    }

    public TensorArrayConcatV3(){}
   @Override
    public String onnxName() {
        throw new NoOpNameFoundException("No onnx op name found for " + opName());
    }

    @Override
    public String tensorflowName() {
        return "TensorArrayConcatV3";
    }


    @Override
    public String toString() {
        return opName();
    }

    @Override
    public String opName() {
        return "tensorarrayconcatv3";
    }

    @Override
    public TensorList execute(SameDiff sameDiff) {
       val list = getList(sameDiff);
       val array = list.concat();
       val name = this.getOwnName();
       sameDiff.setArrayForVariable(name, array);

       return list;
    }

    @Override
    public void initFromOnnx(OnnxProto3.NodeProto node, SameDiff initWith, Map<String, OnnxProto3.AttributeProto> attributesForNode, OnnxProto3.GraphProto graph) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Op.Type opType() {
        return Op.Type.CUSTOM;
    }

    @Override
    public List<DataType> calculateOutputDataTypes(java.util.List<org.nd4j.linalg.api.buffer.DataType> inputDataType){
        //Same output type as the TensorArray - which is defined by input 0
        SDVariable tArr = arg(0);
        TensorArrayV3 t3 = (TensorArrayV3) sameDiff.getVariableOutputFunction(tArr.getVarName());
        org.nd4j.linalg.api.buffer.DataType dt = t3.getTensorArrayDataType();
        return Collections.singletonList(dt);
    }
}
