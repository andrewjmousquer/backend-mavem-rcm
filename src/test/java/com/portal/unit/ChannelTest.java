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

import com.portal.dto.ChannelDTO;
import com.portal.model.Channel;

@ExtendWith(SpringExtension.class)
class ChannelTest {
	
	@Test
	void givenChannelDTO_whenConvertToEntity_thenReturnChannel() {
		ChannelDTO dtoMock = new ChannelDTO( 1, "Channel 1", true, true, true );
		Channel entity = Channel.toEntity( dtoMock ); 
		
		assertNotNull( entity );
		assertEquals( dtoMock.getId(), entity.getId());
		assertEquals( dtoMock.getName(), entity.getName());
		assertEquals( dtoMock.getActive(), entity.getActive());
		assertEquals( dtoMock.getHasPartner(), entity.getHasPartner());
		assertEquals( dtoMock.getHasInternalSale(), entity.getHasInternalSale());
	}
	
	@Test
	void givenChannel_whenConvertToDTO_thenReturnChannelDTO() {
		Channel entityMock = new Channel( 1, "Channel 1", true, true, true );
		ChannelDTO dto = ChannelDTO.toDTO( entityMock ); 
		
		assertNotNull( dto );
		assertEquals( entityMock.getId(), dto.getId());
		assertEquals( entityMock.getName(), dto.getName());
		assertEquals( entityMock.getActive(), dto.getActive());
		assertEquals( entityMock.getHasPartner(), dto.getHasPartner());
		assertEquals( entityMock.getHasInternalSale(), dto.getHasInternalSale());
	}
	
	@Test
	void givenListChannel_whenConvertToDTO_thenReturnListChannelDTO() {
		
		Channel entity1 = new Channel( 1, "Channel 1", true, true, true );
		Channel entity2 = new Channel( 2, "Channel 2", true, true, true );
		
		List<Channel> entities = Arrays.asList( entity1, entity2 );
		
		ChannelDTO dto1 = new ChannelDTO( 1, "Channel 1", true, true, true );
		ChannelDTO dto2 = new ChannelDTO( 2, "Channel 2", true, true, true );
		
		List<ChannelDTO> dtos = ChannelDTO.toDTO( entities ); 
		
		assertNotNull( dtos );
		assertFalse( dtos.isEmpty() );
        assertThat( dtos, containsInAnyOrder(dto1, dto2) );
	}
	
	@Test
	void givenNullChannelDTO_whenConvertToEntity_thenReturnNull() {
		Channel entity = Channel.toEntity( null ); 
		assertNull( entity );
	}
	
	@Test
	void givenNullChannel_whenConvertToDTO_thenReturnNull() {
		Channel entity = null;
		ChannelDTO dto = ChannelDTO.toDTO( entity );
		assertNull( dto );
	}
}
