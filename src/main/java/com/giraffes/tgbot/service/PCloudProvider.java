package com.giraffes.tgbot.service;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.RemoteFile;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class PCloudProvider {
    private final ApiClient apiClient;

    @SneakyThrows
    public ImageData imageDataByIndex(int index) {
        String filename = String.format("%s.png", index);
        String pathToImage = String.format("/nft/images/%s", filename);
        RemoteFile remoteFile = apiClient.loadFile(pathToImage).execute();
        InputStream inputStream = remoteFile.byteStream();
        return new ImageData(inputStream, filename);
    }

    @Data
    @RequiredArgsConstructor
    public static class ImageData {
        private final InputStream inputStream;
        private final String filename;
    }
}
