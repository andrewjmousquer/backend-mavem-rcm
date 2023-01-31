package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalDetailVehicleItem;

public interface IProposalDetailVehicleItemService extends IBaseService<ProposalDetailVehicleItem>{

	public List<ProposalDetailVehicleItem> find( ProposalDetailVehicleItem model, Pageable pageable ) throws AppException, BusException;
}
