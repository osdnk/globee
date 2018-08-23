package pl.codewise.globee.listeners;

import pl.codewise.globee.exceptions.UnsupportedMessageFormReceivedException;
import pl.codewise.globee.services.caching.ResourcesStorage;
import pl.codewise.globee.services.caching.SqsMessagesDeserializer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Slf4j
@AllArgsConstructor
@Service
public class SqsListener implements MessageListener {

    private final ResourcesStorage resourcesStorage;
    private final SqsMessagesDeserializer sqsMessagesDeserializer;

    @Override
    public void onMessage(Message message) {
        String messageText = "<message text expected here>";
        try {
            messageText = ((TextMessage) message).getText();
            message.acknowledge();
            sqsMessagesDeserializer.readMessage(messageText).forEach(r -> r.visit(resourcesStorage));
        } catch (UnsupportedMessageFormReceivedException | JMSException e) {
            log.warn("Unable to handle message received from SQS: \n{}", messageText, e);
        }
    }
}
