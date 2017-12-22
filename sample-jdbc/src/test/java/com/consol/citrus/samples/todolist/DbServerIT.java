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

package com.consol.citrus.samples.todolist;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.jdbc.model.*;
import com.consol.citrus.jdbc.server.JdbcDbServer;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import javax.sql.DataSource;

/**
 * @author Christoph Deppisch
 */
public class DbServerIT extends TestNGCitrusTestDesigner {

    @Autowired
    private JdbcDbServer jdbcDbServer;

    @Autowired
    private DataSource dataSource;

    /** Jdbc database operation marshaller */
    private JdbcMarshaller jdbcMarshaller = new JdbcMarshaller();

    @Test
    @CitrusTest
    public void testCreateTable() {
        String sql = "CREATE TABLE todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255), done BOOLEAN)";

        parallel().actions(
            sql(dataSource)
                .statement(sql),

            sequential().actions(
                receive(jdbcDbServer)
                    .messageType(MessageType.XML)
                    .payload(new Operation(new Execute(new Execute.Statement(sql))), jdbcMarshaller),

                send(jdbcDbServer)
                    .payload(new OperationResult(true), jdbcMarshaller)
            )
        );
    }

    @Test
    @CitrusTest
    public void testSelect() {
        String sql = "SELECT count(*) AS cnt FROM todo_entries WHERE title = 'foo'";
        
        parallel().actions(
            query(dataSource)
                .statement(sql)
                .validate("cnt", "0"),

            sequential().actions(
                receive(jdbcDbServer)
                    .messageType(MessageType.XML)
                    .payload(new Operation(new Execute(new Execute.Statement(sql))), jdbcMarshaller),

                send(jdbcDbServer)
                    .payload(createResultSet(), jdbcMarshaller)
            ));
    }

    /**
     * Creates sample result set.
     * @return
     */
    private OperationResult createResultSet() {
        OperationResult result = new OperationResult(true);
        ResultSet resultSet = new ResultSet(1)
                .columns(new ResultSet.Column("cnt"))
                .rows(new ResultSet.Row("0"));
        result.setResultSet(resultSet);
        return result;
    }

    @Test
    @CitrusTest
    public void testDelete() {
        String sql = "DELETE FROM todo_entries";

        parallel().actions(
            sql(dataSource)
                    .statement(sql),

            sequential().actions(
                receive(jdbcDbServer)
                    .messageType(MessageType.XML)
                    .payload(new Operation(new Execute(new Execute.Statement(sql))), jdbcMarshaller),

                send(jdbcDbServer)
                    .payload(new OperationResult(true), jdbcMarshaller)
            )
        );
    }

    @Test
    @CitrusTest
    public void testDropTable() {
        String sql = "DROP TABLE todo_entries";
        
        parallel().actions(
            sql(dataSource)
                .statement(sql),

            sequential().actions(
                receive(jdbcDbServer)
                    .messageType(MessageType.XML)
                    .payload(new Operation(new Execute(new Execute.Statement(sql))), jdbcMarshaller),

                send(jdbcDbServer)
                    .payload(new OperationResult(true), jdbcMarshaller)
            )
        );
    }



}
