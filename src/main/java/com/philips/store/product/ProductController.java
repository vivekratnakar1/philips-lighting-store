package com.philips.store.product;

import java.util.List;


import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService)
	{
		this.productService = productService;
	}

	@PostMapping
	public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request)
	{

		ProductResponse response = productService.createProduct(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<ProductResponse>> getAllProducts() 
	{

		return ResponseEntity.ok(productService.getAllProducts());
	}
	

	@GetMapping("/search")
	public ResponseEntity<Page<ProductResponse>> searchProducts(@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long categoryId, @RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String direction) 
	{

		if (page < 0 || size < 1 || size > 100) 
		{
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Page must be non-negative and size must be between 1 and 100");
		}

		Set<String> allowedSortFields = Set.of("id", "name", "price", "wattage", "stock");

		if (!allowedSortFields.contains(sortBy))
		{
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sortBy field");
		}

		Sort.Direction sortDirection;

		try {
			sortDirection = Sort.Direction.fromString(direction);
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Direction must be asc or desc");
		}

		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

		return ResponseEntity.ok(productService.searchProducts(keyword, categoryId, minPrice, maxPrice, pageable));
	}
	
	
	
	

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id)
	{

		return ResponseEntity.ok(productService.getProductById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
			@Valid @RequestBody ProductRequest request) 
	{

		return ResponseEntity.ok(productService.updateProduct(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) 
	{

		productService.deactivateProduct(id);

		return ResponseEntity.noContent().build();
	}


}
