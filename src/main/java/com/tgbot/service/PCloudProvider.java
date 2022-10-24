package com.tgbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbot.model.NftImage;
import com.tgbot.model.NftMetadata;
import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.Call;
import com.pcloud.sdk.Callback;
import com.pcloud.sdk.RemoteFile;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PCloudProvider {
    private final static Integer CLOUD_LATCH_AWAIT_SECONDS = 60;
    private final ApiClient apiClient;

    @SneakyThrows
    public NftImage imageDataByIndex(Integer index) {
        Map<Integer, NftImage> result = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(1);

        String filename = String.format("%s.png", index);
        String pathToImage = String.format("/nft/images/%s", filename);
        apiClient.loadFile(pathToImage).enqueue(new Callback<>() {
            @Override
            @SneakyThrows
            public void onResponse(Call<RemoteFile> call, RemoteFile response) {
                result.put(index, new NftImage(index, IOUtils.toByteArray(response.byteStream()), filename));
                latch.countDown();
            }

            @Override
            public void onFailure(Call<RemoteFile> call, Throwable t) {
                log.error("Error while calling cloud for getting image data. ", t);
                latch.countDown();
            }
        });

        if (!latch.await(CLOUD_LATCH_AWAIT_SECONDS, TimeUnit.SECONDS)) {
            throw new RuntimeException("NFT image has not been gotten after awaiting for " + CLOUD_LATCH_AWAIT_SECONDS + " seconds");
        }

        return result.get(index);
    }

    @SneakyThrows
    public Map<Integer, NftMetadata> metadataByIndexes(Collection<Integer> indexes) {
        Map<Integer, NftMetadata> result = new ConcurrentHashMap<>(indexes.size());
        CountDownLatch latch = new CountDownLatch(indexes.size());
        for (Integer index : indexes) {
            String pathToMetadata = String.format("/nft/json/%s.json", index);
            apiClient.loadFile(pathToMetadata).enqueue(new Callback<>() {
                @Override
                @SneakyThrows
                public void onResponse(Call<RemoteFile> call, RemoteFile response) {
                    InputStream inputStream = response.byteStream();
                    ObjectMapper objectMapper = new ObjectMapper();
                    NftMetadata nftMetadata = objectMapper.readValue(inputStream, NftMetadata.class);

                    result.put(index, nftMetadata);
                    latch.countDown();
                }

                @Override
                public void onFailure(Call<RemoteFile> call, Throwable t) {
                    log.error("Error while calling cloud for getting metadata. ", t);
                    latch.countDown();
                }
            });
        }

        if (!latch.await(CLOUD_LATCH_AWAIT_SECONDS, TimeUnit.SECONDS)) {
            throw new RuntimeException("Not all NFT matadatas have been gotten after awaiting for " + CLOUD_LATCH_AWAIT_SECONDS + " seconds");
        }

        if (result.size() != indexes.size()) {
            throw new RuntimeException("Expected to retrieve " + indexes.size() + " metadatas, but got " + result.size());
        }

        return result;
    }

}
