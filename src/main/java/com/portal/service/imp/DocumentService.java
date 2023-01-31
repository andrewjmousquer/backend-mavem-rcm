package com.portal.service.imp;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.validation.Validator;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IDocumentDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Document;
import com.portal.model.Person;
import com.portal.model.UserModel;
import com.portal.service.IAuditService;
import com.portal.service.IDocumentService;
import com.portal.service.IParameterService;
import com.portal.service.IUserService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class DocumentService implements IDocumentService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IDocumentDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private IUserService userService;

	@Autowired
	private IParameterService parameterService;

	@Autowired
	private ObjectMapper objectMapper;

	private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "doc_id");

	@Value("${store.location.document}")
	private String locationDocument;


	/**
	 * Lista todos as documentos.
	 *
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "doc_id");
	 */
	@Override
	public List<Document> listAll(Pageable pageable) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar as documentos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Document.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<Document> saveOrUpdate(Document model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<Document> save( Document model, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateUser( model.getUser() );

			Optional<Document> saved = this.dao.save( model );
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.DOCUMENT_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro de documento: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Document.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um lead
	 * 
	 * @param model objeto lead que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Document> update(Document model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnUpdate.class);
			
			Optional<Document> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O documento a ser atualizado não existe.");
			}
			
			this.validateUser( model.getUser() );
			
			Optional<Document> saved = this.dao.update(model);

			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.DOCUMENT_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de atualização de documento: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Document.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca as documentos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto leads para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "doc_id");
	 */
	@Override
	public List<Document> find( Document model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar as documentos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Document.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca as documentos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "doc_id");
	 */
	@Override
	public List<Document> search( Document model, Pageable pageable ) throws AppException, BusException {
		return this.find(model, pageable);
	}
	
	/**
	 * Busca leads que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Document, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "doc_id")
	 * 
	 * @param model objeto lead para ser buscado
	 */
	@Override
	public Optional<Document> find(Document model) throws AppException, BusException {
		List<Document> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca leads que respeitem os dados do objetDocumentServiceo.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Document, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "doc_id")
	 * 
	 * @param model objeto lead para ser buscado
	 */
	@Override
	public List<Document> search(Document model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca uma fonte pelo seu ID
	 * 
	 * @param id ID de documento
	 */
	@Override
	public Optional<Document> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar uma documento pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Document.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos as documentos.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 */
	@Override
	public List<Document> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de um lead
	 * 
	 * @param id ID de documento
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			Path rootLocation = Paths.get(this.locationDocument);
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<Document> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "A documento a ser excluída não existe.");
			}
			
			// Regra: DOC-D1
			this.validateProposalRelationship(id);

			File file = new File(rootLocation + "/" + entityDB.get().getFilePath());
			if( !Files.deleteIfExists( file.toPath() ) )  {
				throw new AppException( "Apesar da tentativa o documento não foi excluído. Verificar possível falha." );
			}
			
			//this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.DOCUMENT_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão de documento.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Document.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(Document model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: DOC-I1, DOC-I3, DOC-I4, DOC-I5, DOC-I6, DOC-I7
	 * 		  DOC-U1, DOC-U3, DOC-U4, DOC-U5, DOC-U6, DOC-U7
	 *  
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( Document model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group));
	}

	/**
	 * Valida se o arquivo já existe, em caso positivo adicionamos um número na frente.
	 *
	 * Regra: DOC-I9, DOC-U9
	 *
	 * @return
	 * @throws AppException
	 * @throws BusException
	 */
	private File processDuplicate( String directory, String filename, String extension ) throws AppException, BusException {
		try {
			File file = new File( directory + "/" + filename + "." + extension );
	
			int fileIndex = 1;
			while ( file.exists() ) {
				file = new File( directory + "/" + filename + "_" + fileIndex + "." + extension );
				fileIndex++;
			}
			
			return file; 
			
		}catch (Exception e) {
			throw new AppException( "Erro na validação das extensões do arquivo." );
		}
	}
	
	/**
	 * Trata o nome do arquivo para evitar problema com o SO
	 * 
	 * Regra: DOC-I2, DOC-U2
	 */
	private String sanitizeFileName(String fileName) {
		
		if( fileName != null ) {
			String normalizer = Normalizer.normalize(fileName, Normalizer.Form.NFD);
		    Pattern pattern = Pattern.compile("[^\\p{ASCII}]");
		    fileName = pattern.matcher(normalizer).replaceAll("");
		    return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
		}
		
		return null;
	}
	
	/**
	 * Valida a entidade usuário.
	 * 
	 * Regra: DOC-I8,DOC-U8
	 * 
	 * @param user	objeto do usuário que será válidado.
	 */
	private void validateUser( UserModel user  ) throws AppException, BusException {
		
		if( user == null || user.getId() == null ) {
			throw new BusException( "O usuário é o obrigatório para salvar." );
		}
		
		Optional<UserModel> userDB = this.userService.getById( user.getId() );
		if( !userDB.isPresent() ) {
			throw new BusException( "O usuário é inválido ou não existe." );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com a proposta.
	 *  
	 * REGRA: DOC-D1
	 *  
	 * @param docId	ID da fonte que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 * @throws NoSuchMessageException 
	 */
	private void validateProposalRelationship(Integer docId) throws BusException, NoSuchMessageException, AppException {
		try {
			if( docId != null ) {
				boolean exists = this.dao.hasProposalRelationship( docId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o documento pois existe um relacionamento com a proposta." );
				}
				
			} else {
				throw new BusException( "ID do documento está inválido para checar o relacionamento com a proposta." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre documento e proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
     * Valida se a extenssão é suportada
     * 
     * Regra: DOC-I10, DOC-U10
     */
	private void validateFileExtension( String fileName ) throws BusException, AppException {
		
		try {
			if( fileName == null || fileName.isEmpty() ) {
				throw new BusException( "O nome do arquivo não pode ser nulo nem vazio para a avaliação." );
			}
			
			String fileExtension = FilenameUtils.getExtension( fileName );
	
			List<String> extensions = this.listAllowedExtensions();
			
			if( fileExtension != null && extensions != null && !extensions.contains( fileExtension.trim().toLowerCase() ) ) {
				throw new BusException( "A extensão do arquivo não é permitida." );
			}
		} catch( BusException e ) {
			throw e;
		}catch (Exception e) {
			throw new AppException( "Erro na validação das extensões do arquivo." );
		}
	}
	
	/**
	 * Valida se o tipo de conteúdo do arquivo é permitido.
	 * 
	 * Regra: DOC-I10, DOC-U10
	 * @return 
	 */
	private String validateFileContent( InputStream fileInputStream ) throws BusException, AppException {
		try {
			List<String> supportedExtensionsList = this.listAllowedMimeTypes();

			Tika tika = new Tika();
			String mediaType = tika.detect(fileInputStream);

            if( !supportedExtensionsList.contains(mediaType.toLowerCase() ) ) {
                throw new BusException( "O tipo de conteúdo do arquivo não é permitido." );
            }

            return mediaType;
            
		} catch( BusException e ) {
			throw e;
		} catch( Exception e ) {
			throw new AppException( "Erro na validação do tipo de conteúdo do arquivo." );
		}
	}
	
	/**
	 * Valida se o diretório que o arquivo será salvo é válido
	 * 
	 * @param filePath caminho do diretório
	 * @throws BusException 
	 * @throws AppException 
	 */
	private void validateDirectory(String filePath) throws BusException, AppException {
		try {
			
			if( filePath == null || filePath.isEmpty() ) {
				throw new BusException( "O caminho do diretório não pode ser nulo nem vazio." );
			}
			
			File dir = new File( filePath );
			
			if( !dir.isDirectory() || !dir.exists() ) {
				throw new BusException( "O caminho informado não é um diretório ou ele não existe." );
			}
			
		} catch( BusException e ) {
			throw e;
		} catch( Exception e ) {
			throw new AppException( "Erro na validação do diretório destino do arquivo." );
		}
	}
	
	/**
	 * Lista todas as extensões permitidas para o upload dos documentos.
	 */
	private List<String> listAllowedExtensions() throws AppException {
		try {
			String extensions = parameterService.getValueOf( "DOCUMENT_ALLOWED_EXTENSIONS" );
			
			if( extensions != null ) {
				return Arrays.asList( extensions.toLowerCase().split(",") );
			}

		} catch( Exception e ) {
			throw new AppException( "Erro ao listar as extensões permitidas no upload dos documentos." );
		}
		
		return new ArrayList<String>();
	}
	
	/**
	 * Lista todas as extensões permitidas para o upload dos documentos.
	 */
	private List<String> listAllowedMimeTypes() throws AppException {
		try {
			String extensions = parameterService.getValueOf( "DOCUMENT_ALLOWED_MIME_TYPES" );
			
			if( extensions != null ) {
				return Arrays.asList( extensions.toLowerCase().split(",") );
			}

		} catch( Exception e ) {
			throw new AppException( "Erro ao listar as extensões permitidos no upload dos documentos." );
		}
		
		return new ArrayList<String>();
	}
	
	
}
