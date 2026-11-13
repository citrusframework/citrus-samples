package org.citrusframework.samples.endpoints.http.basic

import org.citrusframework.message.MessageType
import org.springframework.http.HttpStatus

given:
    variables {
        todoId = "citrus:randomUUID()"
        todoName = "citrus:concat('todo_', citrus:randomNumber(4))"
        todoDescription = "Description: ${todoName}"
    }

when: //Create entry
    $(http().client(todoClient)
            .send()
            .post("/api/todolist")
            .message()
            .type(MessageType.JSON)
            .contentType("application/json")
            .body("""
                {
                  "id": "${todoId}",
                  "title": "${todoName}",
                  "description": "${todoDescription}",
                  "done": false
                }
            """))

    $(http().client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("${todoId}"))

then: //Verify existence
    $(http().client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .message()
            .accept("application/json"))

    $(http().client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.JSON)
            .validate(validation().jsonPath()
                    .expression('$.id', "${todoId}")
                    .expression('$.title', "${todoName}")
                    .expression('$.description', "${todoDescription}")
                    .expression('$.done', false)))

    $(http().client(todoClient)
            .send()
            .delete("/api/todo/${todoId}")
            .message()
            .accept("application/json"))

    $(http().client(todoClient)
            .receive()
            .response(HttpStatus.OK))
