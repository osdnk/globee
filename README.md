**Globee - a simple tool for caching data about AWS resources which allows to search within its inner storage without delays and Amazon rate limits exceeding.**

Resources data about EC2 Instances, AutoScaling Groups and Launch Configurations are collected once during application startup. 
After that, the storage is updated after receiving notification about concrete resource state change. 
Notifications are configured with Amazon CloudWatch, which sends them to specific Amazon SNS topics (one per each AWS Region) from where they are passed to one global Amazon SQS queue.
Globee provides asynchronous listener for given queue and is able to parse notifications which CloudWatch sends. After successful message deserialization, adequate change in data storage is made.

Globee supports multi-region, which means that details about resources from all regions provided in `application.properties` are collected and available in storage. Regions are to be specified as a list in `globee.resources.regions` property.

To use Globee, it is enough to add its dependency
```
<dependency>
    <groupId>pl.codewise.globee</groupId>
    <artifactId>globee-core</artifactId>
    <version>${core.version}</version>
</dependency>
```

and create a simple application runner that may looks like the following one:
```
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
 
@SpringBootApplication
@ComponentScan({"pl.codewise.commons.aws", "pl.codewise.globee"})

public class GlobeeApplication {
    public static void main(String[] args) {
        SpringApplication.run(GlobeeApplication.class, args);
    }
}
 ```
It is also possible to use Globee SDK for performing some discovery operations, like getting lists of Instances, AutoScaling Groups or Launch Configurations as well as fetching data about single resource.
To do so, the following maven dependency needs to be added:
```
<dependency>
    <groupId>pl.codewise.globee</groupId>
    <artifactId>globee-sdk</artifactId>
    <version>${sdk.version}</version>
</dependency>
```