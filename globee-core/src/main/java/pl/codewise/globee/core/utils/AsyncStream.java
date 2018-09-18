package pl.codewise.globee.core.utils;

import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
public class AsyncStream {

    private AsyncStream() {
    }

    public static <T> void consume(Stream<T> stream, Consumer<T> consumer, ExecutorService executorService) {
        CompletableFuture[] futures = stream
                .map(t -> CompletableFuture.runAsync(() -> consumer.accept(t), executorService))
                .toArray(CompletableFuture[]::new);
        try {
            CompletableFuture.allOf(futures).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage(), e);
            throw new UncheckedExecutionException(e);
        }
    }
}
