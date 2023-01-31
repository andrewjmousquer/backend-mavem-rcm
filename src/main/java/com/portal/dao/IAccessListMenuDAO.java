package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.model.AccessListModel;
import com.portal.model.MenuModel;

public interface IAccessListMenuDAO {

	public List<MenuModel> listMenuByAccessList(Integer id) throws AppException;
	
	public Optional<AccessListModel> save(AccessListModel model, MenuModel menu) throws AppException;
	
	public void delete(Integer id) throws AppException;
}
