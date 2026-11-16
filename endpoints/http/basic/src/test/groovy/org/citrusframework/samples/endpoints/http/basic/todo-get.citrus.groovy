package org.citrusframework.samples.endpoints.http.basic

import org.citrusframework.message.MessageType
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

when:
    $(http().client(todoClient)
            .send()
            .get("/todolist")
            .message()
            .accept(MediaType.TEXT_HTML_VALUE))

then:
    $(http().client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .message()
            .type(MessageType.XHTML)
            .validate(validation().xpath()
                    .expression("//xh:h1", "TODO list"))
            .body("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                    "\"org/w3/xhtml/xhtml1-transitional.dtd\">" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                    "<head>@ignore@</head>" +
                    "<body>@ignore@</body>" +
                    "</html>"))
