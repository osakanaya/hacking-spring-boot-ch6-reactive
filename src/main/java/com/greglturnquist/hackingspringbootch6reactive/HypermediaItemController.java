package com.greglturnquist.hackingspringbootch6reactive;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static org.springframework.hateoas.mediatype.alps.Alps.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.alps.Alps;
import org.springframework.hateoas.mediatype.alps.Type;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class HypermediaItemController {

	private final ItemRepository repository;
	
	public HypermediaItemController(ItemRepository repository) {
		this.repository = repository;
	}
	
	@GetMapping("/hypermedia")
	Mono<RepresentationModel<?>> root() {
		HypermediaItemController controller = methodOn(HypermediaItemController.class);
		
		Mono<Link> selfLink = linkTo(controller.root()).withSelfRel().toMono();
		
		Mono<Link> itemsAggregateLink = 
			linkTo(controller.findAll()).withRel(IanaLinkRelations.ITEM).toMono();
		
		return selfLink.zipWith(itemsAggregateLink)
			.map(links -> Links.of(links.getT1(), links.getT2()))
			.map(links -> new RepresentationModel<>(links.toList()));
		
	}
	
	@GetMapping("/hypermedia/item/{id}")
	Mono<EntityModel<Item>> findOne(@PathVariable String id) {
		
		HypermediaItemController controller = methodOn(HypermediaItemController.class);
		
		Mono<Link> selfLink = linkTo(controller.findOne(id)).withSelfRel().toMono();
		
		Mono<Link> aggregateLink = linkTo(controller.findAll())
			.withRel(IanaLinkRelations.ITEM).toMono();
		
		return Mono.zip(repository.findById(id), selfLink, aggregateLink)
			.map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(), o.getT3())));

	}
	
	@GetMapping("/hypermedia/items")
	Mono<CollectionModel<EntityModel<Item>>> findAll() {
		return this.repository.findAll()
			.flatMap(item -> findOne(item.getId()))
			.collectList()
			.flatMap(entityModels -> linkTo(methodOn(HypermediaItemController.class)
					.findAll()).withSelfRel()
					.toMono()
					.map(selfLink -> CollectionModel.of(entityModels, selfLink)));
	}
	
	@PostMapping("/hypermedia/items")
	Mono<ResponseEntity<?>> addNewItem(@RequestBody Mono<EntityModel<Item>> item) {
		return item
			.map(EntityModel::getContent) // getContet -> Item, map -> Mono<Item>
			.flatMap(this.repository::save) // save -> Mono<Item>
			.map(Item::getId) // getId -> String
			.flatMap(this::findOne) // findOne -> Mono<EntityModel<Item>>
			.map(newModel -> ResponseEntity
				.created(newModel 
					.getRequiredLink(IanaLinkRelations.SELF) 
					.toUri()).build());
	}
	
	@PutMapping("/hypermedia/items/{id}")
	Mono<ResponseEntity<?>> updateItem(
			@RequestBody Mono<EntityModel<Item>> item, @PathVariable String id) {
		return item
			.map(EntityModel::getContent) // getContent -> Item, map -> Mono<Item>
			.map(content -> new Item(id, content.getName(), content.getDescription(), content.getPrice()))
			.flatMap(this.repository::save) // save -> Mono<Item>
			.then(findOne(id)) // then -> Mono<EntityModel<Item>>
			.map(model -> ResponseEntity.noContent()
				.location(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).build());
			// model -> EntityModel<Item>
	}
	
	@GetMapping(value = "/hypermedia/items/profile", produces = MediaTypes.ALPS_JSON_VALUE)
	public Alps profile() {
		return alps().descriptor(Collections.singletonList(descriptor()
				.id(Item.class.getSimpleName() + "-repr")
				.descriptor(Arrays.stream(
					Item.class.getDeclaredFields())
					.map(field -> descriptor()
						.name(field.getName())
						.type(Type.SEMANTIC)
						.build())
					.collect(Collectors.toList()))
				.build()))
			.build();
	}

	@GetMapping("/hypermedia/item/{id}/affordances")
	Mono<EntityModel<Item>> findOneWithAffordances(@PathVariable String id) {
		
		HypermediaItemController controller = methodOn(HypermediaItemController.class);
		
		Mono<Link> selfLink = linkTo(controller.findOne(id))
			.withSelfRel()
			.andAffordance(controller.updateItem(null, id))
			.toMono();
		
		Mono<Link> aggregateLink = linkTo(controller.findAll())
			.withRel(IanaLinkRelations.ITEM).toMono();
		
		return Mono.zip(repository.findById(id), selfLink, aggregateLink)
			.map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(), o.getT3())));

	}
	
	
}
