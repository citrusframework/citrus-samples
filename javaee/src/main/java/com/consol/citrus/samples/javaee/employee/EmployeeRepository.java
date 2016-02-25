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
import com.consol.citrus.samples.javaee.mail.MailService;

import javax.ejb.EJB;
import javax.ejb.Singleton;

@Singleton
public class EmployeeRepository {

    private final Employees employees;

    @EJB
    private MailService mailService;

    public EmployeeRepository() {
        employees = new Employees();
    }

    public void addEmployee(Employee e) {
        employees.getEmployees().add(e);

        if (e.getEmail() != null && e.getEmail().length() > 0) {
            mailService.sendMail(e.getName().toLowerCase() + "@example.com", "Welcome new employee",
                    String.format("We welcome you '%s' to our company - now get to work!", e.getName()));
        }
    }

    public void deleteEmployee(String name) {
        Employee found = findEmployeeByName(name);
        if (found != null) {
            employees.getEmployees().remove(found);
        }
    }

    public void deleteEmployees() {
        employees.getEmployees().clear();
    }

    private Employee findEmployeeByName(String name) {
        for (Employee employee : employees.getEmployees()) {
            if (name.equals(employee.getName()))
                return employee;
        }
        return null;
    }

    public Employees getEmployees() {
        return employees;
    }
}
