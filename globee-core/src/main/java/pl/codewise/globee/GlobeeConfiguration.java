package pl.codewise.globee;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.PredefinedClientConfigurations;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.jms.JMSException;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

@Configuration
public class GlobeeConfiguration {

    private static final String USER_AGENT_HEADER_NAME = "User-Agent";
    private static final String USER_AGENT_HEADER_VALUE = "globee";

    @Bean
    public ClientConfiguration amazonClientConfigurationProvider() {
        return PredefinedClientConfigurations.defaultConfig()
                .withHeader(USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_VALUE);
    }

    @Bean
    @Primary
    public ExecutorService asyncExecutorService() {
        return newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("async-pool-%d").setDaemon(true).build());
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Bean
    public AmazonSQS amazonSQS(@Value("${globee.sqs.region}") String region, ClientConfiguration configuration) {
        return AmazonSQSClientBuilder.standard()
                .withClientConfiguration(configuration)
                .withRegion(region)
                .build();
    }

    @Bean
    public SQSConnection sqsConnection(AmazonSQS amazonSQS)
            throws JMSException {
        return new SQSConnectionFactory(new ProviderConfiguration(), amazonSQS).createConnection();
    }
}
