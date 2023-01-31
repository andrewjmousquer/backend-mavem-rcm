package com.portal.service.imp;

import java.util.List;
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

import com.portal.dao.IProposalDetailVehicleItemDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalDetail;
import com.portal.model.ProposalDetailVehicle;
import com.portal.model.ProposalDetailVehicleItem;
import com.portal.service.IItemService;
import com.portal.service.IPriceItemModelService;
import com.portal.service.IPriceItemService;
import com.portal.service.IProposalDetailVehicleItemService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalDetailVehicleItemService implements IProposalDetailVehicleItemService {
	
	@Autowired
    private Validator validator;
	
	@Autowired
	private IProposalDetailVehicleItemDAO dao;
	
	@Autowired
	private IPriceItemService priceItemService;
	
	@Autowired
	private IPriceItemModelService priceItemModelService;
	
	@Autowired
	private IItemService itemService;
	
	@Autowired
    public MessageSource messageSource;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pdvi_id");
	
	@Override
	public Optional<ProposalDetailVehicleItem> find(ProposalDetailVehicleItem model) throws AppException, BusException {
		List<ProposalDetailVehicleItem> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	@Override
	public Optional<ProposalDetailVehicleItem> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o item de veiculo da proposta pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { ProposalDetail.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public List<ProposalDetailVehicleItem> list() throws AppException, BusException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProposalDetailVehicleItem> search(ProposalDetailVehicleItem model) throws AppException, BusException {
		return dao.search(model);
	}

	@Override
	public Optional<ProposalDetailVehicleItem> saveOrUpdate(ProposalDetailVehicleItem model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}

	@Override
	public Optional<ProposalDetailVehicleItem> save(ProposalDetailVehicleItem model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			
			if(model.getItemPrice() != null) {
				if(model.getItemPrice().getId() == null) {
					log.error( "O detalhe do veículo da proposta a ser atualizado não contem lista de preco ou lista de preco de modelo: {}", model );
					throw new BusException( "O detalhe do veículo da proposta a ser atualizado não contem lista de preco ou lista de preco de modelo.");
				}
			}
			
			if(model.getItemPriceModel() != null) {
				if(model.getItemPriceModel().getId() == null) {
					log.error( "O detalhe do veículo da proposta a ser atualizado não contem lista de preco ou lista de preco de modelo: {}", model );
					throw new BusException( "O detalhe do veículo da proposta a ser atualizado não contem lista de preco ou lista de preco de modelo.");
				}
			}
			
			Optional<ProposalDetailVehicleItem> saved = this.dao.save( model );
			
			//this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PROPOSAL_DETAIL_VEHICLE_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do detalhe do item do veículo da proposta: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { ProposalDetailVehicle.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public Optional<ProposalDetailVehicleItem> update(ProposalDetailVehicleItem model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnUpdate.class);
			
			Optional<ProposalDetailVehicleItem> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O detalhe do veículo da proposta a ser atualizado não existe.");
			}
			
			if(model.getItemPrice() != null) {
				if(model.getItemPrice().getId() == null) {
					log.error( "O detalhe do veículo da proposta a ser atualizado não contem lista de preco ou lista de preco de modelo: {}", model );
					throw new BusException( "O detalhe do veículo da proposta a ser atualizado não contem lista de preco ou lista de preco de modelo.");
				}
			}
			
			if(model.getItemPriceModel() != null) {
				if(model.getItemPriceModel().getId() == null) {
					log.error( "O detalhe do veículo da proposta a ser atualizado não contem lista de preco ou lista de preco de modelo: {}", model );
					throw new BusException( "O detalhe do veículo da proposta a ser atualizado não contem lista de preco ou lista de preco de modelo.");
				}
			}
			
			Optional<ProposalDetailVehicleItem> saved = this.dao.update(model);
			
			return saved;

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do detalhe do item do veículo da proposta: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { ProposalDetailVehicle.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
		
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<ProposalDetailVehicleItem> entityDB = this.getById(id);
			
			if( !entityDB.isPresent() ) {
				throw new BusException( "O detalhe da proposta a ser excluída não existe.");
			}
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do detalhe do veículo da proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { ProposalDetailVehicle.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(ProposalDetailVehicleItem model, AuditOperationType operationType, UserProfileDTO userProfile)
			throws AppException, BusException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ProposalDetailVehicleItem> find(ProposalDetailVehicleItem model, Pageable pageable) throws AppException, BusException {

		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			List<ProposalDetailVehicleItem> proposalDetailVehicleItems = this.dao.find( model, pageable ); 
			
			if(proposalDetailVehicleItems.size() > 0) {
				
				proposalDetailVehicleItems.forEach( items -> {
					
					try {
						
						if(items.getItemPrice() != null) {
							if(items.getItemPrice().getId() > 0) {

								items.setItemPriceModel(null);
								items.setItemPrice(this.priceItemService.getById(items.getItemPrice().getId()).get());
								
								items.getItemPrice().setItem(this.itemService.getById(items.getItemPrice().getItem().getId()).get());
							}
						}
						
						if(items.getItemPriceModel() != null) {
							if(items.getItemPriceModel().getId() > 0) {
								
								items.setItemPrice(null);
								items.setItemPriceModel(this.priceItemModelService.getById(items.getItemPriceModel().getId()).get());
								
								items.getItemPriceModel().setItem(this.itemService.getById(items.getItemPriceModel().getItem().getId()).get());
							}
						}
						
					} catch (AppException | BusException e) {
						e.printStackTrace();
					}
				});
			}
			
			return proposalDetailVehicleItems;
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar o detalhe dos itens do veículo da proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { ProposalDetailVehicleItem.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: PDV-I1,PDV-I2,PDV-I4
	 * 		  PDV-U1,PDV-U2,PDV-U4
	 *  
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( ProposalDetailVehicleItem model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}

}
