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
import com.portal.dao.ICustomerDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CustomerModel;
import com.portal.service.IAuditService;
import com.portal.service.ICustomerService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CustomerService implements ICustomerService {

	@Autowired
	private ICustomerDAO dao;
	
	@Autowired
	private MessageSource messageSource;
	
    @Autowired 
	private IAuditService auditService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
	@Override
	public Optional<CustomerModel> find(CustomerModel model) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public Optional<CustomerModel> getById(Integer id) throws AppException, BusException {
		return dao.getById(id);
	}

	@Override
	public List<CustomerModel> list() throws AppException, BusException {
		return dao.list();
	}

	@Override
	public List<CustomerModel> search(CustomerModel model) throws AppException, BusException {
		return dao.search(model);
	}

	@Override
	public Optional<CustomerModel> saveOrUpdate(CustomerModel model, UserProfileDTO userProfile ) throws AppException, BusException {
		if(model.getId() != null && model.getId() > 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}

	@Override
	public Optional<CustomerModel> save(CustomerModel model, UserProfileDTO userProfile ) throws AppException, BusException {
		this.audit(model, AuditOperationType.CUSTOMER_INSERTED, userProfile);
		return dao.save(model);
	}

	@Override
	public Optional<CustomerModel> update(CustomerModel model, UserProfileDTO userProfile ) throws AppException, BusException {
		this.audit(model, AuditOperationType.CUSTOMER_UPDATED, userProfile);
		return dao.update(model);
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile ) throws AppException, BusException {
		Optional<CustomerModel> model = this.getById(id);
		if(model.isPresent()) {
			this.verifyCustomerConstraint(model.get());
			this.audit(model.get(), AuditOperationType.CUSTOMER_DELETED, userProfile);
			dao.delete(id);		
		}
	}
	
	@Override
	public void audit( CustomerModel model, AuditOperationType operationType, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			String details = "{}";
			details = objectMapper.writeValueAsString(model);
			
			this.auditService.save(details, operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public List<CustomerModel> listByUserId(Integer userId) throws AppException, BusException {
		return dao.listByUserId(userId);
	}
	
	@Override
	public List<CustomerModel> listByHoldingId(Integer holId) throws AppException, BusException {
		return dao.listByHoldingId(holId);
	}
	@Override
	public List<CustomerModel> listByUserHolding(Integer usrId, Integer holId) throws AppException, BusException {
		return dao.listByUserHolding(usrId, holId);
	}

	@Override
	public Optional<CustomerModel> findByCNPJ(String cnpj) throws AppException, BusException {
		return dao.findByCNPJ(cnpj);
	}

	@Override
	public void verifyCustomerConstraint(CustomerModel customer) throws AppException, BusException {
		if(dao.verifyCustomerConstraint(customer.getId())) {
			throw new AppException(this.messageSource.getMessage("error.holding.customerconstraint", new Object [] { customer.getName()}, LocaleContextHolder.getLocale()));
		}
	}
	
}
