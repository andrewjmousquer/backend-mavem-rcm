package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.model.AccessListModel;
import com.portal.model.CheckpointModel;

public interface IAccessListCheckPointDAO {

	public List<CheckpointModel> listCheckpointByAccessList(Integer ckpId, Integer aclId) throws AppException;
	
	public Optional<AccessListModel> save(AccessListModel model, CheckpointModel checkpoint) throws AppException;
	
	public void delete(Integer ckpId, Integer aclId) throws AppException;

}
