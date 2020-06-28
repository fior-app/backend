package app.fior.backend.dto

data class PrivateChatRoomRequest(
        val allieId: String
)

enum class ChatRoomState {
    REQUEST,
    CONFIRM,
    OK,
    REQUEST_DECLINED,
    CONFIRM_DECLINED,
}