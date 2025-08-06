package org.apache.camel.demo.behavior;

import java.time.Duration;
import javax.sql.DataSource;

import org.apache.camel.demo.model.Booking;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.TestBehavior;
import org.citrusframework.actions.sql.ExecuteSqlQueryActionBuilder;

public class VerifyBookingStatus implements TestBehavior, TestActionSupport {

    private final DataSource dataSource;
    private final Booking.Status status;
    private int retryAttempts = 0;

    public VerifyBookingStatus(Booking.Status status, DataSource dataSource) {
        this.status = status;
        this.dataSource = dataSource;
    }

    @Override
    public void apply(TestActionRunner t) {
        ExecuteSqlQueryActionBuilder<?, ?> verifyBookingStatus = sql()
            .dataSource(dataSource)
            .query()
            .statement("select status from booking where booking.id=${bookingId}")
            .validate("status", status.name());

        if (retryAttempts > 0) {
            t.run(repeatOnError()
                    .condition((i, context) -> i > retryAttempts)
                    .autoSleep(Duration.ofMillis(1000L))
                    .actions(verifyBookingStatus)
            );
        } else {
            t.run(verifyBookingStatus);
        }
    }

    public VerifyBookingStatus withRetryAttempts(int count) {
        this.retryAttempts = count;
        return this;
    }
}
