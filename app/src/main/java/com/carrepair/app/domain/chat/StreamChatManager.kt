package com.carrepair.app.domain.chat


import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.User
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.result.Result

object StreamChatManager {

    private var isInitialized = false

    fun init(context: Context, apiKey: String) {
        if (isInitialized) return

        val offlinePluginFactory = StreamOfflinePluginFactory(context)

        ChatClient.Builder(apiKey, context)
            .logLevel(ChatLogLevel.ALL)
            .withPlugins(offlinePluginFactory)
            .build()

        isInitialized = true
    }

    suspend fun connectUser(
        userId: String,
        userName: String,
        userToken: String
    ): Result<ConnectionData> {
        val user = User(
            id = userId,
            name = userName
        )
        return ChatClient.instance().connectUser(user, userToken).await()
    }

    fun disconnectUser() {
        ChatClient.instance().disconnect(flushPersistence = false).enqueue()
    }

    fun getChannelClient(channelId: String): ChannelClient {
        return ChatClient.instance().channel("messaging", channelId)
    }
}