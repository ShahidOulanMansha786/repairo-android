package com.carrepair.app.stomp


import android.util.Log
import com.carrepair.app.data.dto.quote.QuoteResponseDto
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

object StompClientManager {

    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()

    fun connect(token: String) {
        val url = "ws://10.0.2.2:8080/ws/websocket"

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)

        val headers = listOf(
            StompHeader("Authorization", "Bearer $token")
        )

        stompClient!!.connect(headers)

        compositeDisposable.add(
            stompClient!!.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { event ->
                    when (event.type) {
                        LifecycleEvent.Type.OPENED ->
                            Log.d("STOMP", "Connected")
                        LifecycleEvent.Type.ERROR ->
                            Log.e("STOMP", "Error", event.exception)
                        LifecycleEvent.Type.CLOSED ->
                            Log.d("STOMP", "Disconnected")
                        else -> {}
                    }
                }
        )
    }

    fun subscribeToQuotes(
        leadId: Long,
        onQuoteReceived: (QuoteResponseDto) -> Unit
    ): Disposable {
        val gson = Gson()

        return stompClient!!
            .topic("/topic/leads/$leadId/quotes")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ message ->
                val quote = gson.fromJson(
                    message.payload,
                    QuoteResponseDto::class.java
                )
                onQuoteReceived(quote)
            }, { error ->
                Log.e("STOMP", "Subscribe error", error)
            })
    }

    fun disconnect() {
        compositeDisposable.clear()
        stompClient?.disconnect()
    }
}