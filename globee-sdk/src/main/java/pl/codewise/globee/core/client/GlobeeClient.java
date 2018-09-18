package pl.codewise.globee.core.client;

import okhttp3.OkHttpClient;
import pl.codewise.commons.aws.cqrs.model.AwsResource;
import pl.codewise.globee.core.client.exceptions.GlobeeClientException;
import pl.codewise.globee.core.client.services.AutoScalingService;
import pl.codewise.globee.core.client.services.Ec2Service;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;

public class GlobeeClient {

    private final Retrofit retrofit;

    public GlobeeClient(String url, String token) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(
                chain -> chain.proceed(chain.request().newBuilder()
                        .addHeader("api-token", token)
                        .build()));
        this.retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(httpClient.build())
                .build();
    }

    public Ec2Client ec2() {
        return new Ec2Client(retrofit.create(Ec2Service.class));
    }

    public AutoScalingClient autoScaling() {
        return new AutoScalingClient(retrofit.create(AutoScalingService.class));
    }

    static <R extends AwsResource> R fetchResource(Response<List<R>> response) throws GlobeeClientException {
        if (response.isSuccessful()) {
            final List<R> body = response.body();
            if (body.size() == 1) {
                return body.get(0);
            } else if (body.size() == 0) {
                throw new GlobeeClientException("No resource with given ID");
            } else {
                throw new IllegalStateException("More than one resource found for given ID");
            }
        }
        throw new GlobeeClientException("Globee returned HTTP " + response.code());
    }
}
