package com.portal.service.imp;

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

import com.portal.dao.IProposalFollowUpDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.ProposalFollowUp;
import com.portal.service.IProposalFollowUp;
import com.portal.validators.ValidationHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalFollowUpService implements IProposalFollowUp {

    private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "vhe_id");

    @Autowired
    public MessageSource messageSource;

    @Autowired
    private IProposalFollowUpDAO dao;

    @Autowired
    private Validator validator;


    @Override
    public Optional<ProposalFollowUp> save(ProposalFollowUp model) throws AppException {
        return Optional.empty();
    }

    @Override
    public Optional<ProposalFollowUp> update(ProposalFollowUp model) throws AppException {
        return Optional.empty();
    }

    @Override
    public Optional<ProposalFollowUp> find(ProposalFollowUp model) throws AppException, BusException {
        return Optional.empty();
    }

    @Override
    public Optional<ProposalFollowUp> getById(Integer id) throws AppException, BusException {
        try {

            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            return this.dao.getById(id);

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar um Follow Up pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Brand.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<ProposalFollowUp> listAll(Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.listAll(pageable);

        } catch (Exception e) {
            log.error("Erro no processo de listar Follow up.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{ProposalFollowUp.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<ProposalFollowUp> list() throws AppException, BusException {
        return null;
    }

    @Override
    public List<ProposalFollowUp> search(ProposalFollowUp model) throws AppException, BusException {
        return dao.search(model);
    }

    @Override
    public Optional<ProposalFollowUp> saveOrUpdate(ProposalFollowUp model, UserProfileDTO userProfile) throws AppException, BusException {
        return Optional.empty();
    }

    @Override
    public Optional<ProposalFollowUp> save(ProposalFollowUp model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, ValidationHelper.OnSave.class);
            Optional<ProposalFollowUp> saved = this.dao.save(model);
            return saved;
        } catch (Exception e) {
            log.error("Erro no processo de cadastro do detalhe de Follow Up: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.save", new Object[]{ProposalFollowUp.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<ProposalFollowUp> update(ProposalFollowUp model, UserProfileDTO userProfile) throws AppException, BusException {
    	 try {
             this.validateEntity(model, ValidationHelper.OnUpdate.class);
             Optional<ProposalFollowUp> saved = this.dao.update( model );
             return saved;
         } catch (Exception e) {
             log.error( "Erro no processo de atualizacao de Follow Up: {}", model, e );
             throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { ProposalFollowUp.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
         }
     }

    @Override
    public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de exclusão inválido.");
            }
            Optional<ProposalFollowUp> entityDB = this.getById(id);
            if (entityDB == null || !entityDB.isPresent()) {
                throw new BusException("O Follow Up a ser excluído não existe.");
            }
            this.audit((entityDB.isPresent() ? entityDB.get() : null), AuditOperationType.PROPOSAL_FOLLOW_UP_DELETED, userProfile);
            this.dao.delete(id);

        } catch (Exception e) {
            log.error("Erro no processo de exclusão do follow up.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.delete", new Object[]{Brand.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void audit(ProposalFollowUp model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {

    }

    @Override
    public void delete(Integer id) throws AppException {
    	dao.delete(id);
    }

    private void validateEntity(ProposalFollowUp model, Class<?> group) throws AppException, BusException {
        ValidationHelper.generateException(validator.validate(model, group));
    }
}


