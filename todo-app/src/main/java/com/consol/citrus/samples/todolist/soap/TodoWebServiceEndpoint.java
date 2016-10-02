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

package com.consol.citrus.samples.todolist.soap;

import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.consol.citrus.samples.todolist.service.TodoListService;
import com.consol.citrus.samples.todolist.soap.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.server.endpoint.annotation.*;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.List;

/**
 * @author Christoph Deppisch
 */
@Endpoint
public class TodoWebServiceEndpoint {

    private static final String NAMESPACE_URI = "http://citrusframework.org/samples/todolist";

    @Autowired
    private TodoListService todoListService;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addTodoEntryRequest")
    @ResponsePayload
    public AddTodoEntryResponse addTodoEntry(@RequestPayload AddTodoEntryRequest request) {
        todoListService.addEntry(new TodoEntry(request.getTitle(), request.getDescription()));

        AddTodoEntryResponse response = new AddTodoEntryResponse();
        response.setSuccess(true);

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getTodoListRequest")
    @ResponsePayload
    public GetTodoListResponse getTodoList(@RequestPayload GetTodoListRequest request) {
        List<TodoEntry> todoEntryList = todoListService.getAllEntries();

        GetTodoListResponse response = new GetTodoListResponse();
        response.setList(new GetTodoListResponse.List());
        for (TodoEntry todoEntry : todoEntryList) {
            GetTodoListResponse.List.TodoEntry entry = new GetTodoListResponse.List.TodoEntry();
            entry.setId(todoEntry.getId().toString());
            entry.setTitle(todoEntry.getTitle());
            entry.setDescription(todoEntry.getDescription());
            response.getList().getTodoEntry().add(entry);
        }

        return response;
    }

    @Bean(name = "todolist")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema countriesSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("TodoListPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("http://citrusframework.org/samples/todolist");
        wsdl11Definition.setSchema(countriesSchema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema countriesSchema() {
        return new SimpleXsdSchema(new ClassPathResource("schema/TodoList.xsd"));
    }
}
