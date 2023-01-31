package com.portal.service.imp;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IUserDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CustomerModel;
import com.portal.model.ParameterModel;
import com.portal.model.PassHistModel;
import com.portal.model.UserModel;
import com.portal.service.IAccessListCheckPointService;
import com.portal.service.IAccessListMenuService;
import com.portal.service.IAuditService;
import com.portal.service.IParameterService;
import com.portal.service.IPassHistService;
import com.portal.service.IPersonService;
import com.portal.service.IUserCustomerService;
import com.portal.service.IUserService;
import com.portal.utils.PortalPasswordUtils;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class UserService implements IUserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);	
	
	@Autowired
	private IUserDAO dao;

	@Autowired
	private IAccessListCheckPointService accessListCheckPointService;
	
	@Autowired
	private IAccessListMenuService accesslistMenuService;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private IPassHistService passHistService;
	
	@Autowired
	private IParameterService parameterService;
	
	@Autowired
	private IPersonService personService;
	
	@Autowired
	private IUserCustomerService userCustomerService;
	
    @Autowired
    public MessageSource messageSource;
    
    @Autowired
	private ObjectMapper objectMapper;
	
	@Override
	public Optional<UserModel> find(UserModel model) throws AppException, BusException {
		return dao.find(model);
	}
	
	@Override
	public Optional<UserModel> findLogin(UserModel model) throws AppException, BusException {
		Optional<UserModel> user = dao.findLogin(model);
		if(user.isPresent()) {
			if(user.get().getAccessList() != null){
				user.get().getAccessList().setMenus(this.accesslistMenuService.listMenuByAccessList(user.get().getAccessList().getId()));
				user.get().getAccessList().setCheckpoints(this.accessListCheckPointService.listCheckpointByAccessList(null, user.get().getAccessList().getId()));
			}
			user.get().setCustomers(this.userCustomerService.listUserCustomer(user.get().getId()));
		}
		
		return user;
	}
	
	@Override
	public Optional<UserModel> findByUsername(UserModel model) throws AppException, BusException {
		Optional<UserModel> user = dao.findLogin(model);
		if(user.isPresent()){
			return user;
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<UserModel> getById(Integer id) throws AppException, BusException {
		Optional<UserModel> userModel = dao.getById(id);
		if(userModel.isPresent()) {
			if(userModel.get().getPerson() != null) {
				userModel.get().setPerson(this.personService.getById(userModel.get().getPerson().getId()).get());
			}
			
			if(userModel.get().getAccessList() != null) {
				userModel.get().getAccessList().setMenus(this.accesslistMenuService.listMenuByAccessList(userModel.get().getAccessList().getId()));
				userModel.get().getAccessList().setCheckpoints(this.accessListCheckPointService.listCheckpointByAccessList(null, userModel.get().getAccessList().getId()));
			}
			
			userModel.get().setCustomers(this.userCustomerService.listUserCustomer(userModel.get().getId()));
		}
		return userModel;
	}
	
	@Override
	public List<UserModel> list() throws AppException, BusException {
		return dao.list();
	}

	@Override
	public List<UserModel> search(UserModel model) throws AppException, BusException {
		return dao.search(model);
	}

	@Override
	public Optional<UserModel> saveOrUpdate(UserModel model, UserProfileDTO userProfile ) throws AppException, BusException {
		if(model.getId() != null && model.getId() > 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}
	
	@Override
	public Optional<UserModel> save(UserModel model, UserProfileDTO userProfile) throws AppException, BusException {
	
		Optional<UserModel> username = this.find(new UserModel(model.getUsername()));
		if(username.isPresent()) {
			if(username.get() != null && model.getUsername().trim().equals(username.get().getUsername().trim())) {
				throw new BusException(this.messageSource.getMessage("error.user.usernameExists", null, LocaleContextHolder.getLocale()));
			}
		}
		
		this.validatePasswordPattern(model.getPassword());
		
		// Save PERSON
		if(model.getPerson() != null) {
			this.personService.saveOrUpdate( model.getPerson(), userProfile );
		}
		
		// Save USER
		model.setPassword( PortalPasswordUtils.geraBCrypt(model.getPassword()));
		model.setLastPassChange(new Date());
		
		Optional<UserModel> userSaved = this.dao.save(model);
		
		// Save CUSTOMER
		this.userCustomerService.saveUserCustomer( userSaved.get().getId(), (model.getCustomers() != null ? model.getCustomers() : null));

		// Save PASS_HISTORY
		PassHistModel histModel = new PassHistModel();
		histModel.setPassword( model.getPassword() );
		histModel.setChangeDate( new Date() );
		histModel.setUser( userSaved.get() );
		
		this.passHistService.save( histModel, userProfile );
		
		this.audit( userSaved.get(), AuditOperationType.USER_INSERTED, userProfile );
	
		return userSaved;
	}
	
	@Override
	public Optional<UserModel> update(UserModel model, UserProfileDTO userProfile ) throws AppException, BusException {
		Optional<UserModel> userReturn = Optional.empty();
		Optional<UserModel> userModel = this.getById(model.getId());
		if(userModel.isPresent()) {

			this.personService.update(model.getPerson(), userProfile);
			
			this.syncUserCustomer( model );
			
			if(userModel.get().getBlocked() && !model.getBlocked()) {
				userModel.get().setPassErrorCount(0);
			}
			
			userModel.get().setUsername(model.getUsername());
			userModel.get().setCustomer(model.getCustomer());
			userModel.get().setForgotKey(model.getForgotKey());
			userModel.get().setForgotKeyCreated(model.getForgotKeyCreated());
			userModel.get().setEnabled(model.getEnabled());
			userModel.get().setBlocked(model.getBlocked());
			userModel.get().setChangePass(model.getChangePass());
			userModel.get().setExpirePass(model.getExpirePass());
			userModel.get().setAccessList(model.getAccessList());
			userModel.get().setUserType(model.getUserType());
			
			if(model.getChangePass()) {
				if( !model.getPassword().equals( userModel.get().getPassword() ) ) {
					this.validatePasswordPattern(model.getPassword());
					this.checkPasswordHistory(model);
					
					userModel.get().setChangePass(Boolean.TRUE);
					userModel.get().setPassword( PortalPasswordUtils.geraBCrypt(model.getPassword() ) );
					userModel.get().setLastPassChange( new Date() );
					
					PassHistModel histModel = new PassHistModel();
					histModel.setPassword( userModel.get().getPassword() );
					histModel.setChangeDate( new Date() );
					histModel.setUser( model );
					
					this.passHistService.save( histModel, userProfile );
				}
			}
			
			userReturn = this.dao.update(userModel.get()); 
			
			this.audit( model, AuditOperationType.USER_UPDATED, userProfile );
		}
		
		return userReturn;
	}
	
	@Override
	public Optional<UserModel> updateLoginData(String username) throws AppException, BusException {
		Optional<UserModel> user = this.findByUsername(new UserModel(username));
		if(user.isPresent()) {
			int passErrorCount = 0;
			Optional<ParameterModel> errorCountParameter = this.parameterService.find( new ParameterModel("TOTAL_ERROR_LOGIN") );
			if(errorCountParameter.isPresent()) {
				try {
					passErrorCount = Integer.parseInt(errorCountParameter.get().getValue());
				} catch (Exception e) {
					passErrorCount = 3;
				}
			}
			
			if(user.get().getPassErrorCount() >= passErrorCount) {
				user.get().setBlocked(true);
				this.dao.update(user.get());
			} else {
				user.get().setPassErrorCount(user.get().getPassErrorCount() + 1);
				this.dao.update(user.get());
			}
		}
		
		return user;
	}
	
	@Override
	public void updatePasswordErrorCount(UserModel user) throws AppException, BusException {
		user.setPassErrorCount(0);
		this.dao.update(user);
	}

	@Override
	public Optional<UserModel> saveUserConfig(UserModel model) throws AppException, BusException {
		return this.dao.saveUserConfig(model);
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile ) throws AppException, BusException {
		Optional<UserModel> model = this.getById(id);
		if(model.isPresent()) {
			this.deleteUserCustomer( model.get().getId(), 0 );
			this.passHistService.deleteByUser(model.get().getId(), userProfile);
			this.dao.delete( id );
			this.audit(model.get(), AuditOperationType.USER_DELETED, userProfile);
		}
	}
		
	@Override
	public Optional<UserModel> changePassword(UserModel model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validatePasswordPattern(model.getPassword());

			model.setPassword( PortalPasswordUtils.geraBCrypt(model.getPassword()));
			model.setLastPassChange(new Date());
			model.setExpirePass(false);
			this.dao.changePassword(model);

			PassHistModel passHist = new PassHistModel();
			passHist.setPassword( model.getPassword() );
			passHist.setChangeDate( model.getLastPassChange() );
			passHist.setUser( model );
			this.passHistService.save( passHist, userProfile );

			return Optional.ofNullable(model);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppException( e.getMessage() );
		}
	}

    @Override
	public void audit( UserModel model, AuditOperationType operationType, UserProfileDTO userProfile ) throws AppException, BusException {
    	try {
    		String details = "{}";
			details = objectMapper.writeValueAsString(model);
			
			this.auditService.save(details, operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}	
    
	private void validatePasswordPattern( String password ) throws AppException, BusException {
		String passPattern = null; 
		try {
			Optional<ParameterModel> parameterPattern = this.parameterService.find( new ParameterModel("PASS_PATTERN") );
			if(parameterPattern.isPresent()) {
				passPattern = parameterPattern.get().getValue();	
			}
		} catch( Exception e ) {
		}
				
		if( passPattern != null ) {
			//TODO DESCOBRIR PORQUE QUANDO VEM DO BANCO NÃO FUNCIONA
			passPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*(_|[^\\w])).{8,16}$";
			
			Pattern pattern = Pattern.compile( passPattern );
			Matcher matcher = pattern.matcher( Pattern.quote(password));
			
			if(!matcher.matches()) {
				throw new BusException( this.messageSource.getMessage("error.user.passPattern", null, LocaleContextHolder.getLocale()) );
			}
		}
	}
	
	private void syncUserCustomer(UserModel user) throws AppException, BusException {
		this.userCustomerService.deleteUserCustomer(user.getId(), 0);
		if(!CollectionUtils.isEmpty(user.getCustomers())) {
			for(CustomerModel customer : user.getCustomers()) {
				this.userCustomerService.saveUserCustomer(user.getId(), customer.getId());
			}
		}
	}

	public void saveUserCustomer(Integer usrId, Integer cusId) throws AppException, BusException {
		this.userCustomerService.saveUserCustomer(usrId, cusId);
	}
	
	public void saveUserCustomer(Integer usrId, List<CustomerModel> list) throws AppException, BusException {
		this.userCustomerService.saveUserCustomer(usrId, list);
	}
	
	public void deleteUserCustomer(Integer usrId, Integer cusId) throws AppException, BusException {
		this.userCustomerService.deleteUserCustomer(usrId, cusId);
	}

	public void deleteUserCustomer(final Integer usrId, final List<CustomerModel> list) throws AppException, BusException {
		this.userCustomerService.deleteUserCustomer(usrId, list);
	}
	
	private void checkPasswordHistory(UserModel user) throws AppException, BusException {
		if( user != null  ) {
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			Integer passHistNum = 6; 
			try {
				Optional<ParameterModel> parameterHist = this.parameterService.find( new ParameterModel( "PASS_HIST"));
				if(parameterHist.isPresent()) {
					passHistNum = Integer.valueOf( parameterHist.get().getValue() );
				}
			} catch( Exception e ) {
			}
			
			List<PassHistModel> hists = this.passHistService.getPassHistDescLimit(user, passHistNum);
			if( hists != null ) {
				if (hists.stream()
						.filter(passHistModel -> passwordEncoder.matches(user.getPassword(), passHistModel.getPassword()))
						.findFirst().isPresent())
					throw new BusException(this.messageSource.getMessage("error.user.repeatedPass", new Object[]{passHistNum}, LocaleContextHolder.getLocale()) );
			}
		}
	}

	public void resetPassword(Integer id) throws AppException, BusException {
		logger.info("Buscando usuario {}", id);
		Optional<UserModel> userModel = dao.getById(id);
		if(userModel.isPresent()) {
			logger.info("Aplicando reset");
			userModel.get().setPassword(PortalPasswordUtils.geraBCrypt("heineken"));
			userModel.get().setChangePass(true);
			userModel.get().setBlocked(false);
			userModel.get().setLastErrorCount(null);
			userModel.get().setPassErrorCount(0);
			userModel.get().setEnabled(true);
			userModel.get().setLastPassChange(new Date());
			
			logger.info("Salvando");
			this.dao.update(userModel.get());
			
			logger.info("Reset do usuario {} executado", id);
		} else {
			logger.info("Usuario não encontrado");
		}
	}
	
}
