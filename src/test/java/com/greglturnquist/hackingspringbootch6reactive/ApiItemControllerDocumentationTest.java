package com.greglturnquist.hackingspringbootch6reactive;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = ApiItemController.class)
@AutoConfigureRestDocs
public class ApiItemControllerDocumentationTest {

	@Autowired
	private WebTestClient webTestClient;
	
	@MockBean
	InventoryService service;
	
	@MockBean
	ItemRepository repository;
	
	@Test
	void findingAllItems() {
		when(repository.findAll()).thenReturn(
			Flux.just(new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));
		
		this.webTestClient.get().uri("/api/items")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.consumeWith(document("findAll", preprocessResponse(prettyPrint())));
	}
	
	@Test
	void postNewItem() {
		when(repository.save(any())).thenReturn(
			Mono.just(new Item("item-1", "Alf alarm clock", "nothing important", 19.99))
		);
		
		this.webTestClient.post().uri("/api/items")
			.bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.consumeWith(document("post-new-item", preprocessResponse(prettyPrint())));
	}
}
