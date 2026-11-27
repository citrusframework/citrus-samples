package org.citrusframework.samples.quarkus.eventdriven.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class EventDrivenApplication {

    /**
     * Injects an emitter to send messages to the "words-out" channel.
     */
    @Channel("words-out")
    Emitter<String> emitter;

    /**
     * Consume the message from the "words-in" channel, uppercase it and send it to the uppercase channel.
     * This method is called by the framework when a message is received on the "words-in" channel (from the broker).
     **/
    @Incoming("words-in")
    @Outgoing("uppercase")
    public String toUpperCase(String message) {
        return message.toUpperCase();
    }

    /**
     * Consume the uppercase channel (coming from within the application) and print the messages to the console.
     **/
    @Incoming("uppercase")
    public void sink(String word) {
        emitter.send(">> " + word);
    }
}
