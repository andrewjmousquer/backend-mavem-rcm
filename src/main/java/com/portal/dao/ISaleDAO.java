package com.portal.dao;

import com.portal.exceptions.AppException;
import com.portal.model.SaleModel;

public interface ISaleDAO extends IBaseDAO<SaleModel>{

	public Long getTotalRecords(SaleModel model) throws AppException;

}
