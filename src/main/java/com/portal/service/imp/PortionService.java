package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IPortionDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PortionModel;
import com.portal.service.IAuditService;
import com.portal.service.IPortionService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PortionService implements IPortionService {

	@Autowired
	private IPortionDAO dao;

	@Autowired
	private IAuditService auditService;
	
    @Autowired
    private ObjectMapper objectMapper;
	
    @Autowired
    public MessageSource messageSource;
    
	@Override
	public Optional<PortionModel> find(PortionModel model) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public Optional<PortionModel> getById(Integer id) throws AppException, BusException {
		return dao.getById(id);
	}

	@Override
	public List<PortionModel> list() throws AppException, BusException {
		return dao.list();
	}

	@Override
	public List<PortionModel> search(PortionModel model) throws AppException, BusException {
		return dao.search(model);
	}
	@Override
	public List<PortionModel> search(final String text) throws AppException, BusException {
		return dao.search(text);
	}

	@Override
	public Optional<PortionModel> saveOrUpdate(PortionModel model, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<PortionModel> returnModel = Optional.empty();
		if(model.getId() != null && model.getId() > 0) {
			returnModel = this.update(model, userProfile);
		} else {
			returnModel = this.save(model, userProfile);
		}
		
		return returnModel;
	}

	@Override
	public Optional<PortionModel> save(PortionModel model, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<PortionModel> name = this.find(new PortionModel(model.getName(), model.getPaymentType()));
			if(name.isPresent()) {
				if(name.get() != null && (model.getName().equals(name.get().getName())) && (model.getPaymentType().getId().equals(name.get().getPaymentType().getId()))){
					throw new BusException(this.messageSource.getMessage("error.portion.duplicated", null, LocaleContextHolder.getLocale()));
				}
			}

		Optional<PortionModel> portionSaved = this.dao.save(model);
		
		this.audit( portionSaved.get(), AuditOperationType.USER_INSERTED, userProfile );
	
		return portionSaved;
	}

	@Override
	public Optional<PortionModel> update(PortionModel model, UserProfileDTO userProfile) throws AppException, BusException {
		this.audit(model, AuditOperationType.PORTION_EDITED, userProfile);
		return dao.update(model);
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<PortionModel> model = this.getById(id);
		if(model.isPresent()) {
			this.audit(model.get(), AuditOperationType.PORTION_DELETED, userProfile);
			dao.delete(id);		
		}
	}

	@Override
	public void audit(PortionModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
    	try {
			String details = "{}";
			details = objectMapper.writeValueAsString(model);
			
			this.auditService.save(details, operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public List<PortionModel> search(PortionModel model, Pageable pageable) throws AppException, BusException {
		return null;
	}
}
