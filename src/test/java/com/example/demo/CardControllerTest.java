package com.example.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = CardController.class)
class CardControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @SpyBean
    private CardService cardService;

    @Captor
    private ArgumentCaptor<Mono<Card>> argumentCaptor;

    @MockBean
    private CardRepository repository;

    @ParameterizedTest
    @CsvSource({"Stewar Marin, 02/26, 4124213,VISA,06,0", "Stewar Marin, 02/26, 4124213,VISA,06,1"})
    void post(String title, String date, String number, String type, String code, Integer times) {

        if (times == 0) {
            when(repository.findByName(number)).thenReturn(Mono.just(new Card()));
        }

        if (times == 1) {
            when(repository.findByName(number)).thenReturn(Mono.empty());
        }

        var request = Mono.just(new Card(title, date, number, type, code));
        webTestClient.post()
                .uri("/card")
                .body(request, Card.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(cardService).insert(argumentCaptor.capture());
        verify(repository, times(times)).save(any());

        var card = argumentCaptor.getValue().block();

        Assertions.assertEquals(number, card.getNumber());
        Assertions.assertEquals(title, card.getTitle());
        Assertions.assertEquals(date, card.getDate());
        Assertions.assertEquals(type, card.getType());
        Assertions.assertEquals(code, card.getCode());
    }

    @Test
    void get() {
        webTestClient.get()
                .uri("/card/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Card.class)
                .consumeWith(cardEntityExchangeResult -> {
                    var card = cardEntityExchangeResult.getResponseBody();
                    assert card != null;
                });
    }

    @Test
    void update() {
        var request = Mono.just(new Card());
        webTestClient.put()
                .uri("/card")
                .body(request, Card.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    void delete() {
        webTestClient.delete()
                .uri("/card/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    void list() {
        var list = Flux.just(
                new Card("Stewar Marin", "02/26", "4124213", "VISA", "06"),
                new Card("Raul Alzate", "01/29", "51234123", "PRIME", "12")
        );
        when(repository.findAll()).thenReturn(list);

        webTestClient.get()
                .uri("/card")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("Stewar Marin")
                .jsonPath("$[1].title").isEqualTo("Raul Alzate");

        verify(cardService).listAll();
        verify(repository).findAll();
    }

    @Test
    void filterType() {
        var prueba = Flux.just(new Card("Stewar Marin", "02/26", "4124213", "VISA", "06"),
                new Card("Raul Alzate", "01/29", "51234123", "PRIME", "12")
        );

        prueba = prueba.filter(el -> el.getType().equals("VISA"));

        Assertions.assertEquals(1L,prueba.count().block());

    }

    @Test
    void filterTypeController() {
        var list = Flux.just(
                new Card("alejo", "25/05/2021", "154641651", "VISA", "06"),
                new Card("juan", "31/05/2021", "156513", "MASTERCARD", "03")
        );
        when(repository.findAll()).thenReturn(list);

        webTestClient.get()
                .uri("/card/type/VISA")
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }

}