package com.tgbot.service;

import com.tgbot.model.NftImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class NftImageAsyncSenderService {
    private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final PCloudProvider pCloudProvider;

    public void sendAsync(Collection<Integer> indexes, Consumer<NftImage> sender) {
        for (Integer index : indexes) {
            EXECUTOR.execute(() -> {
                sender.accept(pCloudProvider.imageDataByIndex(index));
            });
        }
    }

    @PreDestroy
    public void destroy() {
        EXECUTOR.shutdown();
    }
}
