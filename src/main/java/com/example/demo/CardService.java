package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@Service
public class CardService {

    private final BiFunction<CardRepository, Card, Mono<Card>> validateBeforeInsert
            = (repo, person) -> repo.findByName(person.getNumber());

    @Autowired
    private CardRepository repository;

    public Flux<Card> listAll() {
        return repository.findAll();
    }

    public Mono<Void> insert(Mono<Card> personMono) {
        return personMono
                .flatMap(person -> validateBeforeInsert.apply(repository, person))
                .switchIfEmpty(Mono.defer(() -> personMono.doOnNext(repository::save)))
                .then();
    }
}
