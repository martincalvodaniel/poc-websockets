package com.dmartinc

import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit
import javax.websocket.ClientEndpoint
import javax.websocket.ContainerProvider
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session

@QuarkusTest
class WebSocketTest {

    @TestHTTPResource("/")
    var uri: URI? = null

    @Test
    fun testHelloEndpoint() {
        ContainerProvider.getWebSocketContainer().connectToServer(Client::class.java, uri)
    }

    @ClientEndpoint
    class Client {
        val messages = LinkedBlockingDeque<String>()

        @OnOpen
        fun open(session: Session) {
            messages.add("CONNECT")
            session.asyncRemote.sendText("_ready_").get()
            assertThat(messages.poll(10, TimeUnit.SECONDS)).isEqualTo("CONNECT")
        }

        @OnMessage
        fun message(msg: String?) {
            println(msg)
            msg?.let { messages.add(it) }
        }
    }
}
