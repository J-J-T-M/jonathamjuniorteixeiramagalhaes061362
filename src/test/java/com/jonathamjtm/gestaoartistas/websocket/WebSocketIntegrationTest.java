package com.jonathamjtm.gestaoartistas.websocket;

import com.jonathamjtm.gestaoartistas.dto.request.AlbumRequest;
import com.jonathamjtm.gestaoartistas.dto.response.AlbumResponse;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.entity.ArtistType;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import com.jonathamjtm.gestaoartistas.service.AlbumService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebSocketIntegrationTest {

    @LocalServerPort
    private Integer port;

    private WebSocketStompClient stompClient;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private ArtistRepository artistRepository;

    @BeforeEach
    void setup() {
        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        this.stompClient = new WebSocketStompClient(new SockJsClient(transports));
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @DisplayName("Tópico 1 e 4: Deve conectar com Origin válido (CORS) e receber notificação ao criar Álbum")
    void shouldReceiveAlbumNotification() throws Exception {
        Artist artist = artistRepository.save(Artist.builder().name("Test Artist").type(ArtistType.SINGER).build());

        String wsUrl = "ws://localhost:" + port + "/ws-gestao-artistas";

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add("Origin", "http://localhost:3000");

        BlockingQueue<AlbumResponse> blockingQueue = new LinkedBlockingDeque<>();

        StompSession session = stompClient.connectAsync(wsUrl, handshakeHeaders, new StompSessionHandlerAdapter() {})
                .get(2, TimeUnit.SECONDS);

        assertThat(session.isConnected()).isTrue();

        session.subscribe("/topic/albums", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return AlbumResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((AlbumResponse) payload);
            }
        });

        AlbumRequest request = new AlbumRequest();
        request.setTitle("Álbum Teste WebSocket");
        request.setReleaseYear(2026);
        request.setArtistIds(List.of(artist.getId()));

        albumService.createAlbum(request);

        AlbumResponse receivedMessage = blockingQueue.poll(3, TimeUnit.SECONDS);

        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getTitle()).isEqualTo("Álbum Teste WebSocket");
    }
}