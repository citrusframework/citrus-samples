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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/todolist")
public class TodoListController {

    @Autowired
    private TodoListService chaosService;

    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("todos", chaosService.getAllEntries());

        return "todo";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String add(@RequestParam(value = "title", defaultValue = "10") String title,
                            @RequestParam(value = "description", defaultValue = "N/A") String description) {
        chaosService.addEntry(new TodoEntry(title, description));
        return "redirect:todolist";
    }

}
