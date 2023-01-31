package com.portal.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.portal.dto.BrandDTO;
import com.portal.model.Brand;

@ExtendWith(SpringExtension.class)
class BrandTest {
	
	@Test
	void givenBrandDTO_whenConvertToEntity_thenReturnBrand() {
		BrandDTO dto = new BrandDTO( 1, "Brand 1", true );
		Brand entity = Brand.toEntity( dto ); 
		
		assertNotNull( entity );
		assertEquals( entity.getId(), dto.getId());
		assertEquals( entity.getName(), dto.getName());
		assertEquals( entity.getActive(), dto.getActive());
	}
	
	@Test
	void givenBrand_whenConvertToDTO_thenReturnBrandDTO() {
		Brand entity = new Brand( 1, "Brand 1", true );
		BrandDTO dto = BrandDTO.toDTO( entity ); 
		
		assertNotNull( dto );
		assertEquals( entity.getId(), dto.getId());
		assertEquals( entity.getName(), dto.getName());
		assertEquals( entity.getActive(), dto.getActive());
	}
	
	@Test
	void givenListBrand_whenConvertToDTO_thenReturnListBrandDTO() {
		
		Brand entity1 = new Brand( 1, "Brand 1", true );
		Brand entity2 = new Brand( 2, "Brand 2", true );
		
		List<Brand> entities = Arrays.asList( entity1, entity2 );
		
		BrandDTO dto1 = new BrandDTO( 1, "Brand 1", true );
		BrandDTO dto2 = new BrandDTO( 2, "Brand 2", true );
		
		List<BrandDTO> dtos = BrandDTO.toDTO( entities ); 
		
		assertNotNull( dtos );
		assertFalse( dtos.isEmpty() );
        assertThat( dtos, containsInAnyOrder(dto1, dto2) );
	}
	
	@Test
	void givenListBrandDTO_whenConvertToEntity_thenReturnListBrand() {
		
		BrandDTO dto1 = new BrandDTO( 1, "Brand 1", true );
		BrandDTO dto2 = new BrandDTO( 2, "Brand 2", true );
		
		Brand entity1 = new Brand( 1, "Brand 1", true );
		Brand entity2 = new Brand( 2, "Brand 2", true );
		
		List<BrandDTO> entities = Arrays.asList( dto1, dto2 );
		
		List<Brand> dtos = Brand.toEntity( entities ); 
		
		assertNotNull( dtos );
		assertFalse( dtos.isEmpty() );
        assertThat( dtos, containsInAnyOrder(entity1, entity2) );
	}
	
	@Test
	void givenNullBrandDTO_whenConvertToEntity_thenReturnNull() {
		BrandDTO dto = null;
		Brand entity = Brand.toEntity( dto ); 
		assertNull( entity );
	}
	
	@Test
	void givenNullBrand_whenConvertToDTO_thenReturnNull() {
		Brand entity = null;
		BrandDTO dto = BrandDTO.toDTO( entity );
		assertNull( dto );
	}
}
