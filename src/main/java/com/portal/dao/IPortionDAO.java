package com.portal.dao;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PortionModel;

import java.util.List;

public interface IPortionDAO extends IBaseDAO<PortionModel>{
    List<PortionModel> search(final String text) throws AppException, BusException ;

}
