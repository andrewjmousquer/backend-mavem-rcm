package com.portal.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.portal.dto.form.PriceListDuplicateItemDTO;
import com.portal.exceptions.AppException;

public interface IPriceListFormDAO {

	public Optional<List<PriceListDuplicateItemDTO>> findProductsOverlay( LocalDateTime start, LocalDateTime end ) throws AppException;
	
	public Optional<List<PriceListDuplicateItemDTO>> findItemOverlay( LocalDateTime start, LocalDateTime end ) throws AppException;

	public Optional<List<PriceListDuplicateItemDTO>> findItemModelOverlay( LocalDateTime start, LocalDateTime end ) throws AppException;
	
}
