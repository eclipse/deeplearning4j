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

package org.deeplearning4j.nn.layers.normalization;

import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.layers.LayerHelper;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.primitives.Pair;

/**
 * Helper for the local response normalization layer.
 *
 * @author saudet
 */
public interface LocalResponseNormalizationHelper extends LayerHelper {
    boolean checkSupported(double k, double n, double alpha, double beta);

    Pair<Gradient, INDArray> backpropGradient(INDArray input, INDArray epsilon, double k, double n, double alpha,
                    double beta, LayerWorkspaceMgr workspaceMgr);

    INDArray activate(INDArray x, boolean training, double k, double n, double alpha, double beta, LayerWorkspaceMgr workspaceMgr);
}
