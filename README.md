# Globee 
## Simple tool for caching data about AWS resources that allows to search within its inner storage without delays and Amazon rate limits exceeding.

Resources data about EC2 Instances, AutoScaling Groups and Launch Configurations are collected once during application startup. 
After that, the storage is updated after receiving notification about concrete resource state change. 
Notifications are configured with Amazon CloudWatch, which sends them to specific Amazon SNS topics (one per each AWS Region) from where they are passed to one global Amazon SQS queue.
Globee provides asynchronous listener for given queue and is able to parse notifications which CloudWatch sends. After successful message deserialization, adequate change in data storage is made.

Globee supports multi-region, which means that details about resources from all regions provided in `application.properties` are collected and available in storage. 
Regions are to be specified as a List (separated by commas) in `globee.resources.regions` property.

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
It is also possible to use **Globee SDK** for performing some discovery operations, like getting lists of Instances, AutoScaling Groups or Launch Configurations as well as fetching data about single resource.
To do so, the following maven dependency has to be added:
```
<dependency>
    <groupId>pl.codewise.globee</groupId>
    <artifactId>globee-sdk</artifactId>
    <version>${sdk.version}</version>
</dependency>
```

### AWS Configuration

- ##### CloudWatch Events

    It is required to create a custom rule with event pattern containing aws.ec2 and aws.autoscaling as sources in each region from which we want data about resources to be collected. 
    In the target section it is necessary to specify concrete SNS topic.

- ##### SNS
    
    We have to provide separate SNS topic in each region for receiving notifications from CloudWatch within that region. 
    After creating new topic, it is required to create subscription selecting Amazon SQS as a protocol and providing its ARN as an endpoint.
    

- ##### SQS

    One global Amazon SQS is responsible for collecting notifications from all SNS topics. Properties such as `globee.sqs.region` and `globee.sqs.queue.name` 
    have to be provided to establish connection and enable reading notifications from the queue. It is highly recommended to create Standard Queue, not FIFO.

Globee acknowledges messages just after reading them and provides its own retries mechanism, so it is enough to provide two parameters in `application.properties` instead of
configuring any retries policy in AWS Console. 
`max.retries.number` specifies how many times Globee will try again to update its storage after receiving notification about some resource in case of any failure.
`milliseconds.between.retries` value is to determine how long will be the interval between consecutive retries.

### Security

As Globee is divided into backend and frontend side, it has to provide some authentication mechanisms. In fact, `globee.api.token` has to be placed in `application.properties` file. 
The very same token has to be present in HTTP header for each request being sent from frontend part of the application in order to receive access to backend data. For example, when sending a request from Postman, it is necessary to add header with key `api-token` and value same as provided with `globee.api.token` in `application.properties`.

### Outline of how Globee works

There is one separated class GlobeeConfiguration with configured beans. Moreover, classes extending ResourceStorage class own AwsCloudFactory 
which comes from commons-aws project and is responsible for both verifying users permissions to use concrete AWS profile and perform discovery operations using that profile.

Once the application is started, BaseManager is filling the so-called storage, which consists of maps with resources IDs as keys and resources (objects extending AwsResource from commons-aws) as values.
Async calls are made for all resources from all regions specified in `application.properties`. There is a retry mechanism which repeat the call in case of AmazonAutoScalingException connected with exceeding rate limit.
Just before filling the base, connection with SQS is being established. Thanks to that order, we may be sure that none of the notifications will be missed.
Notification processing is nothing more than extracting ID of the resource which state change triggered CloudWatch rule. If notification contains information about resource deletion (eg. Instance termination), then resource data will be deleted, otherwise its details will be overwritten.

SqsListener is responsible for receiving and handling messages from SQS. It implements onMessage method which in our case tries to get message payload and update storage by putting new object for previously present ID or remove given ID from the Map. 
In case of exception connected with throttling or rate limit exceeding, message text is cached as a String and retry is being scheduled in a new thread.
That approach allows to acknowledge message just after getting its payload without JMSException which is pretty convenient as it does not require any additional configuration of re-delivery policy associated with messages that might have not been handled properly.
SqsListener delegates message deserialization to SqsMessagesDeserializer which recognizes what type of resource the notification applies to and then extracts ID of that resource. Moreover, it judges whether resource has been deleted or only its state has changed.

ResourcesStorage class implements ResourceVisitor interface which allows calling visit method after extracting resource ID no matter what is the exact resource type.
That call takes place in SqsListener after deserialization. Then proper storage is being updated. All types of storage are grouped under abstract ResourceStorage class. It contains protected Map responsible for holding current data about given resource type, generic visit method updating that Map, initiate method calling from BaseManager on startup to collect initial data about resources
and finally several methods for searching and filtering records in the storage.

Searching and filtering resources by phrases is possible by using AwsResourcesFinder. It returns a List of fields whose values contain given text. Returning an empty List means that none of the fields contains value matching the pattern. 
Searching takes place inside the object's fields and also recursively in each object aggregated by the initial, if it is not a primitive type. The search algorithm is based on the reflection mechanism and if one class extends another, then the base class fields are also searched.

Last but not least, SearchController is responsible for searching and returning results to a given endpoint. Such a result has form of a JSON file that groups objects in accordance with the model proposed in the commons-aws project.
For example, a query sent to the endpoint `http://localhost:8080/launchConfigurations` will return a List of all Launch Configurations. It is possible to add parameters to the query, for example for `http://localhost:8080/instances?region=us-east-1` we will get a List of Instances within exactly one region. Query paramters can be combined by sending queries such as `http://localhost:8080/instances?region=us-east-2&id=i-0123456`. If we want to search results according to a given phrase, but we do not want to explicitly specify for which key we want to find that given value, just send a query using the word *keywords* and separate values by commas (if needed to use more than one): `http://localhost:8080/autoScalingGroups?keywords=some_value_expected_anywhere,anotherOne,yetanotherone`.
Searching for a few keywords results in returning a List of only those resources that contain exactly all of the given words, not at least one of them. Implementation of the static searching method can be found in the ResourcesStorage class.