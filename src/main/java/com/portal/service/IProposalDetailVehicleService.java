package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalDetailVehicle;

public interface IProposalDetailVehicleService extends IBaseService<ProposalDetailVehicle> {
	
	public List<ProposalDetailVehicle> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<ProposalDetailVehicle> find( ProposalDetailVehicle model, Pageable pageable ) throws AppException, BusException;
	
	public List<ProposalDetailVehicle> search( ProposalDetailVehicle model, Pageable pageable ) throws AppException, BusException;
	
	public ProposalDetailVehicle getDetailVehicleByDetail( ProposalDetailVehicle model ) throws AppException, BusException;

}
