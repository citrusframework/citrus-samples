package org.apache.camel.demo.behavior;

import org.apache.camel.demo.model.Booking;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.citrusframework.mail.message.MailMessage;
import org.citrusframework.mail.server.MailServer;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

public class VerifyBookingCompletedMail implements TestBehavior {

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
