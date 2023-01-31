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

import com.portal.dto.ProductDTO;
import com.portal.model.Product;

@ExtendWith(SpringExtension.class)
class ProductTest {
	
	@Test
	void givenProductDTO_whenConvertToEntity_thenReturnProduct() {
		ProductDTO dtoMock = new ProductDTO( 1, "Product 1", true, 10, null );
		Product entity = Product.toEntity( dtoMock ); 
		
		assertNotNull( entity );
		assertEquals( dtoMock.getId(), entity.getId());
		assertEquals( dtoMock.getName(), entity.getName());
		assertEquals( dtoMock.getActive(), entity.getActive());
	}
	
	@Test
	void givenProduct_whenConvertToDTO_thenReturnProductDTO() {
		Product entityMock = new Product( 1, "Product 1", true, 10, null );
		ProductDTO dto = ProductDTO.toDTO( entityMock ); 
		
		assertNotNull( dto );
		assertEquals( dto.getId(), entityMock.getId());
		assertEquals( dto.getName(), entityMock.getName());
		assertEquals( dto.getActive(), entityMock.getActive());
	}
	
	@Test
	void givenListProduct_whenConvertToDTO_thenReturnListProductDTO() {
		
		Product entity1 = new Product( 1, "Product 1", true, 10, null );
		Product entity2 = new Product( 2, "Product 2", true, 10, null );
		
		List<Product> entities = Arrays.asList( entity1, entity2 );
		
		ProductDTO dto1 = new ProductDTO( 1, "Product 1", true, 10, null );
		ProductDTO dto2 = new ProductDTO( 2, "Product 2", true, 10, null );
		
		List<ProductDTO> dtos = ProductDTO.toDTO( entities ); 
		
		assertNotNull( dtos );
		assertFalse( dtos.isEmpty() );
        assertThat( dtos, containsInAnyOrder(dto1, dto2) );
	}
	
	@Test
	void givenNullProductDTO_whenConvertToEntity_thenReturnNull() {
		Product entity = Product.toEntity( null ); 
		assertNull( entity );
	}
	
	@Test
	void givenNullProduct_whenConvertToDTO_thenReturnNull() {
		Product entity = null;
		ProductDTO dto = ProductDTO.toDTO( entity );
		assertNull( dto );
	}
}
