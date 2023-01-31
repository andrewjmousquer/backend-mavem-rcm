package com.portal.service.imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.ISellerAgentDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Seller;
import com.portal.service.IAuditService;
import com.portal.service.ISellerAgentService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class SellerAgentService implements ISellerAgentService {

    @Autowired
    private ISellerAgentDAO dao;

    @Autowired
    private IAuditService auditService;

	public List<Seller> findBySeller( Integer selId ) throws AppException, BusException {
		return dao.findBySeller(selId);
	}
	
	public void save( Integer selId, Integer selAgentId, UserProfileDTO userProfile ) throws AppException, BusException {
		dao.save(selId, selAgentId);
		this.auditService.save(new String ("{ selId: " + selId + ", selAgentId: " + selAgentId + " }"), AuditOperationType.SELLER_AGENT_INSERTED, userProfile);

	}
	
	public void delete( Integer selId, Integer selAgentId, UserProfileDTO userProfile ) throws AppException, BusException {
		dao.delete(selId, selAgentId);
		this.auditService.save(new String ("{ selId: " + selId + ", selAgentId: " + selAgentId + " }"), AuditOperationType.SELLER_AGENT_DELETED, userProfile);
	}
}
