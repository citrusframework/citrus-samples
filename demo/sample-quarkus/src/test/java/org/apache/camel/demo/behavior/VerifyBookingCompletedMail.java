package org.apache.camel.demo.behavior;

import org.apache.camel.demo.model.Booking;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.TestBehavior;
import org.citrusframework.mail.message.MailMessage;
import org.citrusframework.mail.server.MailServer;


public class VerifyBookingCompletedMail implements TestBehavior, TestActionSupport {

    private final Booking booking;
    private final MailServer mailServer;

    public VerifyBookingCompletedMail(Booking booking, MailServer mailServer) {
        this.booking = booking;
        this.mailServer = mailServer;
    }

    @Override
    public void apply(TestActionRunner t) {
        t.run(receive()
            .endpoint(mailServer)
            .message(MailMessage.request()
                    .from("foodmarket@quarkus.io")
                    .to("%s@quarkus.io".formatted(booking.getClient()))
                    .subject("Booking completed!")
                    .body("Hey %s, your booking %s has been completed."
                            .formatted(booking.getClient(), booking.getProduct().getName()), "text/plain"))
        );

        t.run(send()
            .endpoint(mailServer)
            .message(MailMessage.response())
        );
    }
}
