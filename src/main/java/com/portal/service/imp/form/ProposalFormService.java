package com.portal.service.imp.form;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IProposalFormDAO;
import com.portal.dto.ProductWithPriceListIdDTO;
import com.portal.dto.ProposalSearchDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.dto.form.ProductItemFormDTO;
import com.portal.dto.proposal.ProposalDTO;
import com.portal.enums.ProposalState;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.CheckpointModel;
import com.portal.model.ParameterModel;
import com.portal.model.Partner;
import com.portal.model.Product;
import com.portal.model.ProposalFollowUp;
import com.portal.model.ProposalFormProduct;
import com.portal.model.ProposalFrontForm;
import com.portal.model.ProposalItemModelType;
import com.portal.model.ProposalItemType;
import com.portal.model.ProposalProduct;
import com.portal.model.Seller;
import com.portal.service.ICheckpointService;
import com.portal.service.IItemTypeService;
import com.portal.service.IParameterService;
import com.portal.service.IProposalFollowUp;
import com.portal.service.IProposalFormService;
import com.portal.service.IProposalService;
import com.portal.service.IReportService;
import com.portal.service.ISellerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalFormService implements IProposalFormService {

    @Autowired
    private IProposalFormDAO dao;

    @Autowired
    private IParameterService parameterService;

    @Autowired
    private IItemTypeService itemTypeService;

    @Autowired
    private ICheckpointService checkpointService;

    @Autowired
    private IProposalService iProposalService;

    @Autowired
    private IProposalFollowUp proposalFollowUpService;
    
    @Autowired
    private ISellerService sellerService;

    @Autowired
    public MessageSource messageSource;

    @Autowired
    public IReportService reportService;

    @Override
    public List<ProposalFrontForm> getListProposalFrontForm(UserProfileDTO userProfile) throws AppException {
        return this.getListProposalFrontForm(null, userProfile);
    }

    @Override
    public List<ProposalFrontForm> getListProposalFrontForm(ProposalSearchDTO dto, UserProfileDTO userProfile) throws AppException {

        try {
            String daysLimit = this.parameterService.getValueOf("PROPOSAL_DAYS_LIMIT");
            String daysFup = this.parameterService.getValueOf("PROPOSAL_DAYS_FOLLOW_UP");
            Integer proposalDaysLimit = Integer.valueOf(daysLimit);

            Optional<Seller> seller = this.sellerService.getByUser(userProfile.getUser().getId());

            List<ProposalFrontForm> list = this.dao.getListProposalFrontForm(dto, proposalDaysLimit, (seller.isPresent() ? seller.get() : null), userProfile);
            Boolean editAll = Boolean.FALSE;

            for (CheckpointModel checkpointModel : userProfile.getUser().getAccessList().getCheckpoints() ) {
                if (checkpointModel.getName().equals("PROPOSAL.EDIT.ALL")) {
                    editAll = Boolean.TRUE;
                }
            }

            this.fillSeller(list);
            Boolean finalEditAll = editAll;
            list.forEach(proposal -> {
                try {
                    proposal.setIsEdit(Boolean.TRUE);

                    if(!finalEditAll  && proposal.getImmediateDelivery() == Boolean.TRUE) {
                        if (!proposal.getExecutive().getPerson().equals(userProfile.getUser().getPerson())) {
                            proposal.setIsEdit(Boolean.FALSE);
                        }
                        proposal.getExecutive().getAgentList().forEach(agent -> {
                            if (!agent.getPerson().equals(userProfile.getUser().getPerson())) {
                                proposal.setIsEdit(Boolean.FALSE);
                            }
                        });
                    }
                    
                    List<ProposalFollowUp> followUPList = this.proposalFollowUpService.search(new ProposalFollowUp(proposal.getId()));

                    LocalDate dayMinus = LocalDate.now().minusDays(Integer.valueOf(daysFup));
                    dayMinus.atTime(00, 00, 00);
                    Boolean statusValid = this.validityStatusProposal(proposal.getStatus());
                    
                    if (statusValid && proposal.getCreateDate().isBefore(dayMinus.atStartOfDay())) {
                      if (followUPList.size() > 0 && followUPList.get(0).getDate().isBefore(dayMinus.atStartOfDay())) {
                          proposal.setDaysFollowUp(ChronoUnit.DAYS.between(dayMinus, LocalDate.now()));
                      } else if (followUPList.size() == 0) {
                          proposal.setDaysFollowUp(ChronoUnit.DAYS.between(dayMinus, LocalDate.now()));
                      }
                  }
                } catch (AppException e) {
                    throw new RuntimeException(e);
                } catch (BusException e) {
                    throw new RuntimeException(e);
                }
                proposal.setValidityDate(proposal.getValidityDate() != null ? proposal.getValidityDate() : proposal.getCreateDate().toLocalDate().plusDays(proposalDaysLimit));
                proposal.setValidityDays(ChronoUnit.DAYS.between(proposal.getValidityDate().atTime(23, 59, 59), LocalDate.now().atTime(00, 00, 00)));

            });
            
            Collections.sort(list);
            
            return list;
        } catch (Exception e) {
            throw new AppException();
        }
    }

    private void fillSeller(List<ProposalFrontForm> list) {
        list.forEach(item -> {
            try {
                item.setExecutive(this.sellerService.getById(item.getExecutive().getId()).get());
            } catch (AppException e) {
                throw new RuntimeException(e);
            } catch (BusException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Boolean validityStatusProposal(ProposalState status) {
        if (status.equals(ProposalState.FINISHED_WITH_SALE)) {
            return false;
        }
        if (status.equals(ProposalState.FINISHED_WITHOUT_SALE)) {
            return false;
        }
        if (status.equals(ProposalState.CANCELED)) {
            return false;
        }
        return true;
    }

    @Override
    public List<Partner> getListPartnerByChannel(Integer id) throws AppException, BusException {

        try {

            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            return this.dao.getListPartnerByChannel(id);

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar um parceiro pelo ID de canal: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Partner.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Partner> getListPartner() throws AppException, BusException {

        try {

            return this.dao.getListPartner();

        } catch (Exception e) {
            log.error("Erro ao consultar um parceiro: ", e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Partner.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Seller> getlistExecutive() throws AppException, BusException {

        try {

            return this.dao.getlistExecutive();

        } catch (Exception e) {
            log.error("Erro ao consultar um executivo para proposta: ", e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Seller> getExecutiveList(UserProfileDTO userProfile) throws AppException, BusException {
        try {

            return applyExecutiveListRules(getlistExecutive(), userProfile);

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar lista de executivos: {}", e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Seller> getlistSellerByExecutive(Integer id) throws AppException, BusException {

        try {

            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            return this.dao.getlistSellerByExecutive(id);

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar um vendedor pelo ID de executivo: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Brand> getlistBrandByPartner(Integer id) throws AppException, BusException {

        try {

            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            return this.dao.getlistBrandByPartner(id);

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar um marca pelo ID de parceiro: {}", id, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Brand.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Brand> getlistBrandByPartner(String ptnId, String chnId) throws AppException, BusException {

        try {

            return this.dao.getlistBrandByPartner(ptnId, chnId);

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar um marca pelo ID de parceiro: {}", ptnId, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Brand.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<Product> getlistProductByModel(Integer id, Integer year) throws AppException, BusException {

        try {

            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            return this.dao.getlistProductByModel(id, year);

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar um produto pelo ID de modelo e ano: {}", id, year, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Product.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public List<ProductWithPriceListIdDTO> getlistProductByModelV1(Integer id, Integer year, Integer ptnId, Integer chnId) throws AppException, BusException {

        try {

            if (id == null) {
                throw new BusException("ID de busca inválido.");
            }

            return this.dao.getlistProductByModelV1(id, year, ptnId, chnId);

        } catch (BusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Erro ao consultar um produto pelo ID de modelo e ano: {}", id, year, e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Product.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }


    @Override
    public ProposalFormProduct getProductItems(ProductItemFormDTO productItemFormDTO) throws AppException, BusException {

        ProposalFormProduct proposalFormProduct = new ProposalFormProduct();

        List<ProposalProduct> products = this.dao.getProduct(productItemFormDTO);
        if (products == null || products.isEmpty()) {
            throw new BusException("Não foram encontradas listas de preços para esse Modelo, Produto ou Parceiro!");
        } else {
            for (ProposalProduct product : products) {
                if ((product.getPtnId() != null && product.getPtnId() > 0) && (productItemFormDTO.getPtnId() != null && productItemFormDTO.getPtnId() > 0) &&
                        product.getPtnId().equals(productItemFormDTO.getPtnId())) {
                    proposalFormProduct.setProposalProduct(product);
                    break;
                }
                proposalFormProduct.setProposalProduct(product);
            }
        }

        //REALIZA A BUSCA PRIMEIRO PELO PARCEIRO E CASO NÃO ENCONTRE UMA PRICELIST POR PARCEIRO BUSCA GENERICA
        List<ProposalItemModelType> proposalItemModelTypes = this.dao.getListItemModelProduct(productItemFormDTO);
        if (proposalItemModelTypes == null || proposalItemModelTypes.isEmpty()) {
            productItemFormDTO.setPtnId(null);
            proposalItemModelTypes = this.dao.getListItemModelProduct(productItemFormDTO);
        }

        //REALIZA A BUSCA PRIMEIRO PELO PARCEIRO E CASO NÃO ENCONTRE UMA PRICELIST POR PARCEIRO BUSCA GENERICA
        List<ProposalItemType> proposalItemTypes = this.dao.getListItemProduct(productItemFormDTO);
        if (proposalItemTypes == null || proposalItemTypes.isEmpty()) {
            productItemFormDTO.setPtnId(null);
            proposalItemTypes = this.dao.getListItemProduct(productItemFormDTO);
        }

        proposalFormProduct.setProposalItemModelTypes(proposalItemModelTypes);
        proposalFormProduct.setProposalItemTypes(proposalItemTypes);


        if (proposalFormProduct.getProposalItemModelTypes().size() > 0) {
            proposalFormProduct.getProposalItemModelTypes().forEach(itemTypes -> {

                if (itemTypes.getProposalItemModels().size() > 0) {
                    itemTypes.getProposalItemModels().forEach(item -> {

                        try {
                            item.setItemType(this.itemTypeService.getById(item.getIttId()).get());
                        } catch (AppException | BusException e) {
                            e.printStackTrace();
                        }
                    });
                }

            });
        }

        if (proposalFormProduct.getProposalItemTypes().size() > 0) {
            proposalFormProduct.getProposalItemTypes().forEach(itemTypes -> {

                if (itemTypes.getProposalItems().size() > 0) {
                    itemTypes.getProposalItems().forEach(item -> {

                        try {
                            item.setItemType(this.itemTypeService.getById(item.getIttId()).get());
                        } catch (AppException | BusException e) {
                            e.printStackTrace();
                        }
                    });
                }

            });
        }

        return proposalFormProduct;
    }

    @Override
    public List<Seller> getlistInternalSeller() throws AppException, BusException {

        try {

            return this.dao.getlistInternalSeller();

        } catch (Exception e) {
            log.error("Erro ao consultar lista de vendedores internos: {}", e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Seller.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    private List<Seller> applyExecutiveListRules(List<Seller> sellers, UserProfileDTO userProfile) throws AppException, BusException {

        // SE USUÁRIO LOGADO FOR EXECUTIVO DE NEGÓCIO RETORNAR O PRÓPRIO
        List<Seller> loggedUserExecutiveList = sellers.stream().filter(p -> p.getUser().getId().equals(userProfile.getUser().getId())).collect(Collectors.toList());

        // SE USUÁRIO LOGADO POSSUI CHECKPOINT PROPOSAL.CREATE.PREPOSTO RETORNAR TODOS OS EXECUTIVOS AO QUAL É PREPOSTO
        List<CheckpointModel> checkpoints = this.checkpointService.getByCurrentUser(userProfile.getUser().getAccessList().getId());
        if (checkpoints.stream().filter(p -> p.getName().equalsIgnoreCase("PROPOSAL.CREATE.PREPOSTO")).findFirst().isPresent()) {
            loggedUserExecutiveList.addAll(this.dao.getListExecutiveByAgent(userProfile.getUser().getId()));
        }

        // SE USUARIO LOGADO POSSUI CHECKPOINT PROPOSAL.CREATE.SALESTEAM RETORNAR TODOS OS EXECUTIVOS DE TODAS CÉLULAS AO QUAL PERTENCE
        if (checkpoints.stream().filter(p -> p.getName().equalsIgnoreCase("PROPOSAL.CREATE.SALESTEAM")).findFirst().isPresent()) {
            loggedUserExecutiveList.addAll(this.dao.getListExecutiveBySalesTeam(userProfile.getUser().getId()));
        }

        // SE USUARIO LOGADO POSSUI CHECKPOINT PROPOSAL.CREATE.ALL RETORNAR TODOS OS EXECUTIVOS
        if (checkpoints.stream().filter(p -> p.getName().equalsIgnoreCase("PROPOSAL.CREATE.ALL")).findFirst().isPresent()) {
            loggedUserExecutiveList.addAll(sellers);
        }

        return new ArrayList<>(new HashSet<>(loggedUserExecutiveList));
    }

    @Override
    public List<Partner> getListPartnerByChannelAndSeller(Integer channelId, Integer sellerId) throws AppException, BusException {
        try {

            return this.dao.getListPartnerByChannelAndSeller(channelId, sellerId);

        } catch (Exception e) {
            log.error("Erro ao consultar parceiros pelo ID de canal e ID de executivo: {}", e);
            throw new AppException(this.messageSource.getMessage("error.generic.getbyid", new Object[]{Partner.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }
    }

    public byte[] generateProposalReport(String proposalNumber) throws AppException, BusException {
        String jasperUrl = this.parameterService.find(new ParameterModel("JASPER_URL")).get().getValue();
        String jasperUser = this.parameterService.find(new ParameterModel("JASPER_USER")).get().getValue();
        String jasperPass = this.parameterService.find(new ParameterModel("JASPER_PASSWORD")).get().getValue();
        String reportPath = this.parameterService.find(new ParameterModel("PROPOSAL_REPORT_PATH")).get().getValue();
        String proposalParams = this.parameterService.find(new ParameterModel("PROPOSAL_REPORT_PARAMS")).get().getValue() + proposalNumber;

        return this.reportService.getReport(jasperUrl, reportPath, jasperUser, jasperPass, proposalParams);
    }
}
