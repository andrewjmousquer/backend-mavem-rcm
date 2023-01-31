package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;

public interface IBrandService extends IBaseService<Brand> {
	
	public List<Brand> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<Brand> find( Brand brand, Pageable pageable ) throws AppException, BusException;
	
	public List<Brand> search( Brand brand, Pageable pageable ) throws AppException, BusException;
	
}
