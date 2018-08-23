package pl.codewise.globee.services.caching;

import pl.codewise.globee.exceptions.UnsupportedMessageFormReceivedException;
import pl.codewise.globee.utils.GlobeeStringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static pl.codewise.globee.utils.GlobeeStringUtils.extractJson;

@Slf4j
@Service
@AllArgsConstructor
public class SqsMessagesDeserializer {

    private static final List<Pattern> INSTANCE_ID_REGEXP = ImmutableList.of(
            Pattern.compile("\\Wi-\\w{8}\\W"),
            Pattern.compile("\\Wi-\\w{17}\\W"));

    private final ObjectMapper objectMapper;

    public Set<AwsResourceIdWithRegion> readMessage(String messageText)
            throws UnsupportedMessageFormReceivedException {
        try {
            JsonNode node = objectMapper.readTree(messageText);
            final String message = extractJson(node.get("Message").toString());
            final String region = objectMapper.readTree(message).get("region").toString();
            return extractResources(message, region);
        } catch (JsonParseException | IOException e) {
            throw new UnsupportedMessageFormReceivedException("Unsupported notification form, unable to process", e);
        }
    }

    private Set<AwsResourceIdWithRegion> extractResources(String message, String region)
            throws IOException, UnsupportedMessageFormReceivedException {
        if (message.contains("CreateLaunchConfiguration") || message.contains("DeleteLaunchConfiguration")) {
            return extractLaunchConfiguration(message, region);
        }
        if (GlobeeStringUtils.removeQuotationMarks(objectMapper.readTree(message).get("source").toString())
                .equals("aws.autoscaling")) {
            return extractAutoScalingGroup(message, region);
        }
        return extractInstances(message, region);
    }

    private Set<AwsResourceIdWithRegion> extractLaunchConfiguration(String message, String region)
            throws IOException, UnsupportedMessageFormReceivedException {
        boolean shouldResourceBeDeleted = false;
        if (message.contains("DeleteLaunchConfiguration")) {
            shouldResourceBeDeleted = true;
        }
        return Set.of(AwsResourceIdWithRegion.launchConfiguration(
                extractLaunchConfigurationName(message), region, shouldResourceBeDeleted));
    }

    private Set<AwsResourceIdWithRegion> extractAutoScalingGroup(String message, String region)
            throws IOException, UnsupportedMessageFormReceivedException {
        boolean shouldResourceBeDeleted = false;
        if (message.contains("DeleteAutoScalingGroup")) {
            shouldResourceBeDeleted = true;
        }
        return Set.of(AwsResourceIdWithRegion
                .autoScalingGroup(extractAutoScalingGroupName(message), region, shouldResourceBeDeleted));
    }

    private Set<AwsResourceIdWithRegion> extractInstances(String message, String region) {
        Set<String> matched = INSTANCE_ID_REGEXP.stream()
                .map(pattern -> pattern.matcher(message))
                .flatMap(matcher -> matcher.results().map(MatchResult::group))
                .collect(Collectors.toSet());
        if (matched.isEmpty()) {
            log.debug(message);
        }
        return matched.stream()
                .map(GlobeeStringUtils::removeFirstAndLastCharacter)
                .map(id -> AwsResourceIdWithRegion.instance(id, region, false))
                .collect(Collectors.toSet());
    }

    private String extractLaunchConfigurationName(String message)
            throws IOException, UnsupportedMessageFormReceivedException {

        String detail = objectMapper.readTree(message).get("detail").toString();

        Optional<JsonNode> launchConfigurationName =
                Optional.ofNullable(objectMapper.readTree(detail).get("launchConfigurationName"));
        if (launchConfigurationName.isPresent()) {
            return launchConfigurationName.get().toString();
        }
        Optional<JsonNode> requestParameters =
                Optional.ofNullable(objectMapper.readTree(detail).get("requestParameters"));
        if (requestParameters.isPresent()) {
            launchConfigurationName = Optional.ofNullable(
                    objectMapper.readTree(requestParameters.get().toString()).get("launchConfigurationName"));
            if (launchConfigurationName.isPresent()) {
                return launchConfigurationName.get().toString();
            }
        }
        throw new UnsupportedMessageFormReceivedException(
                "Received notification contains no Launch Configuration name");
    }

    private String extractAutoScalingGroupName(String message)
            throws IOException, UnsupportedMessageFormReceivedException {
        String detail = objectMapper.readTree(message).get("detail").toString();

        Optional<JsonNode> autoScalingGroupName =
                Optional.ofNullable(objectMapper.readTree(detail).get("AutoScalingGroupName"));
        if (autoScalingGroupName.isPresent()) {
            return autoScalingGroupName.get().toString();
        }
        Optional<JsonNode> requestParameters =
                Optional.ofNullable(objectMapper.readTree(detail).get("requestParameters"));
        if (requestParameters.isPresent()) {
            autoScalingGroupName = Optional.ofNullable(
                    objectMapper.readTree(requestParameters.get().toString()).get("autoScalingGroupName"));
            if (autoScalingGroupName.isPresent()) {
                return autoScalingGroupName.get().toString();
            }
            Optional<JsonNode> tags =
                    Optional.ofNullable(objectMapper.readTree(requestParameters.get().toString()).get("tags"));
            if (tags.isPresent()) {
                Iterator<JsonNode> nodeIterator = objectMapper.readTree(tags.get().toString()).elements();
                while (nodeIterator.hasNext()) {
                    Optional<String> asgName = extractResourceIdFromTags(nodeIterator.next());
                    if (asgName.isPresent()) {
                        return asgName.get();
                    }
                }
            }
        }
        throw new UnsupportedMessageFormReceivedException(
                "Received notification contains no AutoScaling Group name");
    }

    private Optional<String> extractResourceIdFromTags(JsonNode node) {
        try {
            Optional<JsonNode> resourceId =
                    Optional.ofNullable(objectMapper.readTree(node.toString()).get("resourceId"));
            if (resourceId.isPresent()) {
                return Optional.of(resourceId.get().toString());
            }
        } catch (IOException e) {
            log.error("Tag does not contain field: resourceId", e);
        }
        return Optional.empty();
    }
}
