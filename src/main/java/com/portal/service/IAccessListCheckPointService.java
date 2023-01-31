package com.portal.service;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.AccessListModel;
import com.portal.model.CheckpointModel;

public interface IAccessListCheckPointService {

	public List<CheckpointModel> listCheckpointByAccessList(Integer ckpId, Integer aclId) throws AppException, BusException;
	
	public void saveAccessListCheckpoints(AccessListModel model) throws AppException, BusException;
	
	public void save(AccessListModel model, CheckpointModel checkpoint) throws AppException, BusException;
	
	public void delete(Integer ckpId, Integer aclId) throws AppException, BusException;
	
}
