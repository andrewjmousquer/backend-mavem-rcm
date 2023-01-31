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
import com.portal.dto.ModelDTO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.model.Brand;
import com.portal.model.Model;


@ExtendWith(SpringExtension.class)
class ModelTest {
	
	@Test
	void givenModelDTO_whenConvertToEntity_thenReturnModel() {
		ModelDTO dtoMock = new ModelDTO( 1, "Model 1", true, BrandDTO.builder().id(1).build(), "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
		Model entity = Model.toEntity( dtoMock ); 
		
		assertNotNull( entity );
		assertEquals( dtoMock.getId(), entity.getId());
		assertEquals( dtoMock.getName(), entity.getName());
		assertEquals( dtoMock.getActive(), entity.getActive());
		assertNotNull( dtoMock.getBrand() );
		assertEquals( dtoMock.getBrand().getId(), entity.getId());
		assertEquals( dtoMock.getBodyType(), entity.getBodyType());
		assertEquals( dtoMock.getSize(), entity.getSize());
		assertEquals( dtoMock.getCategory(), entity.getCategory());
	}
	
	@Test
	void givenModel_whenConvertToDTO_thenReturnModelDTO() {
		Model entityMock = new Model( 1, "Model 1", true, Brand.builder().id(1).build(), "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
		ModelDTO dto = ModelDTO.toDTO( entityMock ); 
		
		assertNotNull( dto );
		assertEquals( entityMock.getId(), dto.getId());
		assertEquals( entityMock.getName(), dto.getName());
		assertEquals( entityMock.getActive(), dto.getActive());
		assertNotNull( entityMock.getBrand() );
		assertEquals( entityMock.getBrand().getId(), dto.getId());
		assertEquals( entityMock.getBodyType(), dto.getBodyType());
		assertEquals( entityMock.getSize(), dto.getSize());
		assertEquals( entityMock.getCategory(), dto.getCategory());
		
	}
	
	@Test
	void givenListModel_whenConvertToDTO_thenReturnListModelDTO() {
		
		Model model1 = new Model( 1, "Model 1", true, Brand.builder().id(1).build(), "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
		Model model2 = new Model( 2, "Model 2", true, Brand.builder().id(2).build(), "038002-5", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
		
		List<Model> entities = Arrays.asList( model1, model2 );
		
		ModelDTO dto1 = new ModelDTO( 1, "Model 1", true, BrandDTO.builder().id(1).build(), "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
		ModelDTO dto2 = new ModelDTO( 2, "Model 2", true, BrandDTO.builder().id(2).build(), "038002-5", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
		
		List<ModelDTO> dtos = ModelDTO.toDTO( entities ); 
		
		assertNotNull( dtos );
		assertFalse( dtos.isEmpty() );
        assertThat( dtos, containsInAnyOrder(dto1, dto2) );
	}
	
	@Test
	void givenNullModelDTO_whenConvertToEntity_thenReturnNull() {
		Model model = Model.toEntity( null ); 
		assertNull( model );
	}
	
	@Test
	void givenNullModel_whenConvertToDTO_thenReturnNull() {
		Model model = null;
		ModelDTO dto = ModelDTO.toDTO( model );
		assertNull( dto );
	}
}
