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
import com.portal.dao.ISaleDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.SaleModel;
import com.portal.service.IAuditService;
import com.portal.service.ISaleService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class SaleService implements ISaleService {

	@Autowired
	private ISaleDAO dao;

	@Autowired
	private IAuditService auditService;
	
    @Autowired
    private ObjectMapper objectMapper;
	
    @Autowired
    public MessageSource messageSource;
    
	@Override
	public Optional<SaleModel> find(SaleModel model) throws AppException, BusException {
		return dao.find(model);
	}
	
	public Long getTotalRecords(SaleModel model) throws AppException {
		return dao.getTotalRecords(model);
	}

	@Override
	public Optional<SaleModel> getById(Integer id) throws AppException, BusException {
		return dao.getById(id);
	}

	@Override
	public List<SaleModel> list() throws AppException, BusException {
		return dao.list();
	}

	@Override
	public List<SaleModel> search(SaleModel model) throws AppException, BusException {
		return dao.search(model);
	}

	@Override
	public Optional<SaleModel> saveOrUpdate(SaleModel model, UserProfileDTO userProfile) throws AppException, BusException {
		
		model.setUser(userProfile.getUser());
		
		Optional<SaleModel> returnModel = Optional.empty();
		if(model.getId() != null && model.getId() > 0) {
			returnModel = this.update(model, userProfile);
		} else {
			returnModel = this.save(model, userProfile);
		}
		
		return returnModel;
	}

	@Override
	public Optional<SaleModel> save(SaleModel model, UserProfileDTO userProfile) throws AppException, BusException {
		this.audit(model, AuditOperationType.SALE_INSERTED, userProfile);
		return dao.save(model);
	}

	@Override
	public Optional<SaleModel> update(SaleModel model, UserProfileDTO userProfile) throws AppException, BusException {
		this.audit(model, AuditOperationType.SALE_EDITED, userProfile);
		return dao.update(model);
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<SaleModel> model = this.getById(id);
		if(model.isPresent()) {
			this.audit(model.get(), AuditOperationType.SALE_DELETED, userProfile);
			dao.delete(id);		
		}
	}

	@Override
	public void audit(SaleModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
    	try {
			String details = "{}";
			details = objectMapper.writeValueAsString(model);
			
			this.auditService.save(details, operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}

}
