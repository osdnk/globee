package pl.codewise.globee.client;

import com.amazonaws.regions.Regions;
import org.junit.Ignore;
import org.junit.Test;
import pl.codewise.globee.client.exceptions.GlobeeClientException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class GlobeeClientTest {

    private final GlobeeClient client =
            new GlobeeClient("https://mock.globee.url.com", "globee-password-mock");
    private final Ec2Client ec2Client = client.ec2();
    private final AutoScalingClient autoScalingClient = client.autoScaling();

    @Test
    public void shouldReturnInstancesListWithoutThrowingAnyException() throws IOException, GlobeeClientException {
        assertThat(ec2Client.getInstances(Regions.US_EAST_1)).isNotNull();
        final String id = ec2Client.getInstances().get(0).getInstanceId();
        assertThat(id).isNotNull();
        assertThat(ec2Client.getInstance(id)).isNotNull();
    }

    @Test(expected = GlobeeClientException.class)
    public void shouldThrowExceptionWhenWrongIdProvided() throws IOException, GlobeeClientException {
        ec2Client.getInstance("i-notAnInstanceId");
    }

    @Test
    public void shouldReturnAutoScalingGroupsListWithoutThrowingAnyException()
            throws IOException, GlobeeClientException {
        assertThat(autoScalingClient.getAutoScalingGroups(Regions.US_EAST_1)).isNotNull();
        final String name = autoScalingClient.getAutoScalingGroups().get(0).getAutoScalingGroupName();
        assertThat(name).isNotNull();
        assertThat(autoScalingClient.getAutoScalingGroup(name)).isNotNull();
    }

    @Test
    public void shouldReturnLaunchConfigurationsListWithoutThrowingAnyException()
            throws IOException, GlobeeClientException {
        assertThat(autoScalingClient.getLaunchConfigurations(Regions.US_EAST_1)).isNotNull();
        final String name = autoScalingClient.getLaunchConfigurations().get(0).getName();
        assertThat(name).isNotNull();
        assertThat(autoScalingClient.getLaunchConfiguration(name)).isNotNull();
    }
}