package com.carrepair.app.data.repository

import android.content.Context
import android.net.Uri
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.domain.security.TokenManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

data class ChatChannel(
    val channelId: String = "",
    val leadId: String = "",
    val carOwnerId: String = "",
    val carOwnerName: String = "",
    val repairShopId: String = "",
    val repairShopName: String = "",
    val leadTitle: String = "",
    val lastMessage: String? = null,
    val lastMessageAt: Date? = null,
    val lastMessageSenderId: String? = null
)

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String? = null,
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val createdAt: Date? = null
)

class FirestoreChatRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    private val db = Firebase.firestore("defaultcarrepair")
    private val storage = Firebase.storage

    fun getMessagesFlow(channelId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db
            .collection("chats")
            .document(channelId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)
                            ?.copy(messageId = doc.id)
                    } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendTextMessage(
        channelId: String,
        senderId: String,
        senderName: String,
        text: String
    ) {
        val messageData = hashMapOf(
            "senderId" to senderId,
            "senderName" to senderName,
            "text" to text,
            "imageUrl" to null,
            "isRead" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("chats")
            .document(channelId)
            .collection("messages")
            .add(messageData)
            .await()
        updateLastMessage(channelId, text, senderId)
        notifyOtherParty(channelId, senderName, text)
    }

    suspend fun sendImageMessage(
        channelId: String,
        senderId: String,
        senderName: String,
        imageUri: Uri,
        context: Context
    ) {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference
            .child("chat_images/$channelId/$fileName")
        ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()

        val messageData = hashMapOf(
            "senderId" to senderId,
            "senderName" to senderName,
            "text" to null,
            "imageUrl" to downloadUrl,
            "isRead" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("chats")
            .document(channelId)
            .collection("messages")
            .add(messageData)
            .await()
        updateLastMessage(channelId, "📷 Photo", senderId)
        notifyOtherParty(channelId, senderName, "📷 Photo")
    }

    private suspend fun updateLastMessage(
        channelId: String,
        lastMessage: String,
        senderId: String
    ) {
        db.collection("chats")
            .document(channelId)
            .update(
                "lastMessage", lastMessage,
                "lastMessageAt", FieldValue.serverTimestamp(),
                "lastMessageSenderId", senderId
            ).await()
    }

    suspend fun markMessagesAsRead(
        channelId: String,
        currentUserId: String
    ) {
        val unreadMessages = db
            .collection("chats")
            .document(channelId)
            .collection("messages")
            .whereEqualTo("isRead", false)
            .whereNotEqualTo("senderId", currentUserId)
            .get()
            .await()

        val batch = db.batch()
        unreadMessages.documents.forEach { doc ->
            batch.update(doc.reference, "isRead", true)
        }
        batch.commit().await()
    }

    fun getUserChannelsFlow(userId: String): Flow<List<ChatChannel>> = callbackFlow {
        val mergedMap = mutableMapOf<String, ChatChannel>()

        val ownerListener = db
            .collection("chats")
            .whereEqualTo("carOwnerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatChannel::class.java)
                }?.forEach { channel ->
                    mergedMap[channel.channelId] = channel
                }
                trySend(mergedMap.values.toList())
            }

        val shopListener = db
            .collection("chats")
            .whereEqualTo("repairShopId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatChannel::class.java)
                }?.forEach { channel ->
                    mergedMap[channel.channelId] = channel
                }
                trySend(mergedMap.values.toList())
            }

        awaitClose {
            ownerListener.remove()
            shopListener.remove()
        }
    }

    private suspend fun notifyOtherParty(
        channelId: String,
        senderName: String,
        messagePreview: String
    ) {
        try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            authApi.sendChatNotification(
                token = token,
                body = AuthApi.ChatNotifyRequestDto(
                    channelId = channelId,
                    senderName = senderName,
                    messagePreview = messagePreview
                )
            )
        } catch (e: Exception) {
            // silently ignore — message already saved to Firestore
        }
    }
}