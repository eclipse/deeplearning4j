package org.nd4j.linalg.api.ops.impl.shape.tensorops;

import onnx.OnnxProto3;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.list.compat.TensorList;

import java.util.Map;

public class TensorArrayReadV3 extends BaseTensorOp {

    @Override
    public String tensorflowName() {
        return "TensorArrayReadV3";
    }


    @Override
    public String opName() {
        return "tensorarrayreadv3";
    }

    @Override
    public TensorList execute(SameDiff sameDiff) {
        return null;
    }

    @Override
    public void initFromOnnx(OnnxProto3.NodeProto node, SameDiff initWith, Map<String, OnnxProto3.AttributeProto> attributesForNode, OnnxProto3.GraphProto graph) {
    }
}
