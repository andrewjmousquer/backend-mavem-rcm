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

import org.apache.commons.lang3.StringUtils;
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
import com.portal.dao.impl.ProposalDAO;
import com.portal.enums.ProposalRisk;
import com.portal.enums.ProposalState;
import com.portal.exceptions.BusException;
import com.portal.model.Lead;
import com.portal.model.Proposal;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.LeadService;
import com.portal.service.imp.ProposalService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)
public class ProposalServiceTest {

    @Mock
    ProposalDAO dao;

    @Mock
    AuditService auditService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    Validator validator;

    @Mock
    MessageSource messageSource;

    @Mock
    LeadService leadService;

    @Mock
    private IProposalPersonClientService proposalPersonClientService;

    @Mock
    private IProposalDetailService proposalDetailService;

//	@Spy
//	ProposalStateBuilder buyilder;

    @Spy
    ProposalService serviceInternal;

    @InjectMocks
    ProposalService service;

    @Nested
    class ListAll {
        @DisplayName("Listar as propostas e retornar com sucesso a lista")
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalServiceTest#whenListAllthenReturnEntityList")
        void whenListAll_thenReturnProposalList(int page, int size, String sortDir, String sort) throws Exception {
            when(dao.listAll(any())).thenReturn(Arrays.asList(new Proposal()));

            List<Proposal> list = service.listAll(PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "pps_id"));
            assertFalse(list.isEmpty());
        }

