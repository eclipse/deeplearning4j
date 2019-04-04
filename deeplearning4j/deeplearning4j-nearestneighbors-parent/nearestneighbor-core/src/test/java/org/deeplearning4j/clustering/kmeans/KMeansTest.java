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

package org.deeplearning4j.clustering.kmeans;

import org.deeplearning4j.clustering.cluster.ClusterSet;
import org.deeplearning4j.clustering.cluster.Point;
import org.deeplearning4j.clustering.cluster.PointClassification;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by agibsonccc on 7/2/17.
 */
public class KMeansTest {

    @Test
    public void testKMeans() {
        Nd4j.getRandom().setSeed(7);
        KMeansClustering kMeansClustering = KMeansClustering.setup(5, 5, "euclidean");
        List<Point> points = Point.toPoints(Nd4j.randn(5, 5));
        ClusterSet clusterSet = kMeansClustering.applyTo(points);
        PointClassification pointClassification = clusterSet.classifyPoint(points.get(0));
        System.out.println(pointClassification);
    }

    @Test
    public void testKmeansCosine() {
        Nd4j.getRandom().setSeed(7);
        int numClusters = 5;
        KMeansClustering kMeansClustering = KMeansClustering.setup(numClusters, 1000, "cosinesimilarity", true);
        List<Point> points = Point.toPoints(Nd4j.rand(5, 300));
        ClusterSet clusterSet = kMeansClustering.applyTo(points);
        PointClassification pointClassification = clusterSet.classifyPoint(points.get(0));


        KMeansClustering kMeansClusteringEuclidean = KMeansClustering.setup(numClusters, 1000, "euclidean");
        ClusterSet clusterSetEuclidean = kMeansClusteringEuclidean.applyTo(points);
        PointClassification pointClassificationEuclidean = clusterSetEuclidean.classifyPoint(points.get(0));
        System.out.println("Cosine " + pointClassification);
        System.out.println("Euclidean " + pointClassificationEuclidean);


        assertEquals(pointClassification.getCluster().getPoints().get(0),
                        pointClassificationEuclidean.getCluster().getPoints().get(0));
    }

    @Test
    public void testIssue4748() {

        INDArray result = Nd4j.randn(1, 1500);
        List<Point> points = Point.toPoints(result);

        List<List<Point>> listOfPointLists = new ArrayList<List<Point>>(); // list of point list
        List<Point> points1 = new ArrayList<Point>();

// get the first 2 then 3, then 4.... and make a point list
        for(int i = 2;   i<= points.size(); i++)
        {
            points1 = points.subList(0,i);
            listOfPointLists.add(points1);
        }
//here i create my Kmeans instance and create a List of ClusterSet because i will have many clusters
        KMeansClustering kMeansClusteringEuclidean = KMeansClustering.setup(2, 1000, "euclidean");
        List<ClusterSet> listOfClusterSet = new ArrayList<ClusterSet>();

        // here i go though the Points list list to apply kmeans
        for(int i=0;i< listOfPointLists.size();i++)
        {
            listOfClusterSet.add(kMeansClusteringEuclidean.applyTo(listOfPointLists.get(i)));
        }

        //and here i want to see my result
        for(int i=0;i<  listOfPointLists.size();i++)
        {
            System.out.print(i+" "+listOfClusterSet.get(i).getClusters().get(0).getCenter().getArray().getDouble(0)+ " ");
            System.out.println(i+" "+listOfClusterSet.get(i).getClusters().get(1).getCenter().getArray().getDouble(0));;
        }
    }
}
