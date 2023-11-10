package org.apache.camel.demo;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.camel.demo.model.Booking;
import org.jboss.logging.Logger;

@Singleton
public class MailService {

    private static final Logger LOG = Logger.getLogger(MailService.class);

    @Inject
    ReactiveMailer mailer;

    /**
     * Sends booking completed mail message to the client given in this event.
     * @param booking
     */
    public void send(Booking booking) {
        if (Booking.Status.COMPLETED != booking.getStatus() || booking.getAmount() < 100) {
            return;
        }

        LOG.info("Inform client %s via mail on completed booking: %s".formatted(booking.getClient(), booking.getProduct().getName()));

        mailer.send(
            Mail.withText("%s@quarkus.io".formatted(booking.getClient()),
                "Booking completed!",
                "Hey %s, your booking %s has been completed.".formatted(booking.getClient(), booking.getProduct().getName())
            )
        ).subscribe().with(success -> {
            LOG.info("Mail message successfully sent");
        }, failure -> {
            LOG.info("Mail message failed: " + failure.getMessage());
        });
    }
}
