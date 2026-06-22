package com.intersec.shared.models.common

data class UiMessage(val text: String, val type: MessageType) { enum class MessageType { INFO, WARNING, ERROR } }