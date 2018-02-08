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

package com.consol.citrus.samples.todolist.web;

import com.consol.citrus.samples.todolist.service.ReportingService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/api/reporting")
public class ReportingController {

    @Autowired
    private ReportingService reportingService;

    @ApiOperation(notes = "Send mail reporting.", value = "Send mail reporting", nickname = "sendMailReport" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @RequestMapping(value = "/mail", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void sendMailReport() {
        reportingService.sendMailReport();
    }
}
