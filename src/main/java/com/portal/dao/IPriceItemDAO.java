package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.PriceItem;

public interface IPriceItemDAO extends IBaseDAO<PriceItem> {
	
	public List<PriceItem> find( PriceItem model, Pageable pageable ) throws AppException;
	
	public boolean hasProposalDetailRelationship(Integer ipcId) throws AppException;
	
	/**
	 * Usar a função {@link #find(PriceItem, Pageable)}
	 */
	@Deprecated
	public Optional<PriceItem> find( PriceItem model ) throws AppException; 
}
