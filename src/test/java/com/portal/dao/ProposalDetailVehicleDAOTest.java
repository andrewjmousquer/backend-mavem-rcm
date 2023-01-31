package com.portal.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.portal.dao.impl.ProposalDetailVehicleDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.LeadState;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.enums.PersonClassification;
import com.portal.enums.ProposalRisk;
import com.portal.enums.ProposalState;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Model;
import com.portal.model.Person;
import com.portal.model.PriceList;
import com.portal.model.PriceProduct;
import com.portal.model.Product;
import com.portal.model.ProductModel;
import com.portal.model.Proposal;
import com.portal.model.ProposalDetail;
import com.portal.model.ProposalDetailVehicle;
import com.portal.model.Seller;
import com.portal.model.UserModel;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ChannelService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.ModelService;
import com.portal.service.imp.PersonService;
import com.portal.service.imp.PriceListService;
import com.portal.service.imp.PriceProductService;
import com.portal.service.imp.ProductModelService;
import com.portal.service.imp.ProductService;
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
class ProposalDetailVehicleDAOTest {

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
    private ProposalDetailVehicleDAO dao;

    @Autowired
    private ProposalService proposalService;

    @Autowired
    private ClassifierService classifierService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PriceListService priceListService;

    @Autowired
    private ProductModelService productModelService;

    @Autowired
    private ProposalDetailService proposalDetailService;

    @Autowired
    private PriceProductService productPriceService;

