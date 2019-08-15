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

package org.deeplearning4j.nn.layers.samediff;

import lombok.val;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.MaskState;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.samediff.AbstractSameDiffLayer;
import org.deeplearning4j.nn.gradient.DefaultGradient;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.layers.AbstractLayer;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.base.Preconditions;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.layers.ExternalErrorsFunction;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;
import org.deeplearning4j.nn.workspace.ArrayType;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.nd4j.linalg.util.ArrayUtil;

import java.util.*;

public class SameDiffLayer extends AbstractLayer<AbstractSameDiffLayer> {

    public static final String INPUT_KEY = "input";
    public static final String MASK_KEY = "mask";

    protected SameDiff sameDiff;
    protected SDVariable outputVar;
    protected ExternalErrorsFunction fn;
    protected String outputKey;

    protected INDArray params;
    protected INDArray gradients;
    protected Map<String,INDArray> paramTable;
    protected Map<String,INDArray> gradTable;


    public SameDiffLayer(NeuralNetConfiguration conf, DataType dataType){
        super(conf, dataType);
    }



    @Override
    public Layer clone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPretrainLayer() {
        return false;
    }

    @Override
    public void clearNoiseWeightParams() {
        //TODO - properly support weight noise...
    }

    @Override
    public INDArray activate(boolean training, LayerWorkspaceMgr workspaceMgr) {
        assertInputSet(false);

        try(MemoryWorkspace ws = Nd4j.getWorkspaceManager().scopeOutOfWorkspaces()) {
            if(sameDiff == null){
                doInit();
            }

            org.deeplearning4j.nn.conf.layers.samediff.SameDiffLayer bl = (org.deeplearning4j.nn.conf.layers.samediff.SameDiffLayer) layerConf();
            bl.validateInput(input);

            Map<String,INDArray> phMap = new HashMap<>();
            phMap.put(INPUT_KEY, input);
            if(maskArray != null){
                phMap.put(MASK_KEY, maskArray);
            } else {
                phMap.put(MASK_KEY, layerConf().onesMaskForInput(input));
            }

            for(String s : paramTable.keySet() ) {
                sameDiff.associateArrayWithVariable(paramTable.get(s), s);
            }

            Map<String,INDArray> out = sameDiff.exec(phMap, outputKey);
            INDArray result = out.get(outputKey);

            //Clear placeholders and op inputs to ensure no out-of-scope arrays are still referenced anywhere
            sameDiff.clearPlaceholders(true);
            sameDiff.clearOpInputs();

            return workspaceMgr.dup(ArrayType.ACTIVATIONS, result);
        }
    }


    @Override
    public Pair<Gradient, INDArray> backpropGradient(INDArray epsilon, LayerWorkspaceMgr workspaceMgr) {
        assertInputSet(true);

        Gradient g = new DefaultGradient();

        INDArray dLdIn;
        try(MemoryWorkspace ws = Nd4j.getWorkspaceManager().scopeOutOfWorkspaces()){
            if(sameDiff == null){
                doInit();
            }
            if(!sameDiff.hasGradientFunction()) {
                //Create when scoped out, to ensure any arrays are not in WS
                sameDiff.createGradFunction(INPUT_KEY);
            }

            org.deeplearning4j.nn.conf.layers.samediff.SameDiffLayer bl = (org.deeplearning4j.nn.conf.layers.samediff.SameDiffLayer) layerConf();
            bl.validateInput(input);

            for(String s : paramTable.keySet() ){
                //TODO this should only be necessary, in theory, once!
                sameDiff.associateArrayWithVariable(paramTable.get(s), s);
            }

            Map<String,INDArray> phMap = new HashMap<>();
            phMap.put(INPUT_KEY, input);
            phMap.put(fn.getGradPlaceholderName(), epsilon);
            if(maskArray != null){
                phMap.put(MASK_KEY, maskArray);
            } else {
                phMap.put(MASK_KEY, layerConf().onesMaskForInput(input));
            }

            List<String> requiredGrads = new ArrayList<>(paramTable.size() + 1);
            requiredGrads.add(sameDiff.grad(INPUT_KEY).getVarName());
            for(String s : paramTable.keySet()){
                requiredGrads.add(sameDiff.grad(s).getVarName());
            }

            sameDiff.execBackwards(phMap, requiredGrads);
            for(String s : paramTable.keySet() ){
                INDArray sdGrad = sameDiff.grad(s).getArr();
                INDArray dl4jGrad = gradTable.get(s);
                dl4jGrad.assign(sdGrad);                                            //TODO OPTIMIZE THIS
                g.gradientForVariable().put(s, dl4jGrad);
            }

            SDVariable v = sameDiff.grad(INPUT_KEY);
            dLdIn = v.getArr();

            if(dLdIn == null && fn.getGradPlaceholderName().equals(v.getVarName())){
                //Edge case with lambda layers like identity: SameDiff doesn't store the placeholders
                // So, this getArr() can be trying to get placeholder from SameDiff instance, when it's available here
                dLdIn = epsilon;
            }
        }

        //Clear placeholders and op inputs to ensure no out-of-scope arrays are still referenced anywhere
        sameDiff.clearPlaceholders(true);
        sameDiff.clearOpInputs();

        return new Pair<>(g, workspaceMgr.dup(ArrayType.ACTIVATION_GRAD, dLdIn));   //TODO OPTIMIZE THIS
    }

