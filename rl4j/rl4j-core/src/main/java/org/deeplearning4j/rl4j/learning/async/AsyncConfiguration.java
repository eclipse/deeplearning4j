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

package org.deeplearning4j.rl4j.learning.async;

import org.deeplearning4j.rl4j.learning.ILearning;

/**
 * @author rubenfiszel (ruben.fiszel@epfl.ch) on 8/23/16.
 *
 * Interface configuration for all training method that inherit
 * from AsyncLearning
 */
public interface AsyncConfiguration extends ILearning.LConfiguration {

    Integer getSeed();

    int getMaxEpochStep();

    int getMaxStep();

    int getNumThread();

    int getNstep();

    int getTargetDqnUpdateFreq();

    int getUpdateStart();

    double getRewardFactor();

    double getGamma();

    double getErrorClamp();

}
