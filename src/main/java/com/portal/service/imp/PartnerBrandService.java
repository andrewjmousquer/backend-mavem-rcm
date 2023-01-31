package com.portal.service.imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IPartnerBrandDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Partner;
import com.portal.service.IAuditService;
import com.portal.service.IPartnerBrandService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PartnerBrandService implements IPartnerBrandService {

	@Autowired
	private IPartnerBrandDAO dao; 
	
	@Autowired
	private IAuditService auditService;
	
	@Override
	public List<Brand> findByPartner( Integer ptnId ) throws AppException, BusException {
		return dao.findByPartner(ptnId);
	}

	@Override
	public List<Partner> findByBrand( Integer brdId ) throws AppException, BusException {
		return dao.findByBrand(brdId);
	}

	@Override
	public void save( Integer ptnId, Integer brdId, UserProfileDTO userProfile) throws AppException, BusException {
		dao.save(ptnId, brdId);
		this.auditService.save(new String ("{ ptnId: " + ptnId + ", brdId: " + brdId + " }"), AuditOperationType.PARTNER_BRAND_INSERTED, userProfile);
	}

	@Override
	public void delete( Integer ptnId, Integer brdId, UserProfileDTO userProfile) throws AppException, BusException {
		dao.delete(ptnId, brdId);
		this.auditService.save(new String ("{ ptnId: " + ptnId + ", brdId: " + brdId + " }"), AuditOperationType.PARTNER_BRAND_DELETED, userProfile);
	}

}
