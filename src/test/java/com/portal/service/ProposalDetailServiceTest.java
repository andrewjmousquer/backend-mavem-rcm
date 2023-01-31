package com.portal.service;

import static org.junit.Assert.assertNull;
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
import com.portal.dao.impl.ProposalDetailDAO;
import com.portal.enums.PersonClassification;
import com.portal.enums.ProposalRisk;
import com.portal.enums.ProposalState;
import com.portal.exceptions.BusException;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.ProposalDetail;
import com.portal.model.Seller;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.PersonService;
import com.portal.service.imp.ProposalDetailService;
import com.portal.service.imp.ProposalService;
import com.portal.service.imp.SellerService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)
public class ProposalDetailServiceTest {

    @Mock
    ProposalDetailDAO dao;

    @Mock
    AuditService auditService;

    @Mock
    PersonService personService;

    @Mock
    SellerService sellerService;

    @Mock
    ProposalService proposalService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    Validator validator;

    @Mock
    MessageSource messageSource;

    @Spy
    ProposalDetailService serviceInternal;

    @InjectMocks
    ProposalDetailService service;

    private static final Seller sellerMock = Seller.builder().person(new Person(2, "Person Seller", "Seller", "00000000002", null, null, null, null, PersonClassification.PF.getType())).build();
    private static final Seller internSellerMock = Seller.builder().person(new Person(4, "Person Intern Sela", "Intern Sale", "00000000004", null, null, null, null, PersonClassification.PF.getType())).build();

