package com.portal.service;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.MenuModel;

public interface IMenuService extends IBaseService<MenuModel> {
	
	public List<MenuModel> listTree(Integer usrId) throws AppException, BusException;

	public List<MenuModel> list(Integer usrId) throws AppException, BusException;

	public List<MenuModel> listRoots() throws AppException, BusException;

	public List<MenuModel> listChildren(Integer rootId) throws AppException, BusException;

	public List<MenuModel> listByAccesslistId(Integer aclId) throws AppException, BusException;
	
	public boolean hasChildren(Integer rootId) throws AppException, BusException;
}
