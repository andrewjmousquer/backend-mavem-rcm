package com.portal.service.imp;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.validation.Validator;

import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IItemDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Item;
import com.portal.model.ItemModel;
import com.portal.model.Model;
import com.portal.model.PaymentMethod;
import com.portal.model.Person;
import com.portal.service.IAuditService;
import com.portal.service.IItemModelService;
import com.portal.service.IItemService;
import com.portal.service.IModelService;
import com.portal.utils.FileUtils;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ItemService implements IItemService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IItemDAO dao;
	
	@Autowired
	private IItemModelService itemModelService;
	
	@Autowired
	private IModelService modelService;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Value("${store.location.item}")
	private String locationItem;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itm_id"); 

	/**
	 * Lista todos os itens.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itm_id");
	 */
	@Override
	public List<Item> listAll( Pageable pageable ) throws AppException, BusException {
		
		try {
			
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			List<Item> listItem = this.dao.listAll( pageable );
			/* Caso necessário reativar o codigo onde busca dados de ItemModel
			listItem.forEach(item ->{
				ItemModel itemModel = new ItemModel();
				itemModel.setItem(item);
				
				try {
					item.setItemModels(this.itemModelService.find( itemModel,null));
				} catch (AppException | BusException e) {
					e.printStackTrace();
				}
			});
			*/	
			return listItem;
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar os itens.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Item.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<Item> saveOrUpdate(Item model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<Item> save(Item model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate(model);
			
			Optional<Item> saved = this.dao.save(model);
			
			if(model.getItemModels() != null) {
				
				model.getItemModels().forEach(itemModel -> {
					try {
						itemModel.setItem(saved.get());
						Model m = this.modelService.getById(itemModel.getModel().getId()).get();
						itemModel.setModel(m);
						
						this.itemModelService.save(itemModel, userProfile);
					} catch (AppException | BusException e) {
						log.error( "Erro no processo de cadastro do item -> itemModel: {}", model, e );
					}
				});
			}
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.ITEM_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do item: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Item.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um item
	 * 
	 * @param model objeto item que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Item> update(Item model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			this.validateEntity(model, OnUpdate.class);
			
			// ITM-U4
			Optional<Item> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O item a ser atualizado não existe.");
			}
			
			this.validateHasDuplicate(model);
			
			//Regra: ITM-D3
			this.syncFiles(model, modelDB.get());
			
			/*
			 * Não é permitido alterara a classificação de GENÉRICO ou NÃO do item se estiver associado a uma lista de preço 
			 */
			if( !modelDB.get().getGeneric().equals(model.getGeneric()) ) {
				boolean existsItem = this.dao.hasPriceItemRelationship( model.getId() );
				
				if( existsItem ) {
					throw new BusException( "Não é possível alterar a classificação de genérico do item, pois existe uma lista de preço associada." );
					
				} else {
					boolean existsItemModel = this.dao.hasPriceItemModelRelationship( model.getId() );
					
					if( existsItemModel ) {
						throw new BusException( "Não é possível alterar a classificação de genérico do item, pois existe uma lista de preço associada." );
					}
				}
			}
			
			
			if(model.getItemModels() != null) {
				ItemModel filter = ItemModel.builder().item( Item.builder().id( model.getId() ).build() ).build();
				List<ItemModel> itemModelsBD = this.itemModelService.find( filter, DEFAULT_PAGINATION );
				
				itemModelsBD.removeAll(model.getItemModels());
				
				if( !itemModelsBD.isEmpty() ) {
					for (ItemModel item : itemModelsBD) {
						this.itemModelService.delete(item.getId(), userProfile);
					}
				
				}
				
				model.getItemModels().forEach(itemModel -> {
					try {
						itemModel.setItem(model);
						
						Model m = this.modelService.getById(itemModel.getModel().getId()).get();
						itemModel.setModel(m);
						
						this.itemModelService.saveOrUpdate(itemModel, userProfile);
					} catch (AppException | BusException e) {
						e.printStackTrace();
					}
				});
			}
			
			Optional<Item> saved = this.dao.update(model);

			saved.get().setItemModels(model.getItemModels());
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.ITEM_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do item: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Item.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public void updateFile(Integer id, String column, String value) throws AppException {
		dao.updateFile(id, column, value);
	}
	
	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itm_id");
	 */
	@Override
	public List<Item> find( Item model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar os itens.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Item.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itm_id");
	 */
	@Override
	public List<Item> search( Item model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar os itens.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { Item.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Item, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itm_id")
	 * 
	 * @param model objeto item para ser buscado
	 */
	@Override
	public Optional<Item> find(Item model) throws AppException, BusException {
		List<Item> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Item, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itm_id")
	 * 
	 * @param model objeto item para ser buscado
	 */
	@Override
	public List<Item> search(Item model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca um item pelo seu ID
	 * 
	 * @param id ID do item
	 */
	@Override
	public Optional<Item> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			Optional<Item> item = this.dao.getById( id );
			
			ItemModel itemModel = new ItemModel();
			itemModel.setItem(item.get());
			
			List<ItemModel> itemModelList = this.itemModelService.find( itemModel , null);
			
			if(itemModelList.size() > 0) {
			
				itemModelList.forEach(itemM -> {
					
					try {
						
						Optional<Model> model = this.modelService.getById(itemM.getModel().getId());
						
						itemM.setModel(model.get());
						
					} catch (AppException | BusException e) {
						e.printStackTrace();
					}
				});
				
				item.get().setItemModels(itemModelList);
			}
			
			return item;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um item pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Item.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos os itens.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itm_id");
	 */
	@Override
	public List<Item> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de um item
	 * 
	 * @param id ID do item
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<Item> entityDB = this.getById(id);
			if( entityDB == null || !entityDB.isPresent() ) {
				throw new BusException( "O item a ser excluído não existe.");
			}
			
			// Regra: ITM-D1
			this.deleteItemModelRelationship(id, userProfile);

			// Regra: ITM-D2
			this.validatePriceListRelationship( id );

			//Regra: ITM-D3
			this.deleteFiles(entityDB.get());
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.ITEM_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do item.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Item.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	/**
	 * Exclui todos os relacionamentos com modelo
	 * 
	 * REGRA: ITM-D1
	 * 
	 * AVISO: foi feita em um loop para que possamos rastrear todos os elemento excluídos e para que 
	 * o delete passe pelas regras de exclusão da entidade.
	 */
	private void deleteItemModelRelationship( Integer id, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			ItemModel findByItem = ItemModel.builder()
											.item( Item.builder().id(id).build() )
											.build();
			
			List<ItemModel> modelsDB = this.itemModelService.find(findByItem, DEFAULT_PAGINATION);
			if( modelsDB != null ) {
				log.debug( "Existem {} modelos para serem deletados.", modelsDB.size() );
				for (ItemModel item : modelsDB) {
					Integer imdId = ( item.getItem() != null ? item.getId() : null );
					this.itemModelService.delete( imdId, userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do relacionamento com modelo,", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	private void syncFiles(Item item, Item itemDB) {
		try {
			if(item.getFile() == null || item.getFile().equals("")) {
				Files.deleteIfExists(Paths.get(this.locationItem + "/" + itemDB.getFile()));
			}
		
			if(item.getIcon() == null || item.getIcon().equals("")) {
				Files.deleteIfExists(Paths.get(this.locationItem + "/" + itemDB.getIcon()));
			}
		} catch (IOException e) {
			log.error("Erro ao apagar arquivo");
			e.printStackTrace();
		}
	}
	
	private void deleteFiles(Item item) {
		try {
			Files.deleteIfExists(Paths.get(this.locationItem + "/" + item.getFile()));
			Files.deleteIfExists(Paths.get(this.locationItem + "/" + item.getIcon()));
		} catch (IOException e) {
			log.error("Erro ao apagar arquivo");
			e.printStackTrace();
		}
	}
	
	@Override
	public void audit(Item model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: ITM-I6, ITM-U6, 
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( Item model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}

		Item rnSearch = Item.builder()
				.name( model.getName() )
				.build();

		List<Item> listBD = this.find( rnSearch, null );
		
		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "Já existe um item com o mesmo nome.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && 
				listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe um item com o mesmo nome.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: ITM-I1,ITM-I2,ITM-I3, ITM-I4,ITM-I5 / ITM-U1,ITM-U2,ITM-U3, ITM-U4,ITM-U5
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( Item model, Class<?> group ) throws AppException, BusException {
		try {
			ValidationHelper.generateException( validator.validate( model, group ) );
		} catch (BusException e) {
			throw new BusException("Cadastro não validado.\n" + e);
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com lista de preço.
	 *  
	 * REGRA: ITM-D2
	 *  
	 * @param itmId	ID do item que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 * @throws NoSuchMessageException 
	 */
	private void validatePriceListRelationship(Integer itmId) throws BusException, NoSuchMessageException, AppException {
		try {
			if( itmId != null ) {
				boolean existsItem = this.dao.hasPriceItemRelationship( itmId );
				
				if( existsItem ) {
					throw new BusException( "Não é possível excluir o item pois existe um relacionamento com lista de preço." );
					
				} else {
					boolean existsItemModel = this.dao.hasPriceItemModelRelationship( itmId );
					
					if( existsItemModel ) {
						throw new BusException( "Não é possível excluir o item pois existe um relacionamento com lista de preço." );
					}
				}
				
			} else {
				throw new BusException( "ID do item inválido para checar o relacionamento com lista de preço." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre item e lista de preço.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Executa o upload de imagens do cadastro de Item
	 * @throws BusException 
	 */
	public Boolean store(MultipartFile file, Integer id, String type, UserProfileDTO userProfile)  throws AppException, BusException {
		try {
			if (file.isEmpty()) {
				throw new BusException("O arquivo não pode ser vazio!");
			}
			
			if(!Arrays.asList("image/jpeg", "image/png", "image/x-png").contains(file.getContentType())) {
				throw new BusException("O arquivo deve ser uma imagem!");
			}

			Path rootLocation = Paths.get(this.locationItem);
			String name = (id + "_" + type + "." + FilenameUtils.getExtension(file.getOriginalFilename()));

			Path destinationFile = rootLocation.resolve(name);
			file.transferTo(new File(destinationFile.toString()));
			
			this.updateFile(id, type, name);
			
		} catch (BusException b) {
			throw b;
		} catch (IOException e) {
			log.error("Failed to store file.", e);
			return Boolean.FALSE;
		} catch (AppException e) {
			log.error("Failed to store file in database.", e);
		}
		
		return Boolean.TRUE;
	}

	@Override
	public String getItemImage(String name) throws AppException, IOException {
		String path = this.locationItem + "/" + name;
		if(new File(path).exists()) {
			BufferedImage bufferedImage = ImageIO.read(new File(path));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if (name.contains("jpg")) {
				ImageIO.write(bufferedImage, "jpg", baos);
			} else {
				ImageIO.write(bufferedImage, "png", baos);
			}
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		}
		return null;
	}
	
	@Override 
	public byte[] getImageIcon(Integer id) throws AppException, BusException {
		try {
			Optional<Item> item = this.getById(id);
			if(item.isPresent()) {
				return FileUtils.getFileBytes(this.locationItem + "/" + item.get().getIcon());
			} else {
				throw new BusException("Não foi encontrado um ícone para o item indicado");
			}
		} catch (AppException e) {
			log.error("Failed to store file in database.", e);
		}
		return null;
	}
}
