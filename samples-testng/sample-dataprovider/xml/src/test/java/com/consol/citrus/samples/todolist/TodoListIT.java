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

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import com.consol.citrus.testng.CitrusParameters;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends AbstractTestNGCitrusTest {

    @Test(dataProvider = "todoDataProvider")
    @CitrusXmlTest(name = "TodoList_DataProvider_IT")
    @CitrusParameters( { "todoName", "todoDescription", "done" })
    public void testProvider(String todoName, String todoDescription, boolean done) {}

    @DataProvider(name = "todoDataProvider")
    public Object[][] todoDataProvider() {
        return new Object[][] {
            new Object[] { "todo1", "Description: todo1", false },
            new Object[] { "todo2", "Description: todo2", true },
            new Object[] { "todo3", "Description: todo3", false }
        };
    }

}
