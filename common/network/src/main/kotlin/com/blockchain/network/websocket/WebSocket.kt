package com.blockchain.network.websocket

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

sealed class ConnectionEvent {

    object Connected : ConnectionEvent()

    object Authenticated : ConnectionEvent()

    object ClientDisconnect : ConnectionEvent()

    class Failure(val throwable: Throwable) : ConnectionEvent()
}

interface WebSocketConnection {
    fun open()
    fun close()
    val connectionEvents: Observable<ConnectionEvent>
}

fun WebSocketConnection.openAsDisposable(): Disposable {

    open()

    return object : Disposable {

        private var isDisposed = false

        override fun isDisposed() = isDisposed

        override fun dispose() {
            if (isDisposed) return
            isDisposed = true
            close()
        }
    }
}

interface WebSocketSend<in OUTGOING> {
    fun send(message: OUTGOING)
}

interface WebSocketSendReceive<in OUTGOING, INCOMING> : WebSocketSend<OUTGOING> {
    val responses: Observable<INCOMING>
}

interface WebSocket<in OUTGOING, INCOMING> : WebSocketConnection, WebSocketSendReceive<OUTGOING, INCOMING> {
    interface Listener<in INCOMING> {
        fun onOpen()
        fun onMessage(message: INCOMING)
        fun onClose()
    }

    companion object {
        val NullListener = object : Listener<Any> {
            override fun onOpen() {}

            override fun onMessage(message: Any) {}

            override fun onClose() {}
        }
    }
}

/**
 * Combine a [WebSocketSendReceive] implementation with [WebSocketConnection] implementation
 */
operator fun <OUTGOING, INCOMING> WebSocketSendReceive<OUTGOING, INCOMING>.plus(
    connection: WebSocketConnection
): WebSocket<OUTGOING, INCOMING> {
    return SeparateSendReceiveAndConnection(this, connection)
}

private class SeparateSendReceiveAndConnection<OUTGOING, INCOMING>(
    sendReceive: WebSocketSendReceive<OUTGOING, INCOMING>,
    connection: WebSocketConnection
) : WebSocket<OUTGOING, INCOMING>,
    WebSocketSendReceive<OUTGOING, INCOMING> by sendReceive,
    WebSocketConnection by connection