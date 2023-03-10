package com.portal.service.imp;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Validator;

import org.apache.commons.collections4.map.HashedMap;
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
import com.portal.dao.ISalesOrderDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Proposal;
import com.portal.model.SalesOrder;
import com.portal.service.IAuditService;
import com.portal.service.IJiraIntegrationService;
import com.portal.service.ISalesOrderService;
import com.portal.utils.PortalStringUtils;
import com.portal.utils.PortalTimeUtils;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class SalesOrderService implements ISalesOrderService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private ISalesOrderDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private ProposalService proposalService;
	
	@Autowired
	private IJiraIntegrationService jiraIntegrationService;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pps_id");


	/**
	 * Lista todos os Pedidos de venda.
	 * 
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pps_id");
	 */
	@Override
	public List<SalesOrder> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar Pedidos de Venda.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { SalesOrder.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * M??todo auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se n??o tiver ID ?? save, caso contr??rio ?? update.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usu??rio logado.
	 */
	@Override
	public Optional<SalesOrder> saveOrUpdate(SalesOrder model, UserProfileDTO userProfile) throws AppException, BusException {
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
	 * @param userProfile dados do usu??rio logado.
	 */
	@Override
	public Optional<SalesOrder> save(SalesOrder model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate( model );
			
			Optional<SalesOrder> saved = this.dao.save( model );
			
			// Carregamos para conseguir ter o n??mero gerado
			Optional<SalesOrder> reload = this.getById( saved.get().getId() );
			
			this.audit( ( reload.isPresent() ? reload.get() : null ), AuditOperationType.SALES_INSERTED, userProfile);
			
			// Atualiza a Chave do Jira ap??s integra????o
			if(reload.isPresent()) {
				
				String jiraKey = this.generateTaskJira(reload.get(), userProfile);
				
				reload.get().setJiraKey(jiraKey);
				
				this.update(reload.get(), userProfile);
				
			}
			
			return reload;
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro de Pedido de Venda: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { SalesOrder.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um Pedido de Venda
	 * 
	 * @param model objeto lead que deve ser salvo.
	 * @param userProfile dados do usu??rio logado.
	 */
	@Override
	public Optional<SalesOrder> update(SalesOrder model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnUpdate.class);
			this.validateHasDuplicate( model );
			
			Optional<SalesOrder> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O Pedido de Venda a ser atualizado n??o existe.");
			}
			
			Optional<SalesOrder> saved = this.dao.update(model);
			
			// Carregamos para conseguir ter o n??mero gerado
			Optional<SalesOrder> reload = this.getById( saved.get().getId() );
			
			this.audit( ( reload.isPresent() ? reload.get() : null ), AuditOperationType.SALES_UPDATED, userProfile);
			
			return reload;

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de atualiza????o de proposta: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { SalesOrder.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca as propostas que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o '='
	 * 
	 * @param model objeto leads para ser buscado
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pps_id");
	 */
	@Override
	public List<SalesOrder> find( SalesOrder model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar as propostas.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { SalesOrder.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca as propostas que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o 'LIKE'
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pps_id");
	 */
	@Override
	public List<SalesOrder> search( SalesOrder model, Pageable pageable ) throws AppException, BusException {
		return this.find(model, pageable);
	}

	@Override
	public Optional<SalesOrder> findByProposal(Integer id) throws BusException, AppException {
		try {

			if( id == null ) {
				throw new BusException( "ID de busca inv??lido." );
			}

			Optional<SalesOrder> SalesOrder = this.dao.findByProposal(id);

			return SalesOrder;

		} catch (BusException e) {
			throw e;

		} catch (Exception e) {
			log.error( "Erro ao consultar uma proposta pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.findByProposal", new Object [] { SalesOrder.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca leads que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o '='
	 * 
	 * Esse ?? m??todo ?? uma sobrecarga de {@link #search(SalesOrder, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pps_id")
	 * 
	 * @param model objeto lead para ser buscado
	 */
	@Override
	public Optional<SalesOrder> find(SalesOrder model) throws AppException, BusException {
		List<SalesOrder> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca leads que respeitem os dados do objetSalesOrderServiceo.
	 * Aqui os campos String s??o buscados com o 'LIKE'
	 * 
	 * Esse ?? m??todo ?? uma sobrecarga de {@link #search(SalesOrder, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pps_id")
	 * 
	 * @param model objeto lead para ser buscado
	 */
	@Override
	public List<SalesOrder> search(SalesOrder model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca uma fonte pelo seu ID
	 * 
	 * @param id ID de proposta
	 */
	@Override
	public Optional<SalesOrder> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inv??lido." );
			}
			
			Optional<SalesOrder> SalesOrder = this.dao.getById(id);
			
			return SalesOrder;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar uma proposta pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { SalesOrder.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos as propostas.
	 *
	 * Esse m??todo ?? uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pps_id");
	 */
	@Override
	public List<SalesOrder> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclus??o de um lead
	 * 
	 * @param id ID de proposta
	 * @param userProfile dados do usu??rio logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclus??o inv??lido." );
			}
			
			Optional<SalesOrder> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O Pedido de Venda a ser exclu??do n??o existe.");
			}
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.SALES_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclus??o de proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { SalesOrder.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	@Override
	public void audit(SalesOrder model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	
	/**
	 * Valida a entidade como um todo, passando por regras de formata????o e obrigatoriedade
	 * 
	 *  
	 * @param model entidade a ser validada
	 * @param group grupo de valida????o que ser?? usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( SalesOrder model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}
	
	// FIXME Adicionar numero da Regra de neg??cio quando criado.
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: **** 
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( SalesOrder model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "N??o ?? poss??vel executar a valida????o de duplicado pois o Pedido de Venda est?? nula ou inv??lida." );
		}
		
		SalesOrder rnSearch = SalesOrder.builder().proposal(model.getProposal()).build();

		List<SalesOrder> listBD = this.find( rnSearch, DEFAULT_PAGINATION );

		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() &&
				listBD.stream().anyMatch(p -> !Objects.equals(p.getId(), model.getId()))) {
			throw new BusException( "J?? existe um Pedido de Venda para esta proposta: " + model.getProposal().getNum() );
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && 
				listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getProposal().getNum().equals( model.getProposal().getNum() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "J?? existe uma proposta com a mesma vers??o. Vers??o: " + model.getProposal().getNum() );
			}
		}
	}
	
	/**
	 * Monta a lista de parametros que ser?? enviado para o JIRA criar uma nova tarefa.
	 * 
	 * Regra: **** 
	 * 
	 * @param salesOrder Pedido de Venda que ir?? ser integrado
	 * @throws BusException
	 * @throws AppException
	 */
	private String generateTaskJira(SalesOrder salesOrder, UserProfileDTO requester) throws AppException, BusException {
		try {
			
		Optional<Proposal> proposalToFinded = proposalService.getAllProposalId(salesOrder.getProposal().getId());
		
		if(!proposalToFinded.isPresent())
			throw new BusException( "Proposta n??o encontrada: " + salesOrder.getProposal().getNum() );
		
		this.validateFieldsForJiraIntegration(salesOrder , proposalToFinded.get());
		
		Map<String, Object> valuesIntegration = new HashedMap<>();
		
		valuesIntegration.put(PortalStringUtils.jiraIntegrationFormat("marca"), proposalToFinded.get().getProposalDetailVehicle().getVehicle().getModel().getBrand().getName());
		valuesIntegration.put(PortalStringUtils.jiraIntegrationFormat("modelo"), proposalToFinded.get().getProposalDetailVehicle().getVehicle().getModel().getName());
		valuesIntegration.put(PortalStringUtils.jiraIntegrationFormat("ano modelo"), proposalToFinded.get().getProposalDetailVehicle().getVehicle().getModelYear().toString());
		valuesIntegration.put(PortalStringUtils.jiraIntegrationFormat("placa"), proposalToFinded.get().getProposalDetailVehicle().getVehicle().getPlate());
		valuesIntegration.put(PortalStringUtils.jiraIntegrationFormat("chassi"), proposalToFinded.get().getProposalDetailVehicle().getVehicle().getChassi());
		valuesIntegration.put(PortalStringUtils.jiraIntegrationFormat("data da compra"), PortalTimeUtils.localDateFormat(proposalToFinded.get().getProposalDetailVehicle().getVehicle().getPurchaseDate(), "yyyy-MM-dd"));
		valuesIntegration.put(PortalStringUtils.jiraIntegrationFormat("marca modelo"), proposalToFinded.get().getProposalDetailVehicle().getVehicle().getModel().getBrand().getName() + "/" + proposalToFinded.get().getProposalDetailVehicle().getVehicle().getModel().getName());
		valuesIntegration.put(PortalStringUtils.jiraIntegrationFormat("numero os"), salesOrder.getOrderNumber());
		valuesIntegration.put(PortalStringUtils.jiraIntegrationFormat("chassi ultimos numeros"), proposalToFinded.get().getProposalDetailVehicle().getVehicle().getChassi().substring(proposalToFinded.get().getProposalDetailVehicle().getVehicle().getChassi().length() - 6, proposalToFinded.get().getProposalDetailVehicle().getVehicle().getChassi().length()));
		
		String issueName = valuesIntegration.get(PortalStringUtils.jiraIntegrationFormat("numero os")).toString() + "-" + 
				valuesIntegration.get(PortalStringUtils.jiraIntegrationFormat("marca modelo")).toString() ;
		
		if(!proposalToFinded.isPresent())
			throw new BusException("Proposta do Pedido de Venda n??o existe");
		
		return jiraIntegrationService.createIssue(issueName, valuesIntegration, requester);
		
		} catch (BusException e) {
			throw new BusException(e.getMessage()); 
		}catch (AppException e) {
			throw new AppException(e.getMessage()); 
		} catch (Exception e) {
			throw new AppException("Ocorreu um erro na integra????o com o JIRA, contate o administrador"); 
		}
		
	}
	
	/**
	 * Valida se existe todas as entidades necess??rias para criar TASK JIRA.
	 * 
	 * Regra: **** 
	 * 
	 * @param salesOrder entidade a ser valiadada
	 * @param proposal entidade a ser valiadada
	 * @throws BusException
	 */
	private void validateFieldsForJiraIntegration(SalesOrder salesOrder, Proposal proposal) throws BusException {
		
		if(proposal.getProposalDetailVehicle() == null) 
			throw new BusException( "Detalhamento de veiculo em proposta n??o encontrado!");
		
		if(proposal.getProposalDetailVehicle().getVehicle() == null) 
			throw new BusException( "Veiculo n??o encontrado!");
		
		if(proposal.getProposalDetailVehicle().getVehicle().getModel() == null)
			throw new BusException( "Modelo de veiculo n??o encontrado!");
		
		if(proposal.getProposalDetailVehicle().getVehicle().getChassi() == null)
			throw new BusException( "Chassi de veiculo n??o encontrado!");
		
		if(proposal.getProposalDetailVehicle().getVehicle().getModel().getBrand() == null)
			throw new BusException( "Marca de veiculo n??o encontrado!");
		
		if(salesOrder.getOrderNumber() == null || salesOrder.getOrderNumber() == 0)
			throw new BusException( "Numero do Pedido de Venda n??o encontrado!");
			
	}

	
}
