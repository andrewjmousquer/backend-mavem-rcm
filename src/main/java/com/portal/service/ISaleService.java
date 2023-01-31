package com.portal.service;

import com.portal.exceptions.AppException;
import com.portal.model.SaleModel;

public interface ISaleService extends IBaseService<SaleModel> {
	
	public Long getTotalRecords(SaleModel model) throws AppException;

}
