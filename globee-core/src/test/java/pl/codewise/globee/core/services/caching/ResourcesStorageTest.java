package pl.codewise.globee.core.services.caching;

import com.google.common.collect.ImmutableMap;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.codewise.commons.aws.cqrs.model.AwsInstance;
import pl.codewise.commons.aws.cqrs.model.AwsResource;
import pl.codewise.globee.core.services.crawlers.AwsResourcesFinder;
import pl.codewise.globee.core.utils.ResourceType;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class ResourcesStorageTest {

    private final ResourcesStorage resourcesStorage =
            new ResourcesStorage(new InstanceStorage(null, new AwsResourcesFinder(), null), null, null);

    @Before
    public void before() {
        ResourcesStorage.instanceStorage.setResources(ImmutableMap.of(
                "1", newInstance("1", "globee-instance", "globee-image", "running"),
                "2", newInstance("2", "beeglo", "globee-image", "running"),
                "3", newInstance("3", "globus", "globus-image", "running"),
                "4", newInstance("4", "glob EE", "globee-image", "terminated")
        ));
    }

    @SuppressWarnings("unused")
    private Object instances() {
        return new Object[] {
                new Object[] {"globee-instance,globee-image,running", 1},
                new Object[] {"globee-image,running", 2},
                new Object[] {"running", 3},
                new Object[] {"", 4},
        };
    }

    @Test
    @Parameters(method = "instances")
    public void shouldReturnProperIntersectionOfSetsSearchingKeywordsOnly(String keywords, int expectedSize) {
        Set<? extends AwsResource> found = ResourcesStorage
                .matchAndFilterResources(Map.of("keywords", keywords), ResourceType.INSTANCE);

        assertThat(found)
                .hasSize(expectedSize);
    }

    @Test
    public void shouldReturnProperIntersectionOfSetsSearchingKeywordsInConcreteFields() {
        Set<? extends AwsResource> terminated = ResourcesStorage
                .matchAndFilterResources(Map.of("keywords", "glo", "state", "terminated"),
                        ResourceType.INSTANCE);
        assertThat(terminated.size()).isEqualTo(1);
    }

    private AwsInstance newInstance(String instanceId, String keyName, String imageId, String state) {
        return new AwsInstance.Builder()
                .withInstanceId(instanceId)
                .withKeyName(keyName)
                .withImageId(imageId)
                .withState(state)
                .build();
    }
}