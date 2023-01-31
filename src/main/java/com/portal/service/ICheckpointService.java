package com.portal.service;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CheckpointModel;

public interface ICheckpointService extends IBaseService<CheckpointModel> {
	
	public List<CheckpointModel> listCheckpoints(Integer ckpId, Integer aclId) throws AppException, BusException;
	
	public List<CheckpointModel> getByCurrentUser(Integer aclId) throws AppException, BusException;
	
}
