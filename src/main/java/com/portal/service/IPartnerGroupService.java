package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PartnerGroup;

public interface IPartnerGroupService extends IBaseService<PartnerGroup> {
	
	public List<PartnerGroup> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<PartnerGroup> find( PartnerGroup model, Pageable pageable ) throws AppException, BusException;
	
	public List<PartnerGroup> search( PartnerGroup model, Pageable pageable ) throws AppException, BusException;
	
}
