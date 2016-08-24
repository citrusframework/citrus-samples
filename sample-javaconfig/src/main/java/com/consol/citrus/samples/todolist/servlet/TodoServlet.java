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

package com.consol.citrus.samples.todolist.servlet;

import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.consol.citrus.samples.todolist.service.TodoListService;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;

/**
 * @author Christoph Deppisch
 */
public class TodoServlet extends HttpServlet {

    /** Service implementation */
    private TodoListService todoListService = new TodoListService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getServletContext().getResourceAsStream("/WEB-INF/index.html"), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("<!-- for each entry -->")) {
                    line = reader.readLine();
                    for (TodoEntry entry : todoListService.getAllEntries()) {
                        resp.getWriter().write(
                                line.replace("{{ id }}", entry.getId().toString())
                                    .replace("{{ title }}", entry.getTitle())
                                    .replace("{{ description }}", entry.getDescription())
                        );
                    }

                    if (todoListService.getAllEntries().isEmpty()) {
                        resp.getWriter().write(line.replace("{{ title }}", "No todo entries"));
                    }
                } else {
                    resp.getWriter().write(line);
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String title = req.getParameter("title");
        String description = req.getParameter("description");

        todoListService.addEntry(new TodoEntry(title, description));

        resp.sendRedirect("index.html");
    }
}
