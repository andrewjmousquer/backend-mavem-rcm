package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.model.PartnerPersonCommission;

public interface IPartnerPersonCommissionDAO  {

	public Optional<PartnerPersonCommission> find(PartnerPersonCommission model) throws AppException;

	public List<PartnerPersonCommission> list(PartnerPersonCommission model) throws AppException;
	
	public Optional<PartnerPersonCommission> save(PartnerPersonCommission model) throws AppException;
	
	public Optional<PartnerPersonCommission> update(PartnerPersonCommission model) throws AppException;

	public void delete(PartnerPersonCommission model) throws AppException;

}
