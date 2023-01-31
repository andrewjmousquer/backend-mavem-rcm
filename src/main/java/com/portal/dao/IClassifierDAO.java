package com.portal.dao;

import java.util.List;

import org.springframework.context.NoSuchMessageException;

import com.portal.exceptions.AppException;
import com.portal.model.Classifier;

public interface IClassifierDAO extends IBaseDAO<Classifier>{

	List<Classifier> searchByNameOrType(Classifier model) throws NoSuchMessageException, AppException;

}
