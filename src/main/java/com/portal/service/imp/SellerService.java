package com.portal.service.imp;

import java.util.ArrayList;
import java.util.List;
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
import com.portal.dao.ISellerDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Partner;
import com.portal.model.Person;
import com.portal.model.SalesTeam;
import com.portal.model.Seller;
import com.portal.service.IAuditService;
import com.portal.service.IPersonService;
import com.portal.service.ISellerAgentService;
import com.portal.service.ISellerPartnerService;
import com.portal.service.ISellerSalesTeamService;
import com.portal.service.ISellerService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class SellerService implements ISellerService {

    @Autowired
    private ISellerDAO dao;

    @Autowired
    private Validator validator;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IAuditService auditService;

    @Autowired
    private IPersonService personService;

    @Autowired
    private ISellerPartnerService sellerPartnerService;

    @Autowired
    private ISellerSalesTeamService salesTeamSellerService;

    @Autowired
    private ISellerAgentService sellerAgentService;


    private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "sel_id");

    @Override
    public Optional<Seller> find(Seller model) throws AppException, BusException {
        List<Seller> sellerList = this.find(model, null);
        return Optional.ofNullable(sellerList != null ? sellerList.get(0) : null);
    }

    @Override
    public Optional<Seller> getById(Integer id) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            Optional<Seller> seller = this.dao.getById(id);
            if (seller.isPresent()) {
                seller.get().setPerson(this.personService.getById(seller.get().getPerson().getId()).get());
                seller.get().setSalesTeamList(this.salesTeamSellerService.findBySeller(seller.get().getId()));
                seller.get().setPartnerList(this.sellerPartnerService.findBySeller(seller.get().getId()));
                seller.get().setAgentList(this.sellerAgentService.findBySeller(seller.get().getId()));
            }

            return seller;
        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar um  executivo de negócio pelo ID: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getById", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }


    @Override
    public List<Seller> getByAgent(Integer id) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            List<Seller> seller = this.dao.getByAgent(id);
            if (seller != null && seller.size() > 0) {
                seller.forEach(executive -> {
                    try {
                        executive.setPerson(this.personService.getById(executive.getPerson().getId()).get());
                        executive.setSalesTeamList(this.salesTeamSellerService.findBySeller(executive.getId()));
                        executive.setPartnerList(this.sellerPartnerService.findBySeller(executive.getId()));
                        executive.setAgentList(this.sellerAgentService.findBySeller(executive.getId()));
                    } catch (AppException e) {
                        throw new RuntimeException(e);
                    } catch (BusException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            return seller;
        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar um  executivo de negócio: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getByAgent", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Seller> getBySalesTeam(List<SalesTeam> salesTeamList) throws AppException {
        try {
            if (salesTeamList == null) {
                throw new BusException("ID de busca inválido.");
            }

            List<Seller> seller = this.dao.getBySalesTeam(salesTeamList);

            return seller;
        } catch (Exception e) {
            log.error("Erro ao consultar um  executivo de negócio: {}", salesTeamList, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getBySalesTeam", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }


    public Optional<Seller> getByUser(Integer id) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            Optional<Seller> seller = this.dao.getByUser(id);
            if (seller.isPresent()) {
                seller.get().setPerson(this.personService.getById(seller.get().getPerson().getId()).get());
                seller.get().setSalesTeamList(this.salesTeamSellerService.findBySeller(seller.get().getId()));
                seller.get().setPartnerList(this.sellerPartnerService.findBySeller(seller.get().getId()));
                seller.get().setAgentList(this.sellerAgentService.findBySeller(seller.get().getId()));
            }

            return seller;
        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar um  executivo de negócio pelo usuer: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getByUser", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Seller> list() throws AppException, BusException {
        return this.listAll(null);
    }

    @Override
    public List<Seller> search(Seller model) throws AppException, BusException {
        return this.search(model, null);
    }

    @Override
    public List<Seller> search(Seller model, Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.search(model, pageable);
        } catch (Exception e) {
            log.error("Erro no processo de procurar os executivos de negócio .", e);
            throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }


    @Override
    public List<Seller> find(Seller model, Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.find(model, pageable);

        } catch (Exception e) {
            log.error("Erro no processo de buscar os executivo de negócios.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<Seller> saveOrUpdate(Seller model, UserProfileDTO userProfile) throws AppException, BusException {
        if (model.getId() != null && model.getId() > 0) {
            return this.update(model, userProfile);
        } else {
            return this.save(model, userProfile);
        }
    }

    @Override
    public Optional<Seller> save(Seller seller, UserProfileDTO userProfile) throws AppException, BusException {
        try {

            String document = this.getDocument(seller);
            this.checkSellerDocument(document, seller.getId() == null ? 0 : seller.getId());

            Optional<Person> personSeller = this.personService.saveOrUpdate(seller.getPerson(), userProfile);
            if (personSeller.isPresent()) {
                seller.setPerson(personSeller.get());
            }

            this.validateEntity(seller, OnSave.class);

            Optional<Seller> saved = this.dao.save(seller);
            if (saved.isPresent()) {
                if (seller.getSalesTeamList() != null && !seller.getSalesTeamList().isEmpty())
                    saved.get().setSalesTeamList(seller.getSalesTeamList());
                this.syncSalesTeamSellerRelationship(saved.get(), userProfile);

                if (seller.getPartnerList() != null && !seller.getPartnerList().isEmpty())
                    saved.get().setPartnerList(seller.getPartnerList());
                this.syncPartnerSellerRelationship(saved.get(), userProfile);

                if (seller.getAgentList() != null && !seller.getAgentList().isEmpty())
                    saved.get().setAgentList(seller.getAgentList());
                this.syncAgentSellerRelationship(saved.get(), userProfile);
            }

            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.SELLER_INSERTED, userProfile);

            return saved;

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro no processo de cadastro do executivo de negócios: {}", seller, e);
            throw new AppException(this.messageSource.getMessage("error.generic.save", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public Optional<Seller> update(Seller seller, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.validateEntity(seller, OnUpdate.class);

            String document = this.getDocument(seller);
            this.checkSellerDocument(document, seller.getId());

            Optional<Seller> saved = this.dao.update(seller);
            if (saved.isPresent()) {
                if (seller.getSalesTeamList() != null && !seller.getSalesTeamList().isEmpty())
                    saved.get().setSalesTeamList(seller.getSalesTeamList());
                this.syncSalesTeamSellerRelationship(saved.get(), userProfile);

                if (seller.getPartnerList() != null && !seller.getPartnerList().isEmpty())
                    saved.get().setPartnerList(seller.getPartnerList());
                this.syncPartnerSellerRelationship(saved.get(), userProfile);

                if (seller.getAgentList() != null && !seller.getAgentList().isEmpty())
                    saved.get().setAgentList(seller.getAgentList());
                this.syncAgentSellerRelationship(saved.get(), userProfile);
            }

            this.personService.saveOrUpdate(seller.getPerson(), userProfile);

            this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.SELLER_UPDATED, userProfile);

            return saved;
        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de atualização do seller: {}", seller, e);
            throw new AppException(this.messageSource.getMessage("error.generic.update", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            if (id == null) {
                throw new BusException("ID de exclusão inválido");
            }

            Optional<Seller> entityDB = this.getById(id);
            if (!entityDB.isPresent()) {
                throw new BusException("Seller a ser excluído não existe.");
            }

            this.salesTeamSellerService.delete(id, null, userProfile);
            this.sellerPartnerService.delete(id, null, userProfile);
            this.sellerAgentService.delete(id, null, userProfile);
            this.sellerAgentService.delete(null, id, userProfile);

            this.dao.delete(id);

            this.personService.delete(entityDB.get().getPerson().getId(), userProfile);

            this.audit((entityDB.isPresent() ? entityDB.get() : null), AuditOperationType.SELLER_DELETED, userProfile);
        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de exclusão do seller .", e);
            throw new AppException(this.messageSource.getMessage("error.seller.delete", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void audit(Seller model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
        try {
            this.auditService.save(objectMapper.writeValueAsString(model), operationType, userProfile);
        } catch (JsonProcessingException e) {
            throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Seller> listAll(Pageable pageable) throws AppException, BusException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }
            return this.dao.listAll(pageable);
        } catch (Exception e) {
            log.error("Erro no processo de listar os executivos.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Seller> searchForm(String text, Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = DEFAULT_PAGINATION;
            }

            return this.dao.searchForm(text, pageable);
        } catch (Exception e) {
            log.error("Erro no processo de procurar os seller.", e);
            throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Função que sincroniza os vendedores que são da célula de venda, essa função executa 2 operações
     * <p>
     * 1 - Excluir relacionamento com celula de venda, quando a lista das celulas de vendas não contém mais os vendedores ( ID ) na lista
     * 2 - Insere relacionamento com celula de venda, quando a lista das celulas de vendas contém novos vendedores que ainda não existem salvas no relacionamento
     *
     * @param model       - objeto da celula de vendas que vamos usar na sincronização
     * @param userProfile
     * @throws AppException
     * @throws BusException
     */
    private void syncSalesTeamSellerRelationship(Seller model, UserProfileDTO userProfile) throws BusException, NoSuchMessageException, AppException {
        try {
            if (model != null && model.getId() != null && !model.getId().equals(0)) {
                List<SalesTeam> existsSalesTeam = this.salesTeamSellerService.findBySeller(model.getId());
                List<SalesTeam> salesTeam = model.getSalesTeamList() != null && model.getSalesTeamList().size() > 0 ? model.getSalesTeamList() : null;

                if (salesTeam == null) salesTeam = new ArrayList<>();
                if (existsSalesTeam == null) existsSalesTeam = new ArrayList<>();

                List<SalesTeam> toDelete = new ArrayList<>(existsSalesTeam);
                toDelete.removeAll(salesTeam);

                List<SalesTeam> toInsert = new ArrayList<>(salesTeam);
                toInsert.removeAll(existsSalesTeam);

                for (SalesTeam saleTeam : toDelete) {
                    this.salesTeamSellerService.delete(model.getId(), saleTeam.getId(), userProfile);
                }

                for (SalesTeam saleTeam : toInsert) {
                    this.salesTeamSellerService.save(model.getId(), saleTeam.getId(), userProfile);
                }
            }

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de sincronizar marca e parceiro.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Função que sincroniza os parceiros do executivo, essa função executa 2 operações
     * <p>
     * 1 - Excluir relacionamento com partner, quando a lista dos partners não contém mais os vendedores ( ID ) na lista
     * 2 - Insere relacionamento com partner, quando a lista dos partners contém novos partners que ainda não existem salvos no relacionamento
     *
     * @param model       - objeto da partner que vamos usar na sincronização
     * @param userProfile
     * @throws AppException
     * @throws BusException
     */
    private void syncPartnerSellerRelationship(Seller model, UserProfileDTO userProfile) throws BusException, NoSuchMessageException, AppException {
        try {

            if (model != null && model.getId() != null && !model.getId().equals(0)) {
                List<Partner> existsPartner = this.sellerPartnerService.findBySeller(model.getId());
                List<Partner> partner = model.getPartnerList() != null && model.getPartnerList().size() > 0 ? model.getPartnerList() : null;

                if (partner == null) partner = new ArrayList<>();
                if (existsPartner == null) existsPartner = new ArrayList<>();

                List<Partner> toDelete = new ArrayList<>(existsPartner);
                toDelete.removeAll(partner);

                List<Partner> toInsert = new ArrayList<>(partner);
                toInsert.removeAll(existsPartner);

                for (Partner partnerModel : toDelete) {
                    this.sellerPartnerService.delete(model.getId(), partnerModel.getId(), userProfile);
                }

                for (Partner partnerModel : toInsert) {
                    this.sellerPartnerService.save(model.getId(), partnerModel.getId(), userProfile);
                }
            }

        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de sincronizar marca e parceiro.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Função que sincroniza os agentes do executivo, essa função executa 2 operações
     * <p>
     * 1 - Excluir relacionamento com seller, quando a lista dos sellers não contém mais os agentes ( ID ) na lista
     * 2 - Insere relacionamento com partner, quando a lista dos sellers contém novos agentes que ainda não existem salvos no relacionamento
     *
     * @param model       - objeto da seller que vamos usar na sincronização
     * @param userProfile
     * @throws AppException
     * @throws BusException
     */
    private void syncAgentSellerRelationship(Seller model, UserProfileDTO userProfile) throws BusException, NoSuchMessageException, AppException {
        try {
            if (model != null && model.getId() != null && !model.getId().equals(0)) {
                List<Seller> existsSeller = this.sellerAgentService.findBySeller(model.getId());
                List<Seller> seller = model.getAgentList() != null && model.getAgentList().size() > 0 ? model.getAgentList() : null;

                if (seller == null) seller = new ArrayList<>();
                if (existsSeller == null) existsSeller = new ArrayList<>();

                List<Seller> toDelete = new ArrayList<>(existsSeller);
                toDelete.removeAll(seller);

                List<Seller> toInsert = new ArrayList<>(seller);
                toInsert.removeAll(existsSeller);

                for (Seller modelStream : toDelete) {
                    this.sellerAgentService.delete(model.getId(), modelStream.getId(), userProfile);
                }

                for (Seller modelStream : toInsert) {
                    this.sellerAgentService.save(model.getId(), modelStream.getId(), userProfile);
                }
            }
        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro no processo de sincronizar marca e parceiro.", e);
            throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
        }
    }

    private void validateEntity(Seller model, Class<?> group) throws BusException {
        ValidationHelper.generateException(validator.validate(model, group));
    }

    private void checkSellerDocument(String document, Integer seller) throws AppException, BusException {

        Integer sellerId = this.dao.checkSellerDocument(document);

        if (sellerId != null && !seller.equals(sellerId)) {
            throw new BusException("Já existe um executivo cadastrado com documento.");
        }
    }

    private String getDocument(Seller seller) {

        String document = "";

        if (seller.getPerson().getClassification().getId() == 29) {
            // PJ
            document = seller.getPerson().getCnpj();
        } else if (seller.getPerson().getClassification().getId() == 30) {
            // PF
            document = seller.getPerson().getCpf();
        } else if (seller.getPerson().getClassification().getId() == 31) {
            // RNE
            document = seller.getPerson().getRne();
        }

        return document;
    }
}
