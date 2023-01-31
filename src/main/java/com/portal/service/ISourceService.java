package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Source;

public interface ISourceService extends IBaseService<Source> {
	
	public List<Source> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<Source> find( Source model, Pageable pageable ) throws AppException, BusException;
	
	public List<Source> search( Source model, Pageable pageable ) throws AppException, BusException;
	
}
