package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.PaymentRule;

public interface IPaymentRuleDAO extends IBaseDAO<PaymentRule> {
	
	public List<PaymentRule> listAll( Pageable pageble ) throws AppException;
	
	public List<PaymentRule> find( PaymentRule model, Pageable pageable ) throws AppException;
	
	public List<PaymentRule> search( PaymentRule model, Pageable pageable ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<PaymentRule> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(PaymentRule, Pageable)}
	 */
	@Deprecated
	public Optional<PaymentRule> find( PaymentRule model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(PaymentRule, Pageable)}
	 */
	@Deprecated
	public List<PaymentRule> search( PaymentRule model ) throws AppException;

    List<PaymentRule> listToPaymentMethod(Integer id) throws AppException;
}
