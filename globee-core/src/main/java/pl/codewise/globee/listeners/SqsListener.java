package pl.codewise.globee.listeners;

import com.amazonaws.services.autoscaling.model.AmazonAutoScalingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.codewise.globee.exceptions.UnsupportedMessageFormReceivedException;
import pl.codewise.globee.services.caching.ResourcesStorage;
import pl.codewise.globee.services.caching.SqsMessagesDeserializer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
@Service
public class SqsListener implements MessageListener {

    private final ResourcesStorage resourcesStorage;
    private final SqsMessagesDeserializer sqsMessagesDeserializer;

    @Override
    public void onMessage(Message message) {
        handleReceivedMessage(message);
    }

    private void handleReceivedMessage(Message message) {
        boolean messageProcessed = processMessage((TextMessage) message);
        if (messageProcessed) {
            try {
                message.acknowledge();
            } catch (JMSException e) {
                log.warn("Unable to acknowledge message: {}", e);
            }
        } else {
            try {
                TimeUnit.SECONDS.sleep(1);
                handleReceivedMessage(message);
            } catch (InterruptedException e) {
                log.warn("Unable to sleep thread: {}", e);
            }
        }
    }

    private boolean processMessage(TextMessage message) {
        String messageText = "<message text expected here>";
        try {
            messageText = message.getText();
            sqsMessagesDeserializer.readMessage(messageText).forEach(r -> r.visit(resourcesStorage));
        } catch (UnsupportedMessageFormReceivedException | JMSException e) {
            log.warn("Unable to handle message received from SQS: \n{}", messageText, e);
        } catch (AmazonAutoScalingException e) {
            log.warn("Rate exceeded while updating data, retry in one second: {}", e);
            return false;
        }
        return true;
    }
}
