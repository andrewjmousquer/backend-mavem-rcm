package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.PriceItemModel;

public interface IPriceItemModelDAO extends IBaseDAO<PriceItemModel> {
	
	public List<PriceItemModel> find( PriceItemModel model, Pageable pageable ) throws AppException;
	
	public boolean hasProposalDetailRelationship(Integer ipcId) throws AppException;
	
	/**
	 * Usar a função {@link #find(PriceItemModel, Pageable)}
	 */
	@Deprecated
	public Optional<PriceItemModel> find( PriceItemModel model ) throws AppException; 
}
