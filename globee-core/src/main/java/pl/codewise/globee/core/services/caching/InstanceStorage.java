package pl.codewise.globee.core.services.caching;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.codewise.commons.aws.AwsCloudFactory;
import pl.codewise.commons.aws.cqrs.model.AwsInstance;
import pl.codewise.globee.core.services.crawlers.AwsResourcesFinder;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InstanceStorage extends ResourceStorage<AwsResourceIdWithRegion.Instance, AwsInstance> {

    private final AwsCloudFactory awsCloudFactory;
    private final AwsResourcesFinder awsResourcesFinder;

    public InstanceStorage(AwsCloudFactory awsCloudFactory, AwsResourcesFinder awsResourcesFinder,
            ExecutorService executorService) {
        super(executorService);
        this.awsCloudFactory = awsCloudFactory;
        this.awsResourcesFinder = awsResourcesFinder;
    }

    @Override
    AwsInstance fetchSingle(AwsResourceIdWithRegion.Instance instance) {
        return awsCloudFactory.ec2Discovery(instance.getRegion()).getInstance(instance.getId());
    }

    @Override
    void initiate(String region) {
        awsCloudFactory.ec2Discovery(region).getAllInstances()
                .forEach(instance -> resources.put(instance.getInstanceId(), instance));
    }

    @Override
    Set<AwsInstance> getMatchedResources(String searchedPhrase) {
        readLock.lock();
        try {
            return resources.values().stream()
                    .filter(awsInstance -> !awsResourcesFinder
                            .getFieldsWhichValuesContainSearchedPhrase(awsInstance, searchedPhrase).isEmpty())
                    .collect(Collectors.toSet());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    Set<AwsInstance> getResourcesFilteredForGivenCriteria(String key, String value, Set<AwsInstance> awsResources) {
        Set<AwsInstance> filtered = Sets.newHashSet();
        for (AwsInstance awsInstance : awsResources) {
            if (awsResourcesFinder.getFieldsWhichValuesContainSearchedPhrase(awsInstance, value).contains(key)) {
                filtered.add(awsInstance);
            }
        }
        return filtered;
    }
}
