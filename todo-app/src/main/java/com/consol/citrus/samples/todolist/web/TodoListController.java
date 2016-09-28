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

import java.util.List;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/todolist")
public class TodoListController {

    @Autowired
    private TodoListService todoListService;

    @RequestMapping(method = RequestMethod.GET)
    public String listHtml(Model model) {
        model.addAttribute("todos", todoListService.getAllEntries());

        return "todo";
    }

    @RequestMapping(method = RequestMethod.GET, headers = "accept=application/json")
    @ResponseBody
    public List<TodoEntry> list() {
        return todoListService.getAllEntries();
    }

    @RequestMapping(method = RequestMethod.POST, headers = "content-type=application/x-www-form-urlencoded")
    public String addFormUrlencoded(@RequestParam(value = "title") String title,
                      @RequestParam(value = "description", defaultValue = "N/A") String description) {
        todoListService.addEntry(new TodoEntry(title, description));
        return "redirect:todolist";
    }

    @RequestMapping(method = RequestMethod.POST, headers = "content-type=application/json")
    @ResponseBody
    public String addJson(@RequestBody TodoEntry entry) {
        todoListService.addEntry(entry);
        return entry.getId().toString();
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public String clear() {
        todoListService.removeAll();
        return "redirect:todolist";
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @ResponseBody
    public Integer getTodoCount() {
        return todoListService.getAllEntries().size();
    }

}
