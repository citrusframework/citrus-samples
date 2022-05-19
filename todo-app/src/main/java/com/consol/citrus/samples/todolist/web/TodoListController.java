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

package com.consol.citrus.samples.todolist.web;

import java.util.List;

import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.consol.citrus.samples.todolist.service.TodoListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/api/todolist")
public class TodoListController {

    @Autowired
    private TodoListService todoListService;

    @Operation(description = "Returns all available todo entries.", summary = "List todo entries", operationId = "listTodoEntries" )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<TodoEntry> list() {
        return todoListService.getAllEntries();
    }

    @RequestMapping(value = "/{limit}", method = RequestMethod.GET)
    @ResponseBody
    public List<TodoEntry> listWithLimit(@PathVariable(value = "limit") final int limit) {
        return todoListService.getAllEntries(limit);
    }

    @Operation(description = "Adds new todo entry.", summary = "Add todo entry", operationId = "addTodoEntry" )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String add(@Parameter(description = "Todo entry to be added", required = true) @RequestBody TodoEntry entry) {
        todoListService.addEntry(entry);
        return entry.getId().toString();
    }

    @Operation(description = "Delete all todo entries.", summary = "Delete all todo entries", operationId = "deleteTodoEntries" )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void clear() {
        todoListService.clear();
    }

    @Operation(description = "Gets number of available todo entries.", summary = "Gets number of todo entries", operationId = "getTodoEntryCount" )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @ResponseBody
    public Integer getTodoCount() {
        return todoListService.getAllEntries().size();
    }

}
