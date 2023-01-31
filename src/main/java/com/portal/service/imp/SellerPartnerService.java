package com.portal.service.imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.ISellerPartnerDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Partner;
import com.portal.model.Seller;
import com.portal.service.IAuditService;
import com.portal.service.ISellerPartnerService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class SellerPartnerService implements ISellerPartnerService {

    @Autowired
    private ISellerPartnerDAO dao;

    @Autowired
    private IAuditService auditService;

    public List<Partner> findBySeller( Integer selId ) throws AppException, BusException  {
    	return dao.findBySeller(selId);
    }
	
	public List<Seller> findByPartner( Integer ptnId ) throws AppException, BusException  {
		return dao.findByPartner(ptnId);
	}
	
	public void save( Integer selId, Integer ptnId, UserProfileDTO userProfile ) throws AppException, BusException  {
		dao.save(selId, ptnId);
		this.auditService.save(new String ("{ selId: " + selId + ", sltId: " + selId + " }"), AuditOperationType.SELLER_PARTNER_INSERTED, userProfile);
	}
	
	public void delete( Integer selId, Integer ptnId, UserProfileDTO userProfile ) throws AppException, BusException  {
		dao.delete(selId, ptnId);
		this.auditService.save(new String ("{ selId: " + selId + ", sltId: " + selId + " }"), AuditOperationType.SELLER_PARTNER_DELETED, userProfile);
	}
}
