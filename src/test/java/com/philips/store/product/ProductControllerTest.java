package com.philips.store.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

	@Mock
	private ProductService productService;

	@InjectMocks
	private ProductController productController;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
	}


private ProductResponse createResponse() {
    return new ProductResponse(
        10L,
        "Philips LED Bulb 9W",
        "PH-LED-TEST-001",
        "Test LED bulb",
        new BigDecimal("149.00"),
        9,
        100,
        true,
        1L,
        "LED Bulbs"
    );
}



	@Test
	void createProductReturns201() throws Exception {

		ProductResponse response = createResponse();

		when(productService.createProduct(any(ProductRequest.class))).thenReturn(response);

		String requestJson = """
				{
				    "name": "Philips LED Bulb 9W",
				    "sku": "PH-LED-TEST-001",
				    "description": "Test LED bulb",
				    "price": 149.00,
				    "wattage": 9,
				    "stock": 100,
				    "categoryId": 1
				}
				""";

		mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.name").value("Philips LED Bulb 9W"))
				.andExpect(jsonPath("$.sku").value("PH-LED-TEST-001"));

		verify(productService).createProduct(any(ProductRequest.class));
	}

	@Test
	void getAllProductsReturns200() throws Exception {

		when(productService.getAllProducts()).thenReturn(List.of(createResponse()));

		mockMvc.perform(get("/api/v1/products")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(10))
				.andExpect(jsonPath("$[0].name").value("Philips LED Bulb 9W"));

		verify(productService).getAllProducts();
	}

	@Test
	void getProductByIdReturns200() throws Exception {

		when(productService.getProductById(10L)).thenReturn(createResponse());

		mockMvc.perform(get("/api/v1/products/10")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.sku").value("PH-LED-TEST-001"));

		verify(productService).getProductById(10L);
	}

	@Test
	void updateProductReturns200() throws Exception {

		when(productService.updateProduct(org.mockito.ArgumentMatchers.eq(10L), any(ProductRequest.class)))
				.thenReturn(createResponse());

		String requestJson = """
				{
				    "name": "Philips LED Bulb 9W",
				    "sku": "PH-LED-TEST-001",
				    "description": "Updated LED bulb",
				    "price": 149.00,
				    "wattage": 9,
				    "stock": 100,
				    "categoryId": 1
				}
				""";

		mockMvc.perform(put("/api/v1/products/10").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10));

		verify(productService).updateProduct(org.mockito.ArgumentMatchers.eq(10L), any(ProductRequest.class));
	}

	@Test
	void deactivateProductReturns204() throws Exception {

		mockMvc.perform(delete("/api/v1/products/10")).andExpect(status().isNoContent())
				.andExpect(content().string(""));

		verify(productService).deactivateProduct(10L);
	}

	@Test
	void searchProductsReturns200() throws Exception {

		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

		Page<ProductResponse> page = new PageImpl<>(List.of(createResponse()), pageable, 1);

		when(productService.searchProducts(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
				.thenReturn(page);

		mockMvc.perform(get("/api/v1/products/search")).andExpect(status().isOk());

		verify(productService).searchProducts(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
	}

	@Test
	void invalidSortByReturns400() throws Exception {

		mockMvc.perform(get("/api/v1/products/search").param("sortBy", "invalidField"))
				.andExpect(status().isBadRequest());

		verify(productService, never()).searchProducts(any(), any(), any(), any(), any(Pageable.class));
	}

	@Test
	void invalidPageReturns400() throws Exception {

		mockMvc.perform(get("/api/v1/products/search").param("page", "-1")).andExpect(status().isBadRequest());

		verify(productService, never()).searchProducts(any(), any(), any(), any(), any(Pageable.class));
	}
}
