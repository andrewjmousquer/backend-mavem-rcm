package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.dto.PriceListDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.dto.form.PriceListDuplicateItemDTO;
import com.portal.dto.form.PriceListFormDTO;
import com.portal.dto.form.PriceListFormSearchDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;

public interface IPriceListFormService {
	
	public List<PriceListFormSearchDTO> search( PriceListDTO model, Pageable pageable ) throws AppException, BusException;
	
	public Optional<PriceListFormDTO> getById( Integer id ) throws AppException, BusException;
	
	public void save( PriceListFormDTO model, UserProfileDTO userProfile ) throws AppException, BusException;

	public void update(PriceListFormDTO model, UserProfileDTO userProfile) throws AppException, BusException;

	public void delete( Integer id, UserProfileDTO userProfile) throws AppException, BusException;
	
	public List<PriceListDuplicateItemDTO> checkDuplicateItem( PriceListFormDTO model ) throws AppException, BusException;
}
