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

package com.consol.citrus.samples.todolist;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import javax.sql.DataSource;

/**
 * @author Christoph Deppisch
 */
public class ExecuteQueryIT extends TestNGCitrusTestDesigner {

    @Autowired
    private JdbcServer jdbcServer;

    @Autowired
    private DataSource dataSource;

    @Test
    @CitrusTest
    public void testCreateTable() {
        parallel().actions(
                sql(dataSource)
                        .statement("CREATE TABLE todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255), done BOOLEAN)"),

                sequential().actions(
                        receive(jdbcServer)
                                .messageType(MessageType.JSON)
                                .message(JdbcMessage.execute("CREATE TABLE todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255), done BOOLEAN)")),

                        send(jdbcServer)
                                .message(JdbcMessage.result()
                                        .success())
                )
        );
    }

    @Test
    @CitrusTest
    public void testSelect() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        parallel().actions(
                query(dataSource)
                        .statement("SELECT id, title, description FROM todo_entries")
                        .validate("id", "${todoId}")
                        .validate("title", "${todoName}")
                        .validate("description", "${todoDescription}"),

                sequential().actions(
                        receive(jdbcServer)
                                .messageType(MessageType.JSON)
                                .message(JdbcMessage.execute("SELECT id, title, description FROM todo_entries")),

                        send(jdbcServer)
                                .messageType(MessageType.JSON)
                                .message(JdbcMessage.result()
                                        .dataSet(new ClassPathResource("dataset.json")))
                ));
    }

    @Test
    @CitrusTest
    public void testDelete() {
        String sql = "DELETE FROM todo_entries";

        parallel().actions(
                sql(dataSource)
                        .statement(sql),

                sequential().actions(
                        receive(jdbcServer)
                                .messageType(MessageType.JSON)
                                .message(JdbcMessage.execute(sql)),

                        send(jdbcServer)
                                .message(JdbcMessage.result().rowsUpdated(10))
                )
        );
    }

    @Test
    @CitrusTest
    public void testDropTable() {
        String sql = "DROP TABLE todo_entries";

        parallel().actions(
                sql(dataSource)
                        .statement(sql),

                sequential().actions(
                        receive(jdbcServer)
                                .messageType(MessageType.JSON)
                                .message(JdbcMessage.execute(sql)),

                        send(jdbcServer)
                                .message(JdbcMessage.result()
                                        .success())
                )
        );
    }



}
