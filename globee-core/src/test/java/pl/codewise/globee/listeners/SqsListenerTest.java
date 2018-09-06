package pl.codewise.globee.listeners;

import com.amazonaws.services.autoscaling.model.AmazonAutoScalingException;
import org.junit.Before;
import org.junit.Test;
import pl.codewise.globee.services.caching.ResourcesStorage;
import pl.codewise.globee.services.caching.SqsMessagesDeserializer;

import javax.jms.TextMessage;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SqsListenerTest {

    private final int maxRetriesNumber = 3;
    private final int millisecondsBetweenRetries = 100;
    private AtomicBoolean retriesSucceeded = new AtomicBoolean(false);
    private AtomicInteger exceptionsCounter = new AtomicInteger(0);

    private final TextMessage message = mock(TextMessage.class);
    private final SqsMessagesDeserializer sqsMessagesDeserializer = mock(SqsMessagesDeserializer.class);
    private final SqsListener sqsListener = new SqsListener(mock(ResourcesStorage.class), sqsMessagesDeserializer,
            maxRetriesNumber, millisecondsBetweenRetries);

    @Before
    public void mockSqsMessagesDeserializer() throws Exception {
        when(sqsMessagesDeserializer.readMessage(any())).thenAnswer(messageText -> {
            if (!(exceptionsCounter.getAndIncrement() > 1)) {
                throw new AmazonAutoScalingException("Throttling");
            }
            if (exceptionsCounter.get() == 4) {
                throw new RuntimeException(
                        "Should never happen as method is obliged to return valid Set during third execution");
            }
            retriesSucceeded.set(true);
            return Collections.emptySet();
        });
        when(message.getText()).thenReturn("Hello World");
    }

    @Test
    public void shouldExecuteSuccessfullyDuringRetryAfterAutoScalingExceptions() {
        sqsListener.onMessage(message);
        await().atMost(maxRetriesNumber * millisecondsBetweenRetries, TimeUnit.MILLISECONDS)
                .pollDelay(millisecondsBetweenRetries, TimeUnit.MILLISECONDS).untilTrue(retriesSucceeded);
    }
}