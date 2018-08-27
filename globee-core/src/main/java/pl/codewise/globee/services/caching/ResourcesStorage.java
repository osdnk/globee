package pl.codewise.globee.services.caching;

import com.google.common.base.Stopwatch;
import pl.codewise.globee.utils.ResourceType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.codewise.commons.aws.cqrs.model.AwsResource;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class ResourcesStorage implements ResourceVisitor {

    @VisibleForTesting
    static InstanceStorage instanceStorage;
    private static LaunchConfigurationStorage launchConfigurationStorage;
    private static AutoScalingGroupStorage autoScalingGroupStorage;

    public ResourcesStorage(InstanceStorage instanceStorage, LaunchConfigurationStorage launchConfigurationStorage,
            AutoScalingGroupStorage autoScalingGroupStorage) {
        ResourcesStorage.instanceStorage = instanceStorage;
        ResourcesStorage.launchConfigurationStorage = launchConfigurationStorage;
        ResourcesStorage.autoScalingGroupStorage = autoScalingGroupStorage;
    }

    @Override
    public void visit(AwsResourceIdWithRegion.Instance instance) {
        instanceStorage.visit(instance);
    }

    @Override
    public void visit(AwsResourceIdWithRegion.LaunchConfiguration launchConfiguration) {
        launchConfigurationStorage.visit(launchConfiguration);
    }

    public static Set<? extends AwsResource> matchAndFilterResources(Map<String, String> query, ResourceType type) {
        switch (type) {
            case INSTANCE:
                return matchAndFilterResourcesImpl(query, ResourcesStorage.instanceStorage);
            case LAUNCH_CONFIGURATION:
                return matchAndFilterResourcesImpl(query, ResourcesStorage.launchConfigurationStorage);
            case AUTO_SCALING_GROUP:
                return matchAndFilterResourcesImpl(query, ResourcesStorage.autoScalingGroupStorage);
        }
        log.error("No valid ResourceType provided");
        return Collections.emptySet();
    }

    @Override
    public void visit(AwsResourceIdWithRegion.AutoScalingGroup autoScalingGroup) {
        autoScalingGroupStorage.visit(autoScalingGroup);
    }

    void initiate(List<String> regions) {
        Stopwatch all = Stopwatch.createStarted();
        Stopwatch instances = Stopwatch.createStarted();
        instanceStorage.initiate(regions);
        log.info("Pulling data about {} Instances took {}", instancesCount(), instances.stop());
        Stopwatch lc = Stopwatch.createStarted();
        launchConfigurationStorage.initiate(regions);
        log.info("Pulling data about {} Launch Configurations took {}", launchConfigurationsCount(), lc.stop());
        Stopwatch asg = Stopwatch.createStarted();
        autoScalingGroupStorage.initiate(regions);
        log.info("Pulling data about {} AutoScaling Groups took {}", autoScalingGroupsCount(), asg.stop());
        log.info("Pulling data from AWS took {}. There have been pulled details about {} resources",
                all.stop(), (instancesCount() + launchConfigurationsCount() + autoScalingGroupsCount()));
    }

    private static Set<? extends AwsResource> matchAndFilterResourcesImpl(Map<String, String> query,
            ResourceStorage storage) {
        Set<? extends AwsResource> matchedResources;
        if (!query.containsKey("keywords")) {
            matchedResources = storage.getMatchedResources("");
        } else {
            Set<String> keywords = Sets.newHashSet(Splitter.on(',').trimResults().splitToList(query.get("keywords")));
            Set<Set<? extends AwsResource>> results = Sets.newHashSet();
            for (String keyword : keywords) {
                results.add(storage.getMatchedResources(keyword));
            }
            final Iterator<Set<? extends AwsResource>> sortedResults = results.stream().sorted(
                    Comparator.comparingInt(Set::size)
            ).iterator();
            matchedResources = sortedResults.next();
            while (sortedResults.hasNext()) {
                matchedResources.retainAll(sortedResults.next());
            }
        }
        for (Map.Entry<String, String> entry : query.entrySet()) {
            if (!entry.getKey().equals("keywords")) {
                matchedResources = storage.getResourcesFilteredForGivenCriteria(entry.getKey(), entry.getValue(),
                        matchedResources);
            }
        }
        return matchedResources;
    }

    private int instancesCount() {
        return instanceStorage.size();
    }

    private int launchConfigurationsCount() {
        return launchConfigurationStorage.size();
    }

    private int autoScalingGroupsCount() {
        return autoScalingGroupStorage.size();
    }
}