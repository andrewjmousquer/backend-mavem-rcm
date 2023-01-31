package com.portal.service.imp;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IParameterDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ParameterModel;
import com.portal.service.IAuditService;
import com.portal.service.IParameterService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ParameterService implements IParameterService {
	
	@Autowired
	private IParameterDAO dao;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Override
	public Optional<ParameterModel> getById(Integer id) throws AppException, BusException {
		return this.dao.getById(id);
	}
	
	@Override
	public Optional<ParameterModel> find(ParameterModel model) throws AppException {
		return this.dao.find(model);
	}
	
	@Override
	public List<ParameterModel> search(ParameterModel model) throws AppException {
		return this.dao.search(model);
	}

	@Override
	public List<ParameterModel> list() throws AppException, BusException {
		return this.dao.list();
	}
	
	@Override
	public Optional<ParameterModel> saveOrUpdate( ParameterModel parameter, UserProfileDTO userProfile ) throws AppException, BusException {
		if( parameter != null && ( parameter.getId() == null || parameter.getId().equals( 0 ) ) ) {
			return this.save( parameter, userProfile );
		} else {
			return this.update( parameter, userProfile );
		}
	}

	@Override
	public Optional<ParameterModel> save( ParameterModel parameter, UserProfileDTO userProfile ) throws AppException, BusException {
		this.validateDuplicateParameter( parameter );
		Optional<ParameterModel> parameterModel = this.dao.save( parameter );
		this.audit( parameterModel.get(), AuditOperationType.PARAMETER_INSERTED, userProfile );
		return parameterModel;
	}

	@Override
	public Optional<ParameterModel> update(ParameterModel parameter, UserProfileDTO userProfile) throws AppException, BusException {
		this.audit( parameter, AuditOperationType.PARAMETER_UPDATED, userProfile );
		return this.dao.update( parameter );
	}
	
	@Override
	public void delete( Integer id, UserProfileDTO userProfile ) throws AppException, BusException {
		Optional<ParameterModel> parameterModel = this.getById(id);
		if(parameterModel.isPresent()) {
			this.audit(parameterModel.get(), AuditOperationType.PARAMETER_DELETED, userProfile );
			this.dao.delete(id);
		}
	}

	@Override
	public void audit( ParameterModel model, AuditOperationType operationType, UserProfileDTO userProfile ) throws AppException, BusException {
		String details = String.format("prmId:%s;name:%s;value:%s;description:%s",model.getId(), model.getName(), model.getValue(), model.getDescription());
		this.auditService.save( details, operationType, userProfile );
	}

	@Override
	public String getValueOf(String name) {
		try {
			Optional<ParameterModel> optional = find(new ParameterModel(name));
			if (optional.isPresent()) {
				return optional.get().getValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void validateDuplicateParameter( ParameterModel parameter ) throws AppException, BusException {
		if( parameter != null ) {
			Optional<ParameterModel> foundParam = this.dao.find( parameter );
			if(foundParam.isPresent()) {
				throw new BusException(this.messageSource.getMessage("error.parameter.duplicate", null, LocaleContextHolder.getLocale()));
			}
		}
	}

	@Override
	public List<String> getListFromConcatenatedParameter(String parameter) throws AppException{
		Optional<ParameterModel> optParm = this.find( new ParameterModel( parameter ) );
		if( optParm != null && optParm.isPresent() ) {
			List<String> listReturn = new LinkedList<String>();
			String s = optParm.get().getValue();

			if( s != null && !s.isEmpty() ) {
				String[] idsStatus = s.split(",");
				//XXX: ###Dúvida aqui###
				for (String id : idsStatus) {
					try {
						listReturn.add( id  );
					} catch (Exception e) {
					}
				}
			}
			
			return listReturn;
		}
		
		return null;
	}
	
}
