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
import com.portal.dao.IAddressDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.AddressModel;
import com.portal.model.CityModel;
import com.portal.service.IAddressService;
import com.portal.service.IAuditService;
import com.portal.service.ICityService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class AddressService implements IAddressService {

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private IAddressDAO dao;
	
    @Autowired 
	private IAuditService auditService;
	
	@Autowired
	private ICityService cityService;
	
	@Autowired
	private ObjectMapper objectMapper;
		
	public Optional<AddressModel> getById(Integer id) throws AppException, BusException {
		return this.dao.getById(id);
	}
	
	@Override
	public Optional<AddressModel> find(AddressModel model) throws AppException, BusException {
		return dao.find(model);
	}	
	
	public List<AddressModel> list() throws AppException, BusException {
		return this.dao.list();
	}

	public List<AddressModel> search(AddressModel model) throws AppException, BusException {
		return this.dao.search(model);
	}
	
	public Optional<AddressModel> saveOrUpdate(AddressModel model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return update(model, userProfile);
		} else {
			return save(model, userProfile);
		}
	}

	public Optional<AddressModel> save(AddressModel model, UserProfileDTO userProfile) throws BusException, AppException {
		this.validateAddress(model);
		this.audit(model, AuditOperationType.ADDRESS_INSERTED, userProfile);
		return this.dao.save(model);
	}

	public Optional<AddressModel>  update(AddressModel model, UserProfileDTO userProfile) throws AppException, BusException {
		this.validateAddress(model);
		this.audit(model, AuditOperationType.ADDRESS_UPDATED, userProfile);
		return this.dao.update(model);
	}

	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<AddressModel> addressModel = dao.getById(id);
		if(addressModel.isPresent()) {
			this.audit(addressModel.get(), AuditOperationType.ADDRESS_UPDATED, userProfile);
			this.dao.delete(id);
		}
	}
	
	private void validateAddress( AddressModel model ) throws BusException, AppException {
		if( model != null && (model.getCity() == null || model.getCity().getId() == null) ) {
			throw new BusException( this.messageSource.getMessage("error.address.city", null, LocaleContextHolder.getLocale()) );
		}
	}

	public List<AddressModel> fillCity( List<AddressModel> addresses ) throws AppException, BusException {
		if( addresses != null && !addresses.isEmpty() ) {
			addresses.forEach(address -> {
				try {
					this.fillCity(address);
				} catch (AppException | BusException e) {
					e.printStackTrace();
				}
			});
		}
		return addresses;
	}

	public Optional<AddressModel> fillCity( AddressModel address ) throws AppException, BusException {
		Optional<AddressModel> addressModel = Optional.empty();
		
		if( address != null && address.getCity() != null ) {
			addressModel = Optional.ofNullable(address);
			Optional<CityModel> city = this.cityService.getById( address.getCity().getId() ); 
			
			if(city.isPresent()) {
				addressModel.get().setCity( city.get() );
			}
		}
		return addressModel;
	}

	@Override
	public void audit(AddressModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			String details = "{}";
			details = objectMapper.writeValueAsString(model);
			
			this.auditService.save(details, operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}

}
