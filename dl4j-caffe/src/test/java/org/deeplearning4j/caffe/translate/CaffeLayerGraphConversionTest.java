package org.deeplearning4j.caffe.translate;

import org.deeplearning4j.caffe.CaffeTestUtil;
import org.deeplearning4j.caffe.projo.Caffe.NetParameter;
import org.deeplearning4j.dag.Graph;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertTrue;

/**
 * @author jeffreytang
 */
public class CaffeLayerGraphConversionTest {

    @Test
    public void convertTest() throws IOException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        NetParameter net = CaffeTestUtil.getNet();
        Graph graph = CaffeLayerGraphConversion.convert(net);
        System.out.println(graph);
        assertTrue(graph != null);

    }
}
