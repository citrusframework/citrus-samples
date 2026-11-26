package org.citrusframework.samples.quarkus.eventdriven.jms;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;

/**
 * A bean producing random words every 5 seconds and sending them to the prices JMS queue.
 */
@ApplicationScoped
public class WordProducer {

    @Inject
    ConnectionFactory connectionFactory;

    public void send(String word) {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            context.createProducer().send(context.createQueue("words-out"), word);
        }
    }
}
