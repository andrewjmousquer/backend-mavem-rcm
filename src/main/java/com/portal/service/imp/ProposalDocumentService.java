package com.portal.service.imp;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.portal.dao.IProposalDocumentDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Classifier;
import com.portal.model.Document;
import com.portal.model.Proposal;
import com.portal.model.UserModel;
import com.portal.service.IDocumentService;
import com.portal.service.IProposalDocumentService;
import com.portal.service.IProposalService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalDocumentService implements IProposalDocumentService {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private IProposalService proposalService;

	@Autowired
	private IProposalDocumentDAO dao;

	@Autowired
	private IDocumentService documentService;


	@Value("${store.location.document}")
	private String locationDocument;


	@Override
	public void save(Integer proposalId, Integer documentId) throws AppException, BusException {
		try {

			if (proposalId == null || proposalId.equals(0) || documentId == null || documentId.equals(0)) {
				throw new BusException("Não é possível salvar o relacionamento entre proposta e documento com o ID de proposta e/ou documento está inválido.");
			}

			Optional<Document> personDB = this.documentService.getById(documentId);
			if (!personDB.isPresent()) {
				throw new BusException("Não é possível salvar o relacionamento entre proposta e documento com o document inexistente.");
			}

			Optional<Proposal> proposalDB = this.proposalService.getById(proposalId);
			if (!proposalDB.isPresent()) {
				throw new BusException("Não é possível salvar o relacionamento entre proposta e documento com a proposta inexistente.");
			}

			this.dao.save(proposalId, documentId);

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo salvar o relacionamento entre proposta e documento.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { ProposalDocumentService.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}		
	}

	@Override
	public void delete(Integer proposalId, Integer documentId) throws AppException, BusException {
		if( proposalId == null || proposalId.equals( 0 ) || documentId == null || documentId.equals( 0 ) ) {
			throw new BusException( "Não é possível excluir o relacionamento entre proposta e documento com o ID de proposta e/ou documento está inválido." );
		}
		
		this.dao.delete(proposalId, documentId);
	}

	@Override
	public void deleteByProposal(Integer proposalId) throws AppException, BusException {
		if (proposalId == null || proposalId.equals(0)) {
			throw new BusException("Não é possível excluir o relacionamento entre proposta e documento com o ID de proposta está inválido.");
		}

		this.dao.deleteByProposal(proposalId);
	}

	@Override
	public Document store(MultipartFile multipartFile, Integer id,  UserProfileDTO userProfile) {
		Path rootLocation = Paths.get(this.locationDocument);

		Document document = new Document();

		if (multipartFile.isEmpty()) {
			log.error("Falha, não há arquivo!.");
			return null;
		}

		try {
			String name = ("document_" + new Date().getTime() + "." + FilenameUtils.getExtension(multipartFile.getOriginalFilename()));

			Path destinationFile = rootLocation.resolve(name);
			if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
				log.error("Não foi possível amarzenar no diretório.");
			}

			try (InputStream inputStream = multipartFile.getInputStream()) {

				// Block no image file
				if (Arrays.asList("image/jpeg", "image/png", "image/x-png").contains(multipartFile.getContentType())) {
					ImageIO.read(inputStream).toString();
				} else {
					File uploadedFile = new File(rootLocation + "\\" + multipartFile.getOriginalFilename());
					FileOutputStream fos = new FileOutputStream(uploadedFile);
					fos.write(multipartFile.getBytes());
				}
				Files.copy(multipartFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
			}

			UserModel user = userProfile.getUser();
			fillDocument(document, name, user, id, multipartFile);
			this.documentService.save(document, userProfile);
		} catch (AppException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BusException e) {
			e.printStackTrace();
		}
		return document;
	}

	@Override
	public String findDocument(Integer id) throws AppException, BusException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String document = this.dao.getDocumentUrl(id);
		String URI = this.locationDocument + "/" + document;
		File file = new File(URI);
		if (Arrays.asList("image/jpeg", "image/png", "image/x-png").contains(document)) {
			BufferedImage bufferedImage = ImageIO.read(file);
			if (document.contains("jpg")) {
				ImageIO.write(bufferedImage, "jpg", baos);
			} else {
				ImageIO.write(bufferedImage, "png", baos);
			}
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		}else{

			byte[] inFileBytes = Files.readAllBytes(Paths.get(URI));
			return Base64.getEncoder().encodeToString(inFileBytes);
		}
	}

	private void fillDocument(Document document, String name, UserModel user, Integer id, MultipartFile multipartFile) {
		document.setFileName(multipartFile.getOriginalFilename());
		document.setFilePath(name);
		document.setCreateDate(LocalDateTime.now());
		document.setUser(user);
		document.setContentType(multipartFile.getContentType());
		document.setType(new Classifier(id));
	}

	@Override
	public Optional<Document> getDocument(Integer proposalId, Integer documentId) throws AppException, BusException {
		if (documentId == null || documentId.equals(0) || proposalId == null || proposalId.equals(0)) {
			throw new BusException("Não é possível buscar o relacionamento entre proposta e documento com o ID da proposta e/ou da documento inválido.");
		}

		return this.dao.getDocument(proposalId, documentId);
	}

	@Override
	public Optional<Proposal> getProposal(Integer proposalId, Integer documentId) throws AppException, BusException {
		if( documentId == null || documentId.equals( 0 ) || proposalId == null || proposalId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre proposta e documento com o ID da proposta e/ou da documento inválido." );
		}
		
		return this.dao.getProposal( proposalId, documentId);
	}

	@Override
	public List<Document> findByProposal(Integer proposalId) throws AppException, BusException {
		if( proposalId == null || proposalId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre proposta e documento com o ID da proposta inválida." );
		}
		
		return this.dao.findByProposal(proposalId);
	}
	
	@Override
	public List<Proposal> findByDocument(Integer documentId) throws AppException, BusException {
		if( documentId == null || documentId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre proposta e documento com o ID da documento inválida." );
		}
		
		return this.dao.findByDocument(documentId);
	}
}
