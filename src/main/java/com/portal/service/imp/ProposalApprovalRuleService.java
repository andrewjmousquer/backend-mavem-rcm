package com.portal.service.imp;


import java.math.BigDecimal;
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
import com.portal.dao.IProposalApprovalRuleDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalApproval;
import com.portal.model.ProposalApprovalRule;
import com.portal.model.Seller;
import com.portal.service.IAuditService;
import com.portal.service.IProposalApprovalRuleService;
import com.portal.service.ISellerService;
import com.portal.utils.PortalNumberUtils;
import com.portal.validators.ValidationHelper;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalApprovalRuleService implements IProposalApprovalRuleService {

    @Autowired
    private IProposalApprovalRuleDAO dao;

    @Autowired
    private Validator validator;

    @Autowired
    public MessageSource messageSource;

    @Autowired
    private ISellerService sellerService;

    @Autowired
    private IAuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "par_id");

    @Override
    public Optional<ProposalApprovalRule> find(ProposalApprovalRule model) throws AppException, BusException {
        List<ProposalApprovalRule> proposalApprovalRuleList = this.find(model, null);
        return Optional.ofNullable((proposalApprovalRuleList != null && !proposalApprovalRuleList.isEmpty()) ? proposalApprovalRuleList.get(0) : null);
    }

    @Override
    public List<ProposalApprovalRule> find(ProposalApprovalRule model, Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.find(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de alçada de aprovação.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{ProposalApprovalRule.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<ProposalApprovalRule> getById(Integer id) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }
            return this.dao.getById(id);

        } catch (Exception e) {
            log.error("Erro ao consultar a alçada de aprovação pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getById", new Object[]{ProposalApprovalRule.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<ProposalApprovalRule> getByJob(Integer id) throws AppException {
        try {
            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }
            return this.dao.getByJob(id);

        } catch (Exception e) {
            log.error("Erro ao consultar a alçada de aprovação pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getById", new Object[]{ProposalApprovalRule.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }


    @Override
    public List<ProposalApprovalRule> list() throws AppException, BusException {
        return this.listAll(null);
    }


    @Override
    public List<ProposalApprovalRule> listAll(Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }
            return this.dao.listAll(pageable);
        } catch (Exception e) {
            log.error("Erro no processo de listar as alçadas de aprovação.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{ProposalApprovalRule.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<ProposalApprovalRule> search(ProposalApprovalRule model) throws AppException, BusException {
        return this.search(model, null);
    }

    @Override
    public List<ProposalApprovalRule> search(ProposalApprovalRule model, Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }
            return this.dao.search(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de procurar as alaçadas de aprovação.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{ProposalApprovalRule.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<ProposalApprovalRule> searchForm(String text, Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.searchForm(text, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de procurar as alaçadas de aprovação.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{ProposalApprovalRule.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<ProposalApprovalRule> saveOrUpdate(ProposalApprovalRule model, UserProfileDTO userProfile) throws AppException, BusException {
        if (model.getId() != null && model.getId() > 0) {
            return this.update(model, userProfile);
        } else {
            return this.save(model, userProfile);
        }
    }

    @Override
    public Optional<ProposalApprovalRule> save(ProposalApprovalRule model, UserProfileDTO userProfile) throws AppException, BusException {
        try {

            this.validateEntity(model, ValidationHelper.OnSave.class);

            Optional<ProposalApprovalRule> exists = this.find(ProposalApprovalRule.builder().job(model.getJob()).build());
            if (exists.isPresent()) {
                throw new BusException("Já existe uma alçada de aprovação para esse cargo!");
            }

            Optional<ProposalApprovalRule> saved = this.dao.save(model);

            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.PROPOSAL_APPROVAL_RULE_INSERTED, userProfile);

            return saved;

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de cadastro de alçada comercial: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.save", new Object[]{ProposalApprovalRule.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<ProposalApprovalRule> update(ProposalApprovalRule model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, ValidationHelper.OnUpdate.class);

            Optional<ProposalApprovalRule> saved = this.dao.update(model);

            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.PROPOSAL_APPROVAL_RULE_UPDATED, userProfile);

            return saved;
        } catch (Exception e) {
            log.error("Erro no processo de atualização da alçada de aprovação: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.update", new Object[]{ProposalApprovalRule.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de exclusão inválido");
            }

            Optional<ProposalApprovalRule> entityDB = this.getById(id);
            if (!entityDB.isPresent()) {
                throw new BusException("Alçada de aprovação a ser excluído não existe.");
            }

            this.dao.delete(id);

            this.audit((entityDB.isPresent() ? entityDB.get() : null), AuditOperationType.PROPOSAL_APPROVAL_RULE_DELETED, userProfile);

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclusão de uma alçada de aprovaçã.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.delete", new Object[]{ProposalApprovalRule.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void validateRuleApproval(ProposalApproval model, UserProfileDTO userProfile) throws AppException, BusException {
    	BigDecimal limit = new BigDecimal(0);
    	Optional<Seller> seller = this.sellerService.getByUser(userProfile.getUser().getId());
    	if(seller.isPresent()) {
    		Optional<ProposalApprovalRule> proposalApprovalRule = this.getByJob(seller.get().getJob().getId());
    		if(proposalApprovalRule.isPresent()) {
                limit = new BigDecimal(proposalApprovalRule.get().getValue());
    		}
    	}
        
        if (model.getDiscount().compareTo(limit) == 1) {
            throw new BusException("Você não possui alçada para a aprovação deste desconto." +
                    " Sua alçada: R$ " + PortalNumberUtils.formatDoubleInCurrency(limit.doubleValue()) + "" +
                    " Desconto: R$ " + PortalNumberUtils.formatBigDecimal(model.getDiscount()));
        }
    }

    @Override
    public void audit(ProposalApprovalRule model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.auditService.save(objectMapper.writeValueAsString(model), operationType, userProfile);
        } catch (JsonProcessingException e) {
            throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
        }
    }

    private void validateEntity(ProposalApprovalRule model, Class<?> group) throws BusException {
        ValidationHelper.generateException(validator.validate(model, group));
    }


}
