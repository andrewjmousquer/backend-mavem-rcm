package com.portal.service.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IVehicleDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.dto.VehicleDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.*;
import com.portal.service.IAuditService;
import com.portal.service.IModelService;
import com.portal.service.IProposalService;
import com.portal.service.IVehicleService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;
import lombok.extern.slf4j.Slf4j;
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

import javax.validation.Validator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class VehicleService implements IVehicleService {

    @Autowired
    private Validator validator;

    @Autowired
    private IVehicleDAO dao;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IProposalService proposalService;

    @Autowired
    public MessageSource messageSource;

    @Autowired
    private IAuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "vhe_id");


    /**
     * Lista todos os veículos.
     *
     * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "vhe_id");
     */
    @Override
    public List<VehicleModel> listAll(Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.listAll(pageable);

        } catch (Exception e) {
            log.error("Erro no processo de listar os veículos.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
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
    public Optional<VehicleModel> saveOrUpdate(VehicleModel model, UserProfileDTO userProfile) throws AppException, BusException {
        if (model != null && model.getId() != null && model.getId() != 0) {
            return this.update(model, userProfile);
        } else {
            return this.save(model, userProfile);
        }
    }

    /**
     * Salva um novo objeto.
     *
     * @param vehicleModel objeto que deve ser salvo.
     * @param userProfile  dados do usuário logado.
     */
    @Override
    public Optional<VehicleModel> save(VehicleModel vehicleModel, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(vehicleModel, OnSave.class);
            this.validateHasDuplicate(vehicleModel);
            this.validateModel(vehicleModel);

            Optional<VehicleModel> saved = this.dao.save(vehicleModel);

            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.VEHICLE_INSERTED, userProfile);

            return saved;

        } catch (BusException e) {
                throw e;
        } catch (Exception e) {
            log.error("Erro no processo de cadastro de veículo: {}", vehicleModel, e);
            throw new AppException(this.messageSource.getMessage("error.generic.save", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Atualiza um lead
     *
     * @param model       objeto lead que deve ser salvo.
     * @param userProfile dados do usuário logado.
     */
    @Override
    public Optional<VehicleModel> update(VehicleModel model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, OnUpdate.class);
            this.validateHasDuplicate(model);
            this.validateModel(model);

            Optional<VehicleModel> modelDB = this.getById(model.getId());
            if (!modelDB.isPresent()) {
                throw new BusException("O veículo a ser atualizado não existe.");
            }

            Optional<VehicleModel> saved = this.dao.update(model);

            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.VEHICLE_UPDATED, userProfile);

            return saved;

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de atualização de veículo: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.update", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Busca os veículos que respeitem os dados do objeto.
     * Aqui os campos String são buscados com o '='
     *
     * @param model    objeto leads para ser buscado
     * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "vhe_id");
     */
    @Override
    public List<VehicleModel> find(VehicleModel model, Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.find(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de buscar os veículos.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Busca os veículos que respeitem os dados do objeto.
     * Aqui os campos String são buscados com o 'LIKE'
     *
     * @param model    objeto canais para ser buscado
     * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "vhe_id");
     */
    @Override
    public List<VehicleModel> search(VehicleModel model, Pageable pageable) throws AppException, BusException {

        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.search(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de procurar os veiculos.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }

    }

    public List<VehicleModel> searchForm(String searchText, Pageable pageable) throws AppException, BusException {

        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.searchForm(searchText, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de procurar os veiculos.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }

    }

    @Override
    public List<VehicleModel> search(VehicleDTO vehicleDTO, boolean like, Pageable pageable) throws AppException, BusException {

        VehicleModel vehicleModel = VehicleModel.toEntity(vehicleDTO);
        if (like) {
            return this.search(vehicleModel, pageable);
        } else {
            return this.find(vehicleModel, pageable);
        }
    }

    @Override
    public List<VehicleModel> getByBrand(String brand, Pageable pageReq) throws AppException {
        List<VehicleModel> vehicleModelList = this.dao.getByBrand(brand, pageReq);
        return vehicleModelList;
    }

    /**
     * Busca o veiculo pelo chassi
     *
     * @param chassi em string do veículo
     */
    @Override
    public Optional<VehicleModel> getByChassi(String chassi) throws AppException {
        return this.dao.getByChassi(chassi);
    }

    /**
     * Busca leads que respeitem os dados do objeto.
     * Aqui os campos String são buscados com o '='
     * <p>
     * Esse é método é uma sobrecarga de {@link #search(VehicleModel, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "vhe_id")
     *
     * @param model objeto lead para ser buscado
     */
    @Override
    public Optional<VehicleModel> find(VehicleModel model) throws AppException, BusException {
        List<VehicleModel> models = this.find(model, null);
        return Optional.ofNullable((models != null ? models.get(0) : null));
    }

    /**
     * Busca leads que respeitem os dados do objetVehicleServiceo.
     * Aqui os campos String são buscados com o 'LIKE'
     * <p>
     * Esse é método é uma sobrecarga de {@link #search(VehicleModel, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "vhe_id")
     *
     * @param model objeto lead para ser buscado
     */
    @Override
    public List<VehicleModel> search(VehicleModel model) throws AppException, BusException {
        return this.search(model, null);
    }

    /**
     * Busca uma fonte pelo seu ID
     *
     * @param id ID de veículo
     */
    @Override
    public Optional<VehicleModel> getById(Integer id) throws AppException, BusException {
        try {

            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }
            Optional<VehicleModel> vehicleModel = this.dao.getById(id);
            List<ProposalFrontForm> proposals = this.proposalService.getByVehicle(vehicleModel.get());
            if (proposals != null && proposals.size() > 0) {
                vehicleModel.get().setProposals(proposals);
            }
            return vehicleModel;

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar uma veículo pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Lista todos os veículos.
     * <p>
     * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
     */
    @Override
    public List<VehicleModel> list() throws AppException, BusException {
        return this.listAll(null);
    }

    /**
     * Efetua a exclusão de um lead
     *
     * @param id          ID de veículo
     * @param userProfile dados do usuário logado.
     */
    @Override
    public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de exclusão inválido.");
            }

            Optional<VehicleModel> entityDB = this.getById(id);
            if (!entityDB.isPresent()) {
                throw new BusException("A veículo a ser excluída não existe.");
            }

            // REGRA: VHE-D1
            this.validateVehicleItemRelationship(id);

            this.audit((entityDB.isPresent() ? entityDB.get() : null), AuditOperationType.VEHICLE_DELETED, userProfile);

            this.dao.delete(id);

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclusão de veículo.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.delete", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void audit(VehicleModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.auditService.save(objectMapper.writeValueAsString(model), operationType, userProfile);
        } catch (JsonProcessingException e) {
            throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
        }
    }


    /**
     * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
     * <p>
     * Regra: VHE-I1, VHE-I2
     * VHE-U1, VHE-U2
     *
     * @param model entidade a ser validada
     * @param group grupo de validação que será usado
     * @throws AppException
     * @throws BusException
     */
    private void validateEntity(VehicleModel model, Class<?> group) throws AppException, BusException {
        ValidationHelper.generateException(validator.validate(model, group));
    }

    /**
     * Valida se existe entidade duplicada.
     * <p>
     * Regra: VHE-I4, VHE-U4
     *
     * @param model entidade a ser valiadada
     * @throws AppException
     * @throws BusException
     */
    private void validateHasDuplicate(VehicleModel model) throws AppException, BusException {

        if (model == null) {
            throw new BusException("Não é possível chegar a duplicidade com o objeto da entidade nula.");
        }

        List<VehicleModel> searchPlate = this.find(VehicleModel.builder().plate(model.getPlate()).build(), null);

        //validação de duplicidade para save
        if ((model.getId() == null || model.getId().equals(0)) && searchPlate != null && !searchPlate.isEmpty()) {
            if (model.getPlate() != null) throw new BusException("Já existe um veículo com a mesma placa.");
        }

        //validação de duplicidade para update
        if (model.getId() != null && !model.getId().equals(0) && searchPlate != null && !searchPlate.isEmpty()) {
            if (model.getPlate() != null) {
                long count = searchPlate.stream()
                        .filter(item -> !item.getId().equals(model.getId()) && item.getPlate() != null && item.getPlate().equals(model.getPlate()))
                        .count();
                if (count > 0) throw new BusException("Já existe um veículo com a mesma placa.");
            }
        }

        List<VehicleModel> searchChassi = this.find(VehicleModel.builder().chassi(model.getChassi()).build(), null);
        //validação de duplicidade para save
        if ((model.getId() == null || model.getId().equals(0)) && searchChassi != null && !searchChassi.isEmpty()) {
            throw new BusException("Já existe um veículo com o mesmo chassi.");
        }
        
        //validação de duplicidade para update
        if (model.getId() != null && !model.getId().equals(0) && searchChassi != null && !searchChassi.isEmpty()) {
            long countChassi = searchChassi.stream()
                    .filter(item -> !item.getId().equals(model.getId()) && item.getChassi().equals(model.getChassi()))
                    .count();
            if (countChassi > 0) {
                throw new BusException("Já existe um veículo com o mesmo chassi.");
            }
        }
    }

    /**
     * Valida se o vendedor relacionado existe
     * <p>
     * REGRA: VHE-I3, VHE-U3
     *
     * @throws BusException
     * @throws AppException
     */
    private void validateModel(VehicleModel model) throws BusException, AppException {
        try {
            if (model == null) {
                throw new AppException("Não é possível executar a validação do modelo pois a entidade veículo está nula ou inválida.");
            }

            if (model.getModel() == null && model.getModel().getId() == null) {
                throw new AppException("Não é possível executar a validação do modelo pois a entidade modelo está nula ou inválida.");
            }

            Optional<Model> modelDB = modelService.getById(model.getModel().getId());

            if (!modelDB.isPresent()) {
                throw new BusException("Não é possível salvar o veículo da proposta com o modelo não existente.");
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao executar a validação do veículo relacionado o detalhe da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Valida se existe algum relacionamento com algum detalhe de proposta.
     * <p>
     * REGRA: VHE-D1
     *
     * @param vheId ID do veículo que deve ser verificada
     * @throws AppException           Em caso de erro sistêmico
     * @throws BusException           Em caso de erro relacionado a regra de negócio
     * @throws NoSuchMessageException
     */
    private void validateVehicleItemRelationship(Integer vheId) throws
            BusException, NoSuchMessageException, AppException {
        try {
            if (vheId != null) {
                boolean exists = this.dao.hasProposalDetailRelationship(vheId);
                if (exists) {
                    throw new BusException("Não é possível excluir o veículo pois existe um relacionamento com detalhe da proposta.");
                }

            } else {
                throw new BusException("ID do veículo inválido para checar o relacionamento com detalhe da proposta.");
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao carregar o relacionamento entre veículo e o detalhe da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }
}
