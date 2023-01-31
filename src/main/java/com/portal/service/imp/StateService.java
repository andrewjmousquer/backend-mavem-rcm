package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IStateDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.StateModel;
import com.portal.service.IStateService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class StateService implements IStateService {

	@Autowired
	private IStateDAO dao;

	@Override
	public Optional<StateModel> find(StateModel model) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public Optional<StateModel> getById(Integer id) throws AppException, BusException {
		return dao.getById(id);
	}

	@Override
	@Cacheable(value="listAllStates")
	public List<StateModel> list() throws AppException, BusException {
		return dao.list();
	}

	@Override
	public List<StateModel> search(StateModel model) throws AppException, BusException {
		return dao.search(model);
	}

	@Override
	public Optional<StateModel> saveOrUpdate(StateModel model, UserProfileDTO userProfile) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public Optional<StateModel> save(StateModel model, UserProfileDTO userProfile) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public Optional<StateModel> update(StateModel model, UserProfileDTO userProfile) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		dao.delete(id);
	}

	@Override
	public void audit(StateModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		throw new NotImplementedException( "StateService.audit Not Implemented" );
	}

	@Override
	public List<StateModel> getByCountryId(Integer couId) throws AppException{
		return dao.getByCountryId(couId);
	}
}
