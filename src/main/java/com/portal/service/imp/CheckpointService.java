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
import com.portal.dao.ICheckpointDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CheckpointModel;
import com.portal.service.IAccessListCheckPointService;
import com.portal.service.IAuditService;
import com.portal.service.ICheckpointService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CheckpointService implements ICheckpointService {
	
	@Autowired
	private ICheckpointDAO dao;

	@Autowired
	private IAccessListCheckPointService accessListCheckPointService;

	@Autowired
	private MessageSource messageSource;
        
    @Autowired 
	private IAuditService auditService;
    
    @Autowired
    private ObjectMapper objectMapper;
	
	@Override
	public Optional<CheckpointModel> find(CheckpointModel model) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public Optional<CheckpointModel> getById(Integer id) throws AppException, BusException {
		return dao.getById(id);
	}

	@Override
	public List<CheckpointModel> list() throws AppException, BusException {
		return dao.list();
	}

	@Override
	public List<CheckpointModel> search(CheckpointModel model) throws AppException, BusException {
		return dao.search(model);
	}
	
	@Override
	public List<CheckpointModel> listCheckpoints(Integer ckpId, Integer aclId) throws AppException, BusException {
		return accessListCheckPointService.listCheckpointByAccessList(ckpId, aclId);
	}

	@Override
	public Optional<CheckpointModel> saveOrUpdate(CheckpointModel model, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<CheckpointModel> exists = this.dao.find(new CheckpointModel(model.getName()));
		if(exists.isPresent()) {
			if((model.getId() != null && model.getId() > 0 && !exists.get().getId().equals(model.getId())) || model.getId() == null) {
				throw new BusException(this.messageSource.getMessage("error.checkpoint.checkpoinExists", null, LocaleContextHolder.getLocale()));
			}
		}
		
		Optional<CheckpointModel> savedModel = Optional.empty();
		
		if(model.getId() != null && model.getId() > 0) {
			savedModel = this.update(model, userProfile);
		} else {
			savedModel = this.save(model, userProfile);
		}
		
		return savedModel;
	}

	@Override
	public Optional<CheckpointModel> save(CheckpointModel model, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<CheckpointModel> modelInserted = dao.save(model);
		this.audit(modelInserted.get(), AuditOperationType.CHECKPOINT_INSERTED, userProfile);
		return modelInserted;
	}

	@Override
	public Optional<CheckpointModel> update(CheckpointModel model, UserProfileDTO userProfile)	throws AppException, BusException {
		this.audit(model, AuditOperationType.CHECKPOINT_UPDATED, userProfile);
		return dao.update(model);
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<CheckpointModel> model = this.getById(id);
		if(model.isPresent()) {
			this.accessListCheckPointService.delete(model.get().getId(), null);

			this.audit(model.get(), AuditOperationType.CHECKPOINT_DELETED, userProfile);
			this.dao.delete(id);		
		}
	}

	@Override
	public void audit(CheckpointModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			String details = "{}";
			details = objectMapper.writeValueAsString(model);
			
			this.auditService.save(details, operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}	
	
	@Override
	public List<CheckpointModel> getByCurrentUser(Integer aclId) throws AppException, BusException {
		return accessListCheckPointService.listCheckpointByAccessList(null, aclId);
	}
}
