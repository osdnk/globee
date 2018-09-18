package pl.codewise.globee.core.listeners;

import com.amazonaws.services.autoscaling.model.AmazonAutoScalingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.codewise.globee.core.exceptions.UnsupportedMessageFormReceivedException;
import pl.codewise.globee.core.services.caching.ResourcesStorage;
import pl.codewise.globee.core.services.caching.SqsMessagesDeserializer;

import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SqsListener implements MessageListener {

    private final ResourcesStorage resourcesStorage;
    private final SqsMessagesDeserializer sqsMessagesDeserializer;
    private final int maxRetriesNumber;
    private final int millisecondsBetweenRetries;
    private final ScheduledExecutorService unprocessedExecutorService = Executors.newSingleThreadScheduledExecutor();

    public SqsListener(ResourcesStorage resourcesStorage,
            SqsMessagesDeserializer sqsMessagesDeserializer, @Value("${max.retries.number}") int maxRetriesNumber,
            @Value("${milliseconds.between.retries}") int millisecondsBetweenRetries) {
        this.resourcesStorage = resourcesStorage;
        this.sqsMessagesDeserializer = sqsMessagesDeserializer;
        this.maxRetriesNumber = maxRetriesNumber;
        this.millisecondsBetweenRetries = millisecondsBetweenRetries;
    }

    @PreDestroy
    public void shutdown() {
        unprocessedExecutorService.shutdown();
    }

    @Override
    public void onMessage(Message message) {
        handleReceivedMessage(message);
    }

    private void handleReceivedMessage(Message message) {
        final String messagePayload;
        try {
            messagePayload = ((TextMessage) message).getText();
            message.acknowledge();
        } catch (JMSException e) {
            log.error("Unable to read message from SQS", e);
            return;
        }
        if (!processMessage(messagePayload)) {
            tryProcessInFuture(messagePayload, maxRetriesNumber);
        }
    }

    private void tryProcessInFuture(String messagePayload, Integer tryNumber) {
        Runnable process = () -> {
            if (tryNumber == 0) {
                log.error("Max number of retries reached, message will not be processed");
                return;
            }
            if (!processMessage(messagePayload)) {
                tryProcessInFuture(messagePayload, tryNumber - 1);
            }
        };
        unprocessedExecutorService.schedule(process, millisecondsBetweenRetries, TimeUnit.MILLISECONDS);
    }

    private boolean processMessage(String messageText) {
        try {
            sqsMessagesDeserializer.readMessage(messageText).forEach(r -> r.visit(resourcesStorage));
        } catch (UnsupportedMessageFormReceivedException e) {
            log.error("Unable to handle message received from SQS:\n{}", messageText, e);
        } catch (AmazonAutoScalingException e) {
            log.warn("Rate exceeded while updating data, retry in {} ms: {}", millisecondsBetweenRetries, e.getErrorMessage());
            return false;
        }
        return true;
    }
}
