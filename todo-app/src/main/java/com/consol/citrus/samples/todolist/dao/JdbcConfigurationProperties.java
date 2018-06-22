/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.samples.todolist.dao;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
@ConfigurationProperties(prefix = "todo.jdbc")
public class JdbcConfigurationProperties {

    /**
     * JDBC server host
     */
    private String host;

    /**
     * JDBC server port
     */
    private String port;

    /**
     * Full qualified JDBC driver class name.
     */
    private String driverClassName;

    /**
     * JDBC connection url.
     */
    private String url;

    /**
     * JDBC user name.
     */
    private String username;

    /**
     * JDBC user password.
     */
    private String password;

    /**
     * Database file.
     */
    private String file = "file:target/testdb";

    /**
     * Auto create tables on application startup.
     */
    private boolean autoCreateTables = true;

    /**
     * Gets the driverClassName.
     *
     * @return
     */
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * Sets the driverClassName.
     *
     * @param driverClassName
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * Gets the url.
     *
     * @return
     */
    public String getUrl() {
        if (StringUtils.hasText(url)) {
            return url;
        } else {
            return String.format("jdbc:hsqldb:hsql://%s:%s/testdb", host, port);
        }
    }

    /**
     * Sets the url.
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the username.
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the host.
     *
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host.
     *
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the port.
     *
     * @return
     */
    public String getPort() {
        return port;
    }

    /**
     * Sets the port.
     *
     * @param port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Gets the file.
     *
     * @return
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the file.
     *
     * @param file
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Gets the autoCreateTables.
     *
     * @return
     */
    public boolean isAutoCreateTables() {
        return autoCreateTables;
    }

    /**
     * Sets the autoCreateTables.
     *
     * @param autoCreateTables
     */
    public void setAutoCreateTables(boolean autoCreateTables) {
        this.autoCreateTables = autoCreateTables;
    }
}
