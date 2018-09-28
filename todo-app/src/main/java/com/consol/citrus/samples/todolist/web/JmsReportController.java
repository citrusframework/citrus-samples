package com.consol.citrus.samples.todolist.web;

import com.consol.citrus.samples.todolist.jms.TodoJmsReportProducer;
import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.consol.citrus.samples.todolist.service.TodoListService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/api/jms/report")
@ConditionalOnProperty(prefix = "todo.jms", value = "enabled")
public class JmsReportController {

    @Autowired
    private TodoJmsReportProducer reportProducer;

    @Autowired
    private TodoListService todoListService;

    @ApiOperation(notes = "Send Jms reporting.", value = "Send Jms reporting", nickname = "sendJmsReport" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @RequestMapping(value = "/done", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void sendJmsReport() {
        reportProducer.sendReport(todoListService.getAllEntries()
                                                    .parallelStream()
                                                    .filter(TodoEntry::isDone)
                                                    .collect(Collectors.toList()));
    }
}
