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
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageType;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ExecuteQueryIT extends TestNGCitrusTestRunner {

    @Autowired
    private JdbcServer jdbcServer;

    @Autowired
    private DataSource dataSource;

    @Test
    @CitrusTest
    public void testCreateTable() {
        async()
            .actions(sql(executeSQLbuilder -> executeSQLbuilder
                .dataSource(dataSource)
                .statement("CREATE TABLE IF NOT EXISTS todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255), done BOOLEAN)")));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .messageType(MessageType.JSON)
            .message(JdbcMessage.execute("CREATE TABLE IF NOT EXISTS todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255), done BOOLEAN)")));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.success()));
    }

    @Test
    @CitrusTest
    public void testSelect() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        async()
            .actions(query(exeucteSQLBuilder-> exeucteSQLBuilder
                .dataSource(dataSource)
                .statement("SELECT id, title, description FROM todo_entries")
                .validate("id", "${todoId}")
                .validate("title", "${todoName}")
                .validate("description", "${todoDescription}")));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .messageType(MessageType.JSON)
            .message(JdbcMessage.execute("SELECT id, title, description FROM todo_entries")));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(jdbcServer)
            .messageType(MessageType.JSON)
            .message(JdbcMessage.success()
                                .dataSet(new ClassPathResource("dataset.json"))));
    }

    @Test
    @CitrusTest
    public void testDelete() {
        String sql = "DELETE FROM todo_entries";

        async()
            .actions(sql(executeSQLbuilder -> executeSQLbuilder
                .dataSource(dataSource)
                .statement(sql)));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .messageType(MessageType.JSON)
            .message(JdbcMessage.execute(sql)));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.success().rowsUpdated(10)));
    }

    @Test
    @CitrusTest
    public void testDropTable() {
        String sql = "DROP TABLE todo_entries";

        async()
            .actions(sql(exeucteSQLBuilder -> exeucteSQLBuilder
                .dataSource(dataSource)
                .statement(sql)));

        receive(receiveMessageBuilder -> receiveMessageBuilder
            .endpoint(jdbcServer)
            .messageType(MessageType.JSON)
            .message(JdbcMessage.execute(sql)));

        send(sendMessageBuilder -> sendMessageBuilder
            .endpoint(jdbcServer)
            .message(JdbcMessage.success()));
    }
}
