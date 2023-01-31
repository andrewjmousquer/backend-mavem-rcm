package com.portal.service;

import java.util.List;

import javax.validation.Valid;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Classifier;

public interface IClassifierService extends IBaseService<Classifier> {

	List<Classifier> searchByNameOrType(@Valid Classifier classifierModel) throws AppException, BusException;

}
