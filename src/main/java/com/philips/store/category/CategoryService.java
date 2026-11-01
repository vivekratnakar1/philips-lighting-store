package com.philips.store.category;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Transactional
	public CategoryResponse createCategory(CategoryRequest request) {

		String name = request.getName().trim();

		if (categoryRepository.existsByNameIgnoreCase(name)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
		}

		CategoryEntity category = new CategoryEntity();
		category.setName(name);
		category.setDescription(request.getDescription());

		CategoryEntity saved = categoryRepository.save(category);

		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> getAllCategories() {

		return categoryRepository.findAll().stream().filter(CategoryEntity::isActive).map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public CategoryResponse getCategoryById(Long id) {

		CategoryEntity category = findActiveCategory(id);

		return toResponse(category);
	}

	@Transactional
	public CategoryResponse updateCategory(Long id, CategoryRequest request) {

		CategoryEntity category = findActiveCategory(id);
		String name = request.getName().trim();

		boolean duplicate = categoryRepository.existsByNameIgnoreCase(name);

		if (duplicate && !category.getName().equalsIgnoreCase(name)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
		}

		category.setName(name);
		category.setDescription(request.getDescription());

		return toResponse(categoryRepository.save(category));
	}

	@Transactional
	public void deactivateCategory(Long id) {

		CategoryEntity category = findActiveCategory(id);
		category.setActive(false);

		categoryRepository.save(category);
	}

	private CategoryEntity findActiveCategory(Long id) {

		CategoryEntity category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

		if (!category.isActive()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
		}

		return category;
	}

	private CategoryResponse toResponse(CategoryEntity category) {

		return new CategoryResponse(category.getId(), category.getName(), category.getDescription(),
				category.isActive());
	}
}
