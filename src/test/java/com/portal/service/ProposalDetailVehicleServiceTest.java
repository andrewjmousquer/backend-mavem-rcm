package com.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import org.mockito.Spy;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.impl.ProposalDetailVehicleDAO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.enums.PersonClassification;
import com.portal.enums.ProposalRisk;
import com.portal.enums.ProposalState;
import com.portal.exceptions.BusException;
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
import com.portal.service.imp.AuditService;
import com.portal.service.imp.PriceProductService;
import com.portal.service.imp.ProposalDetailService;
import com.portal.service.imp.ProposalDetailVehicleService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)
public class ProposalDetailVehicleServiceTest {

    @Mock
    ProposalDetailVehicleDAO dao;

    @Mock
    ProposalDetailService proposalDetailService;

    @Mock
    PriceProductService productPriceService;

    @Mock
    AuditService auditService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    Validator validator;

    @Mock
    MessageSource messageSource;

    @Spy
    ProposalDetailVehicleService serviceInternal;

    @InjectMocks
    ProposalDetailVehicleService service;

    private static final Brand brandMock = new Brand(1, "Brand 1", true);
    private static final Model modelMock = new Model(1, "Model 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM);
    private static final Channel channelMock = new Channel(1, "Channel 1", true, true, true);
    private static final Product productMock = new Product(1, "PRODUCT 1", true, 10, null);
    private static final PriceList priceListMock1 = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channelMock, false);
    private static final ProductModel productModelMock = new ProductModel(1, false, 2000, 2015, 10, productMock, modelMock);
    private static final PriceProduct productPriceMock1 = new PriceProduct(1, 100d, priceListMock1, productModelMock);
    private static final Proposal proposalMock1 = new Proposal(1, null, "B12208A", "A", null, LocalDateTime.of(2022, 03, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);
    private static final Seller sellerMock = Seller.builder().person(new Person(1, "Person Seller", "Seller", "00000000002", null, null, null, null, PersonClassification.PF.getType())).build();
    private static final ProposalDetail proposalDetailMock1 = new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, new UserModel(1), null, null, null);

    @Nested
    class ListAll {
        @DisplayName("Listar os detalhes do veículo da propostas e retornar com sucesso a lista")
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalDetailVehicleServiceTest#whenListAllthenReturnEntityList")
        void whenListAll_thenReturnProposalList(int page, int size, String sortDir, String sort) throws Exception {
            when(dao.listAll(any())).thenReturn(Arrays.asList(new ProposalDetailVehicle()));

            List<ProposalDetailVehicle> list = service.listAll(PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "pdv_id"));
            assertFalse(list.isEmpty());
        }

