package com.philips.store.product.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import com.philips.store.category.CategoryEntity;
import com.philips.store.category.CategoryRepository;
import com.philips.store.product.ProductEntity;
import com.philips.store.product.ProductRepository;
import com.philips.store.product.ProductRequest;
import com.philips.store.product.ProductResponse;
import com.philips.store.product.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@InjectMocks
	private ProductService productService;

	private CategoryEntity category;
	private ProductRequest request;

	@BeforeEach
	void setUp() {

		category = new CategoryEntity();
		category.setId(1L);
		category.setName("LED Bulbs");
		category.setActive(true);

		request = new ProductRequest();
		request.setName("Philips LED Bulb 9W");
		request.setSku("PH-LED-TEST-001");
		request.setDescription("Test LED bulb");
		request.setPrice(new BigDecimal("149.00"));
		request.setWattage(9);
		request.setStock(100);
		request.setCategoryId(1L);
	}

	@Test
	void createProductSuccessfully() {

		when(productRepository.existsBySkuIgnoreCase("PH-LED-TEST-001")).thenReturn(false);

		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

		when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
			ProductEntity product = invocation.getArgument(0);
			product.setId(10L);
			return product;
		});

		ProductResponse response = productService.createProduct(request);

		assertEquals(10L, response.getId());
		assertEquals("Philips LED Bulb 9W", response.getName());
		assertEquals("PH-LED-TEST-001", response.getSku());
		assertEquals(new BigDecimal("149.00"), response.getPrice());
		assertEquals("LED Bulbs", response.getCategoryName());
		assertTrue(response.isActive());

		verify(productRepository).save(any(ProductEntity.class));
	}

	@Test
	void createProductWithDuplicateSkuThrowsConflict() {

		when(productRepository.existsBySkuIgnoreCase("PH-LED-TEST-001")).thenReturn(true);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> productService.createProduct(request));

		assertEquals(409, exception.getStatusCode().value());

		verify(productRepository, never()).save(any(ProductEntity.class));

		verify(categoryRepository, never()).findById(1L);
	}

	@Test
	void createProductWithMissingCategoryThrowsNotFound() {

		when(productRepository.existsBySkuIgnoreCase("PH-LED-TEST-001")).thenReturn(false);

		when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> productService.createProduct(request));

		assertEquals(404, exception.getStatusCode().value());

		verify(productRepository, never()).save(any(ProductEntity.class));
	}
}
