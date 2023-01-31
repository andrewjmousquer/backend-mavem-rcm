package com.portal.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.ProposalDetailVehicleItem;

public interface IProposalDetailVehicleItemDAO extends IBaseDAO<ProposalDetailVehicleItem> {
	
	public List<ProposalDetailVehicleItem> find( ProposalDetailVehicleItem model, Pageable pageable ) throws AppException;
}
