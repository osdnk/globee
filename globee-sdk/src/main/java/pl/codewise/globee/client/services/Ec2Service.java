package pl.codewise.globee.client.services;

import pl.codewise.commons.aws.cqrs.model.AwsInstance;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface Ec2Service {

    @GET("/instances")
    Call<List<AwsInstance>> searchInstances(@Query("keywords") String keyword);

    @GET("/instances")
    Call<List<AwsInstance>> getInstances();

    @GET("/instances")
    Call<List<AwsInstance>> getInstance(@Query("instanceId") String instanceId);

    @GET("/instances")
    Call<List<AwsInstance>> getInstances(@Query("region") String region);
}
