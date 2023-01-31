package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProductModel;

public interface IProductModelService extends IBaseService<ProductModel> {


	public List<ProductModel> listAll(Pageable pageable ) throws AppException, BusException;

	public List<ProductModel> find( ProductModel model, Pageable pageable ) throws AppException, BusException;

	public List<ProductModel> search( ProductModel model, Pageable pageable ) throws AppException, BusException;

    List<ProductModel> getByProduct(Integer id) throws AppException;
}