        @DisplayName("Dada a paginação nula então retorna a lista")
        @Test
        void givenNullPagination_whenListAll_thenReturnProposalList() throws Exception {
            when(dao.listAll(any())).thenReturn(Arrays.asList(new ProposalDetailVehicle()));

            List<ProposalDetailVehicle> list = service.listAll(null);
            assertFalse(list.isEmpty());
        }
    }

    @Nested
    class Save {
        @Test
        @DisplayName("Dada um detalhe de veículo da proposta válido quando salvar retornar o objeto com o novo ID")
        void givenValidProposalDetailVehicle_whenSave_thenReturnId() throws Exception {
            ProposalDetailVehicle model = new ProposalDetailVehicle(null, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null);
            ProposalDetailVehicle mock = new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(proposalDetailService.getById(anyInt())).thenReturn(Optional.of(proposalDetailMock1));
            when(productPriceService.getById(anyInt())).thenReturn(Optional.of(productPriceMock1));
            when(dao.save(any())).thenReturn(Optional.of(mock));

            Optional<ProposalDetailVehicle> entityDB = service.save(model, null);

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
        @DisplayName("Dada uma proposta duplicada quando salvar deve dar erro")
        void givenDuplicateProposalDetailVehicle_whenSave_thenReturnError() throws Exception {
            ProposalDetailVehicle mockDuplicate = new ProposalDetailVehicle(2, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null);

            when(dao.find(any(), any())).thenReturn(Arrays.asList(mockDuplicate));

            BusException e = assertThrows(BusException.class, () -> service.save(new ProposalDetailVehicle(), null));
            assertEquals("Já existe um veículo para essa proposta.", e.getMessage());
        }

        @Test
        @DisplayName("Dada um registro com o detalhe da proposta não existente quando salvar deve dar erro")
        void givenProposalDetailVehicleWithInvalidProposalDetail_whenSave_thenReturnError() throws Exception {
            ProposalDetailVehicle mock = new ProposalDetailVehicle(null, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(proposalDetailService.getById(anyInt())).thenReturn(Optional.empty());

            BusException e = assertThrows(BusException.class, () -> service.save(mock, null));
            assertEquals("Não é possível salvar o detalhe do veículo com o detalhe da proposta não existente.", e.getMessage());
        }

        @Test
        @DisplayName("Dada um registro com o produto não existente quando salvar deve dar erro")
        void givenProposalDetailVehicleWithInvalidProductPrice_whenSave_thenReturnError() throws Exception {
            ProposalDetailVehicle mock = new ProposalDetailVehicle(null, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(proposalDetailService.getById(anyInt())).thenReturn(Optional.of(proposalDetailMock1));
            when(productPriceService.getById(anyInt())).thenReturn(Optional.empty());

            BusException e = assertThrows(BusException.class, () -> service.save(mock, null));
            assertEquals("Não é possível salvar o detalhe do veículo da proposta com o produto não existente.", e.getMessage());
        }

        @DisplayName("Dado dados inválidos da entidade aplicamos as validaçoes e deve retornar erro")
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalDetailVehicleServiceTest#invalidEntityDataToSaveValidator")
        void givenInvalidProposalDetailVehicle_whenSave_thenTestValidador(ProposalDetailVehicle model) throws Exception {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<ProposalDetailVehicle>> violationSet = validator.validate(model, OnSave.class);

            assertFalse(violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim");
        }
    }

    @Nested
    class Update {
        @Test
        @DisplayName("Dada o detalhe de um veículo quando atualiza retorna com a atualização")
        void givenProposalDetailVehicle_whenUpdate_thenReturnNewProposal() throws Exception {
            ProposalDetailVehicle model = new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null);
            ProposalDetailVehicle mock = new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 110d, 111d, 12d, 13d, 14d, 15d, 16d, 17d, 18d, 19d, 20d, 30, 40, null);

            when(dao.getById(any())).thenReturn(Optional.of(model));
            when(dao.find(any(), any())).thenReturn(null);
            when(proposalDetailService.getById(anyInt())).thenReturn(Optional.of(proposalDetailMock1));
            when(productPriceService.getById(anyInt())).thenReturn(Optional.of(productPriceMock1));
            when(dao.update(any())).thenReturn(Optional.of(mock));

            Optional<ProposalDetailVehicle> entityDB = service.update(model, null);

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
        @DisplayName("Dada o detalhe de um veículo duplicado quando atualizar deve dar erro")
        void givenDuplicateProposalDetailVehicle_whenUpdate_thenReturnError() throws Exception {
            ProposalDetailVehicle mockDuplicate = new ProposalDetailVehicle(2, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null);

            when(dao.getById(any())).thenReturn(Optional.of(new ProposalDetailVehicle()));
            when(dao.find(any(), any())).thenReturn(Arrays.asList(mockDuplicate));

            BusException e = assertThrows(BusException.class, () -> service.update(new ProposalDetailVehicle(), null));
            assertEquals("Já existe um veículo para essa proposta.", e.getMessage());
        }

        @Test
        @DisplayName("Dada um registro com o vendedor não existente quando atualizar deve dar erro")
        void givenProposalDetailVehicleWithInvalidProposalDetail_whenUpdate_thenReturnError() throws Exception {
            ProposalDetailVehicle mock = new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null);

            when(dao.getById(any())).thenReturn(Optional.of(new ProposalDetailVehicle()));
            when(dao.find(any(), any())).thenReturn(null);
            when(proposalDetailService.getById(anyInt())).thenReturn(Optional.empty());

            BusException e = assertThrows(BusException.class, () -> service.update(mock, null));
            assertEquals("Não é possível salvar o detalhe do veículo com o detalhe da proposta não existente.", e.getMessage());
        }

        @Test
        @DisplayName("Dada um registro com a proposta não existente quando atualizar deve dar erro")
        void givenProposalDetailVehicleWithInvalidProductPrice_whenUpdate_thenReturnError() throws Exception {
            ProposalDetailVehicle mock = new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(proposalDetailService.getById(anyInt())).thenReturn(Optional.of(proposalDetailMock1));
            when(productPriceService.getById(anyInt())).thenReturn(Optional.empty());

            BusException e = assertThrows(BusException.class, () -> service.update(mock, null));
            assertEquals("Não é possível salvar o detalhe do veículo da proposta com o produto não existente.", e.getMessage());
        }

        @DisplayName("Dado dados inválidos da entidade aplicamos as validaçoes e deve retornar erro")
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalDetailVehicleServiceTest#invalidEntityDataToUpdateValidator")
        void givenInvalidProposalDetailVehicle_whenUpdate_thenTestValidador(ProposalDetailVehicle model) throws Exception {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<ProposalDetailVehicle>> violationSet = validator.validate(model, OnUpdate.class);

            assertFalse(violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim");
        }
    }

    @Nested
    class Delete {
        @Test
        @DisplayName("Dado um ID válido do detalhe do veículo da proposta quando deleta não da erro")
        void givenValidId_whenDelete_thenNoError() throws Exception {
            when(dao.hasVehicleItemRelationship(any())).thenReturn(false);
            when(dao.getById(any())).thenReturn(Optional.ofNullable(new ProposalDetailVehicle()));
            assertDoesNotThrow(() -> service.delete(1, null));
        }

        @Test
        @DisplayName("Dado um ID inválido do detalhe do veículo da proposta quando deleta da erro")
        void givenInvalidId_whenDelete_thenError() throws Exception {
            BusException e = assertThrows(BusException.class, () -> service.delete(null, null));
            assertEquals("ID de exclusão inválido.", e.getMessage());
        }

        @Test
        @DisplayName("Dado um ID do detalhe do veículo da proposta que não exista quando deleta da erro")
        void givenNoExistedProposalDetailVehicle_whenDelete_thenError() throws Exception {
            when(dao.getById(any())).thenReturn(Optional.empty());
            BusException e = assertThrows(BusException.class, () -> service.delete(999, null));
            assertEquals("O detalhe da proposta a ser excluída não existe.", e.getMessage());
        }

        @Test
        @DisplayName("Dado um ID do detalhe do veículo da proposta que tenha relacionamento com itens então da erro")
        void givenProposalDetailVehicleWithItemRelationship_whenDelete_thenError() throws Exception {
            when(dao.getById(any())).thenReturn(Optional.ofNullable(new ProposalDetailVehicle()));
            when(dao.hasVehicleItemRelationship(any())).thenReturn(true);
            BusException e = assertThrows(BusException.class, () -> service.delete(1, null));
            assertEquals("Não é possível excluir o detalhe do veículo pois existe um relacionamento com itens.", e.getMessage());
        }
    }

    @Nested
    class Find {
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalDetailVehicleServiceTest#listEntityToFind")
        @DisplayName("Quando busca retornar os IDs salvos")
        void whenFind_ReturnProposal(ProposalDetailVehicle mock, ProposalDetailVehicle filter, int expectedSize) throws Exception {
            when(dao.find(any(), any())).thenReturn(Arrays.asList(mock));

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
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> whenListAllthenReturnEntityList() {
        return Stream.of(
                Arguments.of(0, 2, "DESC", "pdv_id", 2),
                Arguments.of(0, 1, "DESC", "pdv_id", 2),
                Arguments.of(0, 1, "DESC", "pdv_id", 2),
                Arguments.of(0, 1, "DESC", "ppr_id", 2),
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
                Arguments.of(1, 1, "DESC", "pdv_id", 2),
                Arguments.of(0, 1, "ASC", "pdv_id", 1)
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> invalidEntityDataToSaveValidator() {
        return Stream.of(
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, null, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 11d, 30, 40, null))
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
        return Stream.of(
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, 7d, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, null, 8d, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 9d, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 10d, 11d, 30, 40, null)),
                Arguments.of(new ProposalDetailVehicle(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 11d, 30, 40, null))
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> listEntityToFind() {
        return Stream.of(
                Arguments.of(new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null), ProposalDetailVehicle.builder().id(1).build(), 1),
                Arguments.of(new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null), ProposalDetailVehicle.builder().proposalDetail(proposalDetailMock1).build(), 1),
                Arguments.of(new ProposalDetailVehicle(1, proposalDetailMock1, productPriceMock1, null, null, null, null, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 30, 40, null), ProposalDetailVehicle.builder().priceProduct(productPriceMock1).build(), 1)
        );
    }
}


