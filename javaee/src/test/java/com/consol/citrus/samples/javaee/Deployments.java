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

package com.consol.citrus.samples.javaee;

import com.consol.citrus.samples.javaee.employee.*;
import com.consol.citrus.samples.javaee.employee.jms.EmployeeJmsResource;
import com.consol.citrus.samples.javaee.employee.model.Employee;
import com.consol.citrus.samples.javaee.employee.model.Employees;
import com.consol.citrus.samples.javaee.mail.MailService;
import com.consol.citrus.samples.javaee.sms.SmsGatewayService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.smsgateway.schema.smsgateway.v1.SmsGateway;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class Deployments {

    private static final String CXF_VERSION = "3.1.4";
    private static final String CXF_GROUP_ID = "org.apache.cxf";

    /**
     * Default employee registry application with REST Http resource.
     * @return
     */
    public static WebArchive employeeWebRegistry() {
        return employeeRegistry().addClasses(RegistryApplication.class, EmployeeResource.class);
    }

    /**
     * Default employee registry application with JMS resource MDB.
     * @return
     */
    public static WebArchive employeeJmsRegistry() {
        return employeeRegistry().addClass(EmployeeJmsResource.class);
    }

    /**
     * Default web archive for the employe registry application.
     * @return
     */
    private static WebArchive employeeRegistry() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        MailService.class, SmsGatewayService.class, Employees.class,
                        Employee.class, EmployeeRepository.class)
                .addPackage(SmsGateway.class.getPackage())
                .addAsLibraries(Maven.configureResolver()
                        .workOffline(true)
                        .resolve(CXF_GROUP_ID + ":cxf-rt-frontend-jaxws:" + CXF_VERSION,
                                CXF_GROUP_ID + ":cxf-rt-transports-http:" + CXF_VERSION)
                        .withTransitivity()
                        .asFile());
    }
}
