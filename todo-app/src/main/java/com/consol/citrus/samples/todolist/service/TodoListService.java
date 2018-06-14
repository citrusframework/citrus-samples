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

package com.consol.citrus.samples.todolist.service;

import com.consol.citrus.samples.todolist.dao.TodoListDao;
import com.consol.citrus.samples.todolist.model.TodoEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author Christoph Deppisch
 */
@Service
public class TodoListService {

    @Autowired
    private TodoListDao todoListDao;

    public void addEntry(TodoEntry entry) {
        todoListDao.save(entry);
    }

    public List<TodoEntry> getAllEntries() {
        return todoListDao.list();
    }

    public List<TodoEntry> getAllEntries(int limit) {
        return todoListDao.list(limit);
    }

    public void clear() {
        todoListDao.deleteAll();
    }

    public TodoEntry getEntry(UUID uuid) {
        for (TodoEntry entry : todoListDao.list()) {
            if (entry.getId().equals(uuid)) {
                return entry;
            }
        }

        throw new RuntimeException(String.format("Unable to find entry with uuid '%s'", uuid));
    }

    public void deleteEntry(String title) {
        for (TodoEntry entry : todoListDao.list()) {
            if (entry.getTitle().equals(title)) {
                todoListDao.delete(entry);
            }
        }
    }

    public void deleteEntry(UUID uuid) {
        for (TodoEntry entry : todoListDao.list()) {
            if (entry.getId().equals(uuid)) {
                todoListDao.delete(entry);
                return;
            }
        }

        throw new RuntimeException(String.format("Unable to find entry with uuid '%s'", uuid));
    }

    public void setStatus(UUID uuid, boolean done) {
        TodoEntry entry = getEntry(uuid);
        entry.setDone(done);
        todoListDao.update(entry);
    }
}
