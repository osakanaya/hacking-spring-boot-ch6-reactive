package com.greglturnquist.hackingspringbootch6reactive;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
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

@WebFluxTest(controllers = HypermediaItemController.class)
@AutoConfigureRestDocs
public class HypermediaItemControllerDocumentationTest {

	@Autowired
	private WebTestClient webtestClient;
	
	@MockBean
	InventoryService service;
	
	@MockBean
	ItemRepository repository;
	
	@Test
	void findOneTest() {
		when(repository.findById("item-1")).thenReturn(
				Mono.just(new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));

		this.webtestClient.get().uri("/hypermedia/item/item-1")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.consumeWith(document("findOne-hypermedia", preprocessResponse(prettyPrint()),
				links(
					linkWithRel("self").description("Canonical link to this `Item`"),
					linkWithRel("item").description("Link back to the aggregate root"))));
	}
	
	@Test
	void findAllTest() {
		when(repository.findAll()).thenReturn(
				Flux.just(new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));
		
		when(repository.findById(anyString())).thenReturn(
				Mono.just(new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));
		
		this.webtestClient.get().uri("/hypermedia/items")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.consumeWith(document("findAll-hypermedia", preprocessResponse(prettyPrint())));
	}
	
	@Test
	void postNewItem() {
		when(repository.save(any())).thenReturn(
				Mono.just(new Item("item-1", "Alf alarm clock", "nothing important", 19.99))
		);

		when(repository.findById("item-1")).thenReturn(
				Mono.just(new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));

		this.webtestClient.post().uri("/hypermedia/items")
			.body(Mono.just(new Item("Alf alarm clock", "nothing I really need", 19.99)), Item.class)
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.consumeWith(document("post-new-item-hypermedia", preprocessResponse(prettyPrint())));
	}

	@Test
	void updateItem() {
		when(repository.save(any())).thenReturn(
				Mono.just(new Item("item-1", "Alf alarm clock", "nothing important", 19.99))
		);

		when(repository.findById("item-1")).thenReturn(
				Mono.just(new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));

		this.webtestClient.put().uri("/hypermedia/items/item-1")
			.body(Mono.just(new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)), Item.class)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody()
			.consumeWith(document("put-item-hypermedia", preprocessResponse(prettyPrint())));
		
	}

	@Test
	void findProfile() {
		this.webtestClient.get().uri("/hypermedia/items/profile")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.consumeWith(document("profile", preprocessResponse(prettyPrint())));
	}
}
