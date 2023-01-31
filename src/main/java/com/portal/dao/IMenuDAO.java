package com.portal.dao;

import java.util.List;

import com.portal.enums.MenuType;
import com.portal.exceptions.AppException;
import com.portal.model.MenuModel;

public interface IMenuDAO extends IBaseDAO<MenuModel> {

	public List<MenuModel> listByUser( Integer usrId ) throws AppException;
	
	public List<MenuModel> list( Integer usrId ) throws AppException;
	
	public List<MenuModel> list( MenuType type ) throws AppException;

	public List<MenuModel> list( MenuType type, Integer usrId ) throws AppException;
	
	public List<MenuModel> listRoots() throws AppException;
	
	public List<MenuModel> listRoots(Integer usrId) throws AppException;
	
	public List<MenuModel> listSub(Integer usrId) throws AppException;
	
	public boolean hasChildren(Integer rootId) throws AppException;
	
	public List<MenuModel> listByAccesslistId(Integer aclId) throws AppException;
	
	public boolean hasAccesslist(MenuModel menu) throws AppException;
	
	public boolean menuExists(MenuModel menu) throws AppException;
}
