package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PartnerPersonCommission;

public interface IPartnerPersonCommissionService  {
	
	public Optional<PartnerPersonCommission> find(PartnerPersonCommission model) throws AppException, BusException;
	
	public List<PartnerPersonCommission> list(PartnerPersonCommission model) throws AppException, BusException;
	
	public Optional<PartnerPersonCommission> save(PartnerPersonCommission model, UserProfileDTO profile) throws AppException, BusException;
	
	public Optional<PartnerPersonCommission> update(PartnerPersonCommission model, UserProfileDTO profile) throws AppException, BusException;

	public void delete(PartnerPersonCommission model, UserProfileDTO profile) throws AppException, BusException;

	public void audit(PartnerPersonCommission model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException;

}
