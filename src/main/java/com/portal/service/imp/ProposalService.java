package com.portal.service.imp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Validator;

import com.portal.enums.PersonClassification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IProposalDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.dto.proposal.ProposalDTO;
import com.portal.enums.AuditOperationType;
import com.portal.enums.ProposalState;
import com.portal.enums.SalesOrderState;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Channel;
import com.portal.model.Document;
import com.portal.model.Lead;
import com.portal.model.ParameterModel;
import com.portal.model.PaymentMethod;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.ProposalApproval;
import com.portal.model.ProposalCommission;
import com.portal.model.ProposalDetail;
import com.portal.model.ProposalDetailVehicle;
import com.portal.model.ProposalDetailVehicleItem;
import com.portal.model.ProposalFollowUp;
import com.portal.model.ProposalFrontForm;
import com.portal.model.ProposalPayment;
import com.portal.model.ProposalPerson;
import com.portal.model.SalesOrder;
import com.portal.model.VehicleModel;
import com.portal.service.IAuditService;
import com.portal.service.IChannelService;
import com.portal.service.IDocumentService;
import com.portal.service.ILeadService;
import com.portal.service.IParameterService;
import com.portal.service.IPersonService;
import com.portal.service.IProposalApprovalRuleService;
import com.portal.service.IProposalApprovalService;
import com.portal.service.IProposalCommissionService;
import com.portal.service.IProposalDetailService;
import com.portal.service.IProposalDetailVehicleItemService;
import com.portal.service.IProposalDetailVehicleService;
import com.portal.service.IProposalDocumentService;
import com.portal.service.IProposalFollowUp;
import com.portal.service.IProposalPaymentService;
import com.portal.service.IProposalPersonClientService;
import com.portal.service.IProposalService;
import com.portal.service.IProposalStateHistoryService;
import com.portal.service.ISalesOrderService;
import com.portal.utils.PortalTimeUtils;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalService implements IProposalService {

    @Autowired
    private Validator validator;

    @Autowired
    private IProposalDAO dao;

    @Autowired
    public MessageSource messageSource;

    @Autowired
    private IAuditService auditService;

    @Autowired
    private ILeadService leadService;

    @Autowired
    private IChannelService channelService;

    @Autowired
    private IProposalPersonClientService proposalPersonClientService;

    @Autowired
    private IProposalDetailService proposalDetailService;

    @Autowired
    private IProposalDetailVehicleService proposalDetailVehicleService;

    @Autowired
    private IProposalDetailVehicleItemService proposalDetailVehicleItemService;

    @Autowired
    private IProposalPaymentService proposalPaymentService;

    @Autowired
    private IProposalDocumentService proposalDocumentService;

    @Autowired
    private IProposalCommissionService proposalCommissionService;

    @Autowired
    private IProposalFollowUp proposalFollowUpService;

    @Autowired
    private IProposalStateHistoryService proposalStateHistoryService;
    @Autowired
    private IDocumentService documentService;

    @Autowired
    private IPersonService personService;

    @Autowired
    private IParameterService parameterService;

    @Autowired
    private ISalesOrderService salesOrderService;

    @Autowired
    private IProposalApprovalRuleService proposalApprovalRuleService;

    @Autowired
    private IProposalApprovalService proposalApprovalService;

//	@Autowired
//	private ProposalStateBuilder leadStateBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE,
            Sort.Direction.fromString("DESC"), "pps_id");

    /**
     * Lista todos as propostas.
     *
     * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os
     *                 valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE,
     *                 Sort.Direction.fromString( "DESC" ), "pps_id");
     */
    @Override
    public List<Proposal> listAll(Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.listAll(pageable);

        } catch (Exception e) {
            log.error("Erro no processo de listar as propostas.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.listall",
                    new Object[]{Proposal.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * M??todo auxiliar que decide com base no ID se a entidade deve ser salva ou
     * atualizada. Se n??o tiver ID ?? save, caso contr??rio ?? update.
     *
     * @param model       objeto que deve ser salvo.
     * @param userProfile dados do usu??rio logado.
     */
    @Override
    public Optional<Proposal> saveOrUpdate(Proposal model, UserProfileDTO userProfile)
            throws AppException, BusException {
        if (model != null && model.getId() != null && model.getId() != 0) {
            return this.update(model, userProfile);
        } else {
            return this.save(model, userProfile);
        }
    }

    /**
     * Salva um novo objeto.
     *
     * @param model       objeto que deve ser salvo.
     * @param userProfile dados do usu??rio logado.
     */
    @Override
    public Optional<Proposal> save(Proposal model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, OnSave.class);
            this.validateHasDuplicate(model);
            this.generateProposalNumber(model);

            Optional<Proposal> saved = this.dao.save(model);

            if (saved.isPresent()) {

                model.getProposalDetail().setProposal(saved.get());
                ProposalDetail proposalDetail = this.proposalDetailService.save(model.getProposalDetail(), userProfile).get();
                model.getProposalDetailVehicle().setProposalDetail(proposalDetail);

                ProposalDetailVehicle proposalDetailVehicle = this.proposalDetailVehicleService.save(model.getProposalDetailVehicle(), userProfile).get();
                model.setProposalDetailVehicle(proposalDetailVehicle);

                this.syncProposalDetailVehicleItem(model, userProfile);

                this.syncProposalPayment(model, userProfile);

                if (!model.getImmediateDelivery()) {
                    this.syncProposalPerson(model, userProfile);
                }

                this.syncProposalCommission(model, userProfile);
            }

            // Carregamos para conseguir ter o n??mero gerado
            Optional<Proposal> reload = this.getAllProposalId(saved.get().getId());

            this.proposalStateHistoryService.saveLog(saved.get(), userProfile);

            this.audit((reload.isPresent() ? reload.get() : null), AuditOperationType.PROPOSAL_INSERTED, userProfile);

            return reload;

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de cadastro de proposta: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.save",
                    new Object[]{Proposal.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Atualiza um lead
     *
     * @param model       objeto lead que deve ser salvo.
     * @param userProfile dados do usu??rio logado.
     */
    @Override
    public Optional<Proposal> update(Proposal model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, OnUpdate.class);
            this.validateHasDuplicate(model);
            // this.validateLead( model );

            Optional<Proposal> modelDB = this.getById(model.getId());
            if (!modelDB.isPresent()) {
                throw new BusException("O proposta a ser atualizado n??o existe.");
            }

            //REGRAS PARA MUDAN??A DE STATUS DA PROPOSTA
            this.validateStatusRules(model, modelDB, userProfile);

            // REGRA: PPS-U2
            if (modelDB.get().getNum() != null && !modelDB.get().getNum().equals(model.getNum())) {
                model.setNum(modelDB.get().getNum());
            }

            if(model.getStatus().equals(ProposalState.ON_CUSTOMER_APPROVAL)){
                model.setValidityDate(LocalDateTime.now().plusDays(model.getProposalDetailVehicle().getAgreedTermDays()));
            }

            Optional<Proposal> saved = this.dao.update(model);
            if (saved.isPresent()) {
                model.getProposalDetail().setProposal(saved.get());
                ProposalDetail proposalDetail = this.proposalDetailService
                        .update(model.getProposalDetail(), userProfile).get();
                model.getProposalDetailVehicle().setProposalDetail(proposalDetail);

                ProposalDetailVehicle proposalDetailVehicle = this.proposalDetailVehicleService
                        .update(model.getProposalDetailVehicle(), userProfile).get();
                model.setProposalDetailVehicle(proposalDetailVehicle);

                this.syncProposalDetailVehicleItem(model, userProfile);

                this.syncProposalPayment(model, userProfile);

                if (model.getImmediateDelivery()) {
                    List<ProposalPerson> proposalPersonList = this.proposalPersonClientService
                            .findByProposal(model.getId());

                    if (proposalPersonList.size() > 0) {
                        this.proposalPersonClientService.deleteByProposal(model.getId());
                    }
                } else {
                    if (model.getPersonList() != null && model.getPersonList().size() > 0) {
                        model.getPersonList().forEach(proposalPerson -> {
                            try {
                                // TODO o save de person deve sair daqui
                                // Esse save de pessoa ?? responsabilidade do ProposalPersonClientService
                                this.personService.saveOrUpdate(proposalPerson.getPerson(), userProfile);
                            } catch (AppException | BusException e) {
                                e.printStackTrace();
                            }
                        });

                        this.syncProposalPerson(model, userProfile);
                    }
                }

                this.syncProposalCommission(model, userProfile);

                this.syncDocuments(model);

                if (saved.get().getStatus().equals(ProposalState.FINISHED_WITH_SALE)
                        && !modelDB.get().getStatus().equals(ProposalState.FINISHED_WITH_SALE)) {
                    this.generateSalesOrder(saved.get(), userProfile);
                }
                if (saved.get().getSalesOrder() != null) {
                    this.syncSalesOrder(saved.get(), modelDB, userProfile);

                }

            }

            this.proposalStateHistoryService.saveLog(saved.get(), userProfile);

            // Carregamos para conseguir ter o n??mero gerado
            Optional<Proposal> reload = this.getAllProposalId(saved.get().getId());

            this.audit((reload.isPresent() ? reload.get() : null), AuditOperationType.PROPOSAL_UPDATED, userProfile);

            return reload;

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de atualiza????o de proposta: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.update",
                    new Object[]{Proposal.class.getSimpleName()}, LocaleContextHolder.getLocale()), e);
        }
    }

    private void syncSalesOrder(Proposal proposal, Optional<Proposal> modelDB, UserProfileDTO userProfile) throws AppException, BusException {
        SalesOrder salesOrder = proposal.getSalesOrder();
        Optional<SalesOrder> salesOrderDB = this.salesOrderService.getById(proposal.getSalesOrder().getId());
        modelDB.get().setSalesOrder(salesOrderDB.get());
        if (salesOrderDB.isPresent()) {
            if (!salesOrderDB.get().getStatusClassification().getValue().equals(salesOrder.getStatusClassification().getValue())) {
                salesOrder.setStatus(SalesOrderState.getById(salesOrder.getStatusClassification().getId()));
                this.salesOrderService.update(salesOrder, userProfile);
            }
        }

    }

    private void validateStatusRules(Proposal model, Optional<Proposal> modelDB, UserProfileDTO userProfile)
            throws AppException, BusException {
        if (!model.getStatus().equals(modelDB.get().getStatus())) {
            ProposalApproval proposalApproval = this.proposalApprovalService.fillDiscount(model);
            proposalApproval.setProposal(model);

            switch (model.getStatus()) {
                case IN_PROGRESS:
                    this.validateInProgressState(model);
                    break;
                case IN_COMMERCIAL_APPROVAL:
                    this.validateInCommercialApprovalState(model);
                    break;
                case COMMERCIAL_APPROVED:
                    this.proposalApprovalRuleService.validateRuleApproval(proposalApproval, userProfile);
                    this.validateCommercialApprovedState(model);
                    break;
                case COMMERCIAL_DISAPPROVED:
                    this.proposalApprovalRuleService.validateRuleApproval(proposalApproval, userProfile);
                    this.validateCommercialDisapprovedState(model);
                    break;
                case ON_CUSTOMER_APPROVAL:
                    this.proposalApprovalRuleService.validateRuleApproval(proposalApproval, userProfile);
                    this.validateOnCustomerApprovalState(model);
                    break;
                case FINISHED_WITH_SALE:
                    this.validateFinishedWithSaleState(model);
                    break;
                case FINISHED_WITHOUT_SALE:
                    this.validateFinishedWithoutSaleState(model);
                    break;
                case CANCELED:
                    this.validateCanceledState(model);
                    break;
            }
        } else {
            if(model.getStatus().getType().getValue().equals("COMMERCIAL_DISAPPROVED")){
            	model.setStatus(ProposalState.IN_PROGRESS);
            }
        }
    }

    /**
     * Gera um Pedido de Venda ap??s aprova????o de Cliente.
     *
     * @param proposal    objeto lead que deve ser salvo.
     * @param userProfile dados do usu??rio logado.
     * @throws BusException
     * @throws AppException
     */
    private void generateSalesOrder(Proposal proposal, UserProfileDTO userProfile) throws AppException, BusException {

        SalesOrder newEntity = new SalesOrder();

        newEntity.setProposal(proposal);
        newEntity.setStatus(SalesOrderState.VALIDATION_BACKOFFICE);
        newEntity.setStatusClassification(SalesOrderState.VALIDATION_BACKOFFICE.getType());
        newEntity.setUser(userProfile.getUser());

        salesOrderService.save(newEntity, userProfile);

    }

    public void generateProposalNumber(Proposal model) throws AppException, BusException {
        // M??cara: BYYMM-0A
        // B: string fixa
        // YY: ano com dois d??gitos
        // MM: m??s com doi d??gitos
        // 0: n??mero sequencial da proposta (o n??mero sequencial ?? cont??nuo, ou seja,
        // n??o pode ser reiniciado e n??o pode ser auto increment)
        // A: letra indicando a vers??o
        // Exemplo: B2207-10305B

        String initialCode = this.parameterService.find(new ParameterModel("PROPOSAL_INITIAL_CODE_LETTER")).get()
                .getValue();
        String fixedLetter = this.parameterService.find(new ParameterModel("PROPOSAL_NUMBER_FIXED_LETTER")).get()
                .getValue();
        Long sequencialProposalNum = this.getLastProposalNumber() + 1;

        StringBuilder proposalNumber = new StringBuilder();
        proposalNumber.append(fixedLetter);
        proposalNumber.append(PortalTimeUtils.localDateToString(LocalDate.now(), "yyMM"));
        proposalNumber.append("-");
        proposalNumber.append(sequencialProposalNum);
        proposalNumber.append(initialCode);

        model.setCod(initialCode);
        model.setNum(sequencialProposalNum);
        model.setProposalNumber(proposalNumber.toString());
    }

    /**
     * Busca as propostas que respeitem os dados do objeto. Aqui os campos String
     * s??o buscados com o '='
     *
     * @param model    objeto leads para ser buscado
     * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os
     *                 valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE,
     *                 Sort.Direction.fromString( "DESC" ), "pps_id");
     */
    @Override
    public List<Proposal> find(Proposal model, Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.find(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de buscar as propostas.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.find",
                    new Object[]{Proposal.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Busca as propostas que respeitem os dados do objeto. Aqui os campos String
     * s??o buscados com o 'LIKE'
     *
     * @param model    objeto canais para ser buscado
     * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os
     *                 valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE,
     *                 Sort.Direction.fromString( "DESC" ), "pps_id");
     */
    @Override
    public List<Proposal> search(Proposal model, Pageable pageable) throws AppException, BusException {
        return this.find(model, pageable);
    }

    /**
     * Busca leads que respeitem os dados do objeto. Aqui os campos String s??o
     * buscados com o '='
     * <p>
     * Esse ?? m??todo ?? uma sobrecarga de {@link #search(Proposal, Pageable)} ser??
     * usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE,
     * Sort.Direction.fromString( "DESC" ), "pps_id")
     *
     * @param model objeto lead para ser buscado
     */
    @Override
    public Optional<Proposal> find(Proposal model) throws AppException, BusException {
        List<Proposal> models = this.find(model, null);
        return Optional.ofNullable((models != null ? models.get(0) : null));
    }

    /**
     * Busca leads que respeitem os dados do objetProposalServiceo. Aqui os campos
     * String s??o buscados com o 'LIKE'
     * <p>
     * Esse ?? m??todo ?? uma sobrecarga de {@link #search(Proposal, Pageable)} ser??
     * usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE,
     * Sort.Direction.fromString( "DESC" ), "pps_id")
     *
     * @param model objeto lead para ser buscado
     */
    @Override
    public List<Proposal> search(Proposal model) throws AppException, BusException {
        return this.search(model, null);
    }

    public ProposalDTO getProposal(Integer id) throws AppException, BusException {
    	try {
	        ProposalDTO dto = new ProposalDTO();
	        dto.setProposal(this.getAllProposalId(id).get());
	        return dto;
    	} catch (BusException e) {
			throw new BusException(e.getMessage());
		} catch (AppException e) {
			throw new AppException(e.getMessage());
		}
    }

    @Override
    public ProposalDTO getProposalForFillFollowUp(Integer id) throws AppException, BusException {
        ProposalDTO dto = new ProposalDTO();
        dto.setProposal(new Proposal());
        dto.getProposal().setProposalFollowUp(this.proposalFollowUpService.search(new ProposalFollowUp(id)));
        return dto;
    }

    @Override
    public Optional<Proposal> getAllProposalIdForFillFollowUp(Integer id) throws BusException, AppException {
        try {

            if (id == null) {
                throw new BusException("ID de busca inv??lido.");
            }

            Optional<Proposal> proposal = this.dao.getById(id);

            if (proposal.isPresent()) {

                proposal.get().setProposalFollowUp(this.proposalFollowUpService.search(new ProposalFollowUp(id)));

                return proposal;
            } else {

                throw new AppException(this.messageSource.getMessage("error.generic.getbyid",
                        new Object[]{Proposal.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar uma proposta pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid",
                    new Object[]{Proposal.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Busca uma fonte pelo seu ID
     *
     * @param id ID de proposta
     */
    @Override
    public Optional<Proposal> getAllProposalId(Integer id) throws AppException, BusException {
        try {

            if (id == null) {
                throw new BusException("ID de busca inv??lido.");
            }

            String daysLimit = this.parameterService.getValueOf("PROPOSAL_DAYS_LIMIT");
            Integer proposalDaysValidity = Integer.valueOf(daysLimit);

            Optional<Proposal> proposal = this.dao.getById(id);

            if (proposal.isPresent()) {

                proposal.get().setValidityDate(proposal.get().getValidityDate() != null ? proposal.get().getValidityDate() : proposal.get().getCreateDate().plusDays(proposalDaysValidity).toLocalDate().atStartOfDay());

                ProposalDetail proposalDetailRet = new ProposalDetail();
                ProposalDetail proposalDetail = new ProposalDetail();
                proposalDetail.setProposal(proposal.get());

                ProposalDetailVehicle proposalDetailVehicleRet = new ProposalDetailVehicle();
                ProposalDetailVehicle proposalDetailVehicle = new ProposalDetailVehicle();
                proposalDetailVehicle.setProposalDetail(proposalDetail);

                List<ProposalDetailVehicleItem> proposalDetailVehicleItemRet = new ArrayList<ProposalDetailVehicleItem>();
                ProposalDetailVehicleItem proposalDetailVehicleItem = new ProposalDetailVehicleItem();
                proposalDetailVehicleItem.setProposalDetailVehicle(proposalDetailVehicle);

                List<ProposalPayment> proposalPaymentRet = new ArrayList<ProposalPayment>();
                ProposalPayment proposalPayment = new ProposalPayment();

                List<ProposalCommission> proposalCommissionRet = new ArrayList<ProposalCommission>();
                ProposalCommission proposalCommission = new ProposalCommission();

                proposalDetailRet = this.proposalDetailService.getDetailByProposal(proposalDetail);
                proposalDetailVehicle.setProposalDetail(proposalDetailRet);

                proposalPayment.setProposalDetail(proposalDetailRet);
                proposalCommission.setProposalDetail(proposalDetailRet);

                proposalDetailVehicleRet = this.proposalDetailVehicleService.getDetailVehicleByDetail(proposalDetailVehicle);
                proposalDetailVehicleItem.setProposalDetailVehicle(proposalDetailVehicleRet);

                proposalDetailVehicleItemRet = this.proposalDetailVehicleItemService.find(proposalDetailVehicleItem,
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("ASC"), "pdvi_id"));

                proposalPaymentRet = this.proposalPaymentService.find(proposalPayment, null);

                proposalCommissionRet = this.proposalCommissionService.find(proposalCommission, null);

                proposal.get().setProposalDetail(proposalDetailRet);
                proposal.get().setProposalDetailVehicle(proposalDetailVehicleRet);
                proposal.get().setProposalDetailVehicleItem(proposalDetailVehicleItemRet);
                proposal.get().setProposalPayment(proposalPaymentRet);
                proposal.get().setPersonList(this.proposalPersonClientService.findByProposal(id));
                proposal.get().setProposalCommission(proposalCommissionRet);
                proposal.get().setDocuments(this.proposalDocumentService.findByProposal(id));
                proposal.get().setProposalFollowUp(this.proposalFollowUpService.search(new ProposalFollowUp(id)));
                Optional<SalesOrder> salesOrder = this.salesOrderService.findByProposal(id);
                proposal.get().setSalesOrder(salesOrder.isPresent() ? salesOrder.get() : null);
                return proposal;
            } else {

                throw new AppException(this.messageSource.getMessage("error.generic.getbyid",
                        new Object[]{Proposal.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar uma proposta pelo ID: {}, Message: {} ", id, e.getMessage());
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid",
                    new Object[]{Proposal.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<ProposalFrontForm> getByVehicle(VehicleModel vehicleModel) throws AppException, BusException {
        return this.dao.getByVehicle(vehicleModel, DEFAULT_PAGINATION);
    }

    /**
     * Busca uma fonte pelo seu ID
     *
     * @param id ID de proposta
     */
    @Override
    public Optional<Proposal> getById(Integer id) throws AppException, BusException {
        try {

            if (id == null) {
                throw new BusException("ID de busca inv??lido.");
            }

            Optional<Proposal> proposal = this.dao.getById(id);

            return proposal;

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar uma proposta pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid",
                    new Object[]{Proposal.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Lista todos as propostas.
     * <p>
     * Esse m??todo ?? uma sobrecarga de {@link #listAll(Pageable)}
     */
    @Override
    public List<Proposal> list() throws AppException, BusException {
        return this.listAll(null);
    }

    /**
     * Efetua a exclus??o de um lead
     *
     * @param id          ID de proposta
     * @param userProfile dados do usu??rio logado.
     */
    @Override
    public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de exclus??o inv??lido.");
            }

            Optional<Proposal> entityDB = this.getById(id);
            if (!entityDB.isPresent()) {
                throw new BusException("A proposta a ser exclu??da n??o existe.");
            }

            if (entityDB.get().getLead() != null) {
                this.validateLeadRelationship(entityDB.get().getLead().getId());
            }

            // REGRA: PPS-D2
            this.proposalPersonClientService.deleteByProposal(id);

            // REGRA: PPS-D3
            this.deleteProposalDetailRelationship(id, userProfile);

            // REGRA: PPS-D1
            this.deleteDocumentRelationship(id, userProfile);

            this.audit((entityDB.isPresent() ? entityDB.get() : null), AuditOperationType.PROPOSAL_DELETED,
                    userProfile);

            this.dao.delete(id);

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclus??o de proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.delete",
                    new Object[]{Proposal.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Exclui todos os detalhes da proposta
     * <p>
     * REGRA: PPS-D3
     * <p>
     * AVISO: foi feita em um loop para que possamos rastrear todos os elemento
     * exclu??dos e para que o delete passe pelas regras de exclus??o da entidade.
     */
    private void deleteProposalDetailRelationship(Integer ppsId, UserProfileDTO userProfile)
            throws AppException, BusException {
        try {
            if (ppsId == null || ppsId.equals(0)) {
                throw new BusException("N??o ?? possivel excluir os detalhes da proposta com o ID da proposta inv??lido");
            }

            ProposalDetail findBy = ProposalDetail.builder().proposal(Proposal.builder().id(ppsId).build()).build();

            List<ProposalDetail> detailsDB = this.proposalDetailService.find(findBy, DEFAULT_PAGINATION);
            if (detailsDB != null) {
                log.debug("Existem {} detalhe(s) relacionados para serem deletados.", detailsDB.size());
                for (ProposalDetail item : detailsDB) {
                    this.proposalDetailService.delete(item.getId(), userProfile);
                }
            }

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclus??o dos detalhes da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.delete",
                    new Object[]{PaymentMethod.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Exclui todos os documentos relacionados a proposta, inclusive seus arquivos
     * fisicos
     * <p>
     * REGRA: PPS-D1
     * <p>
     * AVISO: foi feita em um loop para que possamos rastrear todos os elemento
     * exclu??dos e para que o delete passe pelas regras de exclus??o da entidade.
     *
     * @throws BusException
     * @throws AppException
     * @throws NoSuchMessageException
     */
    private void deleteDocumentRelationship(Integer ppsId, UserProfileDTO userProfile)
            throws BusException, NoSuchMessageException, AppException {
        try {
            if (ppsId == null || ppsId.equals(0)) {
                throw new BusException(
                        "N??o ?? possivel excluir os documentos da proposta com o ID da proposta inv??lido");
            }

            List<Document> documents = this.proposalDocumentService.findByProposal(ppsId);

            // Deleta todos os relacionamentos
            this.proposalDocumentService.deleteByProposal(ppsId);

            if (documents != null) {
                log.debug("Existem {} documento(s) relacionados para serem deletados.", documents.size());
                for (Document item : documents) {
                    this.documentService.delete(item.getId(), userProfile);
                }
            }

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclus??o dos documentos da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.delete",
                    new Object[]{PaymentMethod.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void audit(Proposal model, AuditOperationType operationType, UserProfileDTO userProfile)
            throws AppException, BusException {
        try {
            this.auditService.save(objectMapper.writeValueAsString(model), operationType, userProfile);
        } catch (JsonProcessingException e) {
            throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Valida a entidade como um todo, passando por regras de formata????o e
     * obrigatoriedade
     * <p>
     * Regra: PPS-I1,PPS-I2,PPS-I3,PPS-I4,PPS-I5,PPS-I6
     * PPS-U1,PPS-U2,PPS-U3,PPS-U4,PPS-U5,PPS-U6
     *
     * @param model entidade a ser validada
     * @param group grupo de valida????o que ser?? usado
     * @throws AppException
     * @throws BusException
     */
    private void validateEntity(Proposal model, Class<?> group) throws AppException, BusException {
        ValidationHelper.generateException(validator.validate(model, group));
    }

    /**
     * Valida se existe entidade duplicada.
     * <p>
     * Regra: PPS-I4
     *
     * @param model entidade a ser valiadada
     * @throws AppException
     * @throws BusException
     */
    private void validateHasDuplicate(Proposal model) throws AppException, BusException {

        if (model == null) {
            throw new BusException(
                    "N??o ?? poss??vel executar a valida????o de duplicado pois a proposta est?? nula ou inv??lida.");
        }

        Proposal rnSearch = Proposal.builder().num((model.getNum() == null ? 0 : model.getNum())).cod(model.getCod())
                .build();

        List<Proposal> listBD = this.find(rnSearch, DEFAULT_PAGINATION);

        // Save Action
        if ((model.getId() == null || model.getId().equals(0)) && listBD != null && !listBD.isEmpty()
                && listBD.stream().anyMatch(p -> !Objects.equals(p.getId(), model.getId()))) {
            throw new BusException("J?? existe uma proposta com a mesma vers??o. Vers??o: " + model.getVerion());
        }

        // Update Action
        if (model.getId() != null && !model.getId().equals(0) && listBD != null && !listBD.isEmpty()) {

            long count = listBD.stream()
                    .filter(item -> !item.getId().equals(model.getId()) && item.getVerion().equals(model.getVerion()))
                    .count();

            if (count > 0) {
                throw new BusException("J?? existe uma proposta com a mesma vers??o. Vers??o: " + model.getVerion());
            }
        }
    }

    /**
     * Valida se o lead a ser relacionado ?? v??lido.
     * <p>
     * REGRA: PPS-I7, PPS-U7
     *
     * @throws BusException
     * @throws AppException
     */
    private void validateLead(Proposal model) throws BusException, AppException {
        try {
            if (model == null) {
                throw new AppException(
                        "N??o ?? poss??vel executar a valida????o do lead pois a proposta est?? nula ou inv??lida.");
            }

            if (model.getLead() != null && model.getLead().getId() != null && !model.getLead().getId().equals(0)) {
                Optional<Lead> lead = this.leadService.getById(model.getLead().getId());
                if (!lead.isPresent()) {
                    throw new BusException(
                            "O lead " + model.getLead().getId() + " n??o foi encontrado ou est?? inv??lido.");
                }
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao executar a valida????o do lead relacionado a proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic",
                    new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Valida se existe algum relacionamento com lead.
     * <p>
     * REGRA: PPS-D4
     *
     * @param ledId ID da fonte que deve ser verificada
     * @throws AppException           Em caso de erro sist??mico
     * @throws BusException           Em caso de erro relacionado a regra de neg??cio
     * @throws NoSuchMessageException
     */
    private void validateLeadRelationship(Integer ledId) throws BusException, NoSuchMessageException, AppException {
        try {
            if (ledId != null && !ledId.equals(0)) {
                Optional<Lead> lead = this.leadService.getById(ledId);
                if (lead.isPresent()) {
                    throw new BusException("N??o ?? poss??vel excluir a proposta pois est?? relacionada a um lead.");
                }

            } else {
                throw new BusException("ID do lead inv??lido para checar o relacionamento com lead.");
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao carregar o relacionamento entre proposta e lead.", e);
            throw new AppException(this.messageSource.getMessage("error.generic",
                    new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void validateInProgressState(Proposal entity) throws BusException, AppException {
        try {

            if (entity == null) {
                throw new BusException("Entidade inv??lida para o status " + ProposalState.IN_PROGRESS);
            }

            if (entity.getStatus() == null || !entity.getStatus().equals(ProposalState.IN_PROGRESS)) {
                throw new BusException("O status da proposta est?? inv??lido. Status: " + entity.getStatus());
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro na valida????o das regras para mudan??a de status.", e);
            throw new AppException(this.messageSource.getMessage("error.generic",
                    new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void validateInCommercialApprovalState(Proposal entity) throws BusException, AppException {
        try {

            if (entity == null) {
                throw new BusException("Entidade inv??lida para o status " + ProposalState.IN_COMMERCIAL_APPROVAL);
            }

            if (entity.getStatus() == null || !entity.getStatus().equals(ProposalState.IN_COMMERCIAL_APPROVAL)) {
                throw new BusException("O status da proposta est?? inv??lido. Status: " + entity.getStatus());
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro na valida????o das regras para mudan??a de status.", e);
            throw new AppException(this.messageSource.getMessage("error.generic",
                    new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void validateCommercialDisapprovedState(Proposal entity) throws BusException, AppException {
        try {

            if (entity == null) {
                throw new BusException("Entidade inv??lida para o status " + ProposalState.COMMERCIAL_DISAPPROVED);
            }

            if (entity.getStatus() == null || !entity.getStatus().equals(ProposalState.COMMERCIAL_DISAPPROVED)) {
                throw new BusException("O status da proposta est?? inv??lido. Status: " + entity.getStatus());
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro na valida????o das regras para mudan??a de status.", e);
            throw new AppException(this.messageSource.getMessage("error.generic",
                    new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void validateCommercialApprovedState(Proposal entity) throws BusException, AppException {
        try {

            if (entity == null) {
                throw new BusException("Entidade inv??lida para o status " + ProposalState.COMMERCIAL_APPROVED);
            }

            if (entity.getStatus() == null || !entity.getStatus().equals(ProposalState.COMMERCIAL_APPROVED)) {
                throw new BusException("O status da proposta est?? inv??lido. Status: " + entity.getStatus());
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro na valida????o das regras para mudan??a de status.", e);
            throw new AppException(this.messageSource.getMessage("error.generic",
                    new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void validateOnCustomerApprovalState(Proposal entity) throws BusException, AppException {
        try {

            if (entity == null) {
                throw new BusException("Entidade inv??lida para o status " + ProposalState.ON_CUSTOMER_APPROVAL);
            }

            if (entity.getStatus() == null || !entity.getStatus().equals(ProposalState.ON_CUSTOMER_APPROVAL)) {
                throw new BusException("O status da proposta est?? inv??lido. Status: " + entity.getStatus());
            }

            ProposalApproval proposalApproval = this.proposalApprovalService.fillDiscount(entity);
            Optional<Proposal> modelDB = this.getById(entity.getId());
            if (!modelDB.get().getStatus().equals(ProposalState.COMMERCIAL_APPROVED)) {
                if (!proposalApproval.getDiscount().equals(BigDecimal.ZERO)) {
                    for (ProposalPayment proposalPayment : entity.getProposalPayment()) {
                        if (proposalPayment.getPreApproved().equals(Boolean.FALSE)) {
                            throw new BusException("N??o ?? poss??vel enviar a proposta para Aprova????o Cliente pois a mesma possui condi????es de pagamento que n??o s??o pr??-aprovadas.");
                        }
                    }
                } else {
                    throw new BusException("N??o ?? poss??vel enviar a proposta para Aprova????o Cliente, pois, a mesma possui descontos aplicados.");
                }
            }
        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro na valida????o das regras para mudan??a de status.", e);
            throw new AppException(this.messageSource.getMessage("error.generic",
                    new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void validateFinishedWithoutSaleState(Proposal entity) throws BusException, AppException {
        try {

            if (entity == null) {
                throw new BusException("Entidade inv??lida para o status " + ProposalState.FINISHED_WITHOUT_SALE);
            }

            if (entity.getStatus() == null || !entity.getStatus().equals(ProposalState.FINISHED_WITHOUT_SALE)) {
                throw new BusException("O status da proposta est?? inv??lido. Status: " + entity.getStatus());
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro na valida????o das regras para mudan??a de status.", e);
            throw new AppException(this.messageSource.getMessage("error.generic",
                    new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void validateFinishedWithSaleState(Proposal entity) throws BusException, AppException {
        try {

            if (entity == null) {
                throw new BusException("Entidade inv??lida para o status " + ProposalState.FINISHED_WITH_SALE);
            }

            if (entity.getStatus() == null || !entity.getStatus().equals(ProposalState.FINISHED_WITH_SALE)) {
                throw new BusException("O status da proposta est?? inv??lido. Status: " + entity.getStatus());
            }

            Optional<Channel> channel = this.channelService.getById(entity.getProposalDetail().getChannel().getId());
            if (channel.get().getHasPartner() && entity.getProposalDetailVehicle().getOverPrice() != null && entity.getProposalDetailVehicle().getOverPrice() > 0) {
                if (entity.getProposalCommission().size() == 0) {
                    throw new BusException("A comiss??o externa deve ser preenchida.");
                }
            }

            entity.getPersonList().forEach(person -> {
                if(person.getPerson().getClassification().equals(PersonClassification.PF) && person.getPerson().getCpf() == null){
                    try {
                        throw new BusException("O CPF deve ser informado do cliente deve ser informado.");
                    } catch (BusException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(person.getPerson().getClassification().equals(PersonClassification.PJ) && person.getPerson().getCnpj() == null){
                    try {
                        throw new BusException("O CNPJ deve ser informado do cliente deve ser informado.");
                    } catch (BusException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(person.getPerson().getAddress()== null || person.getPerson().getAddress().getId() <= 0){
                    try {
                        throw new BusException("O Endere??o deve ser informado");
                    } catch (BusException e) {
                        throw new RuntimeException(e);
                    }
                }
            });


        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro na valida????o das regras para mudan??a de status.", e);
            throw new AppException(this.messageSource.getMessage("error.generic",
                    new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void validateCanceledState(Proposal entity) throws BusException, AppException {
        try {

            if (entity == null) {
                throw new BusException("Entidade inv??lida para o status " + ProposalState.CANCELED);
            }

            if (entity.getStatus() == null || !entity.getStatus().equals(ProposalState.CANCELED)) {
                throw new BusException("O status da proposta est?? inv??lido. Status: " + entity.getStatus());
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro na valida????o das regras para mudan??a de status.", e);
            throw new AppException(this.messageSource.getMessage("error.generic",
                    new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    private void syncProposalPerson(Proposal proposal, UserProfileDTO userProfile) throws AppException, BusException {
        if (proposal != null && proposal.getPersonList() != null && proposal.getPersonList().size() > 0) {
            List<ProposalPerson> existsProposalPerson = this.proposalPersonClientService.findByProposal(proposal.getId());
            List<ProposalPerson> personList = proposal.getPersonList();

            if (existsProposalPerson == null)
                existsProposalPerson = new ArrayList<ProposalPerson>();
            if (personList == null)
                personList = new ArrayList<ProposalPerson>();

            List<ProposalPerson> toDelete = new ArrayList<ProposalPerson>(existsProposalPerson);
            toDelete.removeAll(personList);

            List<ProposalPerson> toInsert = new ArrayList<ProposalPerson>(personList);
            toInsert.removeAll(existsProposalPerson);

            List<ProposalPerson> toUpdate = new ArrayList<ProposalPerson>(existsProposalPerson);
            toUpdate.removeAll(toInsert);
            toUpdate.removeAll(toDelete);

            for (ProposalPerson modelDelete : toDelete) {
                this.proposalPersonClientService.delete(proposal.getId(), modelDelete.getPerson().getId());
            }

            for (ProposalPerson modelInsert : toInsert) {
                this.proposalPersonClientService.save(proposal.getId(), modelInsert, userProfile);
            }


            for (ProposalPerson entity : toUpdate) {
                int index = personList.indexOf(entity);
                if (index >= 0) {
                    entity = personList.get(index);
                }

                this.proposalPersonClientService.update(proposal.getId(), entity, userProfile);
            }
        }
    }

    private void syncProposalDetailVehicleItem(Proposal proposal, UserProfileDTO userProfile)
            throws AppException, BusException {
        if (proposal != null && proposal.getProposalDetailVehicleItem() != null
                && proposal.getProposalDetailVehicleItem().size() > 0) {

            ProposalDetailVehicle proposalDetailVehicle = proposal.getProposalDetailVehicle();
            ProposalDetailVehicleItem proposalDetailVehicleItem = new ProposalDetailVehicleItem();
            proposalDetailVehicleItem.setProposalDetailVehicle(proposalDetailVehicle);

            List<ProposalDetailVehicleItem> existsProposalDetailVehicleItem = this.proposalDetailVehicleItemService
                    .find(proposalDetailVehicleItem,
                            PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("ASC"), "pdvi_id"));
            List<ProposalDetailVehicleItem> proposalDetailVehicleItemList = proposal.getProposalDetailVehicleItem();

            proposalDetailVehicleItemList.forEach(list -> {
                list.setProposalDetailVehicle(proposalDetailVehicle);
            });

            if (existsProposalDetailVehicleItem == null)
                existsProposalDetailVehicleItem = new ArrayList<ProposalDetailVehicleItem>();

            List<ProposalDetailVehicleItem> toDelete = new ArrayList<ProposalDetailVehicleItem>(
                    existsProposalDetailVehicleItem);
            toDelete.removeAll(proposalDetailVehicleItemList);

            List<ProposalDetailVehicleItem> toInsert = new ArrayList<ProposalDetailVehicleItem>(
                    proposalDetailVehicleItemList);
            toInsert.removeAll(existsProposalDetailVehicleItem);

            List<ProposalDetailVehicleItem> toUpdate = new ArrayList<ProposalDetailVehicleItem>(
                    existsProposalDetailVehicleItem);
            toUpdate.removeAll(toInsert);
            toUpdate.removeAll(toDelete);

            for (ProposalDetailVehicleItem modelDelete : toDelete) {
                this.proposalDetailVehicleItemService.delete(modelDelete.getId(), userProfile);
            }

            for (ProposalDetailVehicleItem modelInsert : toInsert) {

                modelInsert.setSeller(proposal.getProposalDetail().getSeller());
                modelInsert.setProposalDetailVehicle(proposalDetailVehicle);

                this.proposalDetailVehicleItemService.save(modelInsert, userProfile);
            }

            for (ProposalDetailVehicleItem entity : toUpdate) {
                entity.setSeller(proposal.getProposalDetail().getSeller());
                entity.setProposalDetailVehicle(proposalDetailVehicle);
                this.proposalDetailVehicleItemService.update(entity, userProfile);
            }
        }
    }

    private void syncProposalPayment(Proposal proposal, UserProfileDTO userProfile) throws AppException, BusException {
        if (proposal != null && proposal.getProposalPayment() != null && proposal.getProposalPayment().size() > 0) {

            ProposalDetail proposalDetail = proposal.getProposalDetail();
            ProposalPayment proposalPayment = new ProposalPayment();
            proposalPayment.setProposalDetail(proposalDetail);

            List<ProposalPayment> existsProposalPayment = this.proposalPaymentService.find(proposalPayment, null);
            List<ProposalPayment> proposalPaymentList = proposal.getProposalPayment();

            if (existsProposalPayment == null)
                existsProposalPayment = new ArrayList<ProposalPayment>();
            if (proposalPaymentList == null)
                proposalPaymentList = new ArrayList<ProposalPayment>();

            List<ProposalPayment> toDelete = new ArrayList<ProposalPayment>(existsProposalPayment);
            toDelete.removeAll(proposalPaymentList);

            List<ProposalPayment> toInsert = new ArrayList<ProposalPayment>(proposalPaymentList);
            toInsert.removeAll(existsProposalPayment);

            List<ProposalPayment> toUpdate = new ArrayList<ProposalPayment>(existsProposalPayment);
            toUpdate.removeAll(toInsert);
            toUpdate.removeAll(toDelete);

            for (ProposalPayment modelDelete : toDelete) {
                this.proposalPaymentService.delete(modelDelete.getId(), userProfile);
            }

            for (ProposalPayment modelInsert : toInsert) {
                modelInsert.setProposalDetail(proposalDetail);
                this.proposalPaymentService.save(modelInsert, userProfile);
            }

            for (ProposalPayment entity : toUpdate) {
                int index = proposalPaymentList.indexOf(entity);
                if (index >= 0) {
                    entity = proposalPaymentList.get(index);
                }

                entity.setProposalDetail(proposalDetail);
                this.proposalPaymentService.update(entity, userProfile);
            }
        }
    }

    private void syncProposalCommission(Proposal proposal, UserProfileDTO userProfile)
            throws AppException, BusException {
        if (proposal != null && proposal.getId() != null && !proposal.getId().equals(0)) {
            List<ProposalCommission> existsProposalCommission = this.proposalCommissionService
                    .find(ProposalCommission.builder().proposalDetail(proposal.getProposalDetail()).build(), null);
            List<ProposalCommission> proposalCommissionList = proposal.getProposalCommission() != null
                    && proposal.getProposalCommission().size() > 0 ? proposal.getProposalCommission() : null;

            if (existsProposalCommission == null)
                existsProposalCommission = new ArrayList<ProposalCommission>();
            if (proposalCommissionList == null)
                proposalCommissionList = new ArrayList<ProposalCommission>();

            List<ProposalCommission> toDelete = new ArrayList<ProposalCommission>(existsProposalCommission);
            toDelete.removeAll(proposalCommissionList);

            List<ProposalCommission> toInsert = new ArrayList<ProposalCommission>(proposalCommissionList);
            toInsert.removeAll(existsProposalCommission);

            List<ProposalCommission> toUpdate = new ArrayList<ProposalCommission>(existsProposalCommission);
            toUpdate.removeAll(toInsert);
            toUpdate.removeAll(toDelete);

            for (ProposalCommission modelDelete : toDelete) {
                this.proposalCommissionService.delete(modelDelete.getId(), userProfile);
            }

            for (ProposalCommission modelInsert : toInsert) {
                modelInsert.setProposalDetail(proposal.getProposalDetail());
                modelInsert.setPerson(modelInsert.getPartnerPerson().getPerson());
                this.proposalCommissionService.save(modelInsert, userProfile);
            }

            for (ProposalCommission entity : toUpdate) {
                int index = proposalCommissionList.indexOf(entity);
                if (index >= 0) {
                    entity = proposalCommissionList.get(index);
                }

                entity.setProposalDetail(proposal.getProposalDetail());
                entity.setPerson(entity.getPartnerPerson().getPerson());
                this.proposalCommissionService.update(entity, userProfile);
            }
        }
    }

    private void syncDocuments(Proposal proposal) throws AppException, BusException {
        if (proposal != null) {

            List<Document> existsDocument = this.proposalDocumentService.findByProposal(proposal.getId());
            List<Document> documentList = proposal.getDocuments();

            if (existsDocument == null)
                existsDocument = new ArrayList<Document>();
            if (documentList == null)
                documentList = new ArrayList<Document>();

            List<Document> toDelete = new ArrayList<Document>(existsDocument);
            toDelete.removeAll(documentList);

            List<Document> toInsert = new ArrayList<Document>(documentList);
            toInsert.removeAll(existsDocument);

            List<Document> toUpdate = new ArrayList<Document>(existsDocument);
            toUpdate.removeAll(toInsert);
            toUpdate.removeAll(toDelete);

            for (Document modelDelete : toDelete) {
                this.proposalDocumentService.delete(proposal.getId(), modelDelete.getId());
            }

            for (Document modelInsert : toInsert) {
                this.proposalDocumentService.save(proposal.getId(), modelInsert.getId());
            }

            for (Document entity : toUpdate) {
                int index = documentList.indexOf(entity);
                if (index >= 0) {
                    entity = documentList.get(index);
                }

                this.proposalDocumentService.save(proposal.getId(), entity.getId());
            }
        }
    }

    @Override
    public Long getLastProposalNumber() throws AppException {
        return dao.getLastProposalNumber();
    }


}
