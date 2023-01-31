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
import com.portal.dao.IContactDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Contact;
import com.portal.service.IAuditService;
import com.portal.service.IContactService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ContactService implements IContactService {

	@Autowired
	private IContactDAO dao;
	
	@Autowired
	private IAuditService auditService;
	
    @Autowired
    public MessageSource messageSource;
    
    @Autowired
    private ObjectMapper objectMapper;
	
	@Override
	public Optional<Contact> getById(Integer id) throws AppException, BusException {
		return this.dao.getById(id);
	}

	@Override
	public Optional<Contact> find(Contact model) throws AppException {
		return this.dao.find(model);
	}
	
	@Override
	public List<Contact> list() throws AppException, BusException {
		return this.dao.list();
	}
	
	@Override
	public List<Contact> search(Contact model) throws AppException {
		return this.dao.search(model);
	}
	
	@Override
	public Optional<Contact> saveOrUpdate(Contact model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}

	@Override
	public Optional<Contact> save(Contact model, UserProfileDTO userProfile) throws AppException, BusException {
		return this.dao.save(model);
	}
	
	@Override
	public Optional<Contact> update(Contact model, UserProfileDTO userProfile) throws AppException, BusException {
		return this.dao.update(model);
	}
	
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		this.dao.delete(id);
	}

	@Override
	public void audit(Contact model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			String details = "{}";
			details = objectMapper.writeValueAsString(model);
			
			this.auditService.save(details, operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	@Override
	public List<Contact> findByPerson(Integer id) throws AppException, BusException {
		return this.dao.findByPerson(id);
	}
}
