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
     * Lista todos os ve??culos.
     *
     * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "vhe_id");
     */
    @Override
    public List<VehicleModel> listAll(Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.listAll(pageable);

        } catch (Exception e) {
            log.error("Erro no processo de listar os ve??culos.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * M??todo auxiliar que decide com base no ID se a entidade deve ser salva ou
     * atualizada.
     * Se n??o tiver ID ?? save, caso contr??rio ?? update.
     *
     * @param model       objeto que deve ser salvo.
     * @param userProfile dados do usu??rio logado.
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
     * @param userProfile  dados do usu??rio logado.
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
            log.error("Erro no processo de cadastro de ve??culo: {}", vehicleModel, e);
            throw new AppException(this.messageSource.getMessage("error.generic.save", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Atualiza um lead
     *
     * @param model       objeto lead que deve ser salvo.
     * @param userProfile dados do usu??rio logado.
     */
    @Override
    public Optional<VehicleModel> update(VehicleModel model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, OnUpdate.class);
            this.validateHasDuplicate(model);
            this.validateModel(model);

            Optional<VehicleModel> modelDB = this.getById(model.getId());
            if (!modelDB.isPresent()) {
                throw new BusException("O ve??culo a ser atualizado n??o existe.");
            }

            Optional<VehicleModel> saved = this.dao.update(model);

            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.VEHICLE_UPDATED, userProfile);

            return saved;

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de atualiza????o de ve??culo: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.update", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Busca os ve??culos que respeitem os dados do objeto.
     * Aqui os campos String s??o buscados com o '='
     *
     * @param model    objeto leads para ser buscado
     * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "vhe_id");
     */
    @Override
    public List<VehicleModel> find(VehicleModel model, Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.find(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de buscar os ve??culos.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Busca os ve??culos que respeitem os dados do objeto.
     * Aqui os campos String s??o buscados com o 'LIKE'
     *
     * @param model    objeto canais para ser buscado
     * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "vhe_id");
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
     * @param chassi em string do ve??culo
     */
    @Override
    public Optional<VehicleModel> getByChassi(String chassi) throws AppException {
        return this.dao.getByChassi(chassi);
    }

    /**
     * Busca leads que respeitem os dados do objeto.
     * Aqui os campos String s??o buscados com o '='
     * <p>
     * Esse ?? m??todo ?? uma sobrecarga de {@link #search(VehicleModel, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "vhe_id")
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
     * Aqui os campos String s??o buscados com o 'LIKE'
     * <p>
     * Esse ?? m??todo ?? uma sobrecarga de {@link #search(VehicleModel, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "vhe_id")
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
     * @param id ID de ve??culo
     */
    @Override
    public Optional<VehicleModel> getById(Integer id) throws AppException, BusException {
        try {

            if (id == null) {
                throw new BusException("ID de busca inv??lido.");
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
            log.error("Erro ao consultar uma ve??culo pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{VehicleModel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Lista todos os ve??culos.
     * <p>
     * Esse m??todo ?? uma sobrecarga de {@link #listAll(Pageable)}
     */
    @Override
    public List<VehicleModel> list() throws AppException, BusException {
        return this.listAll(null);
    }

    /**
     * Efetua a exclus??o de um lead
     *
     * @param id          ID de ve??culo
     * @param userProfile dados do usu??rio logado.
     */
    @Override
    public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de exclus??o inv??lido.");
            }

            Optional<VehicleModel> entityDB = this.getById(id);
            if (!entityDB.isPresent()) {
                throw new BusException("A ve??culo a ser exclu??da n??o existe.");
            }

            // REGRA: VHE-D1
            this.validateVehicleItemRelationship(id);

            this.audit((entityDB.isPresent() ? entityDB.get() : null), AuditOperationType.VEHICLE_DELETED, userProfile);

            this.dao.delete(id);

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclus??o de ve??culo.", e);
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
     * Valida a entidade como um todo, passando por regras de formata????o e obrigatoriedade
     * <p>
     * Regra: VHE-I1, VHE-I2
     * VHE-U1, VHE-U2
     *
     * @param model entidade a ser validada
     * @param group grupo de valida????o que ser?? usado
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
            throw new BusException("N??o ?? poss??vel chegar a duplicidade com o objeto da entidade nula.");
        }

        List<VehicleModel> searchPlate = this.find(VehicleModel.builder().plate(model.getPlate()).build(), null);

        //valida????o de duplicidade para save
        if ((model.getId() == null || model.getId().equals(0)) && searchPlate != null && !searchPlate.isEmpty()) {
            if (model.getPlate() != null) throw new BusException("J?? existe um ve??culo com a mesma placa.");
        }

        //valida????o de duplicidade para update
        if (model.getId() != null && !model.getId().equals(0) && searchPlate != null && !searchPlate.isEmpty()) {
            if (model.getPlate() != null) {
                long count = searchPlate.stream()
                        .filter(item -> !item.getId().equals(model.getId()) && item.getPlate() != null && item.getPlate().equals(model.getPlate()))
                        .count();
                if (count > 0) throw new BusException("J?? existe um ve??culo com a mesma placa.");
            }
        }

        List<VehicleModel> searchChassi = this.find(VehicleModel.builder().chassi(model.getChassi()).build(), null);
        //valida????o de duplicidade para save
        if ((model.getId() == null || model.getId().equals(0)) && searchChassi != null && !searchChassi.isEmpty()) {
            throw new BusException("J?? existe um ve??culo com o mesmo chassi.");
        }
        
        //valida????o de duplicidade para update
        if (model.getId() != null && !model.getId().equals(0) && searchChassi != null && !searchChassi.isEmpty()) {
            long countChassi = searchChassi.stream()
                    .filter(item -> !item.getId().equals(model.getId()) && item.getChassi().equals(model.getChassi()))
                    .count();
            if (countChassi > 0) {
                throw new BusException("J?? existe um ve??culo com o mesmo chassi.");
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
                throw new AppException("N??o ?? poss??vel executar a valida????o do modelo pois a entidade ve??culo est?? nula ou inv??lida.");
            }

            if (model.getModel() == null && model.getModel().getId() == null) {
                throw new AppException("N??o ?? poss??vel executar a valida????o do modelo pois a entidade modelo est?? nula ou inv??lida.");
            }

            Optional<Model> modelDB = modelService.getById(model.getModel().getId());

            if (!modelDB.isPresent()) {
                throw new BusException("N??o ?? poss??vel salvar o ve??culo da proposta com o modelo n??o existente.");
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao executar a valida????o do ve??culo relacionado o detalhe da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Valida se existe algum relacionamento com algum detalhe de proposta.
     * <p>
     * REGRA: VHE-D1
     *
     * @param vheId ID do ve??culo que deve ser verificada
     * @throws AppException           Em caso de erro sist??mico
     * @throws BusException           Em caso de erro relacionado a regra de neg??cio
     * @throws NoSuchMessageException
     */
    private void validateVehicleItemRelationship(Integer vheId) throws
            BusException, NoSuchMessageException, AppException {
        try {
            if (vheId != null) {
                boolean exists = this.dao.hasProposalDetailRelationship(vheId);
                if (exists) {
                    throw new BusException("N??o ?? poss??vel excluir o ve??culo pois existe um relacionamento com detalhe da proposta.");
                }

            } else {
                throw new BusException("ID do ve??culo inv??lido para checar o relacionamento com detalhe da proposta.");
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao carregar o relacionamento entre ve??culo e o detalhe da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }
}
