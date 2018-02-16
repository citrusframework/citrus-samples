/*
 * Copyright 2006-2018 the original author or authors.
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
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.jdbc.message.JdbcMessage;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private JdbcServer jdbcServer;


    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testStoredProcedureCall() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        http()
                .client(todoClient)
                .send()
                .get("api/todolist/1")
                .fork(true);



        receive(jdbcServer)
                .message(JdbcMessage.createCallableStatement("{CALL limitedToDoList(?)}"));

        receive(jdbcServer)
                .messageType(MessageType.JSON)
                .message(JdbcMessage.execute("{CALL limitedToDoList(?)} - (1)"));

//        send(jdbcServer)
//                .message(JdbcMessage.result().dataSet("[ {" +
//                        "\"id\": \"" + UUID.randomUUID().toString() + "\"," +
//                        "\"title\": \"${todoName}\"," +
//                        "\"description\": \"${todoDescription}\"," +
//                        "\"done\": \"false\"" +
//                        "} ]"));
//
//        http()
//                .client(todoClient)
//                .receive()
//                .response(HttpStatus.FOUND);
    }
}