        @DisplayName("Dada a paginação nula então retorna a lista")
        @Test
        void givenNullPagination_whenListAll_thenReturnProposalList() throws Exception {
            when(dao.listAll(any())).thenReturn(Arrays.asList(new Proposal()));

            List<Proposal> list = service.listAll(null);
            assertFalse(list.isEmpty());
        }
    }

    @Nested
    class Save {
        @Test
        @DisplayName("Dada uma proposta válida quando salvar retornar o objeto com o novo ID")
        void givenValidProposal_whenSave_thenReturnId() throws Exception {
            Proposal model = new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);
            Proposal mock = new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(leadService.getById(anyInt())).thenReturn(Optional.of(new Lead()));
            when(dao.save(any())).thenReturn(Optional.of(mock));
            when(dao.getById(any())).thenReturn(Optional.of(mock));

            Optional<Proposal> entityDB = service.save(model, null);

            assertNotNull(entityDB);
            assertTrue(entityDB.isPresent());
            assertEquals(mock, entityDB.get());
            assertEquals(mock.getNum(), entityDB.get().getNum());
            assertEquals(mock.getCod(), entityDB.get().getCod());
            assertEquals(mock.getCreateDate(), entityDB.get().getCreateDate());
            assertEquals(mock.getStatus(), entityDB.get().getStatus());
            assertEquals(mock.getLead(), entityDB.get().getLead());
        }

        @Test
        @DisplayName("Dada uma proposta com lead associado quando salvar retornar o objeto com o novo ID")
        void givenProposalWithLead_whenSave_thenReturnId() throws Exception {
            Proposal model = new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, Lead.builder().id(1).build(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);
            Proposal mock = new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, Lead.builder().id(1).build(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(leadService.getById(anyInt())).thenReturn(Optional.of(new Lead()));
            when(dao.save(any())).thenReturn(Optional.of(mock));
            when(dao.getById(any())).thenReturn(Optional.of(mock));

            Optional<Proposal> entityDB = service.save(model, null);

            assertNotNull(entityDB);
            assertTrue(entityDB.isPresent());
            assertEquals(mock, entityDB.get());
            assertEquals(mock.getNum(), entityDB.get().getNum());
            assertEquals(mock.getCod(), entityDB.get().getCod());
            assertEquals(mock.getCreateDate(), entityDB.get().getCreateDate());
            assertEquals(mock.getStatus(), entityDB.get().getStatus());
            assertEquals(mock.getLead(), entityDB.get().getLead());
        }

        @Test
        @DisplayName("Dada uma proposta duplicada quando salvar deve dar erro")
        void givenDuplicateProposal_whenSave_thenReturnError() throws Exception {
            Proposal mock = new Proposal(null, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);

            when(dao.find(any(), any())).thenReturn(Arrays.asList(Proposal.builder().id(2).build()));

            BusException e = assertThrows(BusException.class, () -> service.save(mock, null));
            assertEquals("Já existe uma proposta com a mesma versão. Versão: " + mock.getVerion(), e.getMessage());
        }

        @Test
        @DisplayName("Dada uma proposta com um lead não existente quando salvar deve dar erro")
        void givenProposalWithInvalidLead_whenSave_thenReturnError() throws Exception {
            Proposal mock = new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, Lead.builder().id(1).build(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);

            when(dao.find(any(), any())).thenReturn(null);
            when(leadService.getById(anyInt())).thenReturn(Optional.empty());

            BusException e = assertThrows(BusException.class, () -> service.save(mock, null));
            assertEquals("O lead " + mock.getLead().getId() + " não foi encontrado ou está inválido.", e.getMessage());
        }

        @DisplayName("Dado dados inválidos da entidade aplicamos as validaçoes e deve retornar erro")
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalServiceTest#invalidEntityDataToSaveValidator")
        void givenInvalidProposal_whenSave_thenTestValidador(Proposal model) throws Exception {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<Proposal>> violationSet = validator.validate(model, OnSave.class);

            assertFalse(violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim");
        }
    }

    @Nested
    class Update {
        @Test
        @DisplayName("Dada uma proposta quando atualiza retorna com a atualização")
        void givenProposal_whenUpdate_thenReturnNewProposal() throws Exception {

            Proposal model = new Proposal(1, 1L, "B12208A", "A2", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);
            Proposal mock = new Proposal(1, 1l, "B12208A", "A2", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);

            when(dao.getById(any())).thenReturn(Optional.of(new Proposal()));
            when(dao.find(any(), any())).thenReturn(null);
            when(leadService.getById(anyInt())).thenReturn(Optional.of(new Lead()));
            when(dao.update(any())).thenReturn(Optional.of(mock));

            Optional<Proposal> entityDB = service.update(model, null);

            assertNotNull(entityDB);
            assertTrue(entityDB.isPresent());
            assertEquals(mock, entityDB.get());
            assertEquals(mock.getNum(), entityDB.get().getNum());
            assertEquals(mock.getCod(), entityDB.get().getCod());
            assertEquals(mock.getCreateDate(), entityDB.get().getCreateDate());
            assertEquals(mock.getStatus(), entityDB.get().getStatus());
            assertEquals(mock.getLead(), entityDB.get().getLead());
        }

        @Test
        @DisplayName("Dada uma proposta com lead associado quando atualiza retornar o objeto com o novo ID")
        void givenProposalWithLead_whenUpdate_thenReturnId() throws Exception {
            Proposal model = new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, Lead.builder().id(1).build(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);
            Proposal mock = new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, Lead.builder().id(1).build(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);

            when(dao.getById(any())).thenReturn(Optional.of(new Proposal()));
            when(dao.find(any(), any())).thenReturn(null);
            when(leadService.getById(anyInt())).thenReturn(Optional.of(new Lead()));
            when(dao.update(any())).thenReturn(Optional.of(mock));

            Optional<Proposal> entityDB = service.update(model, null);

            assertNotNull(entityDB);
            assertTrue(entityDB.isPresent());
            assertEquals(mock, entityDB.get());
            assertEquals(mock.getNum(), entityDB.get().getNum());
            assertEquals(mock.getCod(), entityDB.get().getCod());
            assertEquals(mock.getCreateDate(), entityDB.get().getCreateDate());
            assertEquals(mock.getStatus(), entityDB.get().getStatus());
            assertEquals(mock.getLead(), entityDB.get().getLead());
        }

        @Test
        @DisplayName("Dada uma proposta duplicada quando atualiza deve dar erro")
        void givenDuplicateProposal_whenUpdate_thenReturnError() throws Exception {
            Proposal mock = new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);

            when(dao.getById(any())).thenReturn(Optional.of(new Proposal()));
            when(dao.find(any(), any())).thenReturn(Arrays.asList(Proposal.builder().id(2).num(1l).cod("A").build()));

            BusException e = assertThrows(BusException.class, () -> service.update(mock, null));
            assertEquals("Já existe uma proposta com a mesma versão. Versão: " + mock.getVerion(), e.getMessage());
        }

        @DisplayName("Dado dados inválidos da entidade aplicamos as validaçoes e deve retornar erro")
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalServiceTest#invalidEntityDataToUpdateValidator")
        void givenInvalidProposal_whenUpdate_thenTestValidador(Proposal model) throws Exception {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<Proposal>> violationSet = validator.validate(model, OnUpdate.class);

            assertFalse(violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim");
        }

        @Test
        @DisplayName("Dado uma proposta que não existe quando atualizar retorna erro")
        void givenNoExistProposal_whenUpdate_thenReturnError_CHNU4() throws Exception {
            Proposal mock = new Proposal(100, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null);

            when(dao.getById(any())).thenReturn(Optional.empty());
            BusException e = assertThrows(BusException.class, () -> service.update(mock, null));
            assertEquals("O proposta a ser atualizado não existe.", e.getMessage());
        }
    }

    @Nested
    class Delete {
        @Test
        @DisplayName("Dado um ID válido de proposta quando deleta não da erro")
        void givenValidId_whenDelete_thenNoError() throws Exception {
            when(dao.getById(any())).thenReturn(Optional.ofNullable(new Proposal()));
            assertDoesNotThrow(() -> service.delete(1, null));
        }

        @Test
        @DisplayName("Dado um ID inválido de proposta quando deleta da erro")
        void givenInvalidId_whenDelete_thenError() throws Exception {
            BusException e = assertThrows(BusException.class, () -> service.delete(null, null));
            assertEquals("ID de exclusão inválido.", e.getMessage());
        }

        @Test
        @DisplayName("Dado um ID de um proposta que não existia quando deleta da erro")
        void givenNoExistedProposal_whenDelete_thenError() throws Exception {
            when(dao.getById(any())).thenReturn(Optional.empty());
            BusException e = assertThrows(BusException.class, () -> service.delete(999, null));
            assertEquals("A proposta a ser excluída não existe.", e.getMessage());
        }

        @Test
        @DisplayName("Dada uma proposta com um lead relacionado deve dar erro")
        void givenProposalInProposalRelationship_whenDelete_thenError() throws Exception {
            when(dao.getById(any())).thenReturn(Optional.of(Proposal.builder().lead(Lead.builder().id(1).build()).build()));
            when(leadService.getById(any())).thenReturn(Optional.of(new Lead()));

            BusException e = assertThrows(BusException.class, () -> service.delete(1, null));
            assertEquals("Não é possível excluir a proposta pois está relacionada a um lead.", e.getMessage());
        }
    }

    @Nested
    class Find {
        @ParameterizedTest
        @MethodSource("com.portal.service.ProposalServiceTest#listEntityToFind")
        @DisplayName("Quando busca retornar os IDs salvos")
        void whenFind_ReturnProposal(Proposal mock, Proposal filter, int expectedSize) throws Exception {

            when(dao.find(any(), any())).thenReturn(Arrays.asList(mock));

            List<Proposal> entityDB = dao.find(filter, null);

            assertNotNull(entityDB);
            assertFalse(entityDB.isEmpty());
            assertEquals(expectedSize, entityDB.size());

            Optional<Proposal> entity = entityDB.stream().filter(item -> item.getId().equals(mock.getId())).findFirst();

            assertNotNull(entity);
            assertTrue(entity.isPresent());
            assertEquals(mock, entity.get());
            assertEquals(mock.getNum(), entity.get().getNum());
            assertEquals(mock.getCod(), entity.get().getCod());
            assertEquals(mock.getCreateDate(), entity.get().getCreateDate());
            assertEquals(mock.getStatus(), entity.get().getStatus());
            assertEquals(mock.getLead(), entity.get().getLead());
        }
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> whenListAllthenReturnEntityList() {
        return Stream.of(
                Arguments.of(0, 1, "DESC", "id"),
                Arguments.of(0, 1, "DESC", null),
                Arguments.of(0, 1, "DESC", "pps_id"),
                Arguments.of(0, 1, null, "id"),
                Arguments.of(0, 0, "DESC", "id"),
                Arguments.of(0, 0, "DESC", "id"),
                Arguments.of(-1, 0, "DESC", "id")
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> invalidEntityDataToSaveValidator() {
        return Stream.of(
                Arguments.of(new Proposal(0, null, "B12208A", "A", null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
                Arguments.of(new Proposal(1, null, "B12208A", "A", null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
                Arguments.of(new Proposal(null, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
                Arguments.of(new Proposal(null, null, "B12208A", null, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
                Arguments.of(new Proposal(null, null, "B12208A", StringUtils.repeat("A", 2), null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null))
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
        return Stream.of(
                Arguments.of(new Proposal(0, null, "B12208A", "A", null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
                Arguments.of(new Proposal(null, null, "B12208A", "A", null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
                Arguments.of(new Proposal(1, null, "B12208A", null, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null)),
                Arguments.of(new Proposal(1, null, "B12208A", "A", null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null))
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> listEntityToFind() {
        return Stream.of(
                Arguments.of(new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().id(1).build(), 1),
                Arguments.of(new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().num(1l).build(), 1),
                Arguments.of(new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().cod("A").build(), 1),
                Arguments.of(new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().num(1l).cod("A").build(), 1),
                Arguments.of(new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().status(ProposalState.IN_PROGRESS).build(), null, 1),
                Arguments.of(new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().createDate(LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00)).build(), 1),
                Arguments.of(new Proposal(1, 1l, "B12208A", "A", null, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 03, 25, 00, 00, 00, 00), ProposalState.IN_PROGRESS, null, Lead.builder().id(1).build(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, ProposalRisk.NORMAL, null, false, null, null), Proposal.builder().lead(Lead.builder().id(1).build()).build(), 1)
        );
    }
}
