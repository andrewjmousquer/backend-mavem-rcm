package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IPartnerPersonDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Partner;
import com.portal.model.PartnerPerson;
import com.portal.model.PartnerPersonCommission;
import com.portal.model.Person;
import com.portal.service.IAuditService;
import com.portal.service.IPartnerPersonCommissionService;
import com.portal.service.IPartnerPersonService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PartnerPersonService implements IPartnerPersonService {

	@Autowired
	private IPartnerPersonDAO dao; 
	
	@Autowired
	private IAuditService auditService;

	@Autowired
	private IPartnerPersonCommissionService partnerPersonCommissionService;
	
	@Override
	public Optional<PartnerPerson> getPartnerPerson( PartnerPerson model) throws AppException, BusException {
		Optional<PartnerPerson> returnObj = dao.getPartnerPerson(model);
		if(returnObj != null) {
			if(returnObj.isPresent()) {
				PartnerPersonCommission partnerPersonSearch = new PartnerPersonCommission();
				partnerPersonSearch.setPartner(model.getPartner());
				partnerPersonSearch.setPerson(model.getPerson());
				
				returnObj.get().setCommissionList(partnerPersonCommissionService.list(partnerPersonSearch));
			}
		}
		
		return returnObj;
	}

	@Override
	public List<PartnerPerson> findPartnerPerson( PartnerPerson model) throws AppException, BusException {
		List<PartnerPerson> partnerPersonList = dao.findPartnerPerson(model);
		if(partnerPersonList != null && !partnerPersonList.isEmpty()) {
			for(PartnerPerson partnerPerson : partnerPersonList) {
				PartnerPersonCommission partnerPersonSearch = new PartnerPersonCommission();
				partnerPersonSearch.setPartner(partnerPerson.getPartner());
				partnerPersonSearch.setPerson(partnerPerson.getPerson());
				
				partnerPerson.setCommissionList(partnerPersonCommissionService.list(partnerPersonSearch));
			}
		}
		return partnerPersonList;
	}
	
	@Override
	public void save( PartnerPerson model, UserProfileDTO userProfile ) throws AppException, BusException {
		dao.save(model);
		this.auditService.save(new String ("{ ptnId: " + model.getPartner().getId() + ", perId: " + model.getPerson().getId() + " claId: " + model.getPersonType().getId() + "  }"), AuditOperationType.PARTNER_PERSON_INSERTED, userProfile);
		
		if(model.getCommissionList() != null && !model.getCommissionList().isEmpty()) {
			for(PartnerPersonCommission partnerPerson : model.getCommissionList()) {
				partnerPerson.setPartner(model.getPartner());
				partnerPerson.setPerson(model.getPerson());
				partnerPersonCommissionService.save(partnerPerson, userProfile);
			}
		}
		
	}

	@Override
	public void update( PartnerPerson model, UserProfileDTO userProfile ) throws AppException, BusException {
		dao.update(model);
		
		if(model.getCommissionList() != null && !model.getCommissionList().isEmpty()) {
			for(PartnerPersonCommission partnerPerson : model.getCommissionList()) {
				partnerPerson.setPartner(model.getPartner());
				partnerPerson.setPerson(model.getPerson());
				Optional<PartnerPersonCommission> existsComission = this.partnerPersonCommissionService.find(new PartnerPersonCommission(model.getPerson(), model.getPartner(), partnerPerson.getCommissionType()));
				if(existsComission.isPresent()) {
					partnerPersonCommissionService.update(partnerPerson, userProfile);
				} else {
					partnerPersonCommissionService.save(partnerPerson, userProfile);
				}
			}
		}
		
		this.auditService.save(new String ("{ ptnId: " + model.getPartner().getId() + ", perId: " + model.getPerson().getId() + " claId: " + model.getPersonType().getId() + "  }"), AuditOperationType.PARTNER_PERSON_UPDATED, userProfile);
	}

	@Override
	public void delete( Integer ptnId, Integer perId, UserProfileDTO userProfile) throws AppException, BusException {
		
		PartnerPersonCommission deleteComission = new PartnerPersonCommission();
		deleteComission.setPerson(new Person(perId));
		deleteComission.setPartner(new Partner(ptnId));
		
		partnerPersonCommissionService.delete(deleteComission, userProfile);
		
		dao.delete(ptnId, perId);
		this.auditService.save(new String ("{ ptnId: " + ptnId + ", perId: " + perId + " }"), AuditOperationType.PARTNER_PERSON_DELETED, userProfile);
	}

}
