package com.consol.citrus.samples.todolist;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.AbstractMessageValidator;
import com.consol.citrus.validation.context.DefaultValidationContext;
import org.springframework.util.Assert;

/**
 * Validates binary message content.
 */
public class BinaryMessageValidator extends AbstractMessageValidator<DefaultValidationContext> {

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, DefaultValidationContext validationContext) {
        Assert.isTrue(new String(receivedMessage.getPayload(byte[].class))
                .equals(new String(controlMessage.getPayload(byte[].class))), "Binary message validation failed!");
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.BINARY.name());
    }

    @Override
    protected Class<DefaultValidationContext> getRequiredValidationContextType() {
        return DefaultValidationContext.class;
    }
}