    private static final Brand brandMock = new Brand(null, "Brand 1", true);
    private static final Model modelMock = new Model(null, "Model 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM);
    private static final Channel channelMock = new Channel(null, "Channel 1", true, true, true);
    private static final Product productMock = new Product(null, "PRODUCT 1", true, 10, null);
    private static final PriceList priceListMock1 = new PriceList(null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channelMock, false);
    private static final PriceList priceListMock2 = new PriceList(null, "PriceList 2", LocalDateTime.of(2021, 1, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 10, 31, 00, 00, 00, 00), channelMock, false);
    private static final ProductModel productModelMock = new ProductModel(null, false, 2000, 2015, 10, productMock, modelMock);
    private static final PriceProduct productPriceMock1 = new PriceProduct(null, 100d, priceListMock1, productModelMock);
    private static final PriceProduct productPriceMock2 = new PriceProduct(null, 120d, priceListMock2, productModelMock);
    private static final Proposal proposalMock1 = new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2022, 03, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);
    private static final Proposal proposalMock2 = new Proposal(null, null, "B12208B", "B", null, LocalDateTime.of(2022, 03, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);
    private static final Seller sellerMock = Seller.builder().person(new Person(null, "Person Seller", "Seller", "00000000002", null, null, null, null, PersonClassification.PF.getType())).build();
    private static final ProposalDetail proposalDetailMock1 = new ProposalDetail(null, proposalMock1, sellerMock, null, null, null, new UserModel(1), "", "", "");
    private static final ProposalDetail proposalDetailMock2 = new ProposalDetail(null, proposalMock2, sellerMock, null, null, null, new UserModel(1), "", "", "");

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

        Optional<Brand> dbBrand = this.brandService.save(brandMock, profile);
        brandMock.setId(dbBrand.get().getId());

        Optional<Model> dbModel = this.modelService.save(modelMock, profile);
        modelMock.setId(dbModel.get().getId());

        Optional<Channel> dbChannel = channelService.save(channelMock, profile);
        channelMock.setId(dbChannel.get().getId());

        Optional<Product> dbProduct = productService.save(productMock, profile);
        productMock.setId(dbProduct.get().getId());

        Optional<PriceList> dbPricelist1 = priceListService.save(priceListMock1, profile);
        priceListMock1.setId(dbPricelist1.get().getId());

        Optional<PriceList> dbPricelist2 = priceListService.save(priceListMock2, profile);
        priceListMock2.setId(dbPricelist2.get().getId());

        Optional<ProductModel> dbProductModel = productModelService.save(productModelMock, profile);
        productModelMock.setId(dbProductModel.get().getId());

        Optional<PriceProduct> dbProductPrice1 = productPriceService.save(productPriceMock1, profile);
        productPriceMock1.setId(dbProductPrice1.get().getId());

        Optional<PriceProduct> dbProductPrice2 = productPriceService.save(productPriceMock2, profile);
        productPriceMock2.setId(dbProductPrice2.get().getId());

        Optional<Proposal> dbProposal1 = this.proposalService.save(proposalMock1, profile);
        proposalMock1.setId(dbProposal1.get().getId());

        Optional<Proposal> dbProposal2 = this.proposalService.save(proposalMock2, profile);
        proposalMock2.setId(dbProposal2.get().getId());

        Optional<Seller> dbSeller = this.sellerService.save(sellerMock, profile);
        sellerMock.setId(dbSeller.get().getId());

        Optional<ProposalDetail> dbProposalDetail1 = this.proposalDetailService.save(proposalDetailMock1, profile);
        proposalDetailMock1.setId(dbProposalDetail1.get().getId());

        Optional<ProposalDetail> dbProposalDetail2 = this.proposalDetailService.save(proposalDetailMock2, profile);
        proposalDetailMock2.setId(dbProposalDetail2.get().getId());
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalDetailVehicleDAOTest#listEntityToSave")
    @DisplayName("Quando salvar retornar os IDs salvos")
    void whenSave_ReturnSavedId(ProposalDetailVehicle entity) throws Exception {
        Optional<ProposalDetailVehicle> entityId = dao.save(entity);
        assertNotNull(entityId);
        assertTrue(entityId.isPresent());
        assertEquals(entity.getId(), entityId.get().getId());
    }

    @Order(3)
    @DisplayName("listAll -  Quando listar todos os detalhes de veículos da proposta")
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalDetailVehicleDAOTest#whenListAllthenReturnEntityList")
    void whenListAll_ReturnListVehicleDetails(int page, int size, String sortDir, String sort, int validId) throws Exception {
        PageRequest pageReq = PageRequest.of(page, size, Direction.fromString(sortDir), sort);
        List<ProposalDetailVehicle> list = dao.listAll(pageReq);

        assertNotNull(list);
        assertEquals(size, list.size());

        ProposalDetailVehicle dbData = list.get(0);

        assertNotNull(dbData);
        assertEquals(validId, dbData.getId());
    }

    @Test
    @Order(3)
    @DisplayName("getById - Dado um ID existente retornar o detalhe de veículo da proposta")
    void givenExistedProposal_whenGetById_ThenReturnProposal() throws Exception {
        ProposalDetailVehicle mock = new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, 30, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 1d, 40, null, null);
        Optional<ProposalDetailVehicle> entityDB = dao.getById(mock.getId());

        assertNotNull(entityDB);
        assertTrue(entityDB.isPresent());
        assertEquals(mock, entityDB.get());
        assertNotNull(entityDB.get().getProposalDetail());
        assertEquals(mock.getProposalDetail(), entityDB.get().getProposalDetail());
        assertNotNull(entityDB.get().getPriceProduct());
        assertEquals(mock.getPriceProduct(), entityDB.get().getPriceProduct());

        if (mock.getVehicle() != null) {
            assertEquals(mock.getVehicle(), entityDB.get().getVehicle());
        }

        assertEquals(mock.getProductAmountDiscount(), entityDB.get().getProductAmountDiscount());
        assertEquals(mock.getProductPercentDiscount(), entityDB.get().getProductPercentDiscount());
        assertEquals(mock.getProductFinalPrice(), entityDB.get().getProductFinalPrice());
        assertEquals(mock.getOverPrice(), entityDB.get().getOverPrice());
        assertEquals(mock.getOverPricePartnerDiscountAmount(), entityDB.get().getOverPricePartnerDiscountAmount());
        assertEquals(mock.getOverPricePartnerDiscountPercent(), entityDB.get().getOverPricePartnerDiscountPercent());
        assertEquals(mock.getPriceDiscountAmount(), entityDB.get().getPriceDiscountAmount());
        assertEquals(mock.getPriceDiscountPercent(), entityDB.get().getPriceDiscountPercent());
        assertEquals(mock.getTotalAmount(), entityDB.get().getTotalAmount());
        assertEquals(mock.getTotalTaxAmount(), entityDB.get().getTotalTaxAmount());
        assertEquals(mock.getTotalTaxPercent(), entityDB.get().getTotalTaxPercent());
    }

