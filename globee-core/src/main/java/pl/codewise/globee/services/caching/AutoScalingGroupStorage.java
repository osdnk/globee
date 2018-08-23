package pl.codewise.globee.services.caching;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.codewise.commons.aws.AwsCloudFactory;
import pl.codewise.commons.aws.cqrs.model.AwsAutoScalingGroup;
import pl.codewise.globee.services.crawlers.AwsResourcesFinder;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AutoScalingGroupStorage
        extends ResourceStorage<AwsResourceIdWithRegion.AutoScalingGroup, AwsAutoScalingGroup> {

    private final AwsCloudFactory awsCloudFactory;
    private final AwsResourcesFinder awsResourcesFinder;

    public AutoScalingGroupStorage(AwsCloudFactory awsCloudFactory, AwsResourcesFinder awsResourcesFinder,
            ExecutorService executorService) {
        super(executorService);
        this.awsCloudFactory = awsCloudFactory;
        this.awsResourcesFinder = awsResourcesFinder;
    }

    @Override
    AwsAutoScalingGroup fetchSingle(AwsResourceIdWithRegion.AutoScalingGroup autoScalingGroup) {
        return awsCloudFactory.autoScalingDiscovery(autoScalingGroup.getRegion())
                .getAutoScalingGroupByName(autoScalingGroup.getId());
    }

    @Override
    void initiate(String region) {
        awsCloudFactory.autoScalingDiscovery(region).getAutoScalingGroupsByNames(
                awsCloudFactory.autoScalingDiscovery(region).listAllAutoScalingGroupNames()).forEach(
                autoScalingGroup -> resources.put(autoScalingGroup.getAutoScalingGroupName(), autoScalingGroup));
    }

    @Override
    Set<AwsAutoScalingGroup> getMatchedResources(String searchedPhrase) {
        readLock.lock();
        try {
            return resources.values().stream()
                    .filter(autoScalingGroup -> !awsResourcesFinder
                            .getFieldsWhichValuesContainSearchedPhrase(autoScalingGroup, searchedPhrase).isEmpty())
                    .collect(Collectors.toSet());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    Set<AwsAutoScalingGroup> getResourcesFilteredForGivenCriteria(String key, String value,
            Set<AwsAutoScalingGroup> awsResources) {
        Set<AwsAutoScalingGroup> filtered = Sets.newHashSet();
        for (AwsAutoScalingGroup awsAutoScalingGroup : awsResources) {
            if (awsResourcesFinder.getFieldsWhichValuesContainSearchedPhrase(awsAutoScalingGroup, value)
                    .contains(key)) {
                filtered.add(awsAutoScalingGroup);
            }
        }
        return filtered;
    }
}
