package com.mss.user_service.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class AzureStorageService {

    private final BlobContainerClient containerClient;

    /**
     * Upload file lên Azure Blob Storage.
     * @return URL public của file đã upload
     */
    public String uploadFile(MultipartFile file, String folder, String filename) {
        String blobName = folder + "/" + filename;
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        try (InputStream inputStream = file.getInputStream()) {
            blobClient.upload(inputStream, file.getSize(), true);

            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType());
            blobClient.setHttpHeaders(headers);

            return blobClient.getBlobUrl();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Azure", e);
        }
    }

    /**
     * Xóa file khỏi Azure Blob Storage.
     */
    public void deleteFile(String blobUrl) {
        if (blobUrl == null || blobUrl.isBlank()) return;

        String containerUrl = containerClient.getBlobContainerUrl();
        if (blobUrl.startsWith(containerUrl)) {
            String blobName = blobUrl.substring(containerUrl.length() + 1);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.deleteIfExists();
        }
    }
}
