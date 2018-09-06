package pl.codewise.globee.services.caching;

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazonaws.services.autoscaling.model.AmazonAutoScalingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import pl.codewise.globee.exceptions.WrongSqsNameException;
import pl.codewise.globee.listeners.SqsListener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.List;

@Slf4j
@Service
public class BaseManager {

    private final String queueName;
    private final SQSConnection sqsConnection;
    private final AmazonSQSMessagingClientWrapper client;
    private final List<String> regions;
    private final ResourcesStorage resourcesStorage;
    private final SqsListener listener;

    public BaseManager(@Value("${globee.sqs.queue.name}") String queueName,
            @Value("#{'${globee.resources.regions}'.split(',')}") List<String> regions,
            SQSConnection sqsConnection,
            ResourcesStorage resourcesStorage,
            SqsListener listener) {
        this.client = sqsConnection.getWrappedAmazonSQSClient();
        this.sqsConnection = sqsConnection;
        this.queueName = queueName;
        this.resourcesStorage = resourcesStorage;
        this.regions = regions;
        this.listener = listener;
    }

    @PostConstruct
    private void startListening() throws JMSException, WrongSqsNameException {
        if (!client.queueExists(queueName)) {
            throw new WrongSqsNameException("Provided Amazon SQS name does not exist: " + queueName);
        }
        initiateStorage();
        subscribeToTopic();
    }

    @PreDestroy
    private void stopListening() {
        try {
            sqsConnection.close();
            log.info("SQS connection closed");
        } catch (JMSException e) {
            log.warn("Unable to close SQS connection", e);
        }
    }

    @Retryable(
            value = {AmazonAutoScalingException.class},
            maxAttempts = 6,
            backoff = @Backoff(delay = 10000)
    )
    private void initiateStorage() {
        try {
            resourcesStorage.initiate(regions);
        } catch (AmazonAutoScalingException e) {
            log.warn("Rate exceeded while starting the app, retrying in 10 seconds");
            throw e;
        }
    }

    private void subscribeToTopic() throws JMSException {
        Session session = sqsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(queueName);
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(listener);
        sqsConnection.start();
    }
}
