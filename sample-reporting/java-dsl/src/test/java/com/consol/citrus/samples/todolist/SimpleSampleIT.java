/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.todolist;

import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class SimpleSampleIT extends TestNGCitrusTestDesigner {

    @CitrusTest
    public void testSuccessFirst() {
        echo("1st test successful");
    }

    @CitrusTest
    public void testSuccessSecond() {
        echo("2nd test successful");
    }

    @CitrusTest
    public void testSuccessThird() {
        echo("3rd test successful");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    @CitrusTest
    public void testFail() {
        fail("This test should fail!");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    @CitrusTest
    public void testAnotherFail() {
        fail("Another test should fail!");
    }

    @CitrusTest
    public void testSkipped() {
        status(TestCaseMetaInfo.Status.DISABLED);
        echo("This test is skipped");
    }

}
