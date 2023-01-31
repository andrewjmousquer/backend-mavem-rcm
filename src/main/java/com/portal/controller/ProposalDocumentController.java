package com.portal.controller;

import com.portal.dto.ImageDTO;
import com.portal.dto.proposal.ProposalDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Document;
import com.portal.model.Proposal;
import com.portal.service.IDocumentService;
import com.portal.service.IProposalDocumentService;
import com.portal.service.imp.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/protected/proposaldocument")
@Tag(name = "Proposal Document Controller", description = "CRUD for Proposal Document Entity")
public class ProposalDocumentController extends BaseController {

    @Autowired
    private ProposalService proposalService;

    @Autowired
    private IProposalDocumentService service;

    @Autowired
    private IDocumentService documentService;

    @Operation(summary = "Save List Documents")
    @ApiResponse(responseCode = "200", description = "Successfully return list of documents", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/upload/{id}")
    public ResponseEntity<Document> saveList(@RequestBody MultipartFile file, @PathVariable(name = "id", required = true) @Parameter(description = "ID of Type Document") Integer id) {
        if (file == null) {
            return null;
        }
        Document document = storeFile(id, file);

        if (document != null)
            return ResponseEntity.status(HttpStatus.OK).body(document);
        else
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Operation(summary = "Save List Documents")
    @ApiResponse(responseCode = "200", description = "Successfully return list of documents", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will returns CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/{proposalId}/upload")
    public ResponseEntity<List<Document>>
    uploadDocumentList(@PathVariable @Parameter(description = "ID of the proposal") Integer proposalId,
                       @RequestParam Integer[] fId,
                       @RequestBody final MultipartFile... file) {
        try {
            if (file != null) {
                final ProposalDTO foundProposal = proposalService.getProposal(proposalId);
                final Proposal proposal = foundProposal.getProposal();

                final int[] idIndex = {0};
                proposal.getDocuments()
                        .addAll(Arrays.stream(file)
                                .map(_file -> storeFile(fId[idIndex[0]++], _file))
                                .collect(Collectors.toList()));
                Optional<Proposal> updated = proposalService.update(proposal, this.getUserProfile());
                if (updated.isPresent()) {
                    return ResponseEntity.status(HttpStatus.CREATED).body(updated.get().getDocuments());

                }
            }
        } catch (AppException | BusException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private Document storeFile(final Integer id, final MultipartFile file) {
        return this.service.store(file, id, this.getUserProfile());
    }


    @Operation(summary = "Get document file")
    @ApiResponse(responseCode = "200", description = "Successfully get document", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(value = "/document/{id}")
    public @ResponseBody ResponseEntity<ImageDTO> getDocument(@PathVariable(name = "id", required = true) @Parameter(description = "Document ID to be searched") Integer id) throws IOException, AppException, BusException {
        ImageDTO image = new ImageDTO(this.service.findDocument(id));

        if (image != null) {
            return ResponseEntity.ok(image);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Operation(summary = "Delete document by ID")
    @ApiResponse(responseCode = "204", description = "Successfully deleted the document")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @DeleteMapping(path = "/document/{idProposal}/{idDocument}")
    public ResponseEntity<Void> delete(@PathVariable(name = "idProposal", required = true) @Parameter(description = "Document ID to be deleted") int idProposal, @PathVariable(name = "idDocument", required = true) @Parameter(description = "Document ID to be deleted") int idDocument) throws Exception {
        if (idProposal != 0)
            this.service.delete(idProposal, idDocument);

        this.documentService.delete(idDocument, this.getUserProfile());

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
