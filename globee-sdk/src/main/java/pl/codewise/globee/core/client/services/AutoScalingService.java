package pl.codewise.globee.core.client.services;

import pl.codewise.commons.aws.cqrs.model.AwsAutoScalingGroup;
import pl.codewise.commons.aws.cqrs.model.ec2.AwsLaunchConfiguration;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface AutoScalingService {

    @GET("autoScalingGroups")
    Call<List<AwsAutoScalingGroup>> searchAutoScalingGroups(@Query("keywords") String keyword);

    @GET("/autoScalingGroups")
    Call<List<AwsAutoScalingGroup>> getAutoScalingGroups();

    @GET("/autoScalingGroups")
    Call<List<AwsAutoScalingGroup>> getAutoScalingGroup(@Query("autoScalingGroupName") String name);

    @GET("/autoScalingGroups")
    Call<List<AwsAutoScalingGroup>> getAutoScalingGroups(@Query("region") String region);

    @GET("launchConfigurations")
    Call<List<AwsLaunchConfiguration>> searchLaunchConfigurations(@Query("keywords") String keyword);

    @GET("/launchConfigurations")
    Call<List<AwsLaunchConfiguration>> getLaunchConfigurations();

    @GET("/launchConfigurations")
    Call<List<AwsLaunchConfiguration>> getLaunchConfiguration(
            @Query("launchConfigurationName") String launchConfigurationName);

    @GET("/launchConfigurations")
    Call<List<AwsLaunchConfiguration>> getLaunchConfigurations(@Query("region") String region);
}
