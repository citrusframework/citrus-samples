package com.consol.citrus.samples.todolist.web;

import java.util.stream.Collectors;

import com.consol.citrus.samples.todolist.jms.TodoJmsReportProducer;
import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.consol.citrus.samples.todolist.service.TodoListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    @Operation(description = "Send Jms reporting.", summary = "Send Jms reporting", operationId = "sendJmsReport" )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
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
