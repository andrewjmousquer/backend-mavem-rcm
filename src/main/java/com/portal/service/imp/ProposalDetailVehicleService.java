package com.portal.service.imp;

import java.util.List;
import java.util.Objects;
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
import com.portal.dao.IProposalDetailVehicleDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Model;
import com.portal.model.Person;
import com.portal.model.PriceProduct;
import com.portal.model.Product;
import com.portal.model.ProposalDetail;
import com.portal.model.ProposalDetailVehicle;
import com.portal.model.VehicleModel;
import com.portal.service.IAuditService;
import com.portal.service.IModelService;
import com.portal.service.IPriceProductService;
import com.portal.service.IProductService;
import com.portal.service.IProposalDetailService;
import com.portal.service.IProposalDetailVehicleService;
import com.portal.service.IVehicleService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalDetailVehicleService implements IProposalDetailVehicleService {

    @Autowired
    private Validator validator;

    @Autowired
    private IProposalDetailVehicleDAO dao;

    @Autowired
    public MessageSource messageSource;

    @Autowired
    private IAuditService auditService;

	@Autowired
	private IModelService modelService;
    
    @Autowired
    private IProposalDetailService proposalDetailService;

    @Autowired
    private IPriceProductService productPriceService;

    @Autowired
    private IVehicleService vehicleService;

    @Autowired
    private IProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "pdv_id");


    /**
     * Lista todos o detalhe do veículo da proposta.
     *
     * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pdv_id");
     */
    @Override
    public List<ProposalDetailVehicle> listAll(Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.listAll(pageable);

        } catch (Exception e) {
            log.error("Erro no processo de listar o detalhe do veículo da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{ProposalDetailVehicle.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Método auxiliar que decide com base no ID se a entidade deve ser salva ou
     * atualizada.
     * Se não tiver ID é save, caso contrário é update.
     *
     * @param model       objeto que deve ser salvo.
     * @param userProfile dados do usuário logado.
     */
    @Override
    public Optional<ProposalDetailVehicle> saveOrUpdate(ProposalDetailVehicle model, UserProfileDTO userProfile) throws AppException, BusException {
        if (model != null && model.getId() != null && model.getId() != 0) {
            return this.update(model, userProfile);
        } else {
            return this.save(model, userProfile);
        }
    }

    /**
     * Salva um novo objeto.
     *
     * @param model       objeto que deve ser salvo.
     * @param userProfile dados do usuário logado.
     */
    @Override
    public Optional<ProposalDetailVehicle> save(ProposalDetailVehicle model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
        	
            this.validateEntity(model, OnSave.class);
            this.validateHasDuplicate(model);
            this.validateProposalDetail(model);
            this.validateProductPrice(model);

            if(model.getFutureDelivery() != null && model.getFutureDelivery().equals(true)) {
            	model.setVehicle(null);
            } else {
            	this.validateVehicle(model);
            	this.vehicleService.saveOrUpdate(model.getVehicle(), userProfile);
            }

            Optional<ProposalDetailVehicle> saved = this.dao.save(model);

            //this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PROPOSAL_DETAIL_VEHICLE_INSERTED, userProfile);

            return saved;

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de cadastro do detalhe do veículo da proposta: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.save", new Object[]{ProposalDetailVehicle.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Atualiza o objeto
     *
     * @param model       objeto  que deve ser salvo.
     * @param userProfile dados do usuário logado.
     */
    @Override
    public Optional<ProposalDetailVehicle> update(ProposalDetailVehicle model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, OnUpdate.class);
            this.validateProposalDetail(model);
            this.validateProductPrice(model);

            if(model.getFutureDelivery() != null && model.getFutureDelivery().equals(true)) {
            	model.setVehicle(null);
            } else {
            	this.validateVehicle(model);
                if (model.getVehicle() != null) {
            	    this.vehicleService.saveOrUpdate(model.getVehicle(), userProfile);
                }
            }
            
            Optional<ProposalDetailVehicle> saved = this.dao.update(model);

            //this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PROPOSAL_DETAIL_VEHICLE_UPDATED, userProfile);

            return saved;

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de atualização do detalhe do veículo da proposta: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.update", new Object[]{ProposalDetailVehicle.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Busca o detalhe do veículo da proposta que respeitem os dados do objeto.
     * Aqui os campos String são buscados com o '='
     *
     * @param model    objeto para ser buscado
     * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pdv_id");
     */
    @Override
    public List<ProposalDetailVehicle> find(ProposalDetailVehicle model, Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.find(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de buscar o detalhe do veículo da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{ProposalDetailVehicle.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Busca o detalhe do veículo da proposta que respeitem os dados do objeto.
     * Aqui os campos String são buscados com o 'LIKE'
     *
     * @param model    objeto canais para ser buscado
     * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pdv_id");
     */
    @Override
    public List<ProposalDetailVehicle> search(ProposalDetailVehicle model, Pageable pageable) throws AppException, BusException {
        return this.find(model, pageable);
    }

    /**
     * Busca leads que respeitem os dados do objeto.
     * Aqui os campos String são buscados com o '='
     * <p>
     * Esse é método é uma sobrecarga de {@link #search(ProposalDetailVehicle, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pdv_id")
     *
     * @param model objeto  para ser buscado
     */
    @Override
    public Optional<ProposalDetailVehicle> find(ProposalDetailVehicle model) throws AppException, BusException {
        List<ProposalDetailVehicle> models = this.find(model, null);
        return Optional.ofNullable((models != null ? models.get(0) : null));
    }

    /**
     * Busca leads que respeitem os dados do objetProposalServiceo.
     * Aqui os campos String são buscados com o 'LIKE'
     * <p>
     * Esse é método é uma sobrecarga de {@link #search(ProposalDetailVehicle, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pdv_id")
     *
     * @param model objeto para ser buscado
     */
    @Override
    public List<ProposalDetailVehicle> search(ProposalDetailVehicle model) throws AppException, BusException {
        return this.search(model, null);
    }

    /**
     * Busca uma fonte pelo seu ID
     *
     * @param id ID do detalhe do veículo da proposta
     */
    @Override
    public Optional<ProposalDetailVehicle> getById(Integer id) throws AppException, BusException {
        try {

            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            return this.dao.getById(id);

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar o detalhe do veículo da proposta pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{ProposalDetailVehicle.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Lista todos o detalhe do veículo da proposta.
     * <p>
     * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
     *
     * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pdv_id");
     */
    @Override
    public List<ProposalDetailVehicle> list() throws AppException, BusException {
        return this.listAll(null);
    }

    /**
     * Efetua a exclusão do objeto
     *
     * @param id          ID do detalhe do veículo da proposta
     * @param userProfile dados do usuário logado.
     */
    @Override
    public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de exclusão inválido.");
            }

            Optional<ProposalDetailVehicle> entityDB = this.getById(id);
            if (!entityDB.isPresent()) {
                throw new BusException("O detalhe da proposta a ser excluída não existe.");
            }

            // REGRA: PDV-D1
            this.validateVehicleItemRelationship(id);

            this.dao.delete(id);

            this.audit((entityDB.isPresent() ? entityDB.get() : null), AuditOperationType.PROPOSAL_DETAIL_VEHICLE_DELETED, userProfile);

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclusão do detalhe do veículo da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.delete", new Object[]{ProposalDetailVehicle.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void audit(ProposalDetailVehicle model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.auditService.save(objectMapper.writeValueAsString(model), operationType, userProfile);
        } catch (JsonProcessingException e) {
            throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
        }
    }


    /**
     * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
     * <p>
     * Regra: PDV-I1,PDV-I2,PDV-I4
     * PDV-U1,PDV-U2,PDV-U4
     *
     * @param model entidade a ser validada
     * @param group grupo de validação que será usado
     * @throws AppException
     * @throws BusException
     */
    private void validateEntity(ProposalDetailVehicle model, Class<?> group) throws AppException, BusException {
        ValidationHelper.generateException(validator.validate(model, group));
    }

    /**
     * Valida se existe entidade duplicada.
     * <p>
     * Regra: PDV-I5, PDV-U5
     *
     * @param model entidade a ser valiadada
     * @throws AppException
     * @throws BusException
     */
    private void validateHasDuplicate(ProposalDetailVehicle model) throws AppException, BusException {

        if (model == null) {
            throw new BusException("Não é possível executar a validação de duplicado pois o detalhe do veículo da proposta está nula ou inválida.");
        }

        ProposalDetailVehicle rnSearch = ProposalDetailVehicle.builder()
                .proposalDetail(model.getProposalDetail())
                .vehicle(model.getVehicle())
                .build();

        List<ProposalDetailVehicle> listBD = this.find(rnSearch, DEFAULT_PAGINATION);

        // Save Action
        if ((model.getId() == null || model.getId().equals(0)) &&
                listBD != null && !listBD.isEmpty() &&
                listBD.stream().anyMatch(p -> !Objects.equals(p.getId(), model.getId()))) {
            throw new BusException("Já existe um veículo para essa proposta.");
        }

        // Update Action
        if (model.getId() != null && !model.getId().equals(0) &&
                listBD != null && !listBD.isEmpty()) {

            long count = listBD.stream()
                    .filter(item -> !item.getId().equals(model.getId()))
                    .count();

            if (count > 0) {
                throw new BusException("Já existe um veículo para essa proposta.");
            }
        }
    }

    /**
     * Valida se o preço do produto relacionado existe
     * <p>
     * REGRA: PDV-I7, PDV-U7
     *
     * @throws BusException
     * @throws AppException
     */
    private void validateProductPrice(ProposalDetailVehicle model) throws BusException, AppException {
        try {
            if (model == null) {
                throw new AppException("Não é possível executar a validação do produto pois a entidade está nula ou inválida.");
            }

            if (model.getPriceProduct() == null && model.getPriceProduct().getId() == null) {
                throw new AppException("Não é possível executar a validação do produto pois a entidade está nula ou inválida.");
            }

            Optional<PriceProduct> seller = this.productPriceService.getById(model.getPriceProduct().getId());

            if (!seller.isPresent()) {
                throw new BusException("Não é possível salvar o detalhe do veículo da proposta com o produto não existente.");
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao executar a validação do produto relacionado o detalhe do veículo da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Valida se o veículo relacionado existe
     * <p>
     * REGRA: PDV-I8, PDV-U16
     *
     * @throws BusException
     * @throws AppException
     */
    private void validateVehicle(ProposalDetailVehicle model) throws BusException, AppException {
        try {
            if (model == null) {
                throw new AppException("Não é possível executar a validação do veículo pois a entidade está nula ou inválida.");
            }

            if (model.getVehicle() != null && model.getVehicle().getId() != null) {
                Optional<VehicleModel> vehicle = this.vehicleService.getById(model.getVehicle().getId());

                if (!vehicle.isPresent()) {
                    throw new BusException("Não é possível salvar o detalhe da proposta com o veículo não existente.");
                }
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao executar a validação da proposta relacionado ao veiculo.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", new Object[]{ProposalDetail.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Valida se o detalhe da proposta relacionada existe
     * <p>
     * REGRA: PDV-I7, PDV-U7
     *
     * @throws BusException
     * @throws AppException
     */
    private void validateProposalDetail(ProposalDetailVehicle model) throws BusException, AppException {
        try {
            if (model == null) {
                throw new AppException("Não é possível executar a validação do detalhe do veículo pois a entidade está nula ou inválida.");
            }

            if (model.getProposalDetail() == null || model.getProposalDetail().getId() == null) {
                throw new AppException("Não é possível executar a validação do detalhe da proposta pois a entidade está nula ou inválida.");
            }

            Optional<ProposalDetail> proposal = this.proposalDetailService.getById(model.getProposalDetail().getId());

            if (!proposal.isPresent()) {
                throw new BusException("Não é possível salvar o detalhe do veículo com o detalhe da proposta não existente.");
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao executar a validação do detalhe da proposta relacionado ao veículo.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Valida se existe algum relacionamento com itens do veículo.
     * <p>
     * REGRA: PDV-D1
     *
     * @param pdvId ID da fonte que deve ser verificada
     * @throws AppException           Em caso de erro sistêmico
     * @throws BusException           Em caso de erro relacionado a regra de negócio
     * @throws NoSuchMessageException
     */
    private void validateVehicleItemRelationship(Integer pdvId) throws BusException, NoSuchMessageException, AppException {
        try {
            if (pdvId != null) {
                boolean exists = this.dao.hasVehicleItemRelationship(pdvId);
                if (exists) {
                    throw new BusException("Não é possível excluir o detalhe do veículo pois existe um relacionamento com itens.");
                }

            } else {
                throw new BusException("ID do detalhe do veículo inválido para checar o relacionamento com itens.");
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao carregar o relacionamento entre detalhe do veículo e seus itens.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public ProposalDetailVehicle getDetailVehicleByDetail(ProposalDetailVehicle model) throws AppException, BusException {

        try {

            Optional<ProposalDetailVehicle> proposalDetailVehicle = this.dao.getDetailVehicleByDetail(model);

            if (proposalDetailVehicle.isPresent()) {

                if (proposalDetailVehicle.get().getVehicle() != null) {
                    proposalDetailVehicle.get().setVehicle(this.vehicleService.getById(proposalDetailVehicle.get().getVehicle().getId()).get());
                }
                proposalDetailVehicle.get().setPriceProduct(this.productPriceService.getById(proposalDetailVehicle.get().getPriceProduct().getId()).get());

                Optional<Product> product = this.productService.getProductByProductModel(proposalDetailVehicle.get().getPriceProduct().getProductModel().getId());
                if (product.isPresent()) {
                    proposalDetailVehicle.get().getPriceProduct().getProductModel().setProduct(product.get());
                }
                
                Optional<Model> vehicleModel = this.modelService.getById(proposalDetailVehicle.get().getModel().getId());
                if(vehicleModel.isPresent()) {
                	proposalDetailVehicle.get().setModel(vehicleModel.get());
                }
            }

            return proposalDetailVehicle.get();

        } catch (Exception e) {
            log.error("Erro no processo de buscar o detalhe do veículo da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{ProposalDetailVehicle.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }


}