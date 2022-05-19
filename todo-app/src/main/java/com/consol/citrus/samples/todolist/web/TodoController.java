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

import java.util.UUID;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/api/todo")
public class TodoController {

    @Autowired
    private TodoListService todoListService;

    @Operation(description = "Returns a todo entry. Unknown todo id will simulate API error conditions", summary = "Find todo entry by ID", operationId = "getEntryById" )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Todo entry not found")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public TodoEntry getEntry(@Parameter(description = "ID of todo entry that needs to be fetched", required = true) @PathVariable(value = "id") UUID id) {
        return todoListService.getEntry(id);
    }

    @Operation(description = "Sets todo entry status. Unknown todo id will simulate API error conditions", summary = "Set todo entry status", operationId = "setEntryStatus" )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Todo entry not found")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void setEntryStatus(@Parameter(description = "ID of todo entry that needs to be updated", required = true) @PathVariable(value = "id") UUID id,
                               @Parameter(description = "Status to set", required = true) @RequestParam(value = "done") boolean done) {
        todoListService.setStatus(id, done);
    }

    @Operation(description = "Delete todo entries identified by its title", summary = "Delete todo entries by title", operationId = "deleteEntryByTitle" )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteEntryByTitle(@Parameter(description = "Title to identify entries that should be deleted", required = true) @RequestParam(value = "title") String title) {
        todoListService.deleteEntry(title);
    }

    @Operation(description = "Delete todo entry identified by given id. Unknown todo id will simulate API error conditions", summary = "Delete todo entry by id", operationId = "deleteEntryById" )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Todo entry not found")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteEntry(@Parameter(description = "ID of todo entry that needs to be deleted", required = true) @PathVariable(value = "id") UUID id) {
        todoListService.deleteEntry(id);
    }
}
