package com.example.demo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CardRepository extends ReactiveMongoRepository<Card, String> {
    Mono<Card> findByName(String name);
}
