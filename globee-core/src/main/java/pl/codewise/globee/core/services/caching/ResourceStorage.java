package pl.codewise.globee.core.services.caching;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.codewise.commons.aws.cqrs.model.AwsResource;
import pl.codewise.globee.core.utils.AsyncStream;
import pl.codewise.globee.core.utils.GlobeeStringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
abstract class ResourceStorage<R extends AwsResourceIdWithRegion, A extends AwsResource> {

    private final ExecutorService executorService;
    private final ReentrantReadWriteLock readWriteLock;
    final Lock readLock;
    private final Lock writeLock;

    @Setter
    @VisibleForTesting
    protected Map<String, A> resources = Maps.newConcurrentMap();

    ResourceStorage(ExecutorService executorService) {
        this.executorService = executorService;
        this.readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
    }

    void visit(R resource) {
        writeLock.lock();
        try {
            if (resource.isToBeDeleted() && resources.containsKey(resource.getId())) {
                resources.remove(resource.getId());
                log.info("Removing data about {}: {}",
                        GlobeeStringUtils.extractExactClassName(resource.getClass().getName()),
                        resource.getId());
            } else if (!resource.isToBeDeleted()) {
                resources.put(resource.getId(), fetchSingle(resource));
                log.info("Updating data about {}: {}",
                        GlobeeStringUtils.extractExactClassName(resource.getClass().getName()),
                        resource.getId());
            }
        } finally {
            writeLock.unlock();
        }
    }

    void initiate(List<String> regions) {
        AsyncStream.consume(
                regions.stream(),
                this::initiate,
                executorService
        );
    }

    int size() {
        return resources.size();
    }

    abstract A fetchSingle(R resource);

    abstract void initiate(String region);

    abstract Set<A> getMatchedResources(String searchedPhrase);

    abstract Set<A> getResourcesFilteredForGivenCriteria(String key, String value, Set<A> awsResources);
}
