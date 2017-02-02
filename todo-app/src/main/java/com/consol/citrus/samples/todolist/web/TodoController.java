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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/todo")
public class TodoController {

    @Autowired
    private TodoListService todoListService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public TodoEntry getEntry(@PathVariable(value = "id") String id) {
        return todoListService.getEntry(UUID.fromString(id));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, headers = "content-type=application/x-www-form-urlencoded")
    @ResponseBody
    public ResponseEntity setEntryStatus(@PathVariable(value = "id") String id,
                                   @RequestParam(value = "done") boolean done) {
        todoListService.setStatus(UUID.fromString(id), done);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity deleteEntryByTitle(@RequestParam(value = "title") String title) {
        todoListService.deleteEntry(title);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteEntry(@PathVariable(value = "id") String id) {
        todoListService.deleteEntry(UUID.fromString(id));

        return ResponseEntity.ok().build();
    }
}
