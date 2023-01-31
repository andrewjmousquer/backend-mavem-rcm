package com.portal.service.imp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IProposalApprovalDAO;
import com.portal.dto.ProposalApprovalCheckpointRules;
import com.portal.dto.ProposalApprovalDetailDTO;
import com.portal.dto.ProposalApprovalFilterDTO;
import com.portal.dto.ProposalApprovalListDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.dto.proposal.ProposalDTO;
import com.portal.enums.AuditOperationType;
import com.portal.enums.ProposalState;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Proposal;
import com.portal.model.ProposalApproval;
import com.portal.model.ProposalDetailVehicleItem;
import com.portal.model.Seller;
import com.portal.service.IAuditService;
import com.portal.service.IProposalApprovalRuleService;
import com.portal.service.IProposalApprovalService;
import com.portal.service.IProposalDetailVehicleItemService;
import com.portal.service.IProposalService;
import com.portal.validators.ValidationHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalApprovalService implements IProposalApprovalService {

    @Autowired
    private IProposalApprovalDAO dao;

    @Autowired
    private IProposalService proposalService;

    @Autowired
    private IProposalDetailVehicleItemService proposalDetailVehicleItemService;

    
    @Autowired
    private IProposalApprovalRuleService proposalApprovalRuleService;

    @Autowired
    private Validator validator;

    @Autowired
    public MessageSource messageSource;

    @Autowired
    private IAuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private SellerService sellerService;

    private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "pps_id");

    @Override
    public Optional<ProposalApproval> find(ProposalApproval model) throws AppException, BusException {
    	model.setStatus(ProposalState.IN_COMMERCIAL_APPROVAL);
        List<ProposalApproval> proposalApprovalList = this.find(model, null);
        return Optional.ofNullable(proposalApprovalList != null ? proposalApprovalList.get(0) : null);
    }

    @Override
    public Optional<ProposalApproval> getById(Integer id) throws AppException, BusException {
        return Optional.empty();
    }

    @Override
    public List<ProposalApproval> find(ProposalApproval model, Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.find(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de aprovação comercial", e);
            throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{ProposalApproval.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<ProposalApprovalDetailDTO> getByIdProposalAppoval(Integer id) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }
            Optional<ProposalApprovalDetailDTO> proposalApproval = this.dao.getByIdProposalAppoval(id);

            return proposalApproval;

        } catch (Exception e) {
            log.error("Erro ao consultar a aprovação comercial pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getById", new Object[]{ProposalApproval.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<ProposalApproval> list() throws AppException, BusException {
        return null;
    }

    @Override
    public List<ProposalApprovalListDTO> listAll(Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.listAll(pageable);
        } catch (Exception e) {
            log.error("Erro no processo de listar as aprovação comercial", e);
            throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{ProposalApproval.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<ProposalApproval> search(ProposalApproval model) throws AppException, BusException {
        return null;
    }

    @Override
    public List<ProposalApprovalListDTO> search(ProposalApprovalFilterDTO dto, UserProfileDTO userProfile, Pageable pageable) throws AppException {

        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            ProposalApprovalCheckpointRules checkpointRules = this.applyRulesCheckpoin(userProfile);

            String daysLimit = this.parameterService.getValueOf("PROPOSAL_DAYS_LIMIT");
            Integer proposalDaysLimit = Integer.valueOf(daysLimit);
            List<ProposalApprovalListDTO> list = new ArrayList<>();

            dto.setStatus(ProposalState.IN_COMMERCIAL_APPROVAL);
            
            if (checkpointRules.isProposalComercialApprovalAll()) {
                list = this.dao.search(dto, proposalDaysLimit, pageable);
            }

            if (checkpointRules.isProposalComercialApprovalSalesTeam() && !checkpointRules.isProposalComercialApprovalAll()) {
                Optional<Seller> seller = this.sellerService.getByUser(userProfile.getUser().getId());
                List<Seller> sellersSalesTeam = this.sellerService.getBySalesTeam(seller.get().getSalesTeamList());
                dto.getExecutive().addAll(sellersSalesTeam);
                List<ProposalApprovalListDTO> listSalesTeam = this.dao.search(dto, proposalDaysLimit, pageable);
                list.addAll(listSalesTeam);
            }

            list.forEach(item -> {
                item.setValidityDate(item.getValidityDate() != null ? item.getValidityDate() : item.getCreateDate().plusDays(proposalDaysLimit));
            });
            
            this.fillDiscount(list);
            return list;

        } catch (Exception e) {
            log.error("Erro no processo de procurar as aprovações comerciais.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{ProposalApproval.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    public List<ProposalApprovalListDTO> fillDiscount(List<ProposalApprovalListDTO> list) {
        list.forEach(itemApproval -> {
        	itemApproval.setDiscount(Double.valueOf(0));

        	try {
            	itemApproval.setDiscount(itemApproval.getDiscount() + itemApproval.getProposalDetailVehicle().getPriceDiscountAmount());
            	itemApproval.setDiscount(itemApproval.getDiscount() +itemApproval.getProposalDetailVehicle().getProductAmountDiscount());

            	List<ProposalDetailVehicleItem> itens = this.proposalDetailVehicleItemService.search(ProposalDetailVehicleItem.builder().id(itemApproval.getProposalDetailVehicle().getId()).build());
            	if(itens != null && !itens.isEmpty()) {
            		itens.forEach(itemVehicle -> {
                    	itemApproval.setDiscount(itemApproval.getDiscount() + itemVehicle.getAmountDiscount());
            		});
            	}
            	
            	/*
                final ProposalDTO[] proposal = {this.proposalService.getProposal(itemApproval.getId())};
                itemAmountDiscount[0] += proposal[0].getProposal().getProposalDetailVehicle().getPriceDiscountAmount();
                
                double productDiscount = proposal[0].getProposal().getProposalDetailVehicle().getProductAmountDiscount();
                itemAmountDiscount[0] += productDiscount;
                proposal[0].getProposal().getProposalDetailVehicleItem().forEach(itemVehicle -> {
                    itemAmountDiscount[0] += itemVehicle.getAmountDiscount();
            		itemApproval.setDiscount(itemAmountDiscount);
                });
                */
            	
            } catch (AppException e) {
                throw new RuntimeException(e);
            } catch (BusException e) {
                throw new RuntimeException(e);
            }
        });
        return list;
    }

    public ProposalApproval fillDiscount(Proposal proposal) {
        ProposalApproval proposalApproval = new ProposalApproval();
        final BigDecimal[] amountDiscount = new BigDecimal[1];
        amountDiscount[0] = BigDecimal.valueOf(proposal.getProposalDetailVehicle().getPriceDiscountAmount());
        BigDecimal productDiscount = BigDecimal.valueOf(proposal.getProposalDetailVehicle().getProductAmountDiscount());
        amountDiscount[0] = amountDiscount[0].add(productDiscount);
        proposal.getProposalDetailVehicleItem().forEach(vehicleItem -> {
            amountDiscount[0] = amountDiscount[0].add(BigDecimal.valueOf(vehicleItem.getAmountDiscount()));
        });
        proposalApproval.setDiscount(amountDiscount[0]);
        return proposalApproval;
    }

    @Override
    public ProposalApprovalCheckpointRules applyRulesCheckpoin(UserProfileDTO userProfile) {
        ProposalApprovalCheckpointRules checkpointRules = new ProposalApprovalCheckpointRules(false);
        if (userProfile.getUser() != null) {
            if (userProfile.getUser().getAccessList() != null) {
                if (userProfile.getUser().getAccessList().getCheckpoints() != null && !userProfile.getUser().getAccessList().getCheckpoints().isEmpty()) {
                    userProfile.getUser().getAccessList().getCheckpoints().forEach(checkpoint -> {

                        //SE POSSUIR CHECKPOINT PROPOSAL.COMMERCIAL.APPROVAL.ALL
                        if (checkpoint.getName().equals("PROPOSAL.COMMERCIAL.APPROVAL.ALL")) {
                            checkpointRules.setProposalComercialApprovalAll(true);
                        }
                        //SE POSSUIR CHECKPOINT PROPOSAL.COMMERCIAL.APPROVAL.SALESTEAM
                        if (checkpoint.getName().equals("PROPOSAL.COMMERCIAL.APPROVAL.SALESTEAM")) {
                            checkpointRules.setProposalComercialApprovalSalesTeam(true);
                        }
                    });
                }
            }
        }
        return checkpointRules;
    }


    private void fillSeller(List<ProposalApprovalListDTO> list) {
        list.forEach(item -> {
            try {
                item.setExecutive(this.sellerService.getById(item.getExecutive().getId()).get());
            } catch (AppException e) {
                throw new RuntimeException(e);
            } catch (BusException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Optional<ProposalApproval> saveOrUpdate(ProposalApproval model, UserProfileDTO userProfile) throws AppException, BusException {
        if (model.getProposal() != null && model.getProposal().getId() > 0) {
            return this.update(model, userProfile);
        } else {
            return this.save(model, userProfile);
        }
    }

    @Override
    public Optional<ProposalApproval> save(ProposalApproval model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.proposalApprovalRuleService.validateRuleApproval(model, userProfile);

            ProposalDTO proposalDTO = this.proposalService.getProposal(model.getProposal().getId());
            Proposal proposal = proposalDTO.getProposal();
            proposal.setStatus(ProposalState.getByValue(model.getStatus().name()));
            proposalDTO.setProposal(proposal);

            this.proposalService.update(proposal, userProfile);

            model.setPerson(userProfile.getUser().getPerson());
            model.setStatus(ProposalState.getByValue(model.getStatus().name()));

            this.validateEntity(model, ValidationHelper.OnSave.class);

            Optional<ProposalApproval> saved = this.dao.save(model);
            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.PROPOSAL_APPROVAL_INSERTED, userProfile);

            return saved;

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de cadastro de aprovação comercial: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.save", new Object[]{ProposalApproval.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }


    @Override
    public Optional<ProposalApproval> update(ProposalApproval model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, ValidationHelper.OnUpdate.class);

            Optional<ProposalApproval> saved = this.dao.update(model);

            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.PROPOSAL_APPROVAL_UPDATED, userProfile);

            return saved;
        } catch (Exception e) {
            log.error("Erro no processo de atualização da aprovação comercial: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.update", new Object[]{ProposalApproval.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de exclusão inválido");
            }

            Optional<ProposalApproval> entityDB = this.getById(id);
            if (!entityDB.isPresent()) {
                throw new BusException("Aprovação Comecial a ser excluído não existe.");
            }

            this.dao.delete(id);

            this.audit((entityDB.isPresent() ? entityDB.get() : null), AuditOperationType.PROPOSAL_APPROVAL_DELETED, userProfile);

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclusão de uma aprovação comercial.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.delete", new Object[]{ProposalApproval.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void audit(ProposalApproval model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.auditService.save(objectMapper.writeValueAsString(model), operationType, userProfile);
        } catch (JsonProcessingException e) {
            throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
        }
    }

    private void validateEntity(ProposalApproval model, Class<?> group) throws BusException {
        ValidationHelper.generateException(validator.validate(model, group));
    }

}
