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
import com.portal.dao.IPartnerPersonCommissionDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PartnerPersonCommission;
import com.portal.service.IAuditService;
import com.portal.service.IPartnerPersonCommissionService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PartnerPersonCommissionService implements IPartnerPersonCommissionService {

	@Autowired
	private IPartnerPersonCommissionDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	public Optional<PartnerPersonCommission> find(PartnerPersonCommission model) throws AppException, BusException {
		return dao.find(model);
	}
	
	public List<PartnerPersonCommission> list(PartnerPersonCommission model) throws AppException, BusException {
		return dao.list(model);
	}
	
	public Optional<PartnerPersonCommission> save(PartnerPersonCommission model, UserProfileDTO profile) throws AppException, BusException {
		audit(model, AuditOperationType.PARTNER_PERSON_COMMISSION_INSERTED, profile);
		return dao.save(model);
	}
	
	public Optional<PartnerPersonCommission> update(PartnerPersonCommission model, UserProfileDTO profile) throws AppException, BusException {
		audit(model, AuditOperationType.PARTNER_PERSON_COMMISSION_UPDATED, profile);
		return dao.update(model);
	}

	public void delete(PartnerPersonCommission model, UserProfileDTO profile) throws AppException, BusException {
		audit(model, AuditOperationType.PARTNER_PERSON_COMMISSION_DELETED, profile);
		dao.delete(model);
	}
	
	@Override
	public void audit(PartnerPersonCommission model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
}
