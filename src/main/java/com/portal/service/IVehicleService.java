package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.dto.VehicleDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.VehicleModel;

public interface IVehicleService extends IBaseService<VehicleModel> {
	
	public List<VehicleModel> listAll(Pageable pageable ) throws AppException, BusException;
	
	public List<VehicleModel> find(VehicleModel model, Pageable pageable ) throws AppException, BusException;
	
	public List<VehicleModel> search(VehicleModel model, Pageable pageable ) throws AppException, BusException;
	
	public List<VehicleModel> searchForm(String searchText, Pageable pageable ) throws AppException, BusException;

	public List<VehicleModel>  search(VehicleDTO model, boolean like,  Pageable pageable ) throws AppException, BusException;

	public List<VehicleModel> getByBrand(String brand, Pageable pageable) throws AppException;

	public Optional<VehicleModel> getByChassi(String chassi) throws AppException;
}
