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

package org.citrusframework.samples.endpoints.ws.basic;

import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.ws.client.WebServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;


/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    private WebServiceClient todoClient;

    @Test
    @CitrusTest
    public void testAddTodoEntry() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        $(soap()
            .client(todoClient)
            .send()
            .message()
            .soapAction("addTodoEntry")
            .body(Resources.fromClasspath("templates/addTodoEntryRequest.xml")));

        $(soap()
            .client(todoClient)
            .receive()
            .message()
            .body(Resources.fromClasspath("templates/addTodoEntryResponse.xml")));

        $(soap()
            .client(todoClient)
            .send()
            .message()
            .soapAction("getTodoList")
            .body(Resources.fromClasspath("templates/getTodoListRequest.xml")));

        $(soap()
            .client(todoClient)
            .receive()
            .message()
            .validate(validation().xpath()
                    .expression("//todo:todoEntry[todo:title='${todoName}']/todo:description",
                            "${todoDescription}"))
            .body(Resources.fromClasspath("templates/getTodoListResponse.xml")));
    }

    @Test
    @CitrusTestSource(type = TestLoader.XML,
            sources = "classpath:org/citrusframework/samples/endpoints/ws/basic/todo-add-entry.citrus.xml")
    public void testAddTodoEntryXml() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML,
            sources = "classpath:org/citrusframework/samples/endpoints/ws/basic/todo-add-entry.citrus.yaml")
    public void testAddTodoEntryYaml() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.GROOVY,
            sources = "classpath:org/citrusframework/samples/endpoints/ws/basic/todo-add-entry.citrus.groovy")
    public void testAddTodoEntryGroovy() {
    }

}
