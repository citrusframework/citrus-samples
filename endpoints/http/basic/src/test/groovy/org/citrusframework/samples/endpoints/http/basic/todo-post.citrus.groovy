package org.citrusframework.samples.endpoints.http.basic

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

given:
    variables {
        todoName = "citrus:concat('todo_', citrus:randomNumber(4))"
        todoDescription = "Description: ${todoName}"
    }

when:
    $(http().client(todoClient)
            .send()
            .post("/todolist")
            .message()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("title=${todoName}&description=${todoDescription}"))

then:
    $(http().client(todoClient)
            .receive()
            .response(HttpStatus.OK))