    private static final Proposal proposalMock1 = new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2022, 03, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);

    @Nested
    class ListAll {
        @DisplayName("Listar os detalhes de propostas e retornar com sucesso a lista")
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalServiceTest#whenListAllthenReturnEntityList")
        void whenListAll_thenReturnProposalList(int page, int size, String sortDir, String sort) throws Exception {
            when(dao.listAll(any())).thenReturn(Arrays.asList(new ProposalDetail()));

            List<ProposalDetail> list = service.listAll(PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "ppd_id"));
            assertFalse(list.isEmpty());
        }

        @DisplayName("Dada a paginação nula então retorna a lista")
        @Test
        void givenNullPagination_whenListAll_thenReturnProposalList() throws Exception {
            when(dao.listAll(any())).thenReturn(Arrays.asList(new ProposalDetail()));

            List<ProposalDetail> list = service.listAll(null);
            assertFalse(list.isEmpty());
        }
    }

    @Nested
    class Save {
        @Test
        @DisplayName("Dada um detalhe de proposta válido quando salvar retornar o objeto com o novo ID")
        void givenValidProposalDetail_whenSave_thenReturnId() throws Exception {
            ProposalDetail model = new ProposalDetail(null, proposalMock1, sellerMock, null, null, null, null, null, null, null);
            ProposalDetail mock = new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(sellerService.getById(anyInt())).thenReturn(Optional.of(sellerMock));
            when(proposalService.getById(anyInt())).thenReturn(Optional.of(proposalMock1));
            when(dao.save(any())).thenReturn(Optional.of(mock));

            Optional<ProposalDetail> entityDB = service.save(model, null);

            assertNotNull(entityDB);
            assertTrue(entityDB.isPresent());
            assertEquals(mock, entityDB.get());

            assertNotNull(entityDB.get().getProposal());
            assertEquals(mock.getProposal(), entityDB.get().getProposal());

            assertNotNull(entityDB.get().getSeller());
            assertEquals(mock.getSeller(), entityDB.get().getSeller());
            assertEquals(mock.getSeller().getPerson().getName(), entityDB.get().getSeller().getPerson().getName());
            assertEquals(mock.getSeller().getPerson().getJobTitle(), entityDB.get().getSeller().getPerson().getJobTitle());
            assertEquals(mock.getSeller().getPerson().getCpf(), entityDB.get().getSeller().getPerson().getCpf());
            assertEquals(mock.getSeller().getPerson().getCnpj(), entityDB.get().getSeller().getPerson().getCnpj());
            assertEquals(mock.getSeller().getPerson().getRg(), entityDB.get().getSeller().getPerson().getRg());
            assertEquals(mock.getSeller().getPerson().getRne(), entityDB.get().getSeller().getPerson().getRne());

            assertEquals(mock.getInternSale(), entityDB.get().getInternSale());
        }

        @Test
        @DisplayName("Dada uma proposta com vendedor interno associado quando salvar retornar o objeto com o novo ID")
        void givenProposalDetailWithInternSale_whenSave_thenReturnId() throws Exception {
            ProposalDetail model = new ProposalDetail(null, proposalMock1, sellerMock, internSellerMock, null, null, null, null, null, null);
            ProposalDetail mock = new ProposalDetail(1, proposalMock1, sellerMock, internSellerMock, null, null, null, null, null, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(proposalService.getById(anyInt())).thenReturn(Optional.of(proposalMock1));
            when(sellerService.getById(anyInt())).thenReturn(Optional.of(sellerMock), Optional.of(internSellerMock));
            when(dao.save(any())).thenReturn(Optional.of(mock));

            Optional<ProposalDetail> entityDB = service.save(model, null);

            assertNotNull(entityDB.get().getProposal());
            assertEquals(mock.getProposal(), entityDB.get().getProposal());

            assertNotNull(entityDB.get().getSeller());
            assertEquals(mock.getSeller(), entityDB.get().getSeller());
            assertEquals(mock.getSeller().getPerson().getName(), entityDB.get().getSeller().getPerson().getName());
            assertEquals(mock.getSeller().getPerson().getJobTitle(), entityDB.get().getSeller().getPerson().getJobTitle());
            assertEquals(mock.getSeller().getPerson().getCpf(), entityDB.get().getSeller().getPerson().getCpf());
            assertEquals(mock.getSeller().getPerson().getCnpj(), entityDB.get().getSeller().getPerson().getCnpj());
            assertEquals(mock.getSeller().getPerson().getRg(), entityDB.get().getSeller().getPerson().getRg());
            assertEquals(mock.getSeller().getPerson().getRne(), entityDB.get().getSeller().getPerson().getRne());

            assertNotNull(entityDB.get().getInternSale());
            assertEquals(mock.getInternSale(), entityDB.get().getInternSale());
            assertEquals(mock.getInternSale().getPerson().getName(), entityDB.get().getInternSale().getPerson().getName());
            assertEquals(mock.getInternSale().getPerson().getJobTitle(), entityDB.get().getInternSale().getPerson().getJobTitle());
            assertEquals(mock.getInternSale().getPerson().getCpf(), entityDB.get().getInternSale().getPerson().getCpf());
            assertEquals(mock.getInternSale().getPerson().getCnpj(), entityDB.get().getInternSale().getPerson().getCnpj());
            assertEquals(mock.getInternSale().getPerson().getRg(), entityDB.get().getInternSale().getPerson().getRg());
            assertEquals(mock.getInternSale().getPerson().getRne(), entityDB.get().getInternSale().getPerson().getRne());
        }

        @Test
        @DisplayName("Dada uma proposta duplicada quando salvar deve dar erro")
        void givenDuplicateProposalDetail_whenSave_thenReturnError() throws Exception {
            ProposalDetail mock = new ProposalDetail(null, proposalMock1, sellerMock, null, null, null, null, null, null, null);
            ProposalDetail mockDuplicate = new ProposalDetail(2, proposalMock1, sellerMock, null, null, null, null, null, null, null);

            when(dao.find(any(), any())).thenReturn(Arrays.asList(mockDuplicate));

            BusException e = assertThrows(BusException.class, () -> service.save(mock, null));
            assertEquals("Já existe um detalhe para essa proposta.", e.getMessage());
        }

        @Test
        @DisplayName("Dada um registro com o vendedor não existente quando salvar deve dar erro")
        void givenProposalDetailWithInvalidSeller_whenSave_thenReturnError() throws Exception {
            ProposalDetail mock = new ProposalDetail(null, proposalMock1, sellerMock, null, null, null, null, null, null, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(proposalService.getById(anyInt())).thenReturn(Optional.of(proposalMock1));
            when(sellerService.getById(anyInt())).thenReturn(Optional.empty(), Optional.of(internSellerMock));
            when(dao.save(any())).thenReturn(Optional.of(mock));

            BusException e = assertThrows(BusException.class, () -> service.save(mock, null));
            assertEquals("Não é possível salvar o detalhe da proposta com um vendedor não existente.", e.getMessage());
        }

        @Test
        @DisplayName("Dada um registro com a proposta não existente quando salvar deve dar erro")
        void givenProposalDetailWithInvalidProposal_whenSave_thenReturnError() throws Exception {
            ProposalDetail mock = new ProposalDetail(null, proposalMock1, sellerMock, null, null, null, null, null, null, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(proposalService.getById(anyInt())).thenReturn(Optional.empty());
            when(sellerService.getById(anyInt())).thenReturn(Optional.of(sellerMock));
            when(dao.save(any())).thenReturn(Optional.of(mock));

            BusException e = assertThrows(BusException.class, () -> service.save(mock, null));
            assertEquals("Não é possível salvar o detalhe da proposta com uma proposta não existente.", e.getMessage());
        }

        @Test
        @DisplayName("Dada um registro com a proposta não existente quando salvar deve dar erro")
        void givenProposalDetailWithInvalidInternSale_whenSave_thenReturnError() throws Exception {
            ProposalDetail mock = new ProposalDetail(null, proposalMock1, sellerMock, internSellerMock, null, null, null, null, null, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(proposalService.getById(anyInt())).thenReturn(Optional.of(proposalMock1));
            when(sellerService.getById(anyInt())).thenReturn(Optional.of(sellerMock), Optional.empty());

            BusException e = assertThrows(BusException.class, () -> service.save(mock, null));
            assertEquals("Não é possível salvar o detalhe da proposta com um vendedor interno não existente.", e.getMessage());
        }

        @DisplayName("Dado dados inválidos da entidade aplicamos as validaçoes e deve retornar erro")
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalDetailServiceTest#invalidEntityDataToSaveValidator")
        void givenInvalidProposal_whenSave_thenTestValidador(ProposalDetail model) throws Exception {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<ProposalDetail>> violationSet = validator.validate(model, OnSave.class);

            assertFalse(violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim");
        }
    }

    @Nested
    class Update {
        @Test
        @DisplayName("Dada uma proposta quando atualiza retorna com a atualização")
        void givenProposalDetail_whenUpdate_thenReturnNewProposal() throws Exception {
            ProposalDetail model = new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null);
            ProposalDetail mock = new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null);

            when(dao.getById(any())).thenReturn(Optional.of(model));
            when(dao.find(any(), any())).thenReturn(null);
            when(proposalService.getById(anyInt())).thenReturn(Optional.of(proposalMock1));
            when(sellerService.getById(anyInt())).thenReturn(Optional.of(sellerMock));
            when(dao.update(any())).thenReturn(Optional.of(mock));

            Optional<ProposalDetail> entityDB = service.update(model, null);

            assertNotNull(entityDB.get().getProposal());
            assertEquals(mock.getProposal(), entityDB.get().getProposal());

            assertNotNull(entityDB.get().getSeller());
            assertEquals(mock.getSeller(), entityDB.get().getSeller());
            assertEquals(mock.getSeller().getPerson().getName(), entityDB.get().getSeller().getPerson().getName());
            assertEquals(mock.getSeller().getPerson().getJobTitle(), entityDB.get().getSeller().getPerson().getJobTitle());
            assertEquals(mock.getSeller().getPerson().getCpf(), entityDB.get().getSeller().getPerson().getCpf());
            assertEquals(mock.getSeller().getPerson().getCnpj(), entityDB.get().getSeller().getPerson().getCnpj());
            assertEquals(mock.getSeller().getPerson().getRg(), entityDB.get().getSeller().getPerson().getRg());
            assertEquals(mock.getSeller().getPerson().getRne(), entityDB.get().getSeller().getPerson().getRne());

            assertNull(mock.getInternSale());
        }

        @Test
        @DisplayName("Dada uma proposta quando atualiza retorna com a atualização")
        void givenProposalDetailWithInternSale_whenUpdate_thenReturnNewProposal() throws Exception {
            ProposalDetail model = new ProposalDetail(1, proposalMock1, sellerMock, internSellerMock, null, null, null, null, null, null);
            ProposalDetail mock = new ProposalDetail(1, proposalMock1, sellerMock, internSellerMock, null, null, null, null, null, null);

            when(dao.getById(any())).thenReturn(Optional.of(model));
            when(dao.find(any(), any())).thenReturn(null);
            when(proposalService.getById(anyInt())).thenReturn(Optional.of(proposalMock1));
            when(sellerService.getById(anyInt())).thenReturn(Optional.of(sellerMock), Optional.of(internSellerMock));
            when(dao.update(any())).thenReturn(Optional.of(mock));

            Optional<ProposalDetail> entityDB = service.update(model, null);

            assertNotNull(entityDB.get().getProposal());
            assertEquals(mock.getProposal(), entityDB.get().getProposal());

            assertNotNull(entityDB.get().getSeller());
            assertEquals(mock.getSeller(), entityDB.get().getSeller());
            assertEquals(mock.getSeller().getPerson().getName(), entityDB.get().getSeller().getPerson().getName());
            assertEquals(mock.getSeller().getPerson().getJobTitle(), entityDB.get().getSeller().getPerson().getJobTitle());
            assertEquals(mock.getSeller().getPerson().getCpf(), entityDB.get().getSeller().getPerson().getCpf());
            assertEquals(mock.getSeller().getPerson().getCnpj(), entityDB.get().getSeller().getPerson().getCnpj());
            assertEquals(mock.getSeller().getPerson().getRg(), entityDB.get().getSeller().getPerson().getRg());
            assertEquals(mock.getSeller().getPerson().getRne(), entityDB.get().getSeller().getPerson().getRne());

            assertNotNull(entityDB.get().getInternSale());
            assertEquals(mock.getInternSale(), entityDB.get().getInternSale());
            assertEquals(mock.getInternSale().getPerson().getName(), entityDB.get().getInternSale().getPerson().getName());
            assertEquals(mock.getInternSale().getPerson().getJobTitle(), entityDB.get().getInternSale().getPerson().getJobTitle());
            assertEquals(mock.getInternSale().getPerson().getCpf(), entityDB.get().getInternSale().getPerson().getCpf());
            assertEquals(mock.getInternSale().getPerson().getCnpj(), entityDB.get().getInternSale().getPerson().getCnpj());
            assertEquals(mock.getInternSale().getPerson().getRg(), entityDB.get().getInternSale().getPerson().getRg());
            assertEquals(mock.getInternSale().getPerson().getRne(), entityDB.get().getInternSale().getPerson().getRne());
        }

        @Test
        @DisplayName("Dada uma proposta duplicada quando atualizar deve dar erro")
        void givenDuplicateProposalDetail_whenUpdate_thenReturnError() throws Exception {
            ProposalDetail mock = new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null);
            ProposalDetail mockDuplicate = new ProposalDetail(2, proposalMock1, sellerMock, null, null, null, null, null, null, null);

            when(dao.getById(any())).thenReturn(Optional.of(mock));
            when(dao.find(any(), any())).thenReturn(Arrays.asList(mockDuplicate));

            BusException e = assertThrows(BusException.class, () -> service.update(mock, null));
            assertEquals("Já existe um detalhe para essa proposta.", e.getMessage());
        }

        @Test
        @DisplayName("Dada um registro com o vendedor não existente quando atualizar deve dar erro")
        void givenProposalDetailWithInvalidSeller_whenUpdate_thenReturnError() throws Exception {
            ProposalDetail mock = new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null);

            when(dao.getById(any())).thenReturn(Optional.of(mock));
            when(dao.find(any(), any())).thenReturn(null);
            when(proposalService.getById(anyInt())).thenReturn(Optional.of(proposalMock1));
            when(personService.getById(anyInt())).thenReturn(Optional.empty());

            BusException e = assertThrows(BusException.class, () -> service.update(mock, null));
            assertEquals("Não é possível salvar o detalhe da proposta com um vendedor não existente.", e.getMessage());
        }

        @Test
        @DisplayName("Dada um registro com a proposta não existente quando atualizar deve dar erro")
        void givenProposalDetailWithInvalidProposal_whenUpdate_thenReturnError() throws Exception {
            ProposalDetail mock = new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null);

            when(dao.getById(any())).thenReturn(Optional.of(mock));
            when(dao.find(any(), any())).thenReturn(null);
            when(proposalService.getById(anyInt())).thenReturn(Optional.empty());
            when(sellerService.getById(anyInt())).thenReturn(Optional.of(sellerMock));

            BusException e = assertThrows(BusException.class, () -> service.update(mock, null));
            assertEquals("Não é possível salvar o detalhe da proposta com uma proposta não existente.", e.getMessage());
        }

        @Test
        @DisplayName("Dada um registro com a proposta não existente quando atualizar deve dar erro")
        void givenProposalDetailWithInvalidInternSale_whenUpdate_thenReturnError() throws Exception {
            ProposalDetail mock = new ProposalDetail(1, proposalMock1, sellerMock, internSellerMock, null, null, null, null, null, null);

            when(dao.getById(any())).thenReturn(Optional.of(mock));
            when(dao.find(any(), any())).thenReturn(null);
            when(proposalService.getById(anyInt())).thenReturn(Optional.of(proposalMock1));
            when(sellerService.getById(anyInt())).thenReturn(Optional.of(sellerMock), Optional.empty());

            BusException e = assertThrows(BusException.class, () -> service.save(mock, null));
            assertEquals("Não é possível salvar o detalhe da proposta com um vendedor interno não existente.", e.getMessage());
        }

        @Test
        @DisplayName("Dado o detalhe de proposta que não existe quando atualizar retorna erro")
        void givenNoExistProposal_whenUpdate_thenReturnError_CHNU4() throws Exception {
            ProposalDetail mock = new ProposalDetail(1, proposalMock1, sellerMock, internSellerMock, null, null, null, null, null, null);

            when(dao.getById(any())).thenReturn(Optional.of(mock));
            when(dao.find(any(), any())).thenReturn(null);
            when(proposalService.getById(anyInt())).thenReturn(Optional.of(proposalMock1));
            when(sellerService.getById(anyInt())).thenReturn(Optional.of(sellerMock));
            when(dao.getById(any())).thenReturn(Optional.empty());

            BusException e = assertThrows(BusException.class, () -> service.update(mock, null));
            assertEquals("O detalhe da proposta a ser atualizado não existe.", e.getMessage());
        }

        @DisplayName("Dado dados inválidos da entidade aplicamos as validaçoes e deve retornar erro")
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalDetailServiceTest#invalidEntityDataToUpdateValidator")
        void givenInvalidProposal_whenUpdate_thenTestValidador(ProposalDetail model) throws Exception {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<ProposalDetail>> violationSet = validator.validate(model, OnUpdate.class);

            assertFalse(violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim");
        }
    }

    @Nested
    class Delete {
        @Test
        @DisplayName("Dado um ID válido de detalhe da proposta quando deleta não da erro")
        void givenValidId_whenDelete_thenNoError() throws Exception {
            when(dao.getById(any())).thenReturn(Optional.ofNullable(new ProposalDetail()));
            assertDoesNotThrow(() -> service.delete(1, null));
        }

        @Test
        @DisplayName("Dado um ID inválido de proposta quando deleta da erro")
        void givenInvalidId_whenDelete_thenError() throws Exception {
            BusException e = assertThrows(BusException.class, () -> service.delete(null, null));
            assertEquals("ID de exclusão inválido.", e.getMessage());
        }

        @Test
        @DisplayName("Dado um ID de um detalhe de proposta que não existia quando deleta da erro")
        void givenNoExistedProposal_whenDelete_thenError() throws Exception {
            when(dao.getById(any())).thenReturn(Optional.empty());
            BusException e = assertThrows(BusException.class, () -> service.delete(999, null));
            assertEquals("O detalhe da proposta a ser excluída não existe.", e.getMessage());
        }
    }

    @Nested
    class Find {
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalDetailServiceTest#listEntityToFind")
        @DisplayName("Quando busca retornar os IDs salvos")
        void whenFind_ReturnProposal(ProposalDetail mock, ProposalDetail filter, int expectedSize) throws Exception {
            when(dao.find(any(), any())).thenReturn(Arrays.asList(mock));

            List<ProposalDetail> entityDB = dao.find(filter, null);

            assertNotNull(entityDB);
            assertFalse(entityDB.isEmpty());
            assertEquals(expectedSize, entityDB.size());

            Optional<ProposalDetail> entity = entityDB.stream().filter(item -> item.getId().equals(mock.getId())).findFirst();

            assertNotNull(entity);
            assertTrue(entity.isPresent());

            assertNotNull(entity.get().getProposal());
            assertEquals(mock.getProposal(), entity.get().getProposal());

            assertNotNull(entity.get().getSeller());
            assertEquals(mock.getSeller(), entity.get().getSeller());
            assertEquals(mock.getSeller().getPerson().getName(), entity.get().getSeller().getPerson().getName());
            assertEquals(mock.getSeller().getPerson().getJobTitle(), entity.get().getSeller().getPerson().getJobTitle());
            assertEquals(mock.getSeller().getPerson().getCpf(), entity.get().getSeller().getPerson().getCpf());
            assertEquals(mock.getSeller().getPerson().getCnpj(), entity.get().getSeller().getPerson().getCnpj());
            assertEquals(mock.getSeller().getPerson().getRg(), entity.get().getSeller().getPerson().getRg());
            assertEquals(mock.getSeller().getPerson().getRne(), entity.get().getSeller().getPerson().getRne());

            if (mock.getInternSale() != null) {
                assertEquals(mock.getInternSale(), entity.get().getInternSale());
                assertEquals(mock.getInternSale().getPerson().getName(), entity.get().getInternSale().getPerson().getName());
                assertEquals(mock.getInternSale().getPerson().getJobTitle(), entity.get().getInternSale().getPerson().getJobTitle());
                assertEquals(mock.getInternSale().getPerson().getCpf(), entity.get().getInternSale().getPerson().getCpf());
                assertEquals(mock.getInternSale().getPerson().getCnpj(), entity.get().getInternSale().getPerson().getCnpj());
                assertEquals(mock.getInternSale().getPerson().getRg(), entity.get().getInternSale().getPerson().getRg());
                assertEquals(mock.getInternSale().getPerson().getRne(), entity.get().getInternSale().getPerson().getRne());
            } else {
                assertNull(mock.getInternSale());
            }
        }
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> whenListAllthenReturnEntityList() {
        return Stream.of(
                Arguments.of(0, 1, "DESC", "id"),
                Arguments.of(0, 1, "DESC", null),
                Arguments.of(0, 1, "DESC", "ppd_id"),
                Arguments.of(0, 1, null, "id"),
                Arguments.of(0, 0, "DESC", "id"),
                Arguments.of(0, 0, "DESC", "id"),
                Arguments.of(-1, 0, "DESC", "id")
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> invalidEntityDataToSaveValidator() {
        return Stream.of(
                Arguments.of(new ProposalDetail(null, null, null, null, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetail(0, proposalMock1, sellerMock, null, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetail(null, proposalMock1, null, null, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetail(null, null, sellerMock, null, null, null, null, null, null, null))
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
        return Stream.of(
                Arguments.of(new ProposalDetail(1, null, null, null, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetail(null, proposalMock1, sellerMock, null, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetail(0, proposalMock1, sellerMock, null, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetail(1, proposalMock1, null, null, null, null, null, null, null, null)),
                Arguments.of(new ProposalDetail(1, null, sellerMock, null, null, null, null, null, null, null))
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> listEntityToFind() {
        return Stream.of(
                Arguments.of(new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null), ProposalDetail.builder().id(1).build(), 1),
                Arguments.of(new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null), ProposalDetail.builder().proposal(proposalMock1).build(), 1),
                Arguments.of(new ProposalDetail(1, proposalMock1, sellerMock, null, null, null, null, null, null, null), ProposalDetail.builder().seller(sellerMock).build(), 1),
                Arguments.of(new ProposalDetail(1, proposalMock1, sellerMock, internSellerMock, null, null, null, null, null, null), ProposalDetail.builder().internSale(internSellerMock).build(), 1),
                Arguments.of(new ProposalDetail(1, proposalMock1, sellerMock, internSellerMock, null, null, null, null, null, null), ProposalDetail.builder().proposal(proposalMock1).seller(sellerMock).internSale(internSellerMock).build(), 1)
        );
    }
}


