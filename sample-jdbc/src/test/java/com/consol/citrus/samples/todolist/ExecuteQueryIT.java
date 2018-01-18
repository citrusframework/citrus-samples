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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jdbc.model.*;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
public class ExecuteQueryIT extends TestNGCitrusTestDesigner {

    @Autowired
    private JdbcServer jdbcServer;

    @Autowired
    private DataSource dataSource;

    /** Jdbc database operation marshaller */
    private ObjectMapper jdbcMarshaller = new JdbcMarshaller();

    @Test
    @CitrusTest
    public void testCreateTable() {
        parallel().actions(
            sql(dataSource)
                .statement("CREATE TABLE todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255), done BOOLEAN)"),

            sequential().actions(
                receive(jdbcServer)
                    .messageType(MessageType.JSON)
                    .payload(new Operation(new Execute(new Execute.Statement("CREATE TABLE todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255), done BOOLEAN)"))), jdbcMarshaller),

                send(jdbcServer)
                    .payload(new OperationResult(true), jdbcMarshaller)
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
                    .payload(new Operation(new Execute(new Execute.Statement("SELECT id, title, description FROM todo_entries"))), jdbcMarshaller),

                send(jdbcServer)
                    .payload(createTodoListResultSet(), jdbcMarshaller)
            ));
    }

    /**
     * Creates sample result set with mocked todolist entries.
     * @return
     */
    private OperationResult createTodoListResultSet() {
        OperationResult result = new OperationResult(true);
        try {
            result.setDataSet(FileUtils.readToString(new ClassPathResource("dataset.json")));
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
        return result;
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
                    .payload(new Operation(new Execute(new Execute.Statement(sql))), jdbcMarshaller),

                send(jdbcServer)
                    .payload(new OperationResult(true), jdbcMarshaller)
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
                    .payload(new Operation(new Execute(new Execute.Statement(sql))), jdbcMarshaller),

                send(jdbcServer)
                    .payload(new OperationResult(true), jdbcMarshaller)
            )
        );
    }



}
