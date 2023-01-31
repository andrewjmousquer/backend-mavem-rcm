package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Model;

public interface IModelService extends IBaseService<Model> {
	
	public List<Model> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<Model> find( Model model, Pageable pageable ) throws AppException, BusException;
	
	public List<Model> search( Model model, Pageable pageable ) throws AppException, BusException;

    List<Model> listAllByBrand(int id, Pageable pageReq) throws AppException;
}
