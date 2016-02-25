/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.samples.javaee.employee;

import com.consol.citrus.samples.javaee.employee.model.Employee;
import com.consol.citrus.samples.javaee.employee.model.Employees;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("employee")
@RequestScoped
public class EmployeeResource {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(EmployeeResource.class);

    @EJB
    private EmployeeRepository repository;

    @GET
    @Produces({ "application/xml", "application/json" })
    public Employees getList() {
        return repository.getEmployees();
    }

    @GET
    @Produces({ "application/json", "application/xml" })
    @Path("{id}")
    public Employee get(@PathParam("id") int id) {
        if (id < repository.getEmployees().getEmployees().size())
            return repository.getEmployees().getEmployees().get(id);
        else
            return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void addToList(@FormParam("name") String name,
                          @FormParam("age") int age, @FormParam("email") String email, @FormParam("mobile") String mobile) {
        log.info("Creating a new employee: " + name);
        repository.addEmployee(new Employee(name, age, email, mobile));
    }

    @PUT
    public void putToList(@FormParam("name") String name,
                          @FormParam("age") int age, @FormParam("email") String email, @FormParam("mobile") String mobile) {
        addToList(name, age, email, mobile);
    }

    @DELETE
    public void deleteAll() {
        repository.deleteEmployees();
    }

    @DELETE
    @Path("{name}")
    public void deleteFromList(@PathParam("name") String name) {
        repository.deleteEmployee(name);
    }

}
