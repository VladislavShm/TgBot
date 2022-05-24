package com.giraffes.tgbot.service;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.Call;
import com.pcloud.sdk.Callback;
import com.pcloud.sdk.RemoteFile;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PCloudProvider {
    private final static Integer LATCH_AWAIT_SECONDS = 60;
    private final ApiClient apiClient;

    @SneakyThrows
    public List<ImageData> imageDataByIndexes(Collection<Integer> indexes) {
        List<ImageData> result = new ArrayList<>(indexes.size());
        CountDownLatch latch = new CountDownLatch(indexes.size());
        for (Integer index : indexes) {
            String filename = String.format("%s.png", index);
            String pathToImage = String.format("/nft/images/%s", filename);
            apiClient.loadFile(pathToImage).enqueue(new Callback<>() {
                @Override
                @SneakyThrows
                public void onResponse(Call<RemoteFile> call, RemoteFile response) {
                    InputStream inputStream = response.byteStream();
                    result.add(new ImageData(inputStream, filename));
                    latch.countDown();
                }

                @Override
                public void onFailure(Call<RemoteFile> call, Throwable t) {
                    log.error("Error while calling cloud. ", t);
                    latch.countDown();
                }
            });
        }

        if (!latch.await(LATCH_AWAIT_SECONDS, TimeUnit.SECONDS)) {
            throw new RuntimeException("Not all NFT have been gotten after awaiting for " + LATCH_AWAIT_SECONDS + " seconds");
        }

        if (result.size() != indexes.size()) {
            throw new RuntimeException("Expected to retrieve " + indexes.size() + " images, but got " + result.size());
        }

        return result;
    }

    @Data
    @RequiredArgsConstructor
    public static class ImageData {
        private final InputStream inputStream;
        private final String filename;
    }
}
