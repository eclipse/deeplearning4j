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

//
// Created by raver119 on 01.11.2017.
// Modified by GS <sgazeos@gmail.com> 4/5/2018

#include <op_boilerplate.h>
#if NOT_EXCLUDED(OP_argmax)

#include <ops/declarable/CustomOperations.h>
#include <ops/declarable/helpers/axis.h>
namespace nd4j {
    namespace ops {
        CUSTOM_OP_IMPL(argmax, 1, 1, false, 0, -2) {
            auto input = INPUT_VARIABLE(0);
            auto output = OUTPUT_VARIABLE(0);

            auto axis = *block.getIArguments();

            nd4j_printf("A %i\n", 0);

            // axis might be dynamic (i.e. tf mode)
            if (block.width() > 1 && axis.size() == 0) {
                auto axisVector = INPUT_VARIABLE(1);
                axis.resize(axisVector->lengthOf());
                helpers::adjustAxis(input, axisVector, axis);

                nd4j_printf("A %i\n", 1);

                input->template applyIndexReduce<simdOps::IndexMax<T>>(output, axis);
            } else {
                helpers::adjustAxis(input->shapeInfo(), &axis);
                nd4j_printf("A %i\n", 2);
                input->template applyIndexReduce<simdOps::IndexMax<T>>(output, axis);
            }

            nd4j_printf("A %i\n", 3);

            STORE_RESULT(output);

            return Status::OK();
        }

        DECLARE_SHAPE_FN(argmax) {
            std::vector<int> dims;

            if (block.width() == 1) {
                dims = *block.getIArguments();
            } else {
                auto y = INPUT_VARIABLE(1);
                dims = y->template asVectorT<int>();
            }

            nd4j_printf("B %i\n", 1);

            // we're resolving negative axis here
            helpers::adjustAxis(inputShape->at(0), &dims);

            if (dims.size() > 1)
                std::sort(dims.begin(), dims.end());

                nd4j_printf("B %i\n", 2);

            // special case - output is scalar
            if (dims.size() == 0 || (dims.size() == 1 && dims.at(0) == MAX_INT)) {
                return SHAPELIST(ShapeUtils<T>::createScalarShapeInfo(block.workspace()));
            }

            nd4j_printf("B %i\n", 3);
            nd4j_printv("Dims", dims);

            shape::TAD tad(inputShape->at(0), dims.data(), dims.size());
            tad.createTadOnlyShapeInfo();

            Nd4jLong tadLength = shape::tadLength(inputShape->at(0), dims.data(), dims.size());
            Nd4jLong numTads = shape::length(inputShape->at(0)) /  tadLength;

            nd4j_printf("B %i\n", 4);

            auto newShape = ShapeUtils<T>::evalReduceShapeInfo('c', dims, inputShape->at(0), false, false, block.getWorkspace());

            return SHAPELIST(newShape);
        }
    }
}

#endif