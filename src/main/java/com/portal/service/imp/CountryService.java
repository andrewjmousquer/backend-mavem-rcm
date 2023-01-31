package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.ICountryDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CountryModel;
import com.portal.service.ICountryService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CountryService implements ICountryService {

	@Autowired
	private ICountryDAO dao;

	@Override
	public Optional<CountryModel> find(CountryModel model) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public Optional<CountryModel> getById(Integer id) throws AppException, BusException {
		return dao.getById(id);
	}

	@Override
	@Cacheable(value="listAllCountries")
	public List<CountryModel> list() throws AppException, BusException {
		return dao.list();
	}

	@Override
	public List<CountryModel> search(CountryModel model) throws AppException, BusException {
		return dao.search(model);
	}

	@Override
	public Optional<CountryModel> saveOrUpdate(CountryModel model, UserProfileDTO userProfile) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public Optional<CountryModel> save(CountryModel model, UserProfileDTO userProfile) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public Optional<CountryModel> update(CountryModel model, UserProfileDTO userProfile) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
	}

	@Override
	public void audit(CountryModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
	}

}
