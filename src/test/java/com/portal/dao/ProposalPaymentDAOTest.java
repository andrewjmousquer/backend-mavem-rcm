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
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.portal.dao.impl.ProposalPaymentDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.PersonClassification;
import com.portal.enums.ProposalRisk;
import com.portal.enums.ProposalState;
import com.portal.model.PaymentMethod;
import com.portal.model.PaymentRule;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.ProposalDetail;
import com.portal.model.ProposalPayment;
import com.portal.model.Seller;
import com.portal.model.UserModel;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.PaymentMethodService;
import com.portal.service.imp.ProposalDetailService;
import com.portal.service.imp.ProposalService;
import com.portal.service.imp.SellerService;

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
class ProposalPaymentDAOTest {

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
    private ProposalPaymentDAO dao;

    @Autowired
    private ClassifierService classifierService;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private ProposalDetailService proposalDetailService;

    @Autowired
    private ProposalService proposalService;

    @Autowired
    private SellerService sellerService;

    private static PaymentMethod paymentMethodMock1 = new PaymentMethod(null, "PYM 1", true);
    private static PaymentRule paymentRuleMock1 = new PaymentRule(null, null, null, paymentMethodMock1, null, true, true);
    private static PaymentMethod paymentMethodMock2 = new PaymentMethod(null, "PYM 2", true);
    private static final Proposal proposalMock = new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2022, 03, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);
    private static final Seller sellerMock = Seller.builder().person(new Person(null, "Person Seller", "Seller", "00000000002", null, null, null, null, PersonClassification.PF.getType())).build();
    private static final ProposalDetail proposalDetailMock = new ProposalDetail(null, proposalMock, sellerMock, null, null, null, new UserModel(1), null, null, null);

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

        Optional<PaymentMethod> dbPym1 = paymentMethodService.save(paymentMethodMock1, profile);
        paymentMethodMock1.setId(dbPym1.get().getId());

        Optional<PaymentMethod> dbPym2 = paymentMethodService.save(paymentMethodMock2, profile);
        paymentMethodMock2.setId(dbPym2.get().getId());

        Optional<Proposal> dbProposal = this.proposalService.save(proposalMock, profile);
        proposalMock.setId(dbProposal.get().getId());

        Optional<Seller> dbSeller = this.sellerService.save(sellerMock, profile);
        sellerMock.setId(dbSeller.get().getId());

        Optional<ProposalDetail> dbProposalDetail = this.proposalDetailService.save(proposalDetailMock, profile);
        proposalDetailMock.setId(dbProposalDetail.get().getId());
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalPaymentDAOTest#listEntityToSave")
    @DisplayName("Quando salvar retornar os IDs salvos")
    void whenSave_ReturnSavedId(ProposalPayment entity) throws Exception {
        Optional<ProposalPayment> entityId = dao.save(entity);

        assertNotNull(entityId);
        assertTrue(entityId.isPresent());
        assertEquals(entity.getId(), entityId.get().getId());
    }

    @Order(3)
    @DisplayName("listAll - Quando listar todos as fontes")
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalPaymentDAOTest#whenListAllthenReturnEntityList")
    void whenListAll_ReturnListPaymentDetail(int page, int size, String sortDir, String sort, int validId) throws Exception {

        PageRequest pageReq = PageRequest.of(page, size, Direction.fromString(sortDir), sort);
        List<ProposalPayment> list = dao.listAll(pageReq);

        assertNotNull(list);
        assertEquals(size, list.size());

        ProposalPayment dbPaymentDetail = list.get(0);

        assertNotNull(dbPaymentDetail);
        assertEquals(validId, dbPaymentDetail.getId());
    }

    @Test
    @Order(3)
    @DisplayName("getById - Dado um ID existente retornar o detalhe do pagamento")
    void givenExistedPaymentDetail_whenGetById_ThenReturnPaymentDetail() throws Exception {
        ProposalPayment mock = new ProposalPayment(null, null, null, null, null, paymentMethodMock1, paymentRuleMock1, proposalDetailMock, null, null, null, null, null, null, null);

        Optional<ProposalPayment> entityDB = dao.getById(mock.getId());

        assertNotNull(entityDB);
        assertTrue(entityDB.isPresent());
        assertEquals(mock, entityDB.get());
        assertEquals(mock.getId(), entityDB.get().getId());
        assertEquals(mock.getPaymentAmount(), entityDB.get().getPaymentAmount());
        assertEquals(mock.getDueDate(), entityDB.get().getDueDate());
        assertEquals(mock.getInstallmentAmount(), entityDB.get().getInstallmentAmount());
        assertNotNull(entityDB.get().getPaymentMethod());
        assertEquals(mock.getPaymentMethod(), entityDB.get().getPaymentMethod());
        assertNotNull(entityDB.get().getProposalDetail());
        assertEquals(mock.getProposalDetail(), entityDB.get().getProposalDetail());
    }

    @Order(3)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalPaymentDAOTest#listEntityToFind")
    @DisplayName("Quando busca retornar os IDs salvos")
    void whenFind_ReturnProposal(ProposalPayment mock, ProposalPayment filter, int expectedSize) throws Exception {
        List<ProposalPayment> entityDB = dao.find(filter, null);

        assertNotNull(entityDB);
        assertFalse(entityDB.isEmpty());
        assertEquals(expectedSize, entityDB.size());

        Optional<ProposalPayment> entity = entityDB.stream().filter(item -> item.getId().equals(mock.getId())).findFirst();

        assertTrue(entity.isPresent());
        assertEquals(mock, entity.get());
        assertEquals(mock.getId(), entity.get().getId());
        assertEquals(mock.getPaymentAmount(), entity.get().getPaymentAmount());
        assertEquals(mock.getDueDate(), entity.get().getDueDate());
        assertEquals(mock.getInstallmentAmount(), mock.getInstallmentAmount());
        assertNotNull(entity.get().getPaymentMethod());
        assertEquals(mock.getPaymentMethod(), entity.get().getPaymentMethod());
        assertNotNull(entity.get().getProposalDetail());
        assertEquals(mock.getProposalDetail(), entity.get().getProposalDetail());
    }

    @Order(4)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalPaymentDAOTest#listEntityToUpdate")
    @DisplayName("update - Quando atualizar retorna o novo objeto")
    void whenUpdate_CheckNewValues(ProposalPayment mock) throws Exception {
        dao.update(mock);

        Optional<ProposalPayment> entityDB = dao.getById(mock.getId());

        assertNotNull(entityDB);
        assertTrue(entityDB.isPresent());
        assertEquals(mock, entityDB.get());
        assertEquals(mock.getId(), entityDB.get().getId());
        assertEquals(mock.getPaymentAmount(), entityDB.get().getPaymentAmount());
        assertEquals(mock.getDueDate(), entityDB.get().getDueDate());
        assertEquals(mock.getInstallmentAmount(), mock.getInstallmentAmount());
        assertNotNull(entityDB.get().getPaymentMethod());
        assertEquals(mock.getPaymentMethod(), entityDB.get().getPaymentMethod());
        assertNotNull(entityDB.get().getProposalDetail());
        assertEquals(mock.getProposalDetail(), entityDB.get().getProposalDetail());
    }

    @Test
    @Order(5)
    @DisplayName("delete - Quando deletado não pode mais existir")
    void givenExistedPaymentDetail_whenDelete_ThenNoFind() throws Exception {
        int id = 2;

        dao.delete(id);

        Optional<ProposalPayment> entityDB = dao.getById(id);

        assertNotNull(entityDB);
        assertFalse(entityDB.isPresent());
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> whenListAllthenReturnEntityList() {
        return Stream.of(
                Arguments.of(0, 2, "DESC", "ppy_id", 2),
                Arguments.of(0, 1, "DESC", "ppy_id", 2),
                Arguments.of(0, 1, "DESC", "ppd_id", 1),
                Arguments.of(0, 1, "DESC", "pym_id", 2),
                Arguments.of(1, 1, "DESC", "ppy_id", 1),
                Arguments.of(0, 1, "ASC", "ppy_id", 1)
        );
    }


}
