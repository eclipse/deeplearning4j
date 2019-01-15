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

package org.deeplearning4j.nn.conf.layers.convolutional.upsampling;

import java.util.Collection;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.deeplearning4j.nn.conf.InputPreProcessor;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.BaseUpsamplingLayer;
import org.deeplearning4j.nn.conf.layers.InputTypeUtil;
import org.deeplearning4j.nn.conf.memory.LayerMemoryReport;
import org.deeplearning4j.nn.conf.memory.MemoryReport;
import org.deeplearning4j.nn.conf.serde.legacyformat.LegacyIntArrayDeserializer;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.deeplearning4j.util.ValidationUtils;
import org.nd4j.base.Preconditions;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.shade.jackson.databind.annotation.JsonDeserialize;

/**
 * Upsampling 2D layer<br> Repeats each value (or rather, set of depth values) in the height and width dimensions by
 * size[0] and size[1] times respectively.<br> If input has shape {@code [minibatch, channels, height, width]} then
 * output has shape {@code [minibatch, channels, height*size[0], width*size[1]]}<br> Example:
 * <pre>
 * Input (slice for one example and channel)
 * [ A, B ]
 * [ C, D ]
 * Size = [2, 2]
 * Output (slice for one example and channel)
 * [ A, A, B, B ]
 * [ A, A, B, B ]
 * [ C, C, D, D ]
 * [ C, C, D, D ]
 * </pre>
 *
 * @author Max Pumperla
 */

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Upsampling2DLayer extends BaseUpsamplingLayer {

    @JsonDeserialize(using = LegacyIntArrayDeserializer.class)
    protected int[] size;

    protected Upsampling2DLayer(UpsamplingBuilder builder) {
        super(builder);
        this.size = builder.getSize();
    }

    @Override
    public Upsampling2DLayer clone() {
        Upsampling2DLayer clone = (Upsampling2DLayer) super.clone();
        return clone;
    }

    @Override
    public org.deeplearning4j.nn.api.Layer instantiate(NeuralNetConfiguration conf,
                    Collection<TrainingListener> trainingListeners, int layerIndex, INDArray layerParamsView,
                    boolean initializeParams) {
        org.deeplearning4j.nn.layers.convolution.upsampling.Upsampling2DLayer ret =
                        new org.deeplearning4j.nn.layers.convolution.upsampling.Upsampling2DLayer(conf);
        ret.setListeners(trainingListeners);
        ret.setIndex(layerIndex);
        ret.setParamsViewArray(layerParamsView);
        Map<String, INDArray> paramTable = initializer().init(conf, layerParamsView, initializeParams);
        ret.setParamTable(paramTable);
        ret.setConf(conf);
        return ret;
    }

    @Override
    public InputType getOutputType(int layerIndex, InputType inputType) {
        if (inputType == null || inputType.getType() != InputType.Type.CNN) {
            throw new IllegalStateException("Invalid input for Upsampling 2D layer (layer name=\"" + getLayerName()
                            + "\"): Expected CNN input, got " + inputType);
        }
        InputType.InputTypeConvolutional i = (InputType.InputTypeConvolutional) inputType;
        val inHeight = i.getHeight();
        val inWidth = i.getWidth();
        val inDepth = i.getChannels();

        return InputType.convolutional(size[0] * inHeight, size[1] * inWidth, inDepth);
    }

    @Override
    public InputPreProcessor getPreProcessorForInputType(InputType inputType) {
        if (inputType == null) {
            throw new IllegalStateException("Invalid input for Upsampling 2D layer (layer name=\"" + getLayerName()
                            + "\"): input is null");
        }
        return InputTypeUtil.getPreProcessorForInputTypeCnnLayers(inputType, getLayerName());
    }

    @Override
    public LayerMemoryReport getMemoryReport(InputType inputType) {
        InputType.InputTypeConvolutional c = (InputType.InputTypeConvolutional) inputType;
        InputType.InputTypeConvolutional outputType = (InputType.InputTypeConvolutional) getOutputType(-1, inputType);

        // During forward pass: im2col array + reduce. Reduce is counted as activations, so only im2col is working mem
        val im2colSizePerEx =
                        c.getChannels() * outputType.getHeight() * outputType.getWidth() * size[0] * size[1] * size[2];

        // Current implementation does NOT cache im2col etc... which means: it's recalculated on each backward pass
        long trainingWorkingSizePerEx = im2colSizePerEx;
        if (getIDropout() != null) {
            //Dup on the input before dropout, but only for training
            trainingWorkingSizePerEx += inputType.arrayElementsPerExample();
        }

        return new LayerMemoryReport.Builder(layerName, Upsampling2DLayer.class, inputType, outputType).standardMemory(0, 0) //No params
                        .workingMemory(0, im2colSizePerEx, 0, trainingWorkingSizePerEx)
                        .cacheMemory(MemoryReport.CACHE_MODE_ALL_ZEROS, MemoryReport.CACHE_MODE_ALL_ZEROS) //No caching
                        .build();
    }


    @NoArgsConstructor
    public static class Builder extends UpsamplingBuilder<Builder> {

        public Builder(int size) {
            super(new int[] {size, size});
        }

        /**
         * Upsampling size int, used for both height and width
         *
         * @param size upsampling size in height and width dimensions
         */
        public Builder size(int size) {

            this.setSize(new int[] {size});
            return this;
        }


        /**
         * Upsampling size array
         *
         * @param size upsampling size in height and width dimensions
         */
        public Builder size(int[] size) {
            this.setSize(size);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Upsampling2DLayer build() {
            return new Upsampling2DLayer(this);
        }

        @Override
        public void setSize(int[] size) {
            this.size = ValidationUtils.validate2(size, "size");
        }
    }

}
