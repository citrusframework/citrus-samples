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

package com.consol.citrus.samples.todolist.dao;

import com.consol.citrus.samples.todolist.model.TodoEntry;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class JdbcTransactionalTodoListDao extends JdbcTodoListDao {

    @Override
    public void save(final TodoEntry entry) {
        super.save(entry);
    }

    @Override
    public List<TodoEntry> list() {
        return super.list();
    }

    @Override
    public void delete(final TodoEntry entry) {
        super.delete(entry);
    }

    @Override
    public void deleteAll() {
        super.deleteAll();
    }

    @Override
    public void update(final TodoEntry entry) {
        super.update(entry);
    }

}
