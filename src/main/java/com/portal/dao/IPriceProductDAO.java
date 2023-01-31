package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.PriceProduct;

public interface IPriceProductDAO extends IBaseDAO<PriceProduct> {
	
	public List<PriceProduct> find( PriceProduct model, Pageable pageable ) throws AppException;
	
	public boolean hasProposalDetailRelationship(Integer ipcId) throws AppException;
	
	/**
	 * Usar a função {@link #find(PriceProduct, Pageable)}
	 */
	@Deprecated
	public Optional<PriceProduct> find( PriceProduct model ) throws AppException; 
}
