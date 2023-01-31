package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.VehicleModel;

public interface IVehicleDAO extends IBaseDAO<VehicleModel> {
	
	public List<VehicleModel> listAll(Pageable pageable ) throws AppException;
	
	public List<VehicleModel> find(VehicleModel model, Pageable pageable ) throws AppException;
	
	public List<VehicleModel> search(VehicleModel model, Pageable pageable ) throws AppException;
	
	public List<VehicleModel> searchForm(String searchText, Pageable pageable ) throws AppException;
	
	public boolean hasProposalDetailRelationship(Integer vheId) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<VehicleModel> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(VehicleModel, Pageable)}
	 */
	@Deprecated
	public Optional<VehicleModel> find(VehicleModel model ) throws AppException;
	
	/**
	 * Usar a função {@link #search(VehicleModel, Pageable)}
	 */
	@Deprecated
	public List<VehicleModel> search(VehicleModel model ) throws AppException;


	public List<VehicleModel> getByBrand(String brand, Pageable pageReq) throws AppException;

	public Optional<VehicleModel> getByChassi(String chassi) throws AppException;
}
