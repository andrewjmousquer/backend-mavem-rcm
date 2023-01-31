package com.portal.model;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuModel {

	private Integer id;
	private String name;
	private String menuPath;
	private String description;
	private String icon;
	private String route;
	private MenuModel root;
	private Classifier type;
	private Integer mnuOrder;
	private List<MenuModel> submenus;
	private boolean show;

	public void addSubMenu( MenuModel submenus ) {
		if( this.submenus == null ) {
			this.submenus = new LinkedList<MenuModel>();
		}

		this.submenus.add(submenus);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj != null && obj instanceof MenuModel) {
			MenuModel model = (MenuModel) obj;
			
			if( this.id.equals( model.getId() ) ) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "MenuModel [id=" + id + ", name=" + name + ", path=" + menuPath + ", rootId=" + root + "]";
	}
	
}