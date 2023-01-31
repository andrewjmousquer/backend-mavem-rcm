package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.ICityDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CityModel;
import com.portal.model.StateModel;
import com.portal.service.ICityService;
import com.portal.service.IStateService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CityService implements ICityService {

	@Autowired
	private ICityDAO dao;
	
	@Autowired
	private IStateService stateService;

	@Override
	public Optional<CityModel> find(CityModel model) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public Optional<CityModel> getById(Integer id) throws AppException, BusException {
		return dao.getById(id);
	}

	@Override
	public List<CityModel> list() throws AppException, BusException {
		return dao.list();
	}

	@Override
	public List<CityModel> search(CityModel model) throws AppException, BusException {
		return dao.search(model);
	}

	@Override
	public Optional<CityModel> saveOrUpdate(CityModel model, UserProfileDTO userProfile) throws AppException, BusException {
	 if (model != null && model.getId() != null && model.getId() != 0) {
            return this.update(model, userProfile);
        } else {
        	return this.save(model, userProfile);
        }
	 }

	@Override
	public Optional<CityModel> save(CityModel model, UserProfileDTO userProfile) throws AppException, BusException {
		return dao.save(model);
	}

	@Override
	public Optional<CityModel> update(CityModel model, UserProfileDTO userProfile) throws AppException, BusException {
		return dao.update(model);
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		dao.delete(id);
	}

	@Override
	public void audit(CityModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		throw new NotImplementedException( "CityService.save Not Implemented" );
	}

	@Override
	public List<CityModel> listAllFillState() throws AppException, BusException {
		return dao.listAllFillState();
	}

	@Override
	public List<CityModel> findFillState(CityModel model) throws AppException, BusException {
		return dao.findFillState(model);
	}

	@Override
	public List<CityModel> fillState(List<CityModel> cities) throws AppException, BusException {
		if( cities != null && !cities.isEmpty() ) {
			cities.forEach(city -> {
				try {
					this.fillState( city );
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		return cities;
	}

	@Override
	public Optional<CityModel> fillState(CityModel model) throws AppException, BusException {
		Optional<CityModel> city = Optional.empty();
		
		if( model != null && model.getState() != null ) {
			Optional<StateModel> cityModel = this.stateService.getById( model.getState().getId());
			city = Optional.ofNullable(model);
			if(cityModel.isPresent()) {
				city.get().setState( cityModel.get()  );
			}
		}
		return city;
	}

	@Override
	public List<CityModel> getByState(Integer steId) throws AppException, BusException {
		return dao.getByState(steId);
	}

}
