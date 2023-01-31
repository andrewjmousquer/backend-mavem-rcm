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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IJobDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Job;
import com.portal.service.IAuditService;
import com.portal.service.IJobService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class JobService implements IJobService {

    @Autowired
    private IJobDAO dao;

    @Autowired
    private Validator validator;

    @Autowired
    private MessageSource messageSource;
    
    @Autowired
    private IAuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "job_id");

    @Override
    public Optional<Job> find(Job model) throws AppException, BusException {
        List<Job> jobList = this.find(model, null);
        return Optional.ofNullable(jobList != null ? jobList.get(0) : null);
    }

    @Override
    public List<Job> find(Job model, Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.find(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de buscar os cargos.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{Job.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<Job> getById(Integer id) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }
            return this.dao.getById(id);

        } catch (Exception e) {
            log.error("Erro ao consultar um cargo pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getById", new Object[]{Job.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Job> list() throws AppException, BusException {
        return this.listAll(null);
    }

    @Override
    public List<Job> listAll(Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }
            return this.dao.listAll(pageable);
        } catch (Exception e) {
            log.error("Erro no processo de listar os cargos.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{Job.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Job> search(Job model) throws AppException, BusException {
        return this.search(model, null);
    }

    @Override
    public List<Job> search(Job model, Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }
            return this.dao.search(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de procurar os cargos .", e);
            throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{Job.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Job> searchForm(String text, Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.searchForm(text, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de procurar os cargos.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{Job.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<Job> saveOrUpdate(Job model, UserProfileDTO userProfile) throws AppException, BusException {
        if (model.getId() != null && model.getId() > 0) {
            return this.update(model, userProfile);
        } else {
            return this.save(model, userProfile);
        }
    }

    @Override
    public Optional<Job> save(Job model, UserProfileDTO userProfile) throws AppException, BusException {
        try {

            this.validateEntity(model, OnSave.class);

            Optional<Job> saved = this.dao.save(model);

            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.JOB_INSERTED, userProfile);

            return saved;

        } catch (Exception e) {
            log.error("Erro no processo de cadastro de cargo: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.save", new Object[]{Job.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<Job> update(Job model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, OnUpdate.class);

            Optional<Job> saved = this.dao.update(model);

            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.JOB_UPDATED, userProfile);

            return saved;
        } catch (Exception e) {
            log.error("Erro no processo de atualização do cargo: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.update", new Object[]{Job.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de exclusão inválido");
            }

            Optional<Job> entityDB = this.getById(id);
            if (!entityDB.isPresent()) {
                throw new BusException("Cargo a ser excluído não existe.");
            }

            this.dao.delete(id);

            this.audit((entityDB.isPresent() ? entityDB.get() : null), AuditOperationType.JOB_DELETED, userProfile);

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclusão de um cargo .", e);
            throw new AppException(this.messageSource.getMessage("error.generic.delete", new Object[]{Job.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void audit(Job model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.auditService.save(objectMapper.writeValueAsString(model), operationType, userProfile);
        } catch (JsonProcessingException e) {
            throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
        }
    }

    private void validateEntity(Job model, Class<?> group) throws BusException {
        ValidationHelper.generateException(validator.validate(model, group));
    }

}
