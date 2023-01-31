package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IPersonQualificationDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PaymentMethod;
import com.portal.model.Person;
import com.portal.model.PersonQualification;
import com.portal.model.Qualification;
import com.portal.service.IPersonQualificationService;
import com.portal.service.IPersonService;
import com.portal.service.IQualificationService;
import com.portal.validators.ValidationHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PersonQualificationService implements IPersonQualificationService {
	
	@Autowired
    private Validator validator;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IPersonQualificationDAO dao;
	
	@Autowired
	private IQualificationService qualificationService;
	
	@Autowired
	private IPersonService personService;
	
	@Override
	public List<PersonQualification> findByPerson(Integer perId) throws AppException, BusException {
		
		if( perId == null || perId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre pessoa e qualificação com o ID da pessoa inválido." );
		}
		
		return this.find( PersonQualification.builder().person( Person.builder().id(perId).build()  ).build() );
	}

	@Override
	public List<PersonQualification> findByQualification(Integer qlfId) throws AppException, BusException {
		if( qlfId == null || qlfId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre pessoa e qualificação com o ID da qualificação inválido." );
		}
		
		return this.find( PersonQualification.builder().qualification( Qualification.builder().id(qlfId).build() ).build() );
	}
	
	@Override
	public List<PersonQualification> find( PersonQualification qualification ) throws AppException, BusException {
		
		if( qualification == null ) {
			throw new BusException( "Não é possível buscar o relacionamento entre pessoa e qualificação com o ID da qualificação e pessoa inválido." );
		}
		
		return this.dao.find(qualification);
	}

	@Override
	public void save( PersonQualification model ) throws AppException, BusException {
		try {
			
			this.validateEntity( model );
			
			// REGRA: PER-QLF-I4,PER-QLF-U4
			Optional<Person> personDB = this.personService.getById( model.getPerson().getId() );
			if( !personDB.isPresent() ) {
				throw new BusException( "Não é possível salvar o relacionamento entre pessoa e qualificação com a pessoa inválida ou inexistente." );
			}
			
			// REGRA: PER-QLF-I2,PER-QLF-U2
			Optional<Qualification> qualificationDB = this.qualificationService.getById( model.getQualification().getId() );
			if( !qualificationDB.isPresent() ) {
				throw new BusException( "Não é possível salvar o relacionamento entre pessoa e qualificação com a qualificação inválida ou inexistente." );
			}
			
			this.dao.save( model );
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo salvar o relacionamento entre pessoa e qualificação: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public void delete(Integer perId, Integer qlfId) throws AppException, BusException {
		if( perId == null || perId.equals( 0 ) || qlfId == null || qlfId.equals( 0 ) ) {
			throw new BusException( "Não é possível excluir o relacionamento entre pessoa e qualificação com o ID da pessoa e qualificação inválidos." );
		}
		
		this.dao.delete(perId, qlfId);
	}

	@Override
	public void deleteByPerson(Integer perId) throws AppException, BusException {
	
		this.dao.deleteByPerson(perId);
	}

	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( PersonQualification model ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model ) );
	}

    @Override
    public void deleteByPerson(PersonQualification modelDelete, UserProfileDTO userProfile) throws AppException {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void update(PersonQualification entity, UserProfileDTO userProfile) {
        // TODO Auto-generated method stub
        
    }

}