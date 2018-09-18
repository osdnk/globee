package pl.codewise.globee.core.controllers;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.codewise.commons.aws.cqrs.model.AwsResource;
import pl.codewise.globee.core.services.caching.ResourcesStorage;
import pl.codewise.globee.core.utils.ResourceType;

import java.util.Map;
import java.util.Set;

@RestController
@Slf4j
public class SearchController {

    @RequestMapping(method = RequestMethod.GET, value = "/launchConfigurations")
    public Set<? extends AwsResource> postLaunchConfigurationsData(@RequestParam Map<String, String> query) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            return ResourcesStorage.matchAndFilterResources(query, ResourceType.LAUNCH_CONFIGURATION);
        } finally {
            log.info("Searching time for query {} within Launch Configurations: {}", query.values(), sw.stop());
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/instances")
    public Set<? extends AwsResource> postInstancesData(@RequestParam Map<String, String> query) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            return ResourcesStorage.matchAndFilterResources(query, ResourceType.INSTANCE);
        } finally {
            log.info("Searching time for query {} within Instances: {}", query.values(), sw.stop());
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/autoScalingGroups")
    public Set<? extends AwsResource> postAutoScalingGroupsData(@RequestParam Map<String, String> query) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            return ResourcesStorage.matchAndFilterResources(query, ResourceType.AUTO_SCALING_GROUP);
        } finally {
            log.info("Searching time for query {} within AutoScaling Groups: {}", query.values(), sw.stop());
        }
    }
}
