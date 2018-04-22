package com.baeldung.reactive.repository;

import com.baeldung.reactive.Spring5ReactiveApplication;
import com.baeldung.reactive.model.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.startsWith;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Spring5ReactiveApplication.class)
public class AccountMongoRepositoryIntegrationTest {

    @Autowired
    AccountMongoRepository repository;

    @BeforeEach
    public void setUp() {
        repository.save(new Account(null, "bruno", 12.3)).block();
        repository.save(new Account(null, "bruno torrao", 12.3)).block();
    }

    @AfterEach
    public void cleanup() {
        repository.deleteAll().block();
    }

    @Test
    public void shouldFindAll_GivenExample() {
        ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("owner", startsWith());
        Example<Account> example = Example.of(new Account(null, "bru", null), matcher);
        Flux<Account> accountFlux = repository.findAll(example);
        List<Account> accounts = accountFlux.collectList().block();
        assert accounts.size() == 2;
        accounts.forEach(account -> {
            assert account.getOwner().equals("bruno") || account.getOwner().equals("bruno torrao");
        });
    }

    @Test
    public void shouldSave_GivenAnAccount() {
        Mono<Account> accountMono = repository.save(new Account(null, "bruno", 12.3));
        assertNotNull(accountMono.block().getId());
    }

    @Test
    public void shouldGetAccount_GivenAnId() {
        Mono<Account> inserted = repository.save(new Account(null, "bruno", 12.3));
        Mono<Account> accountMono = repository.findById(inserted.block().getId());
        assert accountMono.block().getOwner().equals("bruno");
        assert accountMono.block().getValue().equals(12.3);
        assertNotNull(accountMono.block().getId());
    }
}