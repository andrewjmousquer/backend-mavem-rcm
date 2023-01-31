package com.portal.dao;

import com.portal.exceptions.AppException;
import com.portal.model.AccessListModel;

public interface IAccessListDAO extends IBaseDAO<AccessListModel>{

	public boolean hasDuplicatedName(AccessListModel accesslist) throws AppException;
	
	public boolean hasUserAccessList(Integer aclId) throws AppException;
}
