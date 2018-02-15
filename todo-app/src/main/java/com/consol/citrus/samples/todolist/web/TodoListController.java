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

import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.consol.citrus.samples.todolist.service.TodoListService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/api/todolist")
public class TodoListController {

    @Autowired
    private TodoListService todoListService;

    @ApiOperation(notes = "Returns all available todo entries.", value = "List todo entries", nickname = "listTodoEntries" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = TodoEntry[].class)
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

    @ApiOperation(notes = "Adds new todo entry.", value = "Add todo entry", nickname = "addTodoEntry" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class)
    })
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String add(@ApiParam(value = "Todo entry to be added", required = true) @RequestBody TodoEntry entry) {
        todoListService.addEntry(entry);
        return entry.getId().toString();
    }

    @ApiOperation(notes = "Delete all todo entries.", value = "Delete all todo entries", nickname = "deleteTodoEntries" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void clear() {
        todoListService.clear();
    }

    @ApiOperation(notes = "Gets number of available todo entries.", value = "Gets number of todo entries", nickname = "getTodoEntryCount" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = Integer.class)
    })
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @ResponseBody
    public Integer getTodoCount() {
        return todoListService.getAllEntries().size();
    }

}
