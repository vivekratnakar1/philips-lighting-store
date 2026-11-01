package com.philips.store.product;

import java.util.List;
import java.util.Locale;
import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.philips.store.category.CategoryEntity;
import com.philips.store.category.CategoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;

	public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {

		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
	}

	@Transactional
	public ProductResponse createProduct(ProductRequest request) {

		String sku = normalizeSku(request.getSku());

		if (productRepository.existsBySkuIgnoreCase(sku)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Product SKU already exists");
		}

		CategoryEntity category = findActiveCategory(request.getCategoryId());

		ProductEntity product = new ProductEntity();

		product.setName(request.getName().trim());
		product.setSku(sku);
		product.setDescription(request.getDescription());
		product.setPrice(request.getPrice());
		product.setWattage(request.getWattage());
		product.setStock(request.getStock());
		product.setCategory(category);

		ProductEntity saved = productRepository.save(product);

		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> getAllProducts() {

		return productRepository.findAll().stream().filter(ProductEntity::isActive).map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public Page<ProductResponse> searchProducts(String keyword, Long categoryId, BigDecimal minPrice,
			BigDecimal maxPrice, Pageable pageable) {

		if (minPrice != null && minPrice.signum() < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum price cannot be negative");
		}

		if (maxPrice != null && maxPrice.signum() < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum price cannot be negative");
		}

		if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum price cannot exceed maximum price");
		}

		String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

		return productRepository.searchActiveProducts(normalizedKeyword, categoryId, minPrice, maxPrice, pageable)
				.map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public ProductResponse getProductById(Long id) 
	{

		return toResponse(findActiveProduct(id));
	}

	@Transactional
	public ProductResponse updateProduct(Long id, ProductRequest request) 
	{

		ProductEntity product = findActiveProduct(id);
		String sku = normalizeSku(request.getSku());

		boolean duplicateSku = productRepository.existsBySkuIgnoreCase(sku);

		if (duplicateSku && !product.getSku().equalsIgnoreCase(sku))
		{
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Product SKU already exists");
		}

		CategoryEntity category = findActiveCategory(request.getCategoryId());

		product.setName(request.getName().trim());
		product.setSku(sku);
		product.setDescription(request.getDescription());
		product.setPrice(request.getPrice());
		product.setWattage(request.getWattage());
		product.setStock(request.getStock());
		product.setCategory(category);

		ProductEntity updated = productRepository.save(product);

		return toResponse(updated);
	}

	@Transactional
	public void deactivateProduct(Long id) 
	{

		ProductEntity product = findActiveProduct(id);
		product.setActive(false);

		productRepository.save(product);
	}

	private ProductEntity findActiveProduct(Long id)
	{

		ProductEntity product = productRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

		if (!product.isActive()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
		}

		return product;
	}

	private CategoryEntity findActiveCategory(Long categoryId) 
	{

		CategoryEntity category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

		if (!category.isActive()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot use an inactive category");
		}

		return category;
	}

	private String normalizeSku(String sku)
	{
		return sku.trim().toUpperCase(Locale.ROOT);
	}

	private ProductResponse toResponse(ProductEntity product)
	{

		CategoryEntity category = product.getCategory();

		return new ProductResponse(product.getId(), product.getName(), product.getSku(), product.getDescription(),
				product.getPrice(), product.getWattage(), product.getStock(), product.isActive(), category.getId(),
				category.getName());
	}
}
