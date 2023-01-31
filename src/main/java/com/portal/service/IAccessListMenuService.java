package com.portal.service;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.AccessListModel;
import com.portal.model.MenuModel;

public interface IAccessListMenuService {

	public List<MenuModel> listMenuByAccessList(Integer id) throws AppException, BusException;
	
	public void saveAccessListMenus(AccessListModel model) throws AppException, BusException;
	
	public void save(AccessListModel model, MenuModel menu) throws AppException, BusException;
	
	public void delete(Integer id) throws AppException, BusException;
	
}
