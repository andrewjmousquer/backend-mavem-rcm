package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PaymentMethod;

public interface IPaymentMethodService extends IBaseService<PaymentMethod> {
	
	public List<PaymentMethod> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<PaymentMethod> find( PaymentMethod model, Pageable pageable ) throws AppException, BusException;
	
	public List<PaymentMethod> search( PaymentMethod model, Pageable pageable ) throws AppException, BusException;
	
	public Optional<PaymentMethod> getById(Integer id, boolean withRule) throws AppException, BusException;
}
