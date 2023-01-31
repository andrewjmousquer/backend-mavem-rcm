package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.PaymentMethod;

public interface IPaymentMethodDAO extends IBaseDAO<PaymentMethod> {
	
	public List<PaymentMethod> listAll( Pageable pageble ) throws AppException;
	
	public List<PaymentMethod> find( PaymentMethod brand, Pageable pageable ) throws AppException;
	
	public List<PaymentMethod> search( PaymentMethod brand, Pageable pageable ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<PaymentMethod> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(PaymentMethod, Pageable)}
	 */
	@Deprecated
	public Optional<PaymentMethod> find( PaymentMethod brand ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(PaymentMethod, Pageable)}
	 */
	@Deprecated
	public List<PaymentMethod> search( PaymentMethod brand ) throws AppException;

	public boolean hasProposalRelationship(Integer pymId) throws AppException;
}
