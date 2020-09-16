package com.greglturnquist.hackingspringbootch6reactive;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AltInventoryService {
	private ItemRepository itemRepository;
	private CartRepository cartRepository;
	
	AltInventoryService(
			ItemRepository itemRepository,
			CartRepository cartRepository) {
		this.itemRepository = itemRepository;
		this.cartRepository = cartRepository;
	}
	
	public Mono<Cart> getCart(String cartId) {
		return this.cartRepository.findById(cartId);
	}
	
	public Flux<Item> getInventory() {
		return this.itemRepository.findAll();
	}
	
	Mono<Item> saveItem(Item newItem) {
		return this.itemRepository.save(newItem);
	}
	
	Mono<Void> deleteItem(String itemId) {
		return this.itemRepository.deleteById(itemId);
	}
	
	Mono<Cart> addToCart(String cartId, String id) {
		Cart myCart = this.cartRepository.findById(cartId)
				.defaultIfEmpty(new Cart(cartId))
				.block();
		
		return myCart.getCartItems().stream()
				.filter(cartItem -> cartItem.getItem().getId().equals(id))
				.findAny()
				.map(cartItem -> {
					cartItem.increment();
					return Mono.just(myCart);
				})
				.orElseGet(() -> 
					this.itemRepository.findById(id)
						.map(item -> new CartItem(item))
						.map(cartItem -> {
							myCart.getCartItems().add(cartItem);
							return myCart;
					})
			)
			.flatMap(cart -> this.cartRepository.save(cart));
	}
	
	Mono<Cart> removeOneFromCart(String cartId, String itemId) {
		return this.cartRepository.findById(cartId)
			.defaultIfEmpty(new Cart(cartId))
			.flatMap(cart -> cart.getCartItems().stream()
				.filter(cartItem -> cartItem.getItem().getId().equals(itemId))
				.findAny()
				.map(cartItem -> {
					cartItem.decrement();
					return Mono.just(cart);
				})
				.orElse(Mono.empty()))
			.map(cart -> new Cart(cart.getId(), cart.getCartItems().stream()
					.filter(cartItem -> cartItem.getQuantity() > 0)
					.collect(Collectors.toList())))
			.flatMap(cart -> this.cartRepository.save(cart));
	}	
}
