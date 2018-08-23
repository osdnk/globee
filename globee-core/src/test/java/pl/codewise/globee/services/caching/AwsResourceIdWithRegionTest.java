package pl.codewise.globee.services.caching;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AwsResourceIdWithRegionTest {

    @Test
    public void shouldAddOnlyOneObjectToSet() {
        Set<AwsResourceIdWithRegion> associationSet =
                Sets.newHashSet(AwsResourceIdWithRegion.instance("1", "Russia", false),
                        AwsResourceIdWithRegion.instance("1", "Russia", false));
        assertThat(associationSet.size()).isEqualTo(1);
    }

    @Test
    public void shouldAddTwoObjectsWithSameIdsToSet() {
        Set<AwsResourceIdWithRegion> associationSet =
                Sets.newHashSet(AwsResourceIdWithRegion.instance("2", "USA", false),
                        AwsResourceIdWithRegion.instance("2", "Poland", false));
        assertThat(associationSet.size()).isEqualTo(2);
    }

    @Test
    public void shouldAddTwoObjectsWithSameRegionsToSet() {
        Set<AwsResourceIdWithRegion> associationSet =
                Sets.newHashSet(AwsResourceIdWithRegion.instance("10", "England", false),
                        AwsResourceIdWithRegion.instance("20", "England", false));
        assertThat(associationSet.size()).isEqualTo(2);
    }
}