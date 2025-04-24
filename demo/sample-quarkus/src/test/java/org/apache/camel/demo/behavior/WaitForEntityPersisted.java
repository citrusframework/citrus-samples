package org.apache.camel.demo.behavior;

import java.time.Duration;
import javax.sql.DataSource;

import org.apache.camel.demo.model.Booking;
import org.apache.camel.demo.model.Supply;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;

import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql;
import static org.citrusframework.container.RepeatOnErrorUntilTrue.Builder.repeatOnError;

public class WaitForEntityPersisted implements TestBehavior {

    private final String entityName;
    private final String status;
    private final DataSource dataSource;

    public WaitForEntityPersisted(Booking booking, DataSource dataSource) {
        this.entityName = "booking";
        this.status = booking.getStatus().name();
        this.dataSource = dataSource;
    }

    public WaitForEntityPersisted(Supply supply, DataSource dataSource) {
        this.entityName = "supply";
        this.status = supply.getStatus().name();
        this.dataSource = dataSource;
    }

    @Override
    public void apply(TestActionRunner t) {
        t.run(repeatOnError()
            .condition((i, context) -> i > 25)
            .autoSleep(Duration.ofMillis(1000L))
            .actions(sql().dataSource(dataSource)
                    .query()
                    .statement("select count(id) as found from %s where %s.status='%s'"
                            .formatted(entityName, entityName, status))
                    .validate("found", "1"))
        );
    }
}
