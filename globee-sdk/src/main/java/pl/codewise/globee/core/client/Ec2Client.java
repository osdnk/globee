package pl.codewise.globee.core.client;

import com.amazonaws.regions.Regions;
import lombok.AllArgsConstructor;
import pl.codewise.commons.aws.cqrs.model.AwsInstance;
import pl.codewise.globee.core.client.exceptions.GlobeeClientException;
import pl.codewise.globee.core.client.services.Ec2Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class Ec2Client {

    private final Ec2Service ec2Service;

    public List<AwsInstance> searchInstances(String keyword) throws IOException, GlobeeClientException {
        final Response<List<AwsInstance>> response = ec2Service.searchInstances(keyword).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        throw new GlobeeClientException("Globee returned HTTP " + response.code());
    }

    public List<AwsInstance> getInstances() throws IOException, GlobeeClientException {
        final Response<List<AwsInstance>> response = ec2Service.getInstances().execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        throw new GlobeeClientException("Globee returned HTTP " + response.code());
    }

    public List<AwsInstance> getInstances(Regions region) throws IOException, GlobeeClientException {
        final Response<List<AwsInstance>> response = ec2Service.getInstances(region.getName()).execute();
        if (response.isSuccessful()) {
            return response.body();
        }
        throw new GlobeeClientException("Globee returned HTTP " + response.code());
    }

    public AwsInstance getInstance(String instanceId) throws IOException, GlobeeClientException {
        final Response<List<AwsInstance>> response = ec2Service.getInstance(instanceId).execute();
        return GlobeeClient.fetchResource(response);
    }
}