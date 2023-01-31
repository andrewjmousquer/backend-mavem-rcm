package com.portal.service.imp;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IProposalDetailDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Proposal;
import com.portal.model.ProposalDetail;
import com.portal.model.ProposalDetailVehicle;
import com.portal.model.ProposalPayment;
import com.portal.model.Seller;
import com.portal.service.IAuditService;
import com.portal.service.IProposalDetailService;
import com.portal.service.IProposalDetailVehicleService;
import com.portal.service.IProposalPaymentService;
import com.portal.service.IProposalService;
import com.portal.service.ISellerService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalDetailService implements IProposalDetailService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IProposalDetailDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ISellerService sellerService;
	
	@Autowired
	private IProposalService proposalService;
	
	@Autowired
	private IProposalPaymentService proposalPaymentService; 
	
	@Autowired
	private IProposalDetailVehicleService proposalDetailVehicleService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id");


	/**
	 * Lista todos o detalhe da proposta.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id");
	 */
	@Override
	public List<ProposalDetail> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar o detalhe da proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Método auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se não tiver ID é save, caso contrário é update.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<ProposalDetail> saveOrUpdate(ProposalDetail model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}
	
	/**
	 * Salva um novo objeto.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<ProposalDetail> save( ProposalDetail model, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate( model );
			this.validateProposal(model);
			this.validateSeller(model);
			this.validateInternSale(model);
			
			model.setUser(userProfile.getUser());
			
			Optional<ProposalDetail> saved = this.dao.save( model );
			
			//this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PROPOSAL_DETAIL_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do detalhe da proposta: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza o objeto
	 * 
	 * @param model objeto lead que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<ProposalDetail> update(ProposalDetail model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnUpdate.class);
			//this.validateHasDuplicate( model );
			this.validateProposal(model);
			this.validateSeller(model);
			this.validateInternSale(model);
			
			Optional<ProposalDetail> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O detalhe da proposta a ser atualizado não existe.");
			}
			
			Optional<ProposalDetail> saved = this.dao.update(model);
			
			//this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PROPOSAL_DETAIL_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do detalhe da proposta: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca o detalhe da proposta que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto leads para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id");
	 */
	@Override
	public List<ProposalDetail> find( ProposalDetail model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar o detalhe da proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca o detalhe da proposta que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id");
	 */
	@Override
	public List<ProposalDetail> search( ProposalDetail model, Pageable pageable ) throws AppException, BusException {
		return this.find(model, pageable);
	}
	
	/**
	 * Busca leads que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(ProposalDetail, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id")
	 * 
	 * @param model objeto lead para ser buscado
	 */
	@Override
	public Optional<ProposalDetail> find(ProposalDetail model) throws AppException, BusException {
		List<ProposalDetail> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca leads que respeitem os dados do objetProposalServiceo.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(ProposalDetail, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id")
	 * 
	 * @param model objeto lead para ser buscado
	 */
	@Override
	public List<ProposalDetail> search(ProposalDetail model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca uma fonte pelo seu ID
	 * 
	 * @param id ID do detalhe da proposta
	 */
	@Override
	public Optional<ProposalDetail> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o detalhe da proposta pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos o detalhe da proposta.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id");
	 */
	@Override
	public List<ProposalDetail> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão do objeto
	 * 
	 * @param id ID do detalhe da proposta
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<ProposalDetail> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O detalhe da proposta a ser excluída não existe.");
			}
			
			this.deleteProposalPaymentRelationship( id, userProfile );
			this.deleteProposalDetailVehicleRelationship( id, userProfile );
			
			this.dao.delete( id );

			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PROPOSAL_DETAIL_DELETED, userProfile);

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do detalhe da proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(ProposalDetail model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: PPD-I1, PPD-I2, PPD-U1, PPD-U2
	 *  
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( ProposalDetail model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: PPD-I3, PPD-U3
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( ProposalDetail model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível executar a validação de duplicado pois o detalhe da proposta está nula ou inválida." );
		}
		
		ProposalDetail rnSearch = ProposalDetail.builder()
										.proposal( model.getProposal() )
										.build();

		List<ProposalDetail> listBD = this.find( rnSearch, DEFAULT_PAGINATION );

		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() &&
				listBD.stream().anyMatch(p -> !Objects.equals(p.getId(), model.getId()))) {
			throw new BusException( "Já existe um detalhe para essa proposta.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && 
				listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() )  )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe um detalhe para essa proposta.");
			}
		}
	}
	
	/**
	 * Valida se o vendedor relacionado existe
	 * 
	 * REGRA: PPD-I4, PPD-U4
	 * 
	 * @throws BusException
	 * @throws AppException
	 */
	private void validateSeller( ProposalDetail model ) throws BusException, AppException {
		try {
			if( model == null ) {
				throw new AppException( "Não é possível executar a validação do vendedor pois a entidade está nula ou inválida." );
			}

			if( model.getSeller() != null && model.getSeller().getId() != null ) {
				Optional<Seller> seller = this.sellerService.getById(model.getSeller().getId());
				if( !seller.isPresent() ) {
					throw new BusException( "Não é possível salvar o detalhe da proposta com um vendedor não existente." );
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao executar a validação do vendedor relacionado o detalhe da proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Valida se o vendedor interno relacionado existe
	 * 
	 * REGRA: PPD-I5, PPD-U5
	 * 
	 * @throws BusException
	 * @throws AppException
	 */
	private void validateInternSale( ProposalDetail model ) throws BusException, AppException {
		try {
			if( model == null ) {
				throw new AppException( "Não é possível executar a validação do vendedor interno pois a entidade está nula ou inválida." );
			}

			if( model.getInternSale() != null && model.getInternSale().getId() != null ) {
				Optional<Seller> seller = this.sellerService.getById(model.getInternSale().getId());
				if( !seller.isPresent() ) {
					throw new BusException( "Não é possível salvar o detalhe da proposta com um vendedor interno não existente." );
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao executar a validação do vendedor interno relacionado o detalhe da proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se a proposta relacionada existe
	 * 
	 * REGRA: PPD-I6, PPD-U6
	 * 
	 * @throws BusException
	 * @throws AppException
	 */
	private void validateProposal( ProposalDetail model ) throws BusException, AppException {
		try {
			if( model == null ) {
				throw new AppException( "Não é possível executar a validação da proposta pois a entidade está nula ou inválida." );
			}

			if( model.getProposal() != null && model.getProposal().getId() != null ) {
				Optional<Proposal> proposal = this.proposalService.getById( model.getProposal().getId() );
				
				if( !proposal.isPresent() ) {
					throw new BusException( "Não é possível salvar o detalhe da proposta com uma proposta não existente." );
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao executar a validação da proposta relacionado o detalhe.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Exclui todos os relacionamentos detalhemento do pagamento
	 * 
	 * REGRA: PPD-D1
	 * 
	 * AVISO: foi feita em um loop para que possamos rastrear todos os elemento excluídos e para que 
	 * o delete passe pelas regras de exclusão da entidade.
	 */
	private void deleteProposalPaymentRelationship( Integer id, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			ProposalPayment findBy = ProposalPayment.builder()
														.proposalDetail( ProposalDetail.builder().id(id).build() )
														.build();
			
			List<ProposalPayment> modelsDB = this.proposalPaymentService.search( findBy );
			if( modelsDB != null ) {
				log.debug( "Existem {} detalhes de pagamento para serem deletados.", modelsDB.size() );
				for (ProposalPayment item : modelsDB) {
					this.proposalPaymentService.delete( item.getId(), userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão dos detalhes do pagamento.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Exclui todos os relacionamentos detalhemento do pagamento
	 * 
	 * REGRA: PPD-D2
	 * 
	 * AVISO: foi feita em um loop para que possamos rastrear todos os elemento excluídos e para que 
	 * o delete passe pelas regras de exclusão da entidade.
	 */
	private void deleteProposalDetailVehicleRelationship( Integer id, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			ProposalDetailVehicle findBy = ProposalDetailVehicle.builder()
																			.proposalDetail( ProposalDetail.builder().id(id).build() )
																			.build();
			
			List<ProposalDetailVehicle> modelsDB = this.proposalDetailVehicleService.search(findBy);
			if( modelsDB != null ) {
				log.debug( "Existem {} veículos para serem deletados.", modelsDB.size() );
				for (ProposalDetailVehicle item : modelsDB) {
					this.proposalDetailVehicleService.delete( item.getId(), userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão dos veículos relacionados.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public ProposalDetail getDetailByProposal(ProposalDetail model) throws AppException, BusException {
		
		try {
			
			Optional<ProposalDetail> proposalDetail = this.dao.getDetailByProposal(model);
			
			if(proposalDetail.isPresent()) {
				proposalDetail.get().setSeller(this.sellerService.getById(proposalDetail.get().getSeller().getId()).get());

				if (proposalDetail.get().getInternSale() != null && proposalDetail.get().getInternSale().getId() > 0) {
					proposalDetail.get().setInternSale(this.sellerService.getById(proposalDetail.get().getInternSale().getId()).get());
				}
			}
			
			return proposalDetail.get();
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar o detalhe da proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
