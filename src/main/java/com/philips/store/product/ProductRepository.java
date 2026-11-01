
package com.philips.store.product;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

	boolean existsBySkuIgnoreCase(String sku);

	@Query("""
			SELECT p FROM ProductEntity p
			WHERE p.active = true
			  AND (
			      :keyword IS NULL
			      OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			      OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
			  )
			  AND (:categoryId IS NULL OR p.category.id = :categoryId)
			  AND (:minPrice IS NULL OR p.price >= :minPrice)
			  AND (:maxPrice IS NULL OR p.price <= :maxPrice)
			""")
	Page<ProductEntity> searchActiveProducts(@Param("keyword") String keyword, @Param("categoryId") Long categoryId,
			@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);
}
