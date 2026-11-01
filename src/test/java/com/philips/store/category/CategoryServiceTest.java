package com.philips.store.category;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryRequest request;
    private CategoryEntity category;

    @BeforeEach
    void setUp() {
        request = new CategoryRequest();
        request.setName("LED Bulbs");
        request.setDescription("Energy efficient LED lighting products");

        category = new CategoryEntity();
        category.setId(1L);
        category.setName("LED Bulbs");
        category.setDescription("Energy efficient LED lighting products");
        category.setActive(true);
    }

    @Test
    void createCategorySuccessfully() {

        when(categoryRepository.existsByNameIgnoreCase("LED Bulbs"))
                .thenReturn(false);

        when(categoryRepository.save(any(CategoryEntity.class)))
                .thenAnswer(invocation -> {
                    CategoryEntity saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        CategoryResponse response = categoryService.createCategory(request);

        assertEquals(1L, response.getId());
        assertEquals("LED Bulbs", response.getName());
        assertEquals(
                "Energy efficient LED lighting products",
                response.getDescription());
        assertTrue(response.isActive());

        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    void createCategoryWithDuplicateNameThrowsConflict() {

        when(categoryRepository.existsByNameIgnoreCase("LED Bulbs"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.createCategory(request));

        assertEquals(409, exception.getStatusCode().value());

        verify(categoryRepository, never())
                .save(any(CategoryEntity.class));
    }

    @Test
    void getAllCategoriesReturnsOnlyActiveCategories() {

        CategoryEntity inactiveCategory = new CategoryEntity();
        inactiveCategory.setId(2L);
        inactiveCategory.setName("Old Lights");
        inactiveCategory.setDescription("Inactive category");
        inactiveCategory.setActive(false);

        when(categoryRepository.findAll())
                .thenReturn(List.of(category, inactiveCategory));

        List<CategoryResponse> responses =
                categoryService.getAllCategories();

        assertEquals(1, responses.size());
        assertEquals("LED Bulbs", responses.get(0).getName());
        assertTrue(responses.get(0).isActive());

        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoryByIdSuccessfully() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        CategoryResponse response =
                categoryService.getCategoryById(1L);

        assertEquals(1L, response.getId());
        assertEquals("LED Bulbs", response.getName());
        assertTrue(response.isActive());
    }

    @Test
    void getMissingCategoryThrowsNotFound() {

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.getCategoryById(99L));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void updateCategorySuccessfully() {

        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("LED Lamps");
        updateRequest.setDescription("Updated lighting products");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByNameIgnoreCase("LED Lamps"))
                .thenReturn(false);

        when(categoryRepository.save(any(CategoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response =
                categoryService.updateCategory(1L, updateRequest);

        assertEquals("LED Lamps", response.getName());
        assertEquals("Updated lighting products", response.getDescription());

        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    void deactivateCategorySetsActiveToFalse() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(any(CategoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.deactivateCategory(1L);

        assertEquals(false, category.isActive());

        verify(categoryRepository).save(category);
    }
}
