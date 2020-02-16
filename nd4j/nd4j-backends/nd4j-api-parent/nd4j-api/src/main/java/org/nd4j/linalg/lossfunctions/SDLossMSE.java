package org.nd4j.linalg.lossfunctions;

import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;

public class SDLossMSE extends SameDiffLoss {
    public SDLossMSE(){
        super();
    }

    @Override
    public SDVariable defineLoss(SameDiff sd, SDVariable layerInput, SDVariable labels) {

        return labels.squaredDifference(sd.getVariable("out")).mean(1);

    }
}