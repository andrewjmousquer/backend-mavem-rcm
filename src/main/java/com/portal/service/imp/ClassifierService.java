package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IClassifierDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Classifier;
import com.portal.service.IAuditService;
import com.portal.service.IClassifierService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ClassifierService implements IClassifierService {

	@Autowired
	private IClassifierDAO dao;

	@Autowired
	private IAuditService auditService;
	
    @Autowired
    public MessageSource messageSource;
    
    @Autowired
    private ObjectMapper objectMapper;
	
	@Override
	public Optional<Classifier> find(Classifier model) throws AppException, BusException {
		Optional<Classifier> returnModel = dao.find(model);
		if(returnModel.isPresent()) {
			return returnModel;
		}
		return Optional.empty() ;
	}
	
	@Override
	public Optional<Classifier> getById(Integer id) throws AppException, BusException {
		return dao.getById(id);
	}
	
	@Override
	public List<Classifier> list() throws AppException, BusException {
		return dao.list();
	}

	@Override
	public List<Classifier> search(Classifier model) throws AppException, BusException {
		return dao.search(model);
	}
	
	@Override
	public List<Classifier> searchByNameOrType(Classifier model) throws AppException, BusException {
		return dao.searchByNameOrType(model);
	}

	@Override
	public Optional<Classifier> saveOrUpdate(Classifier model, UserProfileDTO userProfile ) throws AppException, BusException {
		Optional<Classifier> exists = this.getById(model.getId());
		if(exists.isPresent()) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}
	
	@Override
	public Optional<Classifier> save(Classifier model, UserProfileDTO userProfile ) throws AppException, BusException {
		Optional<Classifier> savedClassifier = dao.save(model);
		 if(savedClassifier.isPresent()) {
			 audit(savedClassifier.get(), AuditOperationType.CLASSIFIER_INSERTED, userProfile);
		 }
		 
		 return savedClassifier;
	}
	
	@Override
	public Optional<Classifier> update(Classifier model, UserProfileDTO userProfile ) throws AppException, BusException {
		Optional<Classifier> savedClassifier = dao.update(model);
		 if(savedClassifier.isPresent()) {
			 audit(savedClassifier.get(), AuditOperationType.CLASSIFIER_UPDATED, userProfile);
		 }
		 
		 return savedClassifier;
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile ) throws AppException, BusException {
		 Optional<Classifier> deletedClassifier = dao.getById(id);
		 if(deletedClassifier.isPresent()) {
			audit(deletedClassifier.get(), AuditOperationType.CLASSIFIER_DELETED, userProfile);
			dao.delete(id);		
		 }
	
	}
	
    @Override
	public void audit( Classifier model, AuditOperationType operationType, UserProfileDTO userProfile ) throws AppException, BusException {
       	try {
			String details = "{}";
			details = objectMapper.writeValueAsString(model);
			
			auditService.save(details, operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
}
