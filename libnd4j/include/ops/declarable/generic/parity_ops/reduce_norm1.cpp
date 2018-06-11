//
// Created by george@skymind.io on 6/4/2018.
//

#include <ops/declarable/CustomOperations.h>

namespace nd4j {
namespace ops {
#if NOT_EXCLUDED(OP_reduce_norm1)

    CUSTOM_OP_IMPL(reduce_norm1, 1, 1, false, 0, 0) {
        NDArray<T>* input = INPUT_VARIABLE(0);
        NDArray<T>* output = OUTPUT_VARIABLE(0);
        std::vector<int> axes = *block.getIArguments();

        for(const auto& item : axes)
            REQUIRE_TRUE(item > -input->shapeInfo()[0] || item <input->shapeInfo()[0], 0, "REDUCE_MEAN OP: the input dimension to reduce along must be in range (-%i, %i), but got %i instead !" , input->rankOf(), input->rankOf(), item);

        const bool keepDims = block.getTArguments()->size() > 0 ? (bool)T_ARG(0) : false;
        input->template reduceAlongDimension<simdOps::Norm1<T>>(output, axes, keepDims);

        return ND4J_STATUS_OK;
    }

    DECLARE_SHAPE_FN(reduce_norm1) {    

        const bool keepDims = block.getTArguments()->size() > 0 ? (bool)T_ARG(0) : false;
    
        std::vector<int> dimensions = *block.getIArguments();
        Nd4jLong* outShapeInfo = ShapeUtils<T>::evalReduceShapeInfo(shape::order(inputShape->at(0)), dimensions, inputShape->at(0), keepDims);

        return SHAPELIST(outShapeInfo);
    }
#endif 
#if NOT_EXCLUDED(OP_reduce_norm1_bp)

    DECLARE_SHAPE_FN(reduce_norm1_bp) {    

        const bool keepDims = block.getTArguments()->size() > 0 ? (bool)T_ARG(0) : false;
    
        Nd4jLong* outShapeInfo;// = ShapeUtils<T>::evalReduceShapeInfo(shape::order(inputShape->at(0)), dimensions, inputShape->at(0), keepDims);
        COPY_SHAPE(inputShape->at(0), outShapeInfo);

        return SHAPELIST(outShapeInfo);
    }

    CUSTOM_OP_IMPL(reduce_norm1_bp, 2, 1, false, 0, 0) {
            // L = Sum abs(x_i) for all i = 1 to N
            // dL/dx_i = 1 if x_i >= 0 and -1 when x_i < 0
            // out_i = epsilon_i if x_i > 0 and -epsilon_i when x_i < 0
            // when epsilon is non a scalar, using axes to split output onto epsilon like parts
            // and use LAMBDA with that formula for it.

            auto input = INPUT_VARIABLE(0);
            auto epsilon = INPUT_VARIABLE(1);
            auto output = OUTPUT_VARIABLE(0);

            if (epsilon->isScalar()) {
                auto norm1Backprop = LAMBDA_T(_x, epsilon) {
                    return (_x >= T(0.f) ?(*epsilon)(0):-(*epsilon)(0));
                };
                input->applyLambda(norm1Backprop, output);
            }
            else {
                std::vector<int> axes = *block.getIArguments();
                std::vector<int> dimensions; //(input->rankOf() - axes.size());
                for (Nd4jLong e = 0; e < input->rankOf(); e++) {
                    if (std::find(axes.begin(), axes.end(), e) == axes.end()) {
                        dimensions.emplace_back(e);
                    }
                }
                std::unique_ptr<ResultSet<T>> outList(NDArrayFactory<T>::allTensorsAlongDimension(output, dimensions));
                std::unique_ptr<ResultSet<T>> inList(NDArrayFactory<T>::allTensorsAlongDimension(input, dimensions));
                for (int e = 0; e < outList->size(); ++e) {
                        auto norm1Backprop = LAMBDA_TT(_x, _e) {
                            return (_x >= T(0.f) ?_e:-_e);
                        };
                        inList->at(e)->applyPairwiseLambda(epsilon, norm1Backprop, outList->at(e));
                }
            }

            //delete tmpResult;
            return ND4J_STATUS_OK;
    }
#endif

}
}
