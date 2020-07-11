package app.fior.backend.services

import app.fior.backend.extensions.toFlux
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.models.ParallelTransferOptions
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.ByteBuffer

@Service
class BlobService(val blobContainerAsyncClient: BlobContainerAsyncClient) {

    fun uploadBlob(blobName: String, blob: ByteArray): Mono<String> {
        val blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName)
        val blobByteBuffer = ByteBuffer.wrap(blob).toFlux()
        return blobAsyncClient.upload(blobByteBuffer, ParallelTransferOptions(), true).map {
            "${blobContainerAsyncClient.blobContainerUrl}/$blobName"
        }
    }

}