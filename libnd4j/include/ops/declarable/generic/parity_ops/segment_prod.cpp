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
// Created by george@skymind.io on 2/21/2018.
//

#include <ops/declarable/CustomOperations.h>
#include <ops/declarable/helpers/segment.h>

namespace nd4j {
    namespace ops {
        CUSTOM_OP_IMPL(segment_prod, 2, 1, false, 0, 0) {
            auto input = INPUT_VARIABLE(0);
            auto idxSegments = INPUT_VARIABLE(1);
            auto segmentedOutput = OUTPUT_VARIABLE(0);
            REQUIRE_TRUE(idxSegments->isVector(), 0, "segment_prod: segment indexes array should be a vector, but it rank is %i.", idxSegments->rankOf());
            REQUIRE_TRUE(idxSegments->lengthOf() == input->sizeAt(0), 0, "segment_prod: segment indexes array length should be equal to the input first dimension, but %i != %i.", idxSegments->lengthOf(), input->sizeAt(0));

            auto expected = NDArrayFactory::create(input->dataType(), 0.f, block.launchContext());
            auto wrong = NDArrayFactory::create(input->dataType(), 0.f, block.launchContext());

            REQUIRE_TRUE(helpers::segmentIndicesValidate(block.launchContext(), idxSegments, expected, wrong), 0, "segment_prod: segment indices should be arranged, but %2.1f > %2.1f", expected.e<float>(0), wrong.e<float>(0));

            segmentedOutput->nullify();
            helpers::segmentProdFunctor(block.launchContext(), input, idxSegments, segmentedOutput);

            return Status::OK();
        }

        DECLARE_SHAPE_FN(segment_prod) {
            auto idxVector = INPUT_VARIABLE(1);

            auto in = inputShape->at(0);
            int outRank = shape::rank(in);
            Nd4jLong* outputShape = nullptr;
            int val = (*idxVector).e<int>(idxVector->lengthOf() - 1);

            int numOfClasses = val + 1;

            ALLOCATE(outputShape, block.getWorkspace(), shape::shapeInfoLength(outRank), Nd4jLong);

            outputShape[0] = outRank;
            outputShape[1] = numOfClasses;
            for(int i = 1; i < outRank; ++i)
                outputShape[i + 1] = shape::sizeAt(in, i);

            ShapeUtils::updateStridesAndType(outputShape, in, shape::order(in));

            return SHAPELIST(CONSTANT(outputShape));
        }

        CUSTOM_OP_IMPL(segment_prod_bp, 3, 2, false, 0, 0) {
            auto input = INPUT_VARIABLE(0);
            auto indices = INPUT_VARIABLE(1);
            auto gradOut = INPUT_VARIABLE(2);
            auto output = OUTPUT_VARIABLE(0);
            auto outIndices = OUTPUT_VARIABLE(1);
            outIndices->assign(indices);
            helpers::segmentProdFunctorBP(block.launchContext(), input, indices, gradOut, output);

            return Status::OK();
        }

        DECLARE_TYPES(segment_prod) {
            getOpDescriptor()
                    ->setAllowedInputTypes(0, {ALL_FLOATS, ALL_INTS})
                    ->setAllowedInputTypes(1, {ALL_INTS})
                    ->setAllowedOutputTypes({ALL_FLOATS, ALL_INTS})
                    ->setSameMode(false);
        }


        DECLARE_SHAPE_FN(segment_prod_bp){
            Nd4jLong* in = inputShape->at(0);
            Nd4jLong* inIdx = inputShape->at(1);

            Nd4jLong* outShape;
            Nd4jLong* outIndex;
            COPY_SHAPE(in, outShape);
            COPY_SHAPE(inIdx, outIndex);
            return SHAPELIST(CONSTANT(outShape), CONSTANT(outIndex));
        }

        DECLARE_TYPES(segment_prod_bp) {
            getOpDescriptor()
                    ->setAllowedInputTypes(nd4j::DataType::ANY)
                    ->setAllowedOutputTypes(0, {ALL_FLOATS})
					->setAllowedOutputTypes(1, {ALL_INTS})
                    ->setSameMode(false);
        }
    }
}
