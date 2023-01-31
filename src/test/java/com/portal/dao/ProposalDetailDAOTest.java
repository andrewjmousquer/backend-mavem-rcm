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

import com.portal.dao.impl.ProposalDetailDAO;
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
import com.portal.model.ProposalDetail;
import com.portal.model.Seller;
import com.portal.model.Source;
import com.portal.model.UserModel;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.LeadService;
import com.portal.service.imp.ModelService;
import com.portal.service.imp.PersonService;
import com.portal.service.imp.ProposalService;
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
class ProposalDetailDAOTest {

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
    private ProposalDetailDAO dao;

    @Autowired
    private ProposalService proposalService;

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
    private static final Seller sellerMock2 = Seller.builder().person(new Person(null, "Person Seller 2", "Seller 2", "00000000003", null, null, null, null, PersonClassification.PF.getType())).build();
    private static final Seller internSellerMock = Seller.builder().person(new Person(null, "Person Intern Sela", "Intern Sale", "00000000004", null, null, null, null, PersonClassification.PF.getType())).build();
    private static final Source sourceMock = new Source(null, "Source 1", true);
    private static final Brand brandMock = new Brand(null, "Brand 1", true);
    private static final Model modelMock = new Model(null, "Model 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM);
    private static final Lead leadMock = Lead.builder().id(null).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00))
            .client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType())
            .model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).description("NOTES 1").subject("").build();

    private static final Proposal proposalMock1 = new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2022, 03, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, leadMock, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);
    private static final Proposal proposalMock2 = new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2021, 02, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.FINISHED_WITH_SALE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);

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

        Optional<Seller> dbSeller1 = this.sellerService.save(sellerMock, profile);
        sellerMock.setId(dbSeller1.get().getId());

        Optional<Seller> dbSeller2 = this.sellerService.save(internSellerMock, profile);
        internSellerMock.setId(dbSeller2.get().getId());

        Optional<Seller> dbSeller3 = this.sellerService.save(sellerMock2, profile);
        sellerMock2.setId(dbSeller3.get().getId());

        Optional<Source> dbSource = this.sourceService.save(sourceMock, profile);
        sourceMock.setId(dbSource.get().getId());

        Optional<Brand> dbBrand = this.brandService.save(brandMock, profile);
        brandMock.setId(dbBrand.get().getId());

        Optional<Model> dbModel = this.modelService.save(modelMock, profile);
        modelMock.setId(dbModel.get().getId());

        Optional<Lead> dbLead = this.leadService.save(leadMock, profile);
        leadMock.setId(dbLead.get().getId());

        Optional<Proposal> dbProposal1 = this.proposalService.save(proposalMock1, profile);
        proposalMock1.setId(dbProposal1.get().getId());

        Optional<Proposal> dbProposal2 = this.proposalService.save(proposalMock2, profile);
        proposalMock2.setId(dbProposal2.get().getId());
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalDetailDAOTest#listEntityToSave")
    @DisplayName("Quando salvar retornar os IDs salvos")
    void whenSave_ReturnSavedId(ProposalDetail entity) throws Exception {
        Optional<ProposalDetail> entityId = dao.save(entity);
        assertNotNull(entityId);
        assertTrue(entityId.isPresent());
        assertEquals(entity.getId(), entityId.get().getId());
    }

    @Test
    @Order(3)
    @DisplayName("listAll - Quando listar todos as leads")
    void whenListAll_ReturnListProposal() throws Exception {
        PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "pps_id");
        List<ProposalDetail> list = dao.listAll(pageReq);

        assertEquals(listEntityToSave().count(), list.size());
    }

    @Test
    @Order(3)
    @DisplayName("getById - Dado um ID existente retornar a proposta")
    void givenExistedProposal_whenGetById_ThenReturnProposal() throws Exception {
        ProposalDetail mock = new ProposalDetail(2, proposalMock2, sellerMock, internSellerMock, null, null, null, null, null, null);

        Optional<ProposalDetail> entityDB = dao.getById(mock.getId());

        assertNotNull(entityDB);
        assertTrue(entityDB.isPresent());
        assertEquals(mock, entityDB.get());
        assertNotNull(entityDB.get().getProposal());
        assertEquals(mock.getProposal(), entityDB.get().getProposal());
        assertNotNull(entityDB.get().getSeller());
        assertEquals(mock.getSeller(), entityDB.get().getSeller());
        assertEquals(mock.getSeller().getPerson().getName(), entityDB.get().getSeller().getPerson().getName());
        assertNotNull(entityDB.get().getInternSale());
        assertEquals(mock.getInternSale(), entityDB.get().getInternSale());
    }

    @Order(3)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalDetailDAOTest#listEntityToFind")
    @DisplayName("Quando busca retornar os IDs salvos")
    void whenFind_ReturnProposal(ProposalDetail mock, ProposalDetail filter, int expectedSize) throws Exception {
        List<ProposalDetail> entityDB = dao.find(filter, null);

        assertNotNull(entityDB);
        assertFalse(entityDB.isEmpty());
        assertEquals(expectedSize, entityDB.size());

        Optional<ProposalDetail> entity = entityDB.stream().filter(item -> item.getId().equals(mock.getId())).findFirst();

        assertEquals(mock, entity.get());
        assertNotNull(entity.get().getProposal());
        assertEquals(mock.getProposal(), entity.get().getProposal());
        assertNotNull(entity.get().getSeller());
        assertEquals(mock.getSeller(), entity.get().getSeller());
        assertEquals(mock.getSeller().getPerson().getName(), entity.get().getSeller().getPerson().getName());

        if (mock.getInternSale() != null) {
            assertEquals(mock.getInternSale(), entity.get().getInternSale());
        }
    }

    @Order(4)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalDetailDAOTest#listEntityToUpdate")
    @DisplayName("update - Quando atualizar retorna o novo objeto")
    void whenUpdate_CheckNewValues(ProposalDetail mock) throws Exception {
        dao.update(mock);

        Optional<ProposalDetail> entityDB = dao.getById(mock.getId());

        assertNotNull(entityDB);
        assertTrue(entityDB.isPresent());
        assertEquals(mock, entityDB.get());
        assertNotNull(entityDB.get().getProposal());
        assertEquals(mock.getProposal(), entityDB.get().getProposal());
        assertNotNull(entityDB.get().getSeller());
        assertEquals(mock.getSeller(), entityDB.get().getSeller());
        assertEquals(mock.getSeller().getPerson().getName(), entityDB.get().getSeller().getPerson().getName());

        if (mock.getInternSale() != null) {
            assertNotNull(entityDB.get().getInternSale());
            assertEquals(mock.getInternSale(), entityDB.get().getInternSale());
        }
    }

    @Test
    @Order(5)
    @DisplayName("delete - Quando deletado não pode mais existir")
    void givenExistedProposal_whenDelete_ThenNoFind() throws Exception {
        int id = 2;

        dao.delete(id);

        Optional<ProposalDetail> entityDB = dao.getById(id);

        assertNotNull(entityDB);
        assertFalse(entityDB.isPresent());
    }

    private static Stream<Arguments> listEntityToSave() {
        return Stream.of(
                Arguments.of(new ProposalDetail(null, proposalMock1, sellerMock, null, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetail(null, proposalMock2, sellerMock, internSellerMock, null, null, null, null, null, null))
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> listEntityToUpdate() {
        return Stream.of(
                Arguments.of(new ProposalDetail(1, proposalMock1, sellerMock, internSellerMock, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetail(2, proposalMock2, sellerMock2, null, null, null, null, null, null, null))
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> listEntityToFind() {
        return Stream.of(
                Arguments.of(new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null), ProposalDetail.builder().id(1).build(), 1),
                Arguments.of(new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null), ProposalDetail.builder().proposal(proposalMock1).build(), 1),
                Arguments.of(new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null), ProposalDetail.builder().seller(sellerMock).build(), 2),
                Arguments.of(new ProposalDetail(2, proposalMock2, sellerMock, internSellerMock, null, null, null, null, null, null), ProposalDetail.builder().internSale(internSellerMock).build(), 1),
                Arguments.of(new ProposalDetail(2, proposalMock2, sellerMock, internSellerMock, null, null, null, null, null, null), new ProposalDetail(2, proposalMock2, sellerMock, internSellerMock, null, null, null, null, null, null), 1)
        );
    }

}
