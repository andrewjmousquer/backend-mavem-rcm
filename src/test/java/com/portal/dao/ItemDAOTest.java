package com.portal.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.portal.dao.impl.ItemDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.model.Classifier;
import com.portal.model.Item;
import com.portal.model.ItemType;
import com.portal.model.UserModel;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.ItemTypeService;

/**
 * Quando se trata de test de DAO, todos os teste devem ser executados na ordem correta,
 * todos são criados usando uma ordem lógica com base no registro salvo no DB.
 * Isso por que esse tipo de teste é de integração, então ele leva em consideração os dados previamento inseridos.
 * 
 * @author Brunno Tavares
 */
@SpringBootTest
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
class ItemDAOTest {

	@Container
	private static final MySQLContainer<?> database = new MySQLContainer<>( "mysql:5.7.34" )
																.withInitScript("1-structure.sql")
																.withDatabaseName("carbon");
	
	
	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
	    registry.add("spring.datasource-portal.jdbcUrl", database::getJdbcUrl);
	    registry.add("spring.datasource-portal.username", database::getUsername);
	    registry.add("spring.datasource-portal.password", database::getPassword);
	}
	
	@Autowired
	private ItemDAO dao;
	
	@Autowired
	private ItemTypeService itemTypeService;

	private static final ItemType itemType = new ItemType(1, "ItemType 1", true, true, 1);
	private static final Item mockCheck = new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), itemType, null, null, null, null, null, null, null, null, null);
	
	/**
	 * Devido a injeção de dependências não é possivil usar o @BerforeAll
	 * Por esse motivo forçamos ser o primeiro passo do teste a inserção dos dados 
	 * usados como base.
	 */
	@Test
	@Order(1)
	void setup() throws Exception {
		UserProfileDTO profile = new UserProfileDTO( new UserModel( "MOCK USER" ) );

		Optional<ItemType> dbItemType = itemTypeService.save(itemType, profile);
		itemType.setId( dbItemType.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ItemDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( Item model ) throws Exception {
		Optional<Item> modelDB = dao.save( model );
		assertNotNull( modelDB );
		assertTrue( modelDB.isPresent() );
		assertEquals( model.getId(), modelDB.get().getId() );
		assertEquals( model.getId(), modelDB.get().getId());
		assertEquals( model.getName(), modelDB.get().getName() );
		assertEquals( model.getCod(), modelDB.get().getCod() );
		assertEquals( model.getSeq(), modelDB.get().getSeq() );
		assertEquals( model.getForFree(), modelDB.get().getForFree() );
		assertEquals( model.getGeneric(), modelDB.get().getGeneric() );
		assertNotNull( modelDB.get().getItemType() );
		assertEquals( model.getItemType(), modelDB.get().getItemType() );
		assertEquals( model.getItemType().getName(), modelDB.get().getItemType().getName() );
		assertNotNull( modelDB.get().getMandatory() );
		assertEquals( model.getMandatory(), modelDB.get().getMandatory() );
		assertEquals( model.getDescription(), modelDB.get().getDescription() );
		assertEquals( model.getHyperlink(), modelDB.get().getHyperlink() );
	}
	
	
	@Test
	@Order(3)
	void whenListAll_ReturnList() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "itm_id");
		List<Item> list = dao.listAll( pageReq  );
		assertEquals(listEntityToSave().count(), list.size());
	}
	
	@Test
	@Order(3)
	void givenExistedItem_whenGetById_ThenReturn() throws Exception {
		Item model = Item.builder()
								.id( 1 )
								.build();

		Optional<Item> modelDB = dao.getById( model.getId() );
		
		assertNotNull( modelDB );
		assertTrue( modelDB.isPresent() );
		assertEquals( model, modelDB.get() );
		assertEquals( mockCheck.getId(), modelDB.get().getId());
		assertEquals( mockCheck.getName(), modelDB.get().getName() );
		assertEquals( mockCheck.getCod(), modelDB.get().getCod() );
		assertEquals( mockCheck.getSeq(), modelDB.get().getSeq() );
		assertEquals( mockCheck.getForFree(), modelDB.get().getForFree() );
		assertEquals( mockCheck.getGeneric(), modelDB.get().getGeneric() );
		assertNotNull( modelDB.get().getItemType() );
		assertEquals( mockCheck.getItemType(), modelDB.get().getItemType() );
		assertEquals( mockCheck.getItemType().getName(), modelDB.get().getItemType().getName() );
		assertNotNull( modelDB.get().getMandatory() );
		assertEquals( mockCheck.getMandatory(), modelDB.get().getMandatory() );
		assertEquals( mockCheck.getDescription(), modelDB.get().getDescription() );
		assertEquals( mockCheck.getHyperlink(), modelDB.get().getHyperlink() );
	}

	
	@ParameterizedTest
	@Order(4)
	@MethodSource("com.portal.dao.ItemDAOTest#listEntityToFind")
	void whenFind_ThenReturn( Item toFind, Item mockCheck, int expectedSize ) throws Exception {
		List<Item> modelDB = dao.find( toFind, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( expectedSize, modelDB.size() );
		
		Optional<Item> entity = modelDB.stream().filter(item->item.getId().equals(1) ).findFirst();
		
		if( entity.isPresent() ) {
			assertEquals( mockCheck.getId(), entity.get().getId());
			assertEquals( mockCheck.getName(), entity.get().getName() );
			assertEquals( mockCheck.getCod(), entity.get().getCod() );
			assertEquals( mockCheck.getSeq(), entity.get().getSeq() );
			assertEquals( mockCheck.getForFree(), entity.get().getForFree() );
			assertEquals( mockCheck.getGeneric(), entity.get().getGeneric() );
			assertNotNull( entity.get().getItemType() );
			assertEquals( mockCheck.getItemType(), entity.get().getItemType() );
			assertNotNull( entity.get().getMandatory() );
			assertEquals( mockCheck.getMandatory(), entity.get().getMandatory() );
			assertEquals( mockCheck.getDescription(), entity.get().getDescription() );
			assertEquals( mockCheck.getHyperlink(), entity.get().getHyperlink() );
		}
	}
	
	
	@ParameterizedTest
	@Order(4)
	@MethodSource("com.portal.dao.ItemDAOTest#listEntityToSearch")
	void whenSearch_ThenReturn( Item toSearch, Item mockCheck, int expectedSize ) throws Exception {
		List<Item> modelDB = dao.search( toSearch, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( expectedSize, modelDB.size() );
		
		Optional<Item> testEntity = modelDB.stream().filter(item->item.getId().equals(1) ).findFirst();
		if( testEntity.isPresent() ) {
			assertEquals( mockCheck.getId(), testEntity.get().getId());
			assertEquals( mockCheck.getName(), testEntity.get().getName() );
			assertEquals( mockCheck.getCod(), testEntity.get().getCod() );
			assertEquals( mockCheck.getSeq(), testEntity.get().getSeq() );
			assertEquals( mockCheck.getForFree(), testEntity.get().getForFree() );
			assertEquals( mockCheck.getGeneric(), testEntity.get().getGeneric() );
			assertNotNull( testEntity.get().getItemType() );
			assertEquals( mockCheck.getItemType(), testEntity.get().getItemType() );
			assertNotNull( testEntity.get().getMandatory() );
			assertEquals( mockCheck.getMandatory(), testEntity.get().getMandatory() );
			assertEquals( mockCheck.getDescription(), testEntity.get().getDescription() );
			assertEquals( mockCheck.getHyperlink(), testEntity.get().getHyperlink() );
		}
	}
	
	
	@Order(5)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ItemDAOTest#listEntityToUpdate")
	void whenUpdate_CheckNewValues( Item item ) throws Exception {
		dao.update( item );
		
		Optional<Item> modelDB = dao.getById( item.getId() );
		
		assertNotNull( modelDB );
		assertTrue( modelDB.isPresent() );
		assertEquals( item.getId(), modelDB.get().getId());
		assertEquals( item.getName(), modelDB.get().getName() );
		assertEquals( item.getCod(), modelDB.get().getCod() );
		assertEquals( item.getSeq(), modelDB.get().getSeq() );
		assertEquals( item.getForFree(), modelDB.get().getForFree() );
		assertEquals( item.getGeneric(), modelDB.get().getGeneric() );
		assertNotNull( modelDB.get().getItemType() );
		assertEquals( item.getItemType(), modelDB.get().getItemType() );
		assertNotNull( modelDB.get().getMandatory() );
		assertEquals( item.getMandatory(), modelDB.get().getMandatory() );
		assertEquals( mockCheck.getDescription(), modelDB.get().getDescription() );
		assertEquals( mockCheck.getHyperlink(), modelDB.get().getHyperlink() );
	}
	
	
	@Test
	@Order(6)
	void givenExistedBrand_whenDelete_ThenNoFind() throws Exception {
		dao.delete( 1 );
		
		Optional<Item> modelDB = dao.getById( 1 );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isPresent() );
	}
	
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new Item(null, "Item 1", "200", 1, false, false, new Classifier(23), itemType, "FILE", "ICON", "LABEL", null, null, null, null, null, null) ),
    		Arguments.of( new Item(null, "Item 2", null, 2, false, true, new Classifier(24), itemType, null, null, "DESC", "HTTP", null, null, null, null, null) ),
    		Arguments.of( new Item(null, "Item 3", null, 3, true, false, new Classifier(22), itemType,null,"LABEL", null, null, null, null,null, null, null) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new Item(null, "Item 1", "200", 1, false, false, new Classifier(23), itemType, "FILE", "ICON", "LABEL", null, null, null, null, null, null) ),
    		Arguments.of( new Item(null, "Item 2", null, 2, false, true, new Classifier(24), itemType, null, null, "DESC", "HTTP", null, null, null, null, null) ),
    		Arguments.of( new Item(null, "Item 3", null, 3, true, false, new Classifier(22), itemType,null,"LABEL", null, null, null, null,null, null, null) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToFind() {
	    return Stream.of(
    		Arguments.of( Item.builder().id( 1 ).build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().name("Item 1").build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().name("Item 1").forFree(false).build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().cod("200").build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().seq(1).build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().forFree( true ).build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().generic( true ).build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().itemType( ItemType.builder().id(1).build() ).build(), mockCheck, 3 ),
    		Arguments.of( Item.builder().mandatory( new Classifier(23) ).build(), mockCheck, 1 )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSearch() {
	    return Stream.of(
    		Arguments.of( Item.builder().id( 1 ).build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().name("Item").build(), mockCheck, 3 ),
    		Arguments.of( Item.builder().name("Item 1").forFree(false).build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().cod("200").build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().seq(1).build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().forFree( true ).build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().generic( true ).build(), mockCheck, 1 ),
    		Arguments.of( Item.builder().itemType( ItemType.builder().id(1).build() ).build(), mockCheck, 3 ),
    		Arguments.of( Item.builder().mandatory( new Classifier(23) ).build(), mockCheck, 1 )
	    );
	}
	
}
