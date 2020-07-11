package app.fior.backend.dto

/**
 * DTO for file upload requests
 *
 * @property ext - Extension of the file
 * @property mime - MIME type of the file
 * @property mime - BASE64 encoded data of the file
 * */
data class FileUploadDto(
        val ext: String,
        val mime: String,
        val data: String
)