package com.portal.service.imp;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.IAccessListMenuDAO;
import com.portal.enums.MenuType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.AccessListModel;
import com.portal.model.MenuModel;
import com.portal.service.IAccessListMenuService;

@Service
public class AccessListMenuService implements IAccessListMenuService {

	@Autowired
	private IAccessListMenuDAO dao;
	
	@Override
	public List<MenuModel> listMenuByAccessList(Integer id) throws AppException, BusException{
		List<MenuModel> returMenus = new LinkedList<MenuModel>();
		List<MenuModel> menus = this.dao.listMenuByAccessList(id);
		if(menus != null && !menus.isEmpty()) {
			for(MenuModel menu : menus) {
				if(menu.getType().getType().equals(MenuType.PORTAL_PRODUCT.getType().getType())) {
					if(menu.getRoot() == null) {
						returMenus.add(menu);
					}
				}
			}
			
			for(MenuModel menu : menus) {
				if(menu.getType().getType().equals(MenuType.PORTAL_PRODUCT.getType().getType())) {
					if(menu.getRoot() != null) {
						if(returMenus.contains(menu.getRoot())) {
							MenuModel root = returMenus.get(returMenus.indexOf(menu.getRoot()));
							if(root.getSubmenus() == null) {
								root.setSubmenus(new LinkedList<MenuModel>());
							}
							root.getSubmenus().add(menu);
						}
					}
				}
			}
		}
		
		return returMenus;
	}
	
	@Override
	public void saveAccessListMenus(AccessListModel model) throws AppException, BusException {
		this.delete( model.getId() );
		if(model.getMenus() != null && !model.getMenus().isEmpty()) {
			for (int i = 0 ; i < model.getMenus().size() ; i++) {
				MenuModel menu = model.getMenus().get(i);
				menu.setMnuOrder(i + 1);
				this.dao.save(model, menu);
			}
		}
	}
	
	@Override
	public void save(AccessListModel model, MenuModel menu) throws AppException, BusException{
		dao.save(model, menu);
	}
	
	@Override
	public void delete(Integer id) throws AppException, BusException {
		dao.delete(id);
	}

}
