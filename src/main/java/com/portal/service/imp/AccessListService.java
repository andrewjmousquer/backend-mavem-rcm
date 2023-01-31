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
import com.portal.dao.IAccessListDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.AccessListModel;
import com.portal.service.IAccessListCheckPointService;
import com.portal.service.IAccessListMenuService;
import com.portal.service.IAccessListService;
import com.portal.service.IAuditService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class AccessListService implements IAccessListService {

	@Autowired
	private IAccessListDAO dao;

	@Autowired
	private IAccessListMenuService accessListMenuService;
	
	@Autowired
	private IAccessListCheckPointService accessListCheckpointService;
	
	@Autowired
	private MessageSource messageSource;
	
    @Autowired 
	private IAuditService auditService;
    
    @Autowired
    private ObjectMapper objectMapper;
	
	@Override
	public Optional<AccessListModel> find(AccessListModel model) throws AppException, BusException {
		return dao.find(model);
	}
	
	@Override
	public Optional<AccessListModel> getById(Integer id) throws AppException, BusException {
		Optional<AccessListModel> accessList = dao.getById(id);
		if(accessList.isPresent()) {
			accessList.get().setMenus(accessListMenuService.listMenuByAccessList(accessList.get().getId()));
			accessList.get().setCheckpoints(accessListCheckpointService.listCheckpointByAccessList(null, accessList.get().getId()));
		}
		
		return accessList;
	}
	
	@Override
	public List<AccessListModel> list() throws AppException, BusException {
		return dao.list();
	}

	@Override
	public List<AccessListModel> search(AccessListModel model) throws AppException, BusException {
		return dao.search(model);
	}

	@Override
	public Optional<AccessListModel> saveOrUpdate(AccessListModel model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model.getId() != null && model.getId() > 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}
	
	@Override
	public Optional<AccessListModel> save(AccessListModel model, UserProfileDTO userProfile) throws AppException, BusException {
		this.validateAccesslist(model);
		Optional<AccessListModel> accessList = dao.save(model);
		this.accessListMenuService.saveAccessListMenus(accessList.get());
		this.accessListCheckpointService.saveAccessListCheckpoints(accessList.get());
		this.audit(model, AuditOperationType.PERSON_INSERTED, userProfile);
		return accessList;
	}
	
	
	@Override
	public Optional<AccessListModel> update(AccessListModel model, UserProfileDTO userProfile) throws AppException, BusException {
		this.validateAccesslist(model);
		this.accessListMenuService.saveAccessListMenus(model);
		this.accessListCheckpointService.saveAccessListCheckpoints(model);
		this.audit( model, AuditOperationType.ACCESS_LIST_UPDATED, userProfile );
		return this.dao.update(model);
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<AccessListModel> model = this.getById(id);
		if(model.isPresent()) {
			this.validateUserOnDelete(id);
			this.accessListMenuService.delete(id);
			this.accessListCheckpointService.delete(null, id);
			
			this.audit(model.get(), AuditOperationType.ACCESS_LIST_DELETED, userProfile);
			dao.delete(id);
		}
	}
	
	private void validateAccesslist(AccessListModel accesslist) throws AppException, BusException {
		if(this.dao.hasDuplicatedName(accesslist)) {
			throw new BusException(this.messageSource.getMessage("error.accesslist.uqname", null, LocaleContextHolder.getLocale()));
		}
	}
	
	private void validateUserOnDelete(Integer aclId) throws AppException, BusException {
		if(this.dao.hasUserAccessList(aclId)) {
			throw new BusException(this.messageSource.getMessage("error.accesslist.constrainuser", null, LocaleContextHolder.getLocale()));
		}
	}
	
	@Override
	public void audit(AccessListModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			String details = "{}";
			details = objectMapper.writeValueAsString(model);
			
			this.auditService.save(details, operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}

}
