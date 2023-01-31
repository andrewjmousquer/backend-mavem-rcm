package com.portal.service.imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.IAccessListCheckPointDAO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.AccessListModel;
import com.portal.model.CheckpointModel;
import com.portal.service.IAccessListCheckPointService;

@Service
public class AccessListCheckPointService implements IAccessListCheckPointService {

	@Autowired
	private IAccessListCheckPointDAO dao;
	
	@Override
	public List<CheckpointModel> listCheckpointByAccessList(Integer ckpId, Integer aclId) throws AppException, BusException{
		return this.dao.listCheckpointByAccessList(ckpId, aclId);
	}
	
	@Override
	public void saveAccessListCheckpoints(AccessListModel model) throws AppException, BusException {
		this.delete( null, model.getId() );
		if(model.getCheckpoints() != null && !model.getCheckpoints().isEmpty()) {
			for (int i = 0 ; i < model.getCheckpoints().size() ; i++) {
				CheckpointModel checkpoint = model.getCheckpoints().get(i);
				this.dao.save(model, checkpoint);
			}
		}
	}
	
	@Override
	public void save(AccessListModel model, CheckpointModel checkpoint) throws AppException, BusException{
		dao.save(model, checkpoint);
	}
	
	@Override
	public void delete(Integer ckpId, Integer aclId) throws AppException, BusException {
		dao.delete(ckpId, aclId);
	}

}
