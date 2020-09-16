package com.greglturnquist.hackingspringbootch6reactive;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ApiItemController {
	
	private final ItemRepository repository;
	
	public ApiItemController(ItemRepository repository) {
		this.repository = repository;
	}

	@GetMapping("/api/items")
	Flux<Item> findAll() {
		return this.repository.findAll();
	}
	
	@GetMapping("/api/items/{id}")
	Mono<Item> findOne(@PathVariable String id) {
		return this.repository.findById(id);
	}

	
	@PostMapping("/api/items")
	Mono<ResponseEntity<?>> addNewItem(@RequestBody Mono<Item> item) {
		return item.flatMap(s -> this.repository.save(s))
			.map(savedItem -> ResponseEntity.created(URI.create("/api/items/" + savedItem.getId())).body(savedItem));
	}

	@PutMapping("/api/items/{id}")
	public Mono<ResponseEntity<?>> updateItem(@RequestBody Mono<Item> item, @PathVariable String id) {
		return item.map(content -> new Item(id, content.getName(), content.getDescription(), content.getPrice()))
			.flatMap(this.repository::save)
			.thenReturn(ResponseEntity.created(URI.create("/api/items/" + id)).build());
	}
}
