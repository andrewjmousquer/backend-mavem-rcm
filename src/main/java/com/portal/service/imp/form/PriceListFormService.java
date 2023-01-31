package com.portal.service.imp.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IPriceListFormDAO;
import com.portal.dto.PartnerDTO;
import com.portal.dto.PriceItemDTO;
import com.portal.dto.PriceItemModelDTO;
import com.portal.dto.PriceListDTO;
import com.portal.dto.PriceProductDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.dto.form.PriceListDuplicateItemDTO;
import com.portal.dto.form.PriceListFormDTO;
import com.portal.dto.form.PriceListFormSearchDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Item;
import com.portal.model.ItemModel;
import com.portal.model.Model;
import com.portal.model.Partner;
import com.portal.model.PriceItem;
import com.portal.model.PriceItemModel;
import com.portal.model.PriceList;
import com.portal.model.PriceProduct;
import com.portal.model.ProductModel;
import com.portal.service.IBrandService;
import com.portal.service.IChannelService;
import com.portal.service.IItemModelService;
import com.portal.service.IItemService;
import com.portal.service.IModelService;
import com.portal.service.IPriceItemModelService;
import com.portal.service.IPriceItemService;
import com.portal.service.IPriceListFormService;
import com.portal.service.IPriceListPartnerService;
import com.portal.service.IPriceListService;
import com.portal.service.IPriceProductService;
import com.portal.service.IProductModelService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PriceListFormService implements IPriceListFormService {

	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IPriceListFormDAO dao;
	
	@Autowired
	private IPriceListService priceListService;
	
	@Autowired
	private IPriceProductService priceProductService;
	
	@Autowired
	private IProductModelService productModelService;
	
	@Autowired
	private IPriceItemService priceItemService;
	
	@Autowired
	private IPriceItemModelService priceItemModelService;
	
	@Autowired
	private IPriceListPartnerService priceListPartnerService;
	
	@Autowired
	private IItemModelService itemModelService;
	
	@Autowired
	private IItemService itemService;
	
	@Autowired
	private IModelService modelService;
	
	@Autowired
	private IChannelService channelService;
	
	@Autowired
	private IBrandService brandService;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id"); 

	@Override
	public void save( PriceListFormDTO model, UserProfileDTO userProfile ) throws AppException, BusException {
		
		try {
			
			log.debug( "PriceListFormService - save" );

			if( model == null ) {
				throw new AppException( "Não é possível salvar um objeto nulo." );
			}
			
			if( userProfile == null ) {
				throw new AppException( "Não é possível salvar a lista de preço com o usuário e perfil nulos." );
			}
			
			if( model.getPriceList() == null ) {
				throw new BusException( "Não é possível salvar sem os dados da lista de preço." );
			}

			if( ( model.getProducts() == null || model.getProducts().isEmpty() ) && 
					( ( model.getItensModel() == null && model.getItens() == null ) || 
					  ( model.getItensModel().isEmpty() && model.getItens().isEmpty() ) ) ) {
				throw new BusException( "Não é possível salvar sem pelo menos um produto." );
			}
			
			if( model.getPriceList().getAllPartners().booleanValue() && model.getPartners() == null) {
				throw new BusException( "Não é possível salvar sem pelo menos um parceiro." );
			}

			List<PriceListDuplicateItemDTO> duplicates = this.checkDuplicateItem( model );
			if( duplicates != null && !duplicates.isEmpty() ) {
				throw new BusException( "Não é possível salvar a lista pois existem produtos e/ou itens com sobreposição em outra lista." );
			}
			
			Optional<PriceList> newPriceList = this.priceListService.save( PriceList.toEntity( model.getPriceList() ), userProfile);
			
			if( newPriceList.isPresent() ) {
				
				for( PriceProductDTO prdModelDTO : model.getProducts() ) {
					PriceProduct prdModelEntity = PriceProduct.toEntity( prdModelDTO );
					prdModelEntity.setPriceList( newPriceList.get() );

					priceProductService.save(prdModelEntity, userProfile);
				}
				
				if( model.getItens() != null && !model.getItens().isEmpty() ) {
					for( PriceItemDTO itemDTO : model.getItens() ) {
						PriceItem itemEntity = PriceItem.toEntity( itemDTO );
						itemEntity.setPriceList( newPriceList.get() );
	
						priceItemService.save(itemEntity, userProfile);
					}
				}
				
				if( model.getItensModel() != null && !model.getItensModel().isEmpty() ) {
					for( PriceItemModelDTO itemModelDTO : model.getItensModel() ) {
						PriceItemModel itemModelEntity = PriceItemModel.toEntity( itemModelDTO );
						itemModelEntity.setPriceList( newPriceList.get() );
						
						priceItemModelService.save(itemModelEntity, userProfile);
					}
				}
				
				if( !model.getPriceList().getAllPartners().booleanValue() && model.getPartners() != null && !model.getPartners().isEmpty() ) {
					for( PartnerDTO partnerDTO : model.getPartners() ) {
						priceListPartnerService.save( newPriceList.get().getId(), partnerDTO.getId() );
					}
				}
			}
			
		} catch (AppException | BusException e) {
			throw e;
			
		} catch (Exception e) {
			throw new AppException( "Erro ao salvar o formulário da lista de preço.", e);
		}
	}
	
	@Override
	public void update(PriceListFormDTO model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			log.debug( "PriceListFormService - update" );
			
			if( model == null ) {
				throw new AppException( "Não é possível salvar um objeto nulo." );
			}
			
			if( userProfile == null ) {
				throw new AppException( "Não é possível salvar a lista de preço com o usuário e perfil nulos." );
			}
			
			if( model.getPriceList() == null ) {
				throw new BusException( "Não é possível salvar sem os dados da lista de preço." );
			}
	
			
			if( ( model.getProducts() == null || model.getProducts().isEmpty() ) && 
					( ( model.getItensModel() == null && model.getItens() == null ) || 
					  ( model.getItensModel().isEmpty() && model.getItens().isEmpty() ) ) ) {
				throw new BusException( "Não é possível salvar sem pelo menos um produto." );
			}
			
			List<PriceListDuplicateItemDTO> duplicates = this.checkDuplicateItem( model );
			if( duplicates != null && !duplicates.isEmpty() ) {
				throw new BusException( "Não é possível salvar a lista pois existem produtos e/ou itens com sobreposição em outra lista." );
			}
			
			this.priceListService.update( PriceList.toEntity( model.getPriceList() ), userProfile);
			
			this.syncProducts( model, userProfile );
			this.syncPartner( model, userProfile );
			this.syncItem(model, userProfile);
			this.syncItemModel(model, userProfile);
			
		} catch (AppException | BusException e) {
			throw e;
			
		} catch (Exception e) {
			throw new AppException( "Erro ao atualizar o formulário da lista de preço.", e);
		}
	}
	
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		log.debug( "PriceListFormService - delete" );
		try {
			
			this.priceListService.delete(id, userProfile);
			
		} catch (AppException | BusException e) {
			throw e;
			
		} catch (Exception e) {
			throw new AppException( "Erro ao excluir a lista de preço.", e);
		}
	}
	
	@Override
	public List<PriceListFormSearchDTO> search( PriceListDTO model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			List<PriceList> searchResult = null;
			if( model.getName() != null && !model.getName().isEmpty() ) {
				searchResult = priceListService.search( PriceList.toEntity( model ) );
			} else {
				searchResult = priceListService.listAll(pageable);
			}
			
			List<PriceListFormSearchDTO> result = new ArrayList<>();
			
			if( searchResult != null ) {
				for (PriceList priceList : searchResult) {
					if( priceList != null && ( priceList.getAllPartners() != null && !priceList.getAllPartners().booleanValue() ) ) {
						List<Partner> partnersDB = this.priceListPartnerService.findByPriceList( priceList.getId() );
						result.add( new PriceListFormSearchDTO( PriceListDTO.toDTO( priceList ) , ( partnersDB != null ? partnersDB.size() : 0 ) ) );
					} else {
						result.add( new PriceListFormSearchDTO( PriceListDTO.toDTO( priceList ), 0 ) );
					}
				}
			}
			
			return result;
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar uma lista de preço.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { PriceListFormService.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public Optional<PriceListFormDTO> getById( Integer id ) throws AppException, BusException {
		PriceListFormDTO form = null;

		try {
			
			if( id == null || id == 0 ) {
				throw new BusException( "Não é possivel buscar uma lista de preço com o ID nulo ou inválido" );
			}

			Optional<PriceList> priceList = this.priceListService.getById( id );
			
			if( priceList.isPresent() ) {
				PriceList findRefs = PriceList.builder().id( priceList.get().getId() ).build();
				
				Optional<Channel> channel = channelService.getById( priceList.get().getChannel().getId() );
				
				if( channel.isPresent() ) {
					priceList.get().setChannel( channel.get() );
				}
				
				List<PriceProductDTO> products = this.loadPriceProduct( findRefs );
				List<PriceItemDTO> itens = this.loadPriceItem( findRefs );
				List<PriceItemModelDTO> itensModel = this.loadPriceItemModel( findRefs );
				
				List<Partner> partners = null;
				if( !priceList.get().getAllPartners().booleanValue() ) {
					partners = priceListPartnerService.findByPriceList( findRefs.getId() );
				}
				
				form = new PriceListFormDTO();
				form.setPriceList( PriceListDTO.toDTO( priceList.get() ) );
				form.setProducts(products);
				form.setItens(itens);
				form.setItensModel(itensModel);
				form.setPartners( PartnerDTO.toDTO(partners) );
			}
			
			
		} catch (AppException | BusException e) {
			log.error( "Erro no processo de buscar uma lista de preço pelo ID.", e );
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar uma lista de preço pelo ID.", e );
			throw new AppException( "Erro no processo de buscar uma lista de preço pelo ID.", e  );
		}
		
		return Optional.ofNullable(form);
	}

	@Override
	public List<PriceListDuplicateItemDTO> checkDuplicateItem( PriceListFormDTO model ) throws AppException, BusException {
		
		try {
			
			if( model == null ) {
				throw new BusException( "Não é possível checar elementos duplicados." );
			}
			
			List<PriceListDuplicateItemDTO> duplicates = new ArrayList<>();
			
			duplicates.addAll( this.checkProductDuplication( model ) );
			duplicates.addAll( this.checkItemDuplication( model ) );
			duplicates.addAll( this.checkItemModelDuplication( model ) );
			
			return duplicates;
			
		} catch (AppException | BusException e) {
			log.error( "Erro ao verificar a sobre posição de itens da lista de preço.", e );
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a sobre posição de itens da lista de preço.", e );
			throw new AppException( "Erro ao verificar a sobre posição de itens da lista de preço.", e  );
		}
	}
	
	private List<PriceListDuplicateItemDTO> checkProductDuplication( PriceListFormDTO model ) throws AppException {
		List<PriceListDuplicateItemDTO> duplicatedList = new ArrayList<>();

		try {

			if( !model.getPriceList().getAllPartners().booleanValue() ) {
				Optional<List<PriceListDuplicateItemDTO>> overlayList = this.dao.findProductsOverlay( model.getPriceList().getStart(), model.getPriceList().getEnd() );				
				
				if( overlayList.isPresent() ) {
					
					if( model.getPartners() != null) {
						for( PartnerDTO partner : model.getPartners() ) {
							if( model.getProducts() != null ) {
						
								for( PriceProductDTO product : model.getProducts() ) {
									
									PriceListDuplicateItemDTO dto = PriceListDuplicateItemDTO.builder()
																						.channelId( model.getPriceList().getChannel().getId() )
																						.partnerId( partner.getId() )
																						.productModelId( product.getProductModel().getId() )
																						.build();
									
									List<PriceListDuplicateItemDTO> duplicatedItens = overlayList.get().parallelStream()
																						.filter( item -> !item.getPriceList().equals( model.getPriceList() ) && item.equals( dto ) )
																						.collect( Collectors.toList() );
									
									duplicatedList.addAll(duplicatedItens);
								}
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			throw new AppException( "Erro ao verificar a duplicidade de produtos dentro das listas de preço." );
		}
		
		return duplicatedList;
	}

	private List<PriceListDuplicateItemDTO> checkItemDuplication( PriceListFormDTO model ) throws AppException {
		List<PriceListDuplicateItemDTO> duplicatedList = new ArrayList<>();

		try {

			if( !model.getPriceList().getAllPartners().booleanValue() ) {
				Optional<List<PriceListDuplicateItemDTO>> overlayList = this.dao.findItemOverlay( model.getPriceList().getStart(), model.getPriceList().getEnd() );				
				
				if( overlayList.isPresent() ) {
					
					if( model.getPartners() != null) {
						for( PartnerDTO partner : model.getPartners() ) {
							if( model.getItens() != null ) {
						
								for( PriceItemDTO item : model.getItens() ) {
									
									PriceListDuplicateItemDTO dto = PriceListDuplicateItemDTO.builder()
																						.channelId( model.getPriceList().getChannel().getId() )
																						.partnerId( partner.getId() )
																						.itemId( item.getItem().getId() )
																						.build();
									
									List<PriceListDuplicateItemDTO> duplicatedItens = overlayList.get().parallelStream()
																						.filter( itemFilter -> !itemFilter.getPriceList().equals( model.getPriceList() ) && itemFilter.equals( dto ) )
																						.collect( Collectors.toList() );
									
									duplicatedList.addAll(duplicatedItens);
								}
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			throw new AppException( "Erro ao verificar a duplicidade de itens dentro das listas de preço." );
		}
		
		return duplicatedList;
	}
	
	private List<PriceListDuplicateItemDTO> checkItemModelDuplication( PriceListFormDTO model ) throws AppException {
		List<PriceListDuplicateItemDTO> duplicatedList = new ArrayList<>();

		try {

			if( !model.getPriceList().getAllPartners().booleanValue() ) {
				Optional<List<PriceListDuplicateItemDTO>> overlayList = this.dao.findItemModelOverlay( model.getPriceList().getStart(), model.getPriceList().getEnd() );				
				
				if( overlayList.isPresent() ) {
					
					if( model.getPartners() != null) {
						for( PartnerDTO partner : model.getPartners() ) {
							if( model.getItens() != null ) {
						
								for( PriceItemModelDTO item : model.getItensModel() ) {
									
									PriceListDuplicateItemDTO dto = PriceListDuplicateItemDTO.builder()
																						.channelId( model.getPriceList().getChannel().getId() )
																						.partnerId( partner.getId() )
																						.itemId( item.getItem().getId() )
																						.itemModelId( ( item.getItemModel() != null && !item.getItemModel().getId().equals(0) ? item.getItemModel().getId() : 0 ) )
																						.brandId( ( item.getBrand() != null && !item.getBrand().getId().equals(0) ? item.getBrand().getId() : 0 ) )
																						.allBrands( ( item.getAllBrands() != null ? item.getAllBrands() : false ) )
																						.allModels( ( item.getAllModels() != null ? item.getAllModels() : false ) )
																						.build();
									
									List<PriceListDuplicateItemDTO> duplicatedItens = overlayList.get().parallelStream()
																						.filter( itemFilter -> !itemFilter.getPriceList().equals( model.getPriceList() ) && itemFilter.equals( dto ) )
																						.collect( Collectors.toList() );
									
									duplicatedList.addAll(duplicatedItens);
								}
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			throw new AppException( "Erro ao verificar a duplicidade de itens dentro das listas de preço." );
		}
		
		return duplicatedList;
	}
	
	private List<PriceItemDTO> loadPriceItem(PriceList findRefs) throws AppException, BusException {
		try {
			if( findRefs != null && findRefs.getId() != null ) {
				
				List<PriceItem> priceItens = this.priceItemService.find( PriceItem.builder().priceList(findRefs).build() , DEFAULT_PAGINATION);
				
				if( priceItens != null ) {
					
					for( PriceItem priceItem : priceItens ) {
					
						Optional<Item> item = this.itemService.getById( priceItem.getItem().getId() );
						
						if( item.isPresent() ) {
							priceItem.setItem( item.get() );
						}
					}
				}
				
				return PriceItemDTO.toDTO( priceItens );
			}
			
			
		} catch (AppException | BusException e) {
			log.error( "Erro ao carregar os itens da lista de preço. ID:{}", findRefs.getId(), e );
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar os itens da lista de preço. ID:{}", findRefs.getId(), e );
			throw new AppException( "Erro ao carregar os itens da lista de preço. ID: " + findRefs.getId());
		}
		
		return new ArrayList<>();
	}

	private List<PriceItemModelDTO> loadPriceItemModel(PriceList findRefs) throws AppException, BusException {
		try {
			if( findRefs != null && findRefs.getId() != null ) {
				
				
				List<PriceItemModel> priceItensModels = this.priceItemModelService.find( PriceItemModel.builder().priceList(findRefs).build() , DEFAULT_PAGINATION);
				
				if( priceItensModels != null ) {
					
					for( PriceItemModel priceItemModel : priceItensModels ) {
						
						Optional<Item> item = this.itemService.getById( priceItemModel.getItem().getId());
						priceItemModel.setItem( (item.isPresent() ? item.get() : null ) );
						
						if( !priceItemModel.getAllBrands().booleanValue() && priceItemModel.getBrand() != null ) {
							Optional<Brand> brand = this.brandService.getById( priceItemModel.getBrand().getId() );
							priceItemModel.setBrand( ( brand.isPresent() ? brand.get() : null ) );
						}
						
						if( !priceItemModel.getAllModels().booleanValue() ) {
							Optional<ItemModel> itemModel = this.itemModelService.getById( priceItemModel.getItemModel().getId() );
							
							if( itemModel.isPresent() ) {
								ItemModel itemModelOpt = itemModel.get();
								
								item = this.itemService.getById( itemModelOpt.getItem().getId() );
								Optional<Model> model = this.modelService.getById( itemModelOpt.getModel().getId() );
								
								itemModelOpt.setItem( ( item.isPresent() ? item.get() : null ) );
								itemModelOpt.setModel( ( model.isPresent() ? model.get() : null ) );
								
								priceItemModel.setItemModel( itemModelOpt );
							}
						}
					}
				}
				
				return PriceItemModelDTO.toDTO( priceItensModels );
			}
			
			
		} catch (AppException | BusException e) {
			log.error( "Erro ao carregar os itens de modelo da lista de preço. ID:{}", findRefs.getId(), e );
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar os itens de modelo da lista de preço. ID:{}", findRefs.getId(), e );
			throw new AppException( "Erro ao carregar os itens de modelo da lista de preço. ID: " + findRefs.getId());
		}
		
		return new ArrayList<>();
	}

	private List<PriceProductDTO> loadPriceProduct(PriceList findRefs) throws AppException, BusException {
		
		try {
			if( findRefs != null && findRefs.getId() != null ) {
				
				List<PriceProduct> priceProdutcs = this.priceProductService.find( PriceProduct.builder().priceList(findRefs).build() , DEFAULT_PAGINATION);
				
				if( priceProdutcs != null ) {
					
					for( PriceProduct pricePrd : priceProdutcs ) {
					
						Optional<ProductModel> prdModels = productModelService.getById( pricePrd.getProductModel().getId() );
						
						if( prdModels.isPresent() ) {
							pricePrd.setProductModel( prdModels.get() );
						}
					}
				}
				
				return PriceProductDTO.toDTO( priceProdutcs );
			}
			
			
		} catch (AppException | BusException e) {
			log.error( "Erro ao carregar os produtos da lista de preço. ID:{}", findRefs.getId(), e );
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar os produtos da lista de preço. ID:{}", findRefs.getId(), e );
			throw new AppException( "Erro ao carregar os produtos da lista de preço. ID: " + findRefs.getId());
		}
		
		return new ArrayList<>();
	}

	private void syncProducts( PriceListFormDTO model, UserProfileDTO profile ) throws AppException, BusException {
		try {
			if( model != null ) {
				PriceList pcl = PriceList.toEntity( model.getPriceList() );
				
				PriceProduct priceProductFilter = PriceProduct.builder().priceList( pcl ).build();
				
				List<PriceProduct> existsPriceProdutcs = this.priceProductService.find( priceProductFilter, DEFAULT_PAGINATION );
				List<PriceProduct> priceProducts = PriceProduct.toEntity( model.getProducts());
				
				if( priceProducts == null ) {
					priceProducts = new ArrayList<>();
				}

				if( existsPriceProdutcs == null ) {
					existsPriceProdutcs = new ArrayList<>();
				}

				// Cria o diff da lista do BD x base para decobrir quais devem ser excluidas
				List<PriceProduct> toDelete = new ArrayList<>( existsPriceProdutcs );
				toDelete.removeAll( priceProducts );

				// Cria o diff da lista do BD x base para decobrir quais devem ser inseridas
				List<PriceProduct> toInsert = new ArrayList<>( priceProducts );
				toInsert.removeAll( existsPriceProdutcs );

				// Cria o diff da lista do BD x base para decobrir quais regras devem ser atualizadas
				List<PriceProduct> toUpdate = new ArrayList<>( priceProducts );
				toUpdate.retainAll( existsPriceProdutcs );

				for( PriceProduct item : toDelete ) {
					this.priceProductService.delete( item.getId(), profile  );
				}

				for( PriceProduct item : toInsert ) {
					item.setPriceList( pcl );
					this.priceProductService.save(item, profile);
				}

				for( PriceProduct item : toUpdate ) {
					item.setPriceList( pcl );
					this.priceProductService.update(item, profile);
				}
			}

		} catch (BusException e) {
			throw e;

		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar os produtos da lista..", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
	private void syncPartner(PriceListFormDTO model, UserProfileDTO profile ) throws AppException, BusException {
		try {
			if( model != null ) {
				PriceList pcl = PriceList.toEntity( model.getPriceList() );
				
				List<Partner> existsPartners = this.priceListPartnerService.findByPriceList( pcl.getId() );
				List<Partner> partners = Partner.toEntity( model.getPartners());
				
				if( partners == null ) {
					partners = new ArrayList<>();
				}

				if( existsPartners == null ) {
					existsPartners = new ArrayList<>();
				}

				// Cria o diff da lista do BD x base para decobrir quais devem ser excluidas
				List<Partner> toDelete = new ArrayList<>( existsPartners );
				toDelete.removeAll( partners );

				// Cria o diff da lista do BD x base para decobrir quais devem ser inseridas
				List<Partner> toInsert = new ArrayList<>( partners );
				toInsert.removeAll( existsPartners );

				// Cria o diff da lista do BD x base para decobrir quais regras devem ser atualizadas
				List<Partner> toUpdate = new ArrayList<>( partners );
				toUpdate.retainAll( existsPartners );

				for( Partner item : toDelete ) {
					this.priceListPartnerService.delete( pcl.getId(), item.getId()  );
				}

				for( Partner item : toInsert ) {
					this.priceListPartnerService.save( pcl.getId(), item.getId() );
				}

				for( Partner item : toUpdate ) {
					this.priceListPartnerService.save( pcl.getId(), item.getId() );
				}
			}

		} catch (BusException e) {
			throw e;

		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar os parceiros da lista..", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	private void syncItem( PriceListFormDTO model, UserProfileDTO profile ) throws AppException, BusException {
		try {
			if( model != null ) {
				PriceList pcl = PriceList.toEntity( model.getPriceList() );
				
				PriceItem priceItemFilter = PriceItem.builder().priceList( pcl ).build();
				
				List<PriceItem> existsPriceItem = this.priceItemService.find( priceItemFilter, DEFAULT_PAGINATION );
				List<PriceItem> priceItens = PriceItem.toEntity( model.getItens());
				
				if( priceItens == null ) {
					priceItens = new ArrayList<>();
				}

				if( existsPriceItem == null ) {
					existsPriceItem = new ArrayList<>();
				}

				// Cria o diff da lista do BD x base para decobrir quais devem ser excluidas
				List<PriceItem> toDelete = new ArrayList<>( existsPriceItem );
				toDelete.removeAll( priceItens );

				// Cria o diff da lista do BD x base para decobrir quais devem ser inseridas
				List<PriceItem> toInsert = new ArrayList<>( priceItens );
				toInsert.removeAll( existsPriceItem );

				// Cria o diff da lista do BD x base para decobrir quais regras devem ser atualizadas
				List<PriceItem> toUpdate = new ArrayList<>( priceItens );
				toUpdate.retainAll( existsPriceItem );

				for( PriceItem item : toDelete ) {
					this.priceItemService.delete( item.getId(), profile  );
				}

				for( PriceItem item : toInsert ) {
					item.setPriceList( pcl );
					this.priceItemService.save(item, profile);
				}

				for( PriceItem item : toUpdate ) {
					item.setPriceList( pcl );
					this.priceItemService.update(item, profile);
				}
			}

		} catch (BusException e) {
			throw e;

		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar os itens da lista..", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
	private void syncItemModel( PriceListFormDTO model, UserProfileDTO profile ) throws AppException, BusException {
		try {
			if( model != null ) {
				PriceList pcl = PriceList.toEntity( model.getPriceList() );
				
				PriceItemModel priceItemModelFilter = PriceItemModel.builder().priceList( pcl ).build();
				
				List<PriceItemModel> existsPriceItemModel = this.priceItemModelService.find( priceItemModelFilter, DEFAULT_PAGINATION );
				List<PriceItemModel> priceIteModels = PriceItemModel.toEntity( model.getItensModel());
				
				if( priceIteModels == null ) {
					priceIteModels = new ArrayList<>();
				}

				if( existsPriceItemModel == null ) {
					existsPriceItemModel = new ArrayList<>();
				}

				// Cria o diff da lista do BD x base para decobrir quais devem ser excluidas
				List<PriceItemModel> toDelete = new ArrayList<>( existsPriceItemModel );
				toDelete.removeAll( priceIteModels );

				// Cria o diff da lista do BD x base para decobrir quais devem ser inseridas
				List<PriceItemModel> toInsert = new ArrayList<>( priceIteModels );
				toInsert.removeAll( existsPriceItemModel );

				// Cria o diff da lista do BD x base para decobrir quais regras devem ser atualizadas
				List<PriceItemModel> toUpdate = new ArrayList<>( priceIteModels );
				toUpdate.retainAll( existsPriceItemModel );

				for( PriceItemModel item : toDelete ) {
					this.priceItemModelService.delete( item.getId(), profile  );
				}

				for( PriceItemModel item : toInsert ) {
					item.setPriceList( pcl );
					this.priceItemModelService.save(item, profile);
				}

				for( PriceItemModel item : toUpdate ) {
					item.setPriceList( pcl );
					this.priceItemModelService.update(item, profile);
				}
			}

		} catch (BusException e) {
			throw e;

		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar os itens por modelo da lista..", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
}
