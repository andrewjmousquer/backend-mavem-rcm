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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IProposalPaymentDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PaymentMethod;
import com.portal.model.Person;
import com.portal.model.ProposalDetail;
import com.portal.model.ProposalPayment;
import com.portal.service.IAuditService;
import com.portal.service.IPaymentMethodService;
import com.portal.service.IPaymentRuleService;
import com.portal.service.IProposalDetailService;
import com.portal.service.IProposalPaymentService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalPaymentService implements IProposalPaymentService {

    @Autowired
    private Validator validator;

    @Autowired
    private IProposalPaymentDAO dao;

    @Autowired
    public MessageSource messageSource;

    @Autowired
    private IAuditService auditService;

    @Autowired
    private IPaymentMethodService paymentMethodService;

    @Autowired
    private IPaymentRuleService paymentRuleService;

    @Autowired
    private IProposalDetailService proposalDetailService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "ppy_id");


    /**
     * Lista todos o detalhe de pagamento da proposta.
     *
     * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppy_id");
     */
    @Override
    public List<ProposalPayment> listAll(Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.listAll(pageable);

        } catch (Exception e) {
            log.error("Erro no processo de listar o detalhe de pagamento da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{ProposalPayment.class.getSimpleName()}, LocaleContextHolder.getLocale()));
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
    public Optional<ProposalPayment> saveOrUpdate(ProposalPayment model, UserProfileDTO userProfile) throws AppException, BusException {
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
    public Optional<ProposalPayment> save(ProposalPayment model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, OnSave.class);
            this.validateProposalDetail(model);
            //this.validatePaymentMethod(model);

            Optional<ProposalPayment> saved = this.dao.save(model);

            //this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PROPOSAL_PAYMENT_INSERTED, userProfile);

            return saved;

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de cadastro do detalhe de pagamento da proposta: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.save", new Object[]{ProposalPayment.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Atualiza o objeto
     *
     * @param model       objeto lead que deve ser salvo.
     * @param userProfile dados do usuário logado.
     */
    @Override
    public Optional<ProposalPayment> update(ProposalPayment model, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(model, OnUpdate.class);
            //this.validateHasDuplicate( model );
            this.validateProposalDetail(model);
            //this.validatePaymentMethod(model);

            Optional<ProposalPayment> modelDB = this.getById(model.getId());
            if (!modelDB.isPresent()) {
                throw new BusException("O detalhe de pagamento da proposta a ser atualizado não existe.");
            }

            Optional<ProposalPayment> saved = this.dao.update(model);

            //this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PROPOSAL_PAYMENT_UPDATED, userProfile);

            return saved;

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de atualização do detalhe de pagamento da proposta: {}", model, e);
            throw new AppException(this.messageSource.getMessage("error.generic.update", new Object[]{ProposalPayment.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Busca o detalhe de pagamento da proposta que respeitem os dados do objeto.
     * Aqui os campos String são buscados com o '='
     *
     * @param model    objeto leads para ser buscado
     * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppy_id");
     */
    @Override
    public List<ProposalPayment> find(ProposalPayment model, Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            List<ProposalPayment> proposalPayments = this.dao.find(model, pageable);

            if (proposalPayments.size() > 0) {

                proposalPayments.forEach(payment -> {

                    try {
                        if (payment.getPaymentMethod() != null && payment.getPaymentMethod().getId() != null && payment.getPaymentMethod().getId() > 0) {
                            payment.setPaymentMethod(this.paymentMethodService.getById(payment.getPaymentMethod().getId()).get());
                            payment.setPaymentRule(this.paymentRuleService.getById(payment.getPaymentRule().getId()).get());
                        }
                    } catch (AppException | BusException e) {
                        e.printStackTrace();
                    }
                    ;
                });
            }

            return proposalPayments;

        } catch (Exception e) {
            log.error("Erro no processo de buscar o detalhe de pagamento da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{ProposalPayment.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Busca o detalhe de pagamento da proposta que respeitem os dados do objeto.
     * Aqui os campos String são buscados com o 'LIKE'
     *
     * @param model    objeto canais para ser buscado
     * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppy_id");
     */
    @Override
    public List<ProposalPayment> search(ProposalPayment model, Pageable pageable) throws AppException, BusException {
        return this.find(model, pageable);
    }

    /**
     * Busca leads que respeitem os dados do objeto.
     * Aqui os campos String são buscados com o '='
     * <p>
     * Esse é método é uma sobrecarga de {@link #search(ProposalPayment, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppy_id")
     *
     * @param model objeto lead para ser buscado
     */
    @Override
    public Optional<ProposalPayment> find(ProposalPayment model) throws AppException, BusException {
        List<ProposalPayment> models = this.find(model, null);
        return Optional.ofNullable((models != null ? models.get(0) : null));
    }

    /**
     * Busca leads que respeitem os dados do objetProposalServiceo.
     * Aqui os campos String são buscados com o 'LIKE'
     * <p>
     * Esse é método é uma sobrecarga de {@link #search(ProposalPayment, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppy_id")
     *
     * @param model objeto lead para ser buscado
     */
    @Override
    public List<ProposalPayment> search(ProposalPayment model) throws AppException, BusException {
        return this.search(model, null);
    }

    /**
     * Busca uma fonte pelo seu ID
     *
     * @param id ID do detalhe de pagamento da proposta
     */
    @Override
    public Optional<ProposalPayment> getById(Integer id) throws AppException, BusException {
        try {

            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            return this.dao.getById(id);

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar o detalhe de pagamento da proposta pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{ProposalPayment.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Lista todos o detalhe de pagamento da proposta.
     * <p>
     * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
     *
     * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppy_id");
     */
    @Override
    public List<ProposalPayment> list() throws AppException, BusException {
        return this.listAll(null);
    }

    /**
     * Efetua a exclusão do objeto
     *
     * @param id          ID do detalhe de pagamento da proposta
     * @param userProfile dados do usuário logado.
     */
    @Override
    public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de exclusão inválido.");
            }

            Optional<ProposalPayment> entityDB = this.getById(id);
            if (!entityDB.isPresent()) {
                throw new BusException("O detalhe de pagamento da proposta a ser excluída não existe.");
            }
            this.dao.delete(id);

            this.audit((entityDB.isPresent() ? entityDB.get() : null), AuditOperationType.PROPOSAL_PAYMENT_DELETED, userProfile);

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclusão do detalhe de pagamento da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.delete", new Object[]{ProposalPayment.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void audit(ProposalPayment model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.auditService.save(objectMapper.writeValueAsString(model), operationType, userProfile);
        } catch (JsonProcessingException e) {
            throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
        }
    }


    /**
     * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
     * <p>
     * Regra: PPY-I1,PPY-I3,PPY-I5,PPY-I6,PPY-I7,PPY-I8
     * PPY-U1,PPY-U3,PPY-U5,PPY-U6,PPY-U7,PPY-U8
     *
     * @param model entidade a ser validada
     * @param group grupo de validação que será usado
     * @throws AppException
     * @throws BusException
     */
    private void validateEntity(ProposalPayment model, Class<?> group) throws AppException, BusException {
        ValidationHelper.generateException(validator.validate(model, group));
    }

    /**
     * Valida se o método de pagamento relacionado existe
     * <p>
     * REGRA: PPY-I4, PPY-U4
     *
     * @throws BusException
     * @throws AppException
     */
    private void validatePaymentMethod(ProposalPayment model) throws BusException, AppException {
        try {
            if (model == null) {
                throw new AppException("Não é possível executar a validação do método de pagamento pois a entidade está nula ou inválida.");
            }

            if (model.getPaymentMethod() != null && model.getPaymentMethod().getId() != null) {
                Optional<PaymentMethod> pymDB = this.paymentMethodService.getById(model.getPaymentMethod().getId());

                if (!pymDB.isPresent()) {
                    throw new BusException("Não é possível salvar o detalhe de pagamento da proposta com o método de pagamento não existente.");
                }
            }


        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao executar a validação do método de pgamento relacionado o detalhe de pagamento da proposta.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }


    /**
     * Valida se a proposta relacionada existe
     * <p>
     * REGRA: PPY-I2, PPY-U2
     *
     * @throws BusException
     * @throws AppException
     */
    private void validateProposalDetail(ProposalPayment model) throws BusException, AppException {
        try {
            if (model == null) {
                throw new AppException("Não é possível executar a validação do detalhe da proposta pois a entidade está nula ou inválida.");
            }

            if (model.getProposalDetail() != null && model.getProposalDetail().getId() != null) {
                Optional<ProposalDetail> proposal = this.proposalDetailService.getById(model.getProposalDetail().getId());

                if (!proposal.isPresent()) {
                    throw new BusException("Não é possível salvar o detalhe de pagamento da proposta com uma proposta não existente.");
                }
            }

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao executar a validação da proposta relacionado o detalhe de pagamento.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }
}
