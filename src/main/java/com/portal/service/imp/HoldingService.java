package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.portal.dao.IHoldingDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.AddressModel;
import com.portal.model.Classifier;
import com.portal.model.CustomerModel;
import com.portal.model.HoldingModel;
import com.portal.model.Person;
import com.portal.service.IAddressService;
import com.portal.service.IAuditService;
import com.portal.service.IClassifierService;
import com.portal.service.ICustomerService;
import com.portal.service.IHoldingService;
import com.portal.service.IPersonService;
import com.portal.utils.PortalNumberUtils;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class HoldingService implements IHoldingService {
	
	@Autowired
	private IHoldingDAO dao;
	
	@Autowired
	private IPersonService personService;
	
	@Autowired
	private IAddressService addressService;
	
	@Autowired
	private ICustomerService customerService;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired 
	private IAuditService auditService;
	
	@Autowired
	private IClassifierService classifierService;
	
	@Override
	public Optional<HoldingModel> getById(Integer id) throws AppException, BusException {
		Optional<HoldingModel> holding = this.dao.getById(id);
		
		if(holding.isPresent()) {
			Optional<AddressModel> address = this.addressService.getById(holding.get().getAddress().getId());
			if(address.isPresent()) {
				holding.get().setAddress(address.get());
			}
			
			List<CustomerModel> customers = this.customerService.listByHoldingId(holding.get().getId());
			if(!CollectionUtils.isEmpty(customers)) {
				holding.get().setCustomers(customers);
			}
			
			Optional<Person> person = this.personService.getById(holding.get().getPerson().getId());
			if(person.isPresent()) {
				holding.get().setPerson(person.get());
				
			}
			Optional<Classifier> type = this.classifierService.getById(holding.get().getType().getId());
			if(type.isPresent()) {
				holding.get().setType(type.get());
			}
		}
		return holding;
	}
		
	@Override
	public Optional<HoldingModel> find(HoldingModel model) throws AppException, BusException {
		return dao.find(model);
	}

	@Override
	public List<HoldingModel> search(HoldingModel model) throws AppException, BusException {
		return dao.search(model);
	}

	@Override
	public List<HoldingModel> list() throws AppException, BusException {
		return this.dao.list();
	}

	@Override
	public Optional<HoldingModel> saveOrUpdate(HoldingModel model, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<HoldingModel> savedModel = Optional.empty();
		
		if(model != null && model.getId() != null && model.getId() != 0) {
			savedModel = update(model, userProfile);
		} else {
			savedModel = save(model, userProfile);
		}
		
		return savedModel;
	}
	
	public Optional<HoldingModel> save(HoldingModel model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model.getCustomers() != null) {
			this.normalizeCnpj( model );
		}
		
		this.validateHolding(model);
		
		this.checkCustomerDefault(model);
		
		if(model.getAddress() != null) {
			this.addressService.save(model.getAddress(), userProfile);
		}
		
		if(model.getPerson() != null) {
			this.personService.save(model.getPerson(), userProfile);
		} 
	
		Optional<HoldingModel> returnModel =  this.dao.save( model );
		
		if(model.getCustomers() != null && !model.getCustomers().isEmpty()) {
			model.getCustomers().forEach(customer ->{
				HoldingModel h = new HoldingModel();
				h.setId(model.getId());
				customer.setHolding(h);
				try {
					this.customerService.save(customer, userProfile );
				} catch (AppException | BusException e) {
					e.printStackTrace();
				}
			});
		}
		
		this.audit( returnModel.get(), AuditOperationType.HOLDING_INSERTED, userProfile );
		
		return returnModel;
	}
	
	public Optional<HoldingModel> update(HoldingModel model, UserProfileDTO userProfile) throws AppException, BusException {
		this.normalizeCnpj( model );
		this.validateHolding(model);
		this.checkCustomerDefault(model);
		
		boolean updateAddress = true;
		
		if(model.getAddress() != null) {
			if(model.getAddress().getId() == 0 
					&& (model.getAddress().getStreet() == null || model.getAddress().getStreet().trim().isEmpty())
					&& (model.getAddress().getNumber() == null ||model.getAddress().getNumber().trim().isEmpty())
					&& (model.getAddress().getComplement() == null || model.getAddress().getComplement().trim().isEmpty())
					&& (model.getAddress().getDistrict() == null || model.getAddress().getDistrict().trim().isEmpty())
					&& (model.getAddress().getZipCode() == null || model.getAddress().getZipCode().trim().isEmpty())
					&& (model.getAddress().getCity() == null || model.getAddress().getCity().getId() == 0)
					&& (model.getAddress().getCity() == null || (model.getAddress().getCity().getState() == null || model.getAddress().getCity().getState().getId() == 0))) {
				
				updateAddress = false;
				model.getAddress().setId(null);
			}
			
			if( updateAddress ) {
				this.addressService.saveOrUpdate(model.getAddress(), userProfile);
			}
		}
		
		model.getPerson().setAddress(model.getAddress());
		if(model.getPerson() != null) {
			this.personService.saveOrUpdate(model.getPerson(), userProfile);
		}
		
		Optional<HoldingModel> holdModel = this.dao.getById(model.getId());
		
		Optional<HoldingModel> holdReturn = this.dao.update(model);
		
		if( model.getCustomers() != null && !model.getCustomers().isEmpty() ) {
			model.getCustomers().forEach(customer ->{
				HoldingModel h = new HoldingModel();
				h.setId(model.getId());
				
				customer.setHolding(h);
				customer.setInserted( (customer.getId() == null || customer.getId() == 0 ) );
				
				if(holdModel.isPresent()) {
					if( customer.getCnpj().equals( holdModel.get().getCnpj() ) ) {
						customer.setCnpj(model.getCnpj());
					}
				}
				
				try {
					this.customerService.saveOrUpdate(customer, userProfile);
				} catch (AppException | BusException e) {
					e.printStackTrace();
				}
			});
		}
		
		// Deletamos os customers necessários
		if(model.getDeletedCustomers() != null && !model.getDeletedCustomers().isEmpty()) {
			model.getDeletedCustomers().forEach(customer ->{
				try {
					this.customerService.delete(customer.getId(), userProfile);
				} catch (AppException | BusException e) {
					e.printStackTrace();
				}
			});
		}

		this.audit( model, AuditOperationType.HOLDING_UPDATED, userProfile );
		
		return holdReturn;
	}
	
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		Optional<HoldingModel> holding = this.getById(id);
		
		if(holding.isPresent()) {
			this.validateUserOnDelete(id);
			
			CustomerModel customerFind = new CustomerModel();
			customerFind.setHolding(holding.get());
			List<CustomerModel> customers = this.customerService.search(customerFind);
			if(customers != null && !customers.isEmpty()) {
				customers.forEach(customer -> {
					if(customer != null && customer.getId() > 0) {
						try {
							this.customerService.delete( customer.getId(), userProfile );
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			
			this.dao.delete(id);
					
			if(holding.get().getAddress() != null && holding.get().getAddress().getId() > 0) {
				this.addressService.delete(holding.get().getAddress().getId(), userProfile);
			}
			
			this.audit( holding.get(), AuditOperationType.HOLDING_DELETED, userProfile );
		}
	}

	private void normalizeCnpj(HoldingModel holding) {
		holding.setCnpj( PortalNumberUtils.normalizeCnpj( holding.getCnpj() ) );
		normalizeCnpj( holding.getCustomers() );
	}
	
	private void normalizeCnpj(List<CustomerModel> customers) {
		customers.forEach(customer -> {
			customer.setCnpj( PortalNumberUtils.normalizeCnpj(customer.getCnpj()));
		});
	}
	
	/**
	 * RN
	 * 
	 * É obrigatório o cadastro de um CUSTOMER como mesmo CNPJ do HOLDING
	 */
	private void checkCustomerDefault(HoldingModel model) throws BusException, AppException {
		boolean cnpjEqual = false;
		
		if( model.getCustomers() == null || model.getCustomers().isEmpty() ) {
			throw new BusException(this.messageSource.getMessage("error.holding.customerdefault", null, LocaleContextHolder.getLocale()));
			
		} else {
			cnpjEqual = model.getCustomers()
					.stream()
					.filter((customer) -> { 
						return (customer.getCnpj().equalsIgnoreCase(model.getCnpj()));
					}).count() > 0;
		}
		if( !cnpjEqual ) {
			throw new BusException(this.messageSource.getMessage("error.holding.customerdefault", null, LocaleContextHolder.getLocale()));
		}
	}
	
	private void validateHolding(HoldingModel model)  throws AppException, BusException {
		String[] cnpj = new String[1];
		
		// Valida um novo holding
		if( model.getId() == null ) {
			Optional<HoldingModel> holdingBD = this.find(new HoldingModel( model.getCnpj()) );
			Optional<CustomerModel> customerBD = this.customerService.findByCNPJ( model.getCnpj() );
			
			if( holdingBD.isPresent() || customerBD.isPresent() ) {
				cnpj[0] = model.getCnpj();
				throw new BusException(this.messageSource.getMessage("error.holding.uqcnpj", cnpj, LocaleContextHolder.getLocale()));
			}
			
		} else {
			// Atualizar
			Optional<HoldingModel> holdingBD = this.find(new HoldingModel( model.getCnpj()) );
			if(holdingBD.isPresent()) {
				if( holdingBD != null && !holdingBD.get().getId().equals( model.getId() ) ) {
					cnpj[0] = model.getCnpj();
					throw new BusException(this.messageSource.getMessage("error.holding.uqcnpj", cnpj, LocaleContextHolder.getLocale()));
				}
			}
			
			Optional<CustomerModel> customerBD = this.customerService.findByCNPJ( model.getCnpj() );
			
			if( customerBD.isPresent() && !customerBD.get().getHolding().getId().equals( model.getId() ) ) {
				cnpj[0] = model.getCnpj();
				throw new BusException(this.messageSource.getMessage("error.holding.uqcnpj", cnpj, LocaleContextHolder.getLocale()));
			}
		}
	}

	@Override
	public List<HoldingModel> fillCustomer(List<HoldingModel> holdings) throws BusException, AppException {
		if( holdings != null && !holdings.isEmpty() ) {
			holdings.forEach(holding -> {
				try {
					this.fillCustomer( holding );
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		return holdings;
	}

	@Override
	public Optional<HoldingModel> fillCustomer(HoldingModel holding) throws BusException, AppException {
		Optional<HoldingModel> holdingModel = Optional.of(new HoldingModel());
		if( holding != null ) {
			CustomerModel findCustomer = new CustomerModel();
			findCustomer.setHolding( holding );
			holdingModel.get().setCustomers( this.customerService.search( findCustomer ) );
		}
		
		return holdingModel;
	}
	
	@Override
	public void audit(HoldingModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		String details = String.format("holId:%s;name:%s;cnpj:%s;razaosocial:%s;ie:%s;im:%s", model.getId(), model.getName(), model.getCnpj(), model.getSocialName(), model.getStateRegistration(), model.getMunicipalRegistration() );
		this.auditService.save(details, operationType, userProfile);
	}
	
	@Override
	public List<HoldingModel> listByUserId(Integer usrId) throws AppException, BusException {
		return this.dao.listByUserId(usrId);
	}
		
	@Override
	public Optional<HoldingModel> getLogo( HoldingModel holding ) throws BusException, AppException {
		return this.dao.getLogo( holding );
	}

	@Override
	public Optional<HoldingModel> getDefaultHolding(Integer usrId) throws AppException, BusException {
		return this.dao.getDefaultHolding(usrId);
	}
	
	private void validateUserOnDelete(Integer holId) throws BusException, AppException {
		if(this.dao.hasUser(holId)) {
			throw new BusException(this.messageSource.getMessage("error.holding.constrainuser", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Optional<HoldingModel> getHoldingByCustomer(Integer cusId) throws BusException, AppException {
		return dao.getHoldingByCustomer(cusId);
	}


}