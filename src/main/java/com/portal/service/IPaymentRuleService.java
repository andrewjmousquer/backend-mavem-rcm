package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.dto.PaymentRuleDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PaymentRule;

public interface IPaymentRuleService extends IBaseService<PaymentRule> {

	public List<PaymentRule> listAll(Pageable pageable) throws AppException, BusException;

	public List<PaymentRule> find(PaymentRule model, Pageable pageable) throws AppException, BusException;
	public List<PaymentRule> findToDto(PaymentRuleDTO model, Pageable pageable) throws AppException, BusException;

	public List<PaymentRule> search(PaymentRule model, Pageable pageable) throws AppException, BusException;
	public List<PaymentRule> searchToDto(PaymentRuleDTO model, Pageable pageable) throws AppException, BusException;

	public Optional<PaymentRule> saveRule(PaymentRuleDTO dto, UserProfileDTO userProfile) throws AppException, BusException;

	public Optional<PaymentRule> updateRule(PaymentRuleDTO dto, UserProfileDTO userProfile) throws AppException, BusException;

    List<PaymentRule> getListRules(Integer id) throws AppException;

//
//	public List<PaymentRule> list( int pymId, Pageable pageble ) throws AppException, BusException;
//	
//
//	public Optional<PaymentRule> saveOrUpdate( PaymentRule model, UserProfileDTO userProfile ) throws AppException, BusException;
//

//
//	public Optional<PaymentRule> getById( Integer id ) throws AppException, BusException;
//
//	public void delete( Integer id, UserProfileDTO userProfile ) throws AppException, BusException;
//
////	public void deleteByPaymentMethod( Integer id, UserProfileDTO userProfile ) throws AppException, BusException;
//	
//	public void audit(PaymentRule model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException;
	
}
