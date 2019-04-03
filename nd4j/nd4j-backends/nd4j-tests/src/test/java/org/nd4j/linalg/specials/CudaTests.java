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

package org.nd4j.linalg.specials;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.linalg.BaseNd4jTest;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ops.executioner.GridExecutioner;
import org.nd4j.linalg.api.ops.executioner.OpExecutioner;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@Slf4j
@RunWith(Parameterized.class)
public class CudaTests extends BaseNd4jTest {

    DataType initialType;

    public CudaTests(Nd4jBackend backend) {
        super(backend);
        this.initialType = Nd4j.dataType();
    }

    @Before
    public void setUp() {
            Nd4j.setDataType(DataType.FLOAT);
        }

    @After
    public void setDown() {
            Nd4j.setDataType(initialType);
        }

    @Test
    public void testMGrid_1() {
        if (!(Nd4j.getExecutioner() instanceof GridExecutioner))
            return;

        val arrayA = Nd4j.create(128, 128);
        val arrayB = Nd4j.create(128, 128);
        val arrayC = Nd4j.create(128, 128);

        arrayA.muli(arrayB);

        val executioner = (GridExecutioner) Nd4j.getExecutioner();

        assertEquals(1, executioner.getQueueLength());

        arrayA.addi(arrayC);

        assertEquals(1, executioner.getQueueLength());
    }


    @Test
    public void testMGrid_2() {
        if (!(Nd4j.getExecutioner() instanceof GridExecutioner))
            return;

        val exp = Nd4j.create(128, 128).assign(2.0);
        Nd4j.getExecutioner().commit();

        val arrayA = Nd4j.create(128, 128);
        val arrayB = Nd4j.create(128, 128);
        arrayA.muli(arrayB);

        val executioner = (GridExecutioner) Nd4j.getExecutioner();

        assertEquals(1, executioner.getQueueLength());

        arrayA.addi(2.0f);

        assertEquals(0, executioner.getQueueLength());

        Nd4j.getExecutioner().commit();

        assertEquals(exp, arrayA);
    }

    @Test(timeout = 10000L)
    public void testContextSpam() throws Exception {
        if (Nd4j.getExecutioner().type() != OpExecutioner.ExecutionerType.CUDA)
            return;

        val success = new AtomicInteger(0);
        val iterations = 101;

        val threads = new ArrayList<Thread>();
        for (int e = 0; e < iterations; e++) {
            val f = e;
            val t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Nd4j.create(1);
                    if (f % 50 == 0)
                        log.info("Context {} created", f);

                    Nd4j.getMemoryManager().releaseCurrentContext();
                    success.incrementAndGet();
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            t.start();
            threads.add(t);
        }

        for (val t: threads)
            t.join();

        assertEquals(iterations, success.get());
    }

    @Test
    public void testSequentialReleaseAndReacquire() throws Exception {
        if (Nd4j.getExecutioner().type() != OpExecutioner.ExecutionerType.CUDA)
            return;

        Nd4j.create(128);

        Nd4j.getMemoryManager().releaseCurrentContext();

        val array = Nd4j.create(128);
        array.addi(1.0f);
    }

    @Test
    public void test(){
        if (Nd4j.getExecutioner().type() != OpExecutioner.ExecutionerType.CUDA)
            return;

        val SD = SameDiff.create();
        val in = SD.one("test", 5, 8, 3, 4);
        SDVariable out = in.reshape(-1, 4);
        SDVariable out1 = out.reshape(4, 15, -1);
        SDVariable out2 = SD.dot(out1, out1, 2);

        SDVariable out3 = out2.reshape(-1, 4);  // <----  error here

        System.out.println(Arrays.toString(out3.eval().toFloatMatrix()));

    }


    @Override
    public char ordering() {
        return 'c';
    }
}
