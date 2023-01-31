package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PortionModel;

public interface IPortionService extends IBaseService<PortionModel> {

    public List<PortionModel> search(PortionModel model, Pageable pageable) throws AppException, BusException;
     List<PortionModel> search(final String text) throws AppException, BusException;

}
