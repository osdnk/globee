package pl.codewise.globee.core.services.caching;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.codewise.commons.aws.AwsCloudFactory;
import pl.codewise.commons.aws.cqrs.model.ec2.AwsLaunchConfiguration;
import pl.codewise.globee.core.services.crawlers.AwsResourcesFinder;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LaunchConfigurationStorage
        extends ResourceStorage<AwsResourceIdWithRegion.LaunchConfiguration, AwsLaunchConfiguration> {

    private final AwsCloudFactory awsCloudFactory;
    private final AwsResourcesFinder awsResourcesFinder;

    LaunchConfigurationStorage(AwsCloudFactory awsCloudFactory, AwsResourcesFinder awsResourcesFinder,
            ExecutorService executorService) {
        super(executorService);
        this.awsCloudFactory = awsCloudFactory;
        this.awsResourcesFinder = awsResourcesFinder;
    }

    @Override
    AwsLaunchConfiguration fetchSingle(AwsResourceIdWithRegion.LaunchConfiguration launchConfiguration) {
        return awsCloudFactory.launchConfigurationDiscovery(launchConfiguration.getRegion())
                .getLaunchConfiguration(launchConfiguration.getId());
    }

    @Override
    void initiate(String region) {
        awsCloudFactory.launchConfigurationDiscovery(region).listAllLaunchConfigurations()
                .forEach(launchConfiguration -> resources.put(launchConfiguration.getId(), launchConfiguration));
    }

    @Override
    Set<AwsLaunchConfiguration> getMatchedResources(String searchedPhrase) {
        readLock.lock();
        try {
            return resources.values().stream()
                    .filter(awsLaunchConfiguration -> !awsResourcesFinder
                            .getFieldsWhichValuesContainSearchedPhrase(awsLaunchConfiguration, searchedPhrase)
                            .isEmpty())
                    .collect(Collectors.toSet());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    Set<AwsLaunchConfiguration> getResourcesFilteredForGivenCriteria(String key, String value,
            Set<AwsLaunchConfiguration> awsResources) {
        Set<AwsLaunchConfiguration> filtered = Sets.newHashSet();
        for (AwsLaunchConfiguration launchConfiguration : awsResources) {
            if (awsResourcesFinder.getFieldsWhichValuesContainSearchedPhrase(launchConfiguration, value)
                    .contains(key)) {
                filtered.add(launchConfiguration);
            }
        }
        return filtered;
    }
}
