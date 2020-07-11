package app.fior.backend.config

import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StorageConfiguration(val properties: FiorConfiguration) {
    private val endpoint: String
        get() {
            if (properties.storage.useEmulatorval) {
                LOGGER.debug("Using emulator address instead..")
                return java.lang.String.format("%s/%s", properties.storage.emulatorBlobHost, properties.storage.accountName)
            }
            return if (properties.storage.enableHttps) {
                java.lang.String.format(BLOB_HTTPS_URL, properties.storage.accountName)
            } else java.lang.String.format(BLOB_URL, properties.storage.accountName)
        }

    @Bean
    fun blobContainerAsyncClient(): BlobContainerAsyncClient {
        val blobServiceAsyncClient = BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(StorageSharedKeyCredential(properties.storage.accountName, properties.storage.accountKey))
                .buildAsyncClient()
        return blobServiceAsyncClient.getBlobContainerAsyncClient(properties.storage.containerName)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(StorageConfiguration::class.java)
        private const val BLOB_URL = "http://%s.blob.core.windows.net"
        private const val BLOB_HTTPS_URL = "https://%s.blob.core.windows.net"
    }
}