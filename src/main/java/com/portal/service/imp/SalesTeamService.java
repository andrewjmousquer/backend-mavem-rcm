package com.portal.service.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.ISalesTeamDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.SalesTeam;
import com.portal.model.Seller;
import com.portal.service.IAuditService;
import com.portal.service.ISalesTeamService;
import com.portal.service.ISellerSalesTeamService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class SalesTeamService implements ISalesTeamService {

	@Autowired
	private ISalesTeamDAO dao;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ISellerSalesTeamService salesTeamSellerService;
	
	@Autowired
	public MessageSource messageSource;
    
	@Autowired
    private Validator validator;
    
	@Autowired
    private ObjectMapper objectMapper;

    private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "slt_id");

    @Override
    public Optional<SalesTeam> find(SalesTeam model) throws AppException, BusException {
        List<SalesTeam> salesTeams = this.find(model, null);
        return Optional.ofNullable(salesTeams != null ? salesTeams.get(0) : null);
    }

    @Override
    public Optional<SalesTeam> getById(Integer id) throws AppException, BusException {
        try {

            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            Optional<SalesTeam> salesTeam = this.dao.getById(id);
            if(salesTeam.isPresent()) {
            	salesTeam.get().setSellerList(salesTeamSellerService.findBySalesTeam(salesTeam.get().getId()));
            }
            
            return salesTeam;
        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar uma celula de venda pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getById", new Object[]{SalesTeam.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<SalesTeam> list() throws AppException, BusException {
        List<SalesTeam> salesTeamList = this.listAll(null);
        return salesTeamList;
    }

    @Override
    public List<SalesTeam> search(SalesTeam model) throws AppException, BusException {
        return this.search(model, null);
    }

    @Override
    public Optional<SalesTeam> saveOrUpdate(SalesTeam model, UserProfileDTO userProfile) throws AppException, BusException {
        if (model.getId() != null && model.getId() > 0) {
            return this.update(model, userProfile);
        } else {
            return this.save(model, userProfile);
        }
    }

    @Override
    public Optional<SalesTeam> save(SalesTeam model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, OnSave.class);
            this.validateHasDuplicate(model);

            Optional<SalesTeam> saved = this.dao.save(model);
            if(saved.isPresent()) {
            	if(model.getSellerList() != null && !model.getSellerList().isEmpty()) {
            		saved.get().setSellerList(model.getSellerList());
            	}
            	
            	this.syncSalesTeamSellerRelationship(saved.get(), userProfile);
            }
            
            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.SALES_TEAM_INSERTED, userProfile);

            return saved;

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro no processo de cadastro da celula de venda: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.save", new Object[]{SalesTeam.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<SalesTeam> update(SalesTeam model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, OnUpdate.class);
            this.validateHasDuplicate(model);
            
            Optional<SalesTeam> saved = this.dao.update(model);
            if(saved.isPresent()) {
            	if(model.getSellerList() != null && !model.getSellerList().isEmpty()) {
            		saved.get().setSellerList(model.getSellerList());
            	}
            	
            	this.syncSalesTeamSellerRelationship(saved.get(), userProfile);
            }
            
            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.SALES_TEAM_UPDATED, userProfile);

            return saved;

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de atualização da célula de venda: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.update", new Object[]{SalesTeam.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }


    @Override
    public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de exclusão inválido");
            }

            Optional<SalesTeam> entityDB = this.getById(id);
            if (!entityDB.isPresent()) {
                throw new BusException("Celula de venda a ser excluído não existe.");
            }
            
            this.salesTeamSellerService.delete(null, id, userProfile);
            this.dao.delete(id);

            this.audit((entityDB.isPresent() ? entityDB.get() : null), AuditOperationType.SALES_TEAM_DELETED, userProfile);

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclusão da celula de venda .", e);
            throw new AppException(this.messageSource.getMessage("error.generic.delete", new Object[]{SalesTeam.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void audit(SalesTeam model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.auditService.save(objectMapper.writeValueAsString(model), operationType, userProfile);
        } catch (JsonProcessingException e) {
            throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<SalesTeam> listAll(Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }
            return this.dao.listAll(pageable);

        } catch (Exception e) {
            log.error("Erro no processo de listar celulas de venda.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{SalesTeam.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<SalesTeam> search(SalesTeam model, Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            List<SalesTeam> salesTeams = this.dao.search(model, pageable);
            return salesTeams;

        } catch (Exception e) {
            log.error("Erro no processo de procurar as celulas de venda .", e);
            throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{SalesTeam.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<SalesTeam> findBySeller(Integer id) throws AppException {
        return this.dao.findBySeller(id);
    }

    @Override
    public List<SalesTeam> find(SalesTeam model, Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.find(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de buscar as celulas de venda.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{SalesTeam.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    private void validateEntity(SalesTeam model, Class<?> group) throws BusException {
        ValidationHelper.generateException(validator.validate(model, group));
    }

	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( SalesTeam model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível checar a duplicidade com o objeto da entidade nula." );
		}
		
		SalesTeam rnSearch = SalesTeam.builder().name( model.getName() ).build();
		List<SalesTeam> listBD = this.find( rnSearch, null );

		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "Já existe uma célula de venda com o mesmo nome.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && 
				listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe uma célula de venda com o mesmo nome.");
			}
		}
	}
    
	/**
	 * Função que sincroniza os vendedores que são da célula de venda, essa função executa 2 operações
	 * 
	 * 1 - Excluir relacionamento com celula de venda, quando a lista das celulas de vendas não contém mais os vendedores ( ID ) na lista
	 * 2 - Insere relacionamento com celula de venda, quando a lista das celulas de vendas contém novos vendedores que ainda não existem salvas no relacionamento
	 * 
	 * @param model - objeto da celula de vendas que vamos usar na sincronização
	 * @param userProfile 
	 * @throws AppException
	 * @throws BusException
	 */
	private void syncSalesTeamSellerRelationship(SalesTeam model, UserProfileDTO userProfile) throws BusException, NoSuchMessageException, AppException {
		try {
			if( model != null && model.getId() != null && !model.getId().equals(0) ) {
				List<Seller> existsSellers = this.salesTeamSellerService.findBySalesTeam( model.getId() );
				List<Seller> sellers = model.getSellerList() != null && model.getSellerList().size() > 0 ? model.getSellerList() : null;
				
				if( sellers == null ) {
					sellers = new ArrayList<>();
				}
				
				if( existsSellers == null ) {
					existsSellers = new ArrayList<>();
				}
				
				List<Seller> toDelete = new ArrayList<>( existsSellers );
				toDelete.removeAll( sellers );
	
				List<Seller> toInsert = new ArrayList<>( sellers );
				toInsert.removeAll( existsSellers );
				
				for( Seller seller : toDelete ) {
					this.salesTeamSellerService.delete( seller.getId(), model.getId(), userProfile);
				}
				
				for( Seller seller : toInsert ) {
					this.salesTeamSellerService.save( seller.getId(), model.getId(), userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar marca e parceiro.", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale())); 
		}
	}

}
