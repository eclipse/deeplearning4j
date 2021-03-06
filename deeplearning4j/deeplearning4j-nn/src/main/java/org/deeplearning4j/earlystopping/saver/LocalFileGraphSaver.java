/*
 *  ******************************************************************************
 *  *
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Apache License, Version 2.0 which is available at
 *  * https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  *  See the NOTICE file distributed with this work for additional
 *  *  information regarding copyright ownership.
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  * License for the specific language governing permissions and limitations
 *  * under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  *****************************************************************************
 */

package org.deeplearning4j.earlystopping.saver;

import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.earlystopping.EarlyStoppingModelSaver;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class LocalFileGraphSaver implements EarlyStoppingModelSaver<ComputationGraph> {

    private static final String BEST_GRAPH_BIN = "bestGraph.bin";
    private static final String LATEST_GRAPH_BIN = "latestGraph.bin";

    private String directory;
    private Charset encoding;

    /**Constructor that uses default character set for configuration (json) encoding
     * @param directory Directory to save networks
     */
    public LocalFileGraphSaver(String directory) {
        this(directory, Charset.defaultCharset());
    }

    /**
     * @param directory Directory to save networks
     * @param encoding Character encoding for configuration (json)
     */
    public LocalFileGraphSaver(String directory, Charset encoding) {
        this.directory = directory;
        this.encoding = encoding;

        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void saveBestModel(ComputationGraph net, double score) throws IOException {
        String confOut = FilenameUtils.concat(directory, BEST_GRAPH_BIN);
        save(net, confOut);
    }

    @Override
    public void saveLatestModel(ComputationGraph net, double score) throws IOException {
        String confOut = FilenameUtils.concat(directory, LATEST_GRAPH_BIN);
        save(net, confOut);
    }

    private void save(ComputationGraph net, String confOut) throws IOException {
        ModelSerializer.writeModel(net, confOut, true);
    }

    @Override
    public ComputationGraph getBestModel() throws IOException {
        String confOut = FilenameUtils.concat(directory, BEST_GRAPH_BIN);
        return load(confOut);
    }

    @Override
    public ComputationGraph getLatestModel() throws IOException {
        String confOut = FilenameUtils.concat(directory, LATEST_GRAPH_BIN);
        return load(confOut);
    }

    private ComputationGraph load(String confOut) throws IOException {
        ComputationGraph net = ModelSerializer.restoreComputationGraph(confOut);
        return net;
    }

    @Override
    public String toString() {
        return "LocalFileGraphSaver(dir=" + directory + ")";
    }
}
