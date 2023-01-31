package com.portal.service.imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.ISellerSalesTeamDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.SalesTeam;
import com.portal.model.Seller;
import com.portal.service.IAuditService;
import com.portal.service.ISellerSalesTeamService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class SellerSalesTeamService implements ISellerSalesTeamService {

	@Autowired
	private ISellerSalesTeamDAO dao;
		
	@Autowired
	private IAuditService auditService;
	
	@Override
	public List<SalesTeam> findBySeller(Integer selId) throws AppException, BusException {
		return dao.findBySeller(selId);
	}

	@Override
	public List<Seller> findBySalesTeam(Integer sltdId) throws AppException, BusException {
		return dao.findBySalesTeam(sltdId);
	}

	@Override
	public void save(Integer selId, Integer sltId, UserProfileDTO userProfile) throws AppException, BusException {
		dao.save(selId, sltId);
		this.auditService.save(new String ("{ selId: " + selId + ", sltId: " + selId + " }"), AuditOperationType.SELLER_SALES_TEAM_INSERTED, userProfile);
	}

	@Override
	public void delete(Integer selId, Integer sltId, UserProfileDTO userProfile) throws AppException, BusException {
		dao.delete(selId, sltId);
		this.auditService.save(new String ("{ selId: " + selId + ", sltId: " + selId + " }"), AuditOperationType.SELLER_SALES_TEAM_DELETED, userProfile);
	}

}
