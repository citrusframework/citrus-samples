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
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/api/todo")
public class TodoController {

    @Autowired
    private TodoListService todoListService;

    @ApiOperation(notes = "Returns a todo entry. Unknown todo id will simulate API error conditions", value = "Find todo entry by ID", nickname = "getEntryById" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = TodoEntry.class),
            @ApiResponse(code = 404, message = "Todo entry not found")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public TodoEntry getEntry(@ApiParam(value = "ID of todo entry that needs to be fetched", required = true) @PathVariable(value = "id") UUID id) {
        return todoListService.getEntry(id);
    }

    @ApiOperation(notes = "Sets todo entry status. Unknown todo id will simulate API error conditions", value = "Set todo entry status", nickname = "setEntryStatus" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Todo entry not found")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void setEntryStatus(@ApiParam(value = "ID of todo entry that needs to be updated", required = true) @PathVariable(value = "id") UUID id,
                               @ApiParam(value = "Status to set", required = true) @RequestParam(value = "done") boolean done) {
        todoListService.setStatus(id, done);
    }

    @ApiOperation(notes = "Delete todo entries identified by its title", value = "Delete todo entries by title", nickname = "deleteEntryByTitle" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteEntryByTitle(@ApiParam(value = "Title to identify entries that should be deleted", required = true) @RequestParam(value = "title") String title) {
        todoListService.deleteEntry(title);
    }

    @ApiOperation(notes = "Delete todo entry identified by given id. Unknown todo id will simulate API error conditions", value = "Delete todo entry by id", nickname = "deleteEntryById" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Todo entry not found")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteEntry(@ApiParam(value = "ID of todo entry that needs to be deleted", required = true) @PathVariable(value = "id") UUID id) {
        todoListService.deleteEntry(id);
    }
}
