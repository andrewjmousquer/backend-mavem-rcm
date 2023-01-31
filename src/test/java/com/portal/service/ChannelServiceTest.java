package com.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.impl.ChannelDAO;
import com.portal.exceptions.BusException;
import com.portal.model.Channel;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.ChannelService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class ChannelServiceTest {

	@Mock
	ChannelDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	ChannelService service;
	
	@Nested
	class ListAll {
		@DisplayName("Listar os canais e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.ChannelServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnChannelList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new Channel() ) );

			List<Channel> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva um novo canal válido e retorna a marca com ID")
		void givenValidChannel_whenSave_thenReturnId() throws Exception {
			Channel mock = new Channel( 1, "Channel 1", true, true, false );
			
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Channel> entityDB = service.save( mock, null );
			
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getName(), entityDB.get().getName( ));
			assertEquals( mock.getActive(), entityDB.get().getActive( ));
			assertEquals( mock.getHasPartner(), entityDB.get().getHasPartner( ));
			assertEquals( mock.getHasInternalSale(), entityDB.get().getHasInternalSale( ));
		}
		
		@DisplayName("Salva um canal e da erro nos validators.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ChannelServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidChannel_whenSave_thenTestValidador( Channel model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Channel>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva um novo canal duplicado com o mesmo nome. CHN-I2")
		void givenDuplicateChannel_whenSave_thenReturnError_CHNI2() throws Exception {
			
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( Channel.builder().id( 1 ).build() ) );
			
			Channel model = Channel.builder()
								.name( "Channel 1" )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "Já existe um canal com o mesmo nome.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza um canal válido e retorna com a atualização")
		void givenChannel_whenUpdate_thenReturnNewChannel() throws Exception {
			Channel mock = new Channel( 1, "Channel 1", true, true, false );			
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Channel() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Channel> entityDB = service.update( mock, null );
			
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getName(), entityDB.get().getName());
			assertEquals( mock.getActive(), entityDB.get().getActive());
			assertEquals( mock.getHasPartner(), entityDB.get().getHasPartner());
			assertEquals( mock.getHasInternalSale(), entityDB.get().getHasInternalSale());
		}
		
		@DisplayName("Atualiza um canal inválido e retorna erro. CHN-U1, CHN-U3")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ChannelServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidChannel_whenUpdate_thenTestValidador( Channel model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Channel>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza um novo canal duplicado com o mesmo nome. CHN-U2")
		void givenDuplicateChannel_whenUpdate_thenReturnError_CHNU2() throws Exception {

			Channel model = Channel.builder()
									.id( 1 )
									.name( "Channel 1" )
									.active( true )
									.build();
			
			Channel duplicateModel = Channel.builder()
										.id( 2 )
										.name( "Channel 1" )
										.active( true )
										.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Channel() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "Já existe um canal com o mesmo nome.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza um canal existente e não pode dar erro de duplicado")
		void givenSelfChannel_whenUpdate_thenNoError() throws Exception {
			
			Channel model = Channel.builder()
								.id( 1 )
								.name( "Channel 1" )
								.active( true )
								.build();

			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Channel() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( model ) ); // Para o método de validation
			
			assertDoesNotThrow( ()->service.update( model, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um canal não existente. CHN-U4")
		void givenNoExistChannel_whenUpdate_thenReturnError_CHNU4() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			Channel model = Channel.builder()
								.id( 1 )
								.name( "Channel 1" )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "O canal a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Delete um canal com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Channel() ) );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um canal com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um canal com que não existe" )
		void givenNoExistedCanal_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O canal a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Deleta um canal com relacionamento com parceiro e retorna erro" )
		void givenChannelInPartnerRelationship_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( new Channel() ) );
			when( dao.hasPartnerRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir o canal pois existe um relacionamento com parceiro.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um canal com relacionamento com lista de preço e retorna erro" )
		void givenGroupInPriceListRelationship_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( new Channel() ) );
			when( dao.hasPriceListRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir o canal pois existe um relacionamento com lista de preço.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
				Arguments.of(0, 1, "DESC", "id"),
				Arguments.of(0, 1, "DESC", null),
				Arguments.of(0, 1, "DESC", "chn_id"),
				Arguments.of(0, 1, null, "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
    			Arguments.of( new Channel(null, null, null, null, null) ),
    			Arguments.of( new Channel(0, "Channel 1", true, true, true) ),
    			Arguments.of( new Channel(null, "Channel 1", null, true, true) ),
    			Arguments.of( new Channel(null, null, null, true, true) ),
    			Arguments.of( new Channel(null, null, null, null, true) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
	    		Arguments.of( new Channel(1, null, null, null, null) ),
	    		Arguments.of( new Channel(0, "Channel 1", true, true, true) ),
	    		Arguments.of( new Channel(null, "Channel 1", true, true, true) ),
    			Arguments.of( new Channel(1, "Channel 1", null, true, true) ),
    			Arguments.of( new Channel(1, null, null, null, true) )
	    );
	}
}
