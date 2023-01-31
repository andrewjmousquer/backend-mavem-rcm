package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Partner;

public interface IPartnerService extends IBaseService<Partner> {

	public List<Partner> listAll(Pageable pageable) throws AppException, BusException;

	public List<Partner> find(Partner model, Pageable pageable) throws AppException, BusException;

	public List<Partner> search(Partner model, Pageable pageable) throws AppException, BusException;

	public List<Partner> searchForm(String searchText, Pageable pageable) throws AppException;

}
