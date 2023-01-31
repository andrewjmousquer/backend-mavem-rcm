package com.portal.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
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

import com.portal.dao.impl.ProposalDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.LeadState;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.enums.PersonClassification;
import com.portal.enums.ProposalRisk;
import com.portal.enums.ProposalState;
import com.portal.enums.SaleProbabilty;
import com.portal.model.Brand;
import com.portal.model.Lead;
import com.portal.model.Model;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.Seller;
import com.portal.model.Source;
import com.portal.model.UserModel;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.LeadService;
import com.portal.service.imp.ModelService;
import com.portal.service.imp.PersonService;
import com.portal.service.imp.SellerService;
import com.portal.service.imp.SourceService;

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
class ProposalDAOTest {

    @Container
    private static final MySQLContainer<?> database = new MySQLContainer<>("mysql:5.7.34")
            .withInitScript("1-structure.sql")
            .withDatabaseName("carbon");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource-portal.jdbcUrl", database::getJdbcUrl);
        registry.add("spring.datasource-portal.username", database::getUsername);
        registry.add("spring.datasource-portal.password", database::getPassword);
    }

    @Autowired
    private ProposalDAO dao;

    @Autowired
    private LeadService leadService;

    @Autowired
    private ClassifierService classifierService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private PersonService personService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ModelService modelService;

    private static final Person clientMock = new Person(null, "Person Client", "Client", "00000000001", null, null, null, null, PersonClassification.PF.getType());
    private static final Seller sellerMock = Seller.builder().person(new Person(null, "Person Seller", "Seller", "00000000002", null, null, null, null, PersonClassification.PF.getType())).build();
    private static final Source sourceMock = new Source(null, "Source 1", true);
    private static final Brand brandMock = new Brand(null, "Brand 1", true);
    private static final Model modelMock = new Model(null, "Model 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM);
    private static final Lead leadMock = Lead.builder().id(null).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00))
            .client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType())
            .model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).description("NOTES 1").subject("").build();

    /**
     * Devido a injeção de dependências não é possivil usar o @BerforeAll
     * Por esse motivo forçamos ser o primeiro passo do teste a inserção dos dados
     * usados como base.
     */
    @Test
    @Order(1)
    void setup() throws Exception {
        UserProfileDTO profile = new UserProfileDTO(new UserModel("MOCK USER"));

        for (ProposalState classifiers : ProposalState.values()) {
            classifierService.save(classifiers.getType(), profile);
        }

        for (PersonClassification classifiers : PersonClassification.values()) {
            classifierService.save(classifiers.getType(), profile);
        }

        for (LeadState classifiers : LeadState.values()) {
            classifierService.save(classifiers.getType(), profile);
        }

        for (ModelBodyType classifiers : ModelBodyType.values()) {
            classifierService.save(classifiers.getType(), profile);
        }

        for (ModelCategory classifiers : ModelCategory.values()) {
            classifierService.save(classifiers.getType(), profile);
        }

        for (ModelSize classifiers : ModelSize.values()) {
            classifierService.save(classifiers.getType(), profile);
        }

        for (SaleProbabilty classifiers : SaleProbabilty.values()) {
            classifierService.save(classifiers.getType(), profile);
        }

        Optional<Person> dbPerson = this.personService.save(clientMock, profile);
        clientMock.setId(dbPerson.get().getId());

        Optional<Seller> dbSeller = this.sellerService.save(sellerMock, profile);
        sellerMock.setId(dbSeller.get().getId());

        Optional<Source> dbSource = this.sourceService.save(sourceMock, profile);
        sourceMock.setId(dbSource.get().getId());

        Optional<Brand> dbBrand = this.brandService.save(brandMock, profile);
        brandMock.setId(dbBrand.get().getId());

        Optional<Model> dbModel = this.modelService.save(modelMock, profile);
        modelMock.setId(dbModel.get().getId());

        Optional<Lead> dbLead = this.leadService.save(leadMock, profile);
        leadMock.setId(dbLead.get().getId());
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalDAOTest#listEntityToSave")
    @DisplayName("Quando salvar retornar os IDs salvos")
    void whenSave_ReturnSavedId(Proposal entity) throws Exception {
        Optional<Proposal> entityId = dao.save(entity);
        assertNotNull(entityId);
        assertTrue(entityId.isPresent());
        assertEquals(entity.getId(), entityId.get().getId());
    }

    @Test
    @Order(3)
    @DisplayName("listAll - Quando listar todos as leads")
    void whenListAll_ReturnListProposal() throws Exception {
        PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "led_id");
        List<Proposal> list = dao.listAll(pageReq);

        assertEquals(listEntityToSave().count(), list.size());
    }

    @Test
    @Order(3)
    @DisplayName("getById - Dado um ID existente retornar a proposta")
    void givenExistedProposal_whenGetById_ThenReturnProposal() throws Exception {
        Proposal mock = new Proposal(4, 4l, "B12208A", "A", null, LocalDateTime.of(2022, 03, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.CANCELED, null, leadMock, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);

        Optional<Proposal> entityDB = dao.getById(mock.getId());

        assertNotNull(entityDB);
        assertTrue(entityDB.isPresent());
        assertEquals(mock, entityDB.get());
        assertEquals(mock.getCreateDate(), entityDB.get().getCreateDate());
        assertEquals(mock.getNum(), entityDB.get().getNum());
        assertEquals(mock.getCod(), entityDB.get().getCod());
        assertNotNull(entityDB.get().getStatus());
        assertEquals(mock.getStatus(), entityDB.get().getStatus());
        assertNotNull(entityDB.get().getLead());
        assertEquals(mock.getLead(), entityDB.get().getLead());
    }

    @Order(3)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalDAOTest#listEntityToFind")
    @DisplayName("Quando busca retornar os IDs salvos")
    void whenFind_ReturnProposal(Proposal mock, Proposal filter, int expectedSize) throws Exception {
        List<Proposal> entityDB = dao.find(filter, null);

        assertNotNull(entityDB);
        assertFalse(entityDB.isEmpty());
        assertEquals(expectedSize, entityDB.size());

        Optional<Proposal> entity = entityDB.stream().filter(item -> item.getId().equals(mock.getId())).findFirst();

        assertEquals(mock, entity.get());
        assertEquals(mock.getCreateDate(), entity.get().getCreateDate());
        assertEquals(mock.getNum(), entity.get().getNum());
        assertEquals(mock.getCod(), entity.get().getCod());
        assertNotNull(entity.get().getStatus());
        assertEquals(mock.getStatus(), entity.get().getStatus());

        if (mock.getLead() != null) {
            assertNotNull(entity.get().getLead());
            assertEquals(mock.getLead(), entity.get().getLead());
        }
    }

    @Order(4)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalDAOTest#listEntityToUpdate")
    @DisplayName("update - Quando atualizar retorna o novo objeto")
    void whenUpdate_CheckNewValues(Proposal mock) throws Exception {
        dao.update(mock);

        Optional<Proposal> entityDB = dao.getById(mock.getId());

        assertNotNull(entityDB);
        assertTrue(entityDB.isPresent());
        assertEquals(mock, entityDB.get());
        assertEquals(mock.getCreateDate(), entityDB.get().getCreateDate());
        assertEquals(mock.getNum(), entityDB.get().getNum());
        assertEquals(mock.getCod(), entityDB.get().getCod());
        assertNotNull(entityDB.get().getStatus());
        assertEquals(mock.getStatus(), entityDB.get().getStatus());

        if (mock.getLead() != null) {
            assertNotNull(entityDB.get().getLead());
            assertEquals(mock.getLead(), entityDB.get().getLead());
        }
    }

    @Test
    @Order(5)
    @DisplayName("delete - Quando deletado não pode mais existir")
    void givenExistedProposal_whenDelete_ThenNoFind() throws Exception {
        int id = 2;

        dao.delete(id);

        Optional<Proposal> entityDB = dao.getById(id);

        assertNotNull(entityDB);
        assertFalse(entityDB.isPresent());
    }

    private static Stream<Arguments> listEntityToSave() {
        return Stream.of(
            Arguments.of(new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
            Arguments.of(new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2022, 01, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.COMMERCIAL_DISAPPROVED, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null, null, null, ProposalRisk.NORMAL, null, false, null, null),
            Arguments.of(new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2022, 02, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.FINISHED_WITH_SALE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
            Arguments.of(new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2022, 03, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.CANCELED, null, leadMock, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null, null, null, ProposalRisk.NORMAL, null, false, null, null))));
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> listEntityToUpdate() {
        return Stream.of(
            Arguments.of(new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.CANCELED, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
            Arguments.of(new Proposal(2, 2l, "B12208A", "A", null, LocalDateTime.of(2022, 01, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.COMMERCIAL_APPROVED, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
            Arguments.of(new Proposal(3, 3l, "B12208A", "A", null, LocalDateTime.of(2022, 02, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_COMMERCIAL_APPROVAL, null, leadMock, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
            Arguments.of(new Proposal(4, 4l, "B12208A", "A", null, LocalDateTime.of(2022, 04, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.COMMERCIAL_APPROVED, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null))
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> listEntityToFind() {
        return Stream.of(
            Arguments.of(new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().id(1).build(), 1),
            Arguments.of(new Proposal(2, 2l, "B12208A", "A", null, LocalDateTime.of(2022, 01, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.COMMERCIAL_DISAPPROVED, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().num(2l).build(), 1),
            Arguments.of(new Proposal(2, 2l, "B12208A", "A", null, LocalDateTime.of(2022, 01, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.COMMERCIAL_DISAPPROVED, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().cod("A").build(), 4),
            Arguments.of(new Proposal(2, 2l, "B12208A", "A", null, LocalDateTime.of(2022, 01, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.COMMERCIAL_DISAPPROVED, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().num(2l).cod("A").build(), 1),
            Arguments.of(new Proposal(3, 3l, "B12208A", "A", null, LocalDateTime.of(2022, 02, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.FINISHED_WITH_SALE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().status(ProposalState.FINISHED_WITH_SALE).build(), 1),
            Arguments.of(new Proposal(4, 4l, "B12208A", "A", null, LocalDateTime.of(2022, 03, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.CANCELED, null, leadMock, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().createDate(LocalDateTime.of(2022, 03, 15, 00, 00, 00, 00)).build(), 1),
            Arguments.of(new Proposal(4, 4l, "B12208A", "A", null, LocalDateTime.of(2022, 03, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.CANCELED, null, leadMock, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().lead(leadMock).build(), 1)
        );
    }

}
