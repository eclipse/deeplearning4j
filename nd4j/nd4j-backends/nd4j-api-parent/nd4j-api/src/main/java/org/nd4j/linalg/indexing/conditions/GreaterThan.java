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

package org.nd4j.linalg.indexing.conditions;

import org.nd4j.linalg.api.complex.IComplexNumber;

/**
 * Created by agibsonccc on 10/8/14.
 */
public class GreaterThan extends BaseCondition {

    /**
     * Special constructor for pairwise boolean operations.
     */
    public GreaterThan() {
        super(0.0);
    }

    public GreaterThan(Number value) {
        super(value);
    }

    public GreaterThan(IComplexNumber complexNumber) {
        super(complexNumber);
    }

    /**
     * Returns condition ID for native side
     *
     * @return
     */
    @Override
    public int condtionNum() {
        return 3;
    }

    @Override
    public Boolean apply(Number input) {
        return input.doubleValue() > value.doubleValue();
    }

    @Override
    public Boolean apply(IComplexNumber input) {
        return input.absoluteValue().doubleValue() > complexNumber.absoluteValue().doubleValue();
    }
}
