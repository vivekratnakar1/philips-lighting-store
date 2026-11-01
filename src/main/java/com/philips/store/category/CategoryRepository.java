package com.philips.store.category;


import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

	boolean existsByNameIgnoreCase(String name);
}
