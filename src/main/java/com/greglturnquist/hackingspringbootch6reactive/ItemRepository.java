package com.greglturnquist.hackingspringbootch6reactive;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;

public interface ItemRepository extends ReactiveCrudRepository<Item, String>, ReactiveQueryByExampleExecutor<Item> {

	Flux<Item> findByNameContaining(String partialName);

	@Query("{ 'name' : ?0, 'price' : ?1 }")
	Flux<Item> findItemsForCustomerMonthlyReport(String name, double price);
	
	@Query("{ 'name' : ?0 }")
	Flux<Item> findSortedStuffForWeeklyReport(String name, Sort sort);
	
	Flux<Item> findByNameContainingIgnoreCase(String partialName);
	
	Flux<Item> findByDescriptionContainingIgnoreCase(String partialName);
	
	Flux<Item> findByNameContainingAndDescriptionContainingAllIgnoreCase(String partialName, String partialDesc);
	
	Flux<Item> findByNameContainingOrDescriptionContainingAllIgnoreCase(String partialName, String partialDesc);
	
}
