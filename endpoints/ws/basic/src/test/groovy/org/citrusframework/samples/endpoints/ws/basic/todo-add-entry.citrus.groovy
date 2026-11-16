package org.citrusframework.samples.endpoints.ws.basic

import org.citrusframework.spi.Resources

given:
    variables {
        todoName = "citrus:concat('todo_', citrus:randomNumber(4))"
        todoDescription = "Description: ${todoName}"
    }

when: //Add entry
    $(soap().client(todoClient)
            .send()
            .message()
            .soapAction("addTodoEntry")
            .body(Resources.fromClasspath("templates/addTodoEntryRequest.xml")))

    $(soap().client(todoClient)
            .receive()
            .message()
            .body(Resources.fromClasspath("templates/addTodoEntryResponse.xml")))

then: //Verify list
    $(soap().client(todoClient)
            .send()
            .message()
            .soapAction("getTodoList")
            .body(Resources.fromClasspath("templates/getTodoListRequest.xml")))

    $(soap().client(todoClient)
            .receive()
            .message()
            .validate(validation().xpath()
                    .expression("//todo:todoEntry[todo:title='${todoName}']/todo:description",
                            "${todoDescription}"))
            .body(Resources.fromClasspath("templates/getTodoListResponse.xml")))