    @Order(3)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalDetailVehicleDAOTest#listEntityToFind")
    @DisplayName("Quando busca retornar os IDs salvos")
    void whenFind_ReturnProposal(ProposalDetailVehicle mock, ProposalDetailVehicle filter, int expectedSize) throws Exception {
        List<ProposalDetailVehicle> entityDB = dao.find(filter, null);

        assertNotNull(entityDB);
        assertFalse(entityDB.isEmpty());
        assertEquals(expectedSize, entityDB.size());

        Optional<ProposalDetailVehicle> entity = entityDB.stream().filter(item -> item.getId().equals(mock.getId())).findFirst();

        assertTrue(entity.isPresent());
        assertEquals(mock, entity.get());
        assertNotNull(entity.get().getProposalDetail());
        assertEquals(mock.getProposalDetail(), entity.get().getProposalDetail());
        assertNotNull(entity.get().getPriceProduct());
        assertEquals(mock.getPriceProduct(), entity.get().getPriceProduct());

        if (mock.getVehicle() != null) {
            assertEquals(mock.getVehicle(), entity.get().getVehicle());
        }

        assertEquals(mock.getProductAmountDiscount(), entity.get().getProductAmountDiscount());
        assertEquals(mock.getProductPercentDiscount(), entity.get().getProductPercentDiscount());
        assertEquals(mock.getProductFinalPrice(), entity.get().getProductFinalPrice());
        assertEquals(mock.getOverPrice(), entity.get().getOverPrice());
        assertEquals(mock.getOverPricePartnerDiscountAmount(), entity.get().getOverPricePartnerDiscountAmount());
        assertEquals(mock.getOverPricePartnerDiscountPercent(), entity.get().getOverPricePartnerDiscountPercent());
        assertEquals(mock.getPriceDiscountAmount(), entity.get().getPriceDiscountAmount());
        assertEquals(mock.getPriceDiscountPercent(), entity.get().getPriceDiscountPercent());
        assertEquals(mock.getTotalAmount(), entity.get().getTotalAmount());
        assertEquals(mock.getTotalTaxAmount(), entity.get().getTotalTaxAmount());
        assertEquals(mock.getTotalTaxPercent(), entity.get().getTotalTaxPercent());
    }

    @Order(4)
    @ParameterizedTest
    @MethodSource("com.portal.dao.ProposalDetailVehicleDAOTest#listEntityToUpdate")
    @DisplayName("update - Quando atualizar retorna o novo objeto")
    void whenUpdate_CheckNewValues(ProposalDetailVehicle mock) throws Exception {
        dao.update(mock);

        Optional<ProposalDetailVehicle> entityDB = dao.getById(mock.getId());

        assertNotNull(entityDB);
        assertTrue(entityDB.isPresent());
        assertEquals(mock, entityDB.get());
        assertNotNull(entityDB.get().getProposalDetail());
        assertEquals(mock.getProposalDetail(), entityDB.get().getProposalDetail());
        assertNotNull(entityDB.get().getPriceProduct());
        assertEquals(mock.getPriceProduct(), entityDB.get().getPriceProduct());

        if (mock.getVehicle() != null) {
            assertEquals(mock.getVehicle(), entityDB.get().getVehicle());
        }

        assertEquals(mock.getProductAmountDiscount(), entityDB.get().getProductAmountDiscount());
        assertEquals(mock.getProductPercentDiscount(), entityDB.get().getProductPercentDiscount());
        assertEquals(mock.getProductFinalPrice(), entityDB.get().getProductFinalPrice());
        assertEquals(mock.getOverPrice(), entityDB.get().getOverPrice());
        assertEquals(mock.getOverPricePartnerDiscountAmount(), entityDB.get().getOverPricePartnerDiscountAmount());
        assertEquals(mock.getOverPricePartnerDiscountPercent(), entityDB.get().getOverPricePartnerDiscountPercent());
        assertEquals(mock.getPriceDiscountAmount(), entityDB.get().getPriceDiscountAmount());
        assertEquals(mock.getPriceDiscountPercent(), entityDB.get().getPriceDiscountPercent());
        assertEquals(mock.getTotalAmount(), entityDB.get().getTotalAmount());
        assertEquals(mock.getTotalTaxAmount(), entityDB.get().getTotalTaxAmount());
        assertEquals(mock.getTotalTaxPercent(), entityDB.get().getTotalTaxPercent());
    }

