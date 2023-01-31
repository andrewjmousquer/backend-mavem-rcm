package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IMenuDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.MenuModel;
import com.portal.service.IAuditService;
import com.portal.service.IMenuService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class MenuService implements IMenuService {

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private IMenuDAO menuDAO;
	
	@Autowired 
	private IAuditService auditService;
	
	@Override
	public Optional<MenuModel> getById(Integer id) throws AppException, BusException {
		return this.menuDAO.getById(id);
	}
	
	@Override
	public List<MenuModel> list() throws AppException, BusException {
		return this.menuDAO.list();
	}

	@Override
	public Optional<MenuModel> find(MenuModel model) throws AppException {
		return this.menuDAO.find(model);
	}

	@Override
	public List<MenuModel> search(MenuModel model) throws AppException {
		return this.menuDAO.search(model);
	}

	@Override
	public Optional<MenuModel> saveOrUpdate(MenuModel model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}
	
	@Override
	public Optional<MenuModel> save(MenuModel model, UserProfileDTO userProfile) throws BusException, AppException {
		this.validateMenu(model);
		Optional<MenuModel> saveModel = this.menuDAO.save(model);
		this.audit( saveModel.get(), AuditOperationType.MENU_INSERTED, userProfile );
		return saveModel;
	}
	
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<MenuModel> menu = this.getById(id);
		
		if(menu.isPresent()) {
			this.validateRootMenuOnDelete(menu.get());
			this.validateAccesslistOnDelete(menu.get());
			this.menuDAO.delete(id);
			this.audit( menu.get(), AuditOperationType.MENU_DELETED, userProfile );
		}
	}

	@Override
	public Optional<MenuModel> update(MenuModel model, UserProfileDTO userProfile) throws AppException, BusException {
		this.validateMenu(model);
		Optional<MenuModel> saveModel = this.menuDAO.update(model);
		this.audit( model, AuditOperationType.MENU_UPDATED, userProfile );
		return saveModel;
	}

	@Override
	public List<MenuModel> listTree( Integer usrId ) throws AppException {
		return this.parseToTreeMode( this.menuDAO.listRoots(usrId), this.menuDAO.listSub(usrId) );
	}
	
	@Override
	public List<MenuModel> list(Integer usrId) throws AppException, BusException {
		return this.menuDAO.list(usrId);
	}
	
	@Override
	public List<MenuModel> listByAccesslistId(Integer aclId) throws AppException, BusException {
		return this.menuDAO.listByAccesslistId(aclId);
	}
	
	@Override
	public List<MenuModel> listRoots() throws AppException, BusException {
		return this.menuDAO.listRoots();
	}
	
	@Override
	public boolean hasChildren(Integer rootId) throws AppException, BusException {
		return this.menuDAO.hasChildren(rootId);
	}
	
	@Override
	public List<MenuModel> listChildren(Integer rootId) throws AppException, BusException {
		MenuModel menuRoot = new MenuModel();
		menuRoot.setId(rootId);
		
		MenuModel menu = new MenuModel();
		menu.setRoot(menuRoot);
		return this.search(menu);
	}
	
	private void validateMenu(MenuModel model) throws BusException, AppException {
		if (model.getId() == null && this.menuDAO.menuExists(model)) {
			String[] msg = {model.getName(), model.getRoute()};
			throw new BusException(this.messageSource.getMessage("error.menu.duplicated", msg, LocaleContextHolder.getLocale()));
		}
	}
	
	private void validateRootMenuOnDelete(MenuModel model) throws BusException, AppException {
		if( this.hasChildren(model.getId()) ) {
			throw new BusException(this.messageSource.getMessage("error.menu.constrainrootid", null, LocaleContextHolder.getLocale()));
		}
	}
	
	private void validateAccesslistOnDelete(MenuModel model) throws BusException, AppException {
		if( this.menuDAO.hasAccesslist(model) ) {
			throw new BusException(this.messageSource.getMessage("error.menu.constrainaccesslist", null, LocaleContextHolder.getLocale()));
		}
	}	
	
	/**
	 * Transforma uma lista inteira de menus em uma lista encadeada pelo menu root.
	 * 
	 * @param menus Lista de menus sem encadeamentos.
	 * @param submenus Lista de submenus sem encadeamentos.
	 * @return Lista de menus já encadeados.
	 */
	private List<MenuModel> parseToTreeMode(List<MenuModel> menus, List<MenuModel> submenus) {
		submenus.forEach(subMenu -> {
			Integer key = subMenu.getId();
			key = subMenu.getRoot().getId();
			
			MenuModel menuKey = new MenuModel();
			menuKey.setId( key );
			
			Integer index = menus.indexOf( menuKey );
			
			if( index > -1 ) {
				MenuModel menuRoot = menus.get( index );
				menuRoot.addSubMenu( subMenu );
			}
		});
		return menus;
	}

	@Override
	public void audit( MenuModel model, AuditOperationType operationType, UserProfileDTO userProfile ) throws AppException, BusException {
		String details = String.format("mnuId:%s;name:%s;description:%s;url:%s;rootId:%s;type:%s;mnuOrder:%s", 
										model.getId(),
										model.getName(),
										model.getDescription(),
										model.getRoute(),
										model.getRoot(),
										model.getType(),
										model.getMnuOrder() );
		
		this.auditService.save( details, operationType, userProfile );
	}
}