    /**Returns the parameters of the neural network as a flattened row vector
     * @return the parameters of the neural network
     */
    @Override
    public INDArray params() {
        return params;
    }

    @Override
    public INDArray getParam(String param) {
        return paramTable.get(param);
    }

    @Override
    public long numParams(){
        return params == null ? 0 : (int)params.length();
    }

    @Override
    public void setParam(String key, INDArray val) {
        if(!paramTable.containsKey(key)){
            throw new IllegalArgumentException("Cannot set parameter, invalid/unknown parameter key: " + key);
        }
        INDArray current = paramTable.get(key);
        if(!Arrays.equals(current.shape(), val.shape())){
            throw new IllegalArgumentException("Cannot set parameter \"" + key + "\", invalid shape: parameter array has shape "
                    + Arrays.toString(current.shape()) + ", trying to set parameter of shape " + Arrays.toString(val.shape()));
        }
    }

    @Override
    public void setParams(INDArray params) {
        if (params != null) {
            throw new UnsupportedOperationException("Not supported");
        }
    }

    protected void setParams(INDArray params, char order) {
        setParams(params);
    }

    @Override
    public void setParamsViewArray(INDArray params) {
        this.params = params;
    }

    @Override
    public INDArray getGradientsViewArray() {
        return gradients;
    }

    @Override
    public void setBackpropGradientsViewArray(INDArray gradients) {
        this.gradients = gradients;
        this.gradTable = layerConf().initializer().getGradientsFromFlattened(conf(), gradients);
    }

    @Override
    public void setParamTable(Map<String, INDArray> paramTable) {
        if(this.paramTable == null){
            this.paramTable = paramTable;
        } else {
            for (Map.Entry<String, INDArray> e : paramTable.entrySet()) {
                setParam(e.getKey(), e.getValue());
            }
        }
    }

    @Override
    public Map<String, INDArray> paramTable() {
        return paramTable(false);
    }

    @Override
    public Map<String, INDArray> paramTable(boolean backpropParamsOnly) {
        return paramTable;
    }

    protected void doInit(){
        try(MemoryWorkspace ws = Nd4j.getWorkspaceManager().scopeOutOfWorkspaces()) {
            org.deeplearning4j.nn.conf.layers.samediff.SameDiffLayer bl = (org.deeplearning4j.nn.conf.layers.samediff.SameDiffLayer) layerConf();
            sameDiff = SameDiff.create();
            Map<String, INDArray> p = paramTable();

            long[] inputShape = input.shape().clone();
            inputShape[0] = -1;
            SDVariable inputVar = sameDiff.placeHolder(INPUT_KEY, dataType, inputShape);
            Map<String, long[]> paramShapes = layerConf().getLayerParams().getParamShapes();
            Map<String, SDVariable> params = new LinkedHashMap<>();
            for (String s : paramShapes.keySet()) {
                val ps = paramShapes.get(s);
                SDVariable v = sameDiff.var(s, dataType, ps);
                params.put(s, v);
            }

            long[] maskShape = ArrayUtil.nTimes((long)inputShape.length, -1);
            SDVariable mask = sameDiff.placeHolder(MASK_KEY, dataType, maskShape);

            SDVariable layerOutput = bl.defineLayer(sameDiff, inputVar, params, mask);
            Preconditions.checkNotNull(layerOutput, "Invalid output: layer output is null");
            outputVar = layerOutput;

            for (Map.Entry<String, INDArray> e : p.entrySet()) {
                sameDiff.associateArrayWithVariable(e.getValue(), sameDiff.getVariable(e.getKey()));
            }

            //Define the function for external errors:
            fn = sameDiff.f().externalErrors(layerOutput);
            fn.outputVariable();

            this.outputKey = outputVar.getVarName();
        }
    }

    @Override
    public Pair<INDArray, MaskState> feedForwardMaskArray(INDArray maskArray, MaskState currentMaskState, int minibatchSize) {
        org.deeplearning4j.nn.conf.layers.samediff.SameDiffLayer bl = (org.deeplearning4j.nn.conf.layers.samediff.SameDiffLayer) layerConf();

        this.maskArray = maskArray;
        this.maskState = currentMaskState;

        return bl.feedForwardMaskArray(maskArray, currentMaskState, minibatchSize);
    }

}