    @Test
    @Order(5)
    @DisplayName("delete - Quando deletado não pode mais existir")
    void givenExistedProposal_whenDelete_ThenNoFind() throws Exception {
        int id = 2;

        dao.delete(id);

        Optional<ProposalDetailVehicle> entityDB = dao.getById(id);

        assertNotNull(entityDB);
        assertFalse(entityDB.isPresent());
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> listEntityToSave() {
        return Stream.of(
                Arguments.of(new ProposalDetailVehicle(null, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, proposalDetailMock2, productPriceMock1, null, null, null, null, 10d, 11d, 12d, 13d, 14d, 15d, 16d, 17d, 18d, 19d, 20d, 30, 40, null))
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> listEntityToUpdate() {
        return Stream.of(
                Arguments.of(new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 11d, 22d, 33d, 44d, 55d, 66d, 77d, 88d, 99d, 110d, 111d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(2, proposalDetailMock2, productPriceMock2, null, null, null, null, 110d, 111d, 12d, 13d, 14d, 15d, 16d, 17d, 18d, 19d, 20d, 30, 40, null))
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> listEntityToFind() {
        return Stream.of(
                Arguments.of(new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null), ProposalDetailVehicle.builder().id(1).build(), 1),
                Arguments.of(new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null), ProposalDetailVehicle.builder().proposalDetail(proposalDetailMock1).build(), 1),
                Arguments.of(new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null), ProposalDetailVehicle.builder().priceProduct(productPriceMock1).build(), 2)
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> whenListAllthenReturnEntityList() {
        return Stream.of(
                Arguments.of(0, 2, "DESC", "pdv_id", 2),
                Arguments.of(0, 1, "DESC", "pdv_id", 2),
                Arguments.of(0, 1, "DESC", "ppd_id", 2),
                Arguments.of(0, 1, "DESC", "ppr_id", 1),
                Arguments.of(0, 1, "DESC", "product_amount_discount", 2),
                Arguments.of(0, 1, "ASC", "product_amount_discount", 1),
                Arguments.of(0, 1, "DESC", "product_percent_discount", 2),
                Arguments.of(0, 1, "ASC", "product_percent_discount", 1),
                Arguments.of(0, 1, "DESC", "product_final_price", 2),
                Arguments.of(0, 1, "ASC", "product_final_price", 1),
                Arguments.of(0, 1, "DESC", "over_price", 2),
                Arguments.of(0, 1, "ASC", "over_price", 1),
                Arguments.of(0, 1, "DESC", "over_price_partner_discount_amount", 2),
                Arguments.of(0, 1, "ASC", "over_price_partner_discount_amount", 1),
                Arguments.of(0, 1, "DESC", "over_price_partner_discount_percent", 2),
                Arguments.of(0, 1, "ASC", "over_price_partner_discount_percent", 1),
                Arguments.of(0, 1, "DESC", "price_discount_amount", 2),
                Arguments.of(0, 1, "ASC", "price_discount_amount", 1),
                Arguments.of(0, 1, "DESC", "price_discount_percent", 2),
                Arguments.of(0, 1, "ASC", "price_discount_percent", 1),
                Arguments.of(0, 1, "DESC", "total_amount", 2),
                Arguments.of(0, 1, "ASC", "total_amount", 1),
                Arguments.of(0, 1, "DESC", "total_tax_amount", 2),
                Arguments.of(0, 1, "ASC", "total_tax_amount", 1),
                Arguments.of(0, 1, "DESC", "total_tax_percent", 2),
                Arguments.of(0, 1, "ASC", "total_tax_percent", 1),
                Arguments.of(1, 1, "DESC", "pdv_id", 1),
                Arguments.of(0, 1, "ASC", "pdv_id", 1)
        );
    }

}
