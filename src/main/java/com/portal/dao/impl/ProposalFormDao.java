package com.portal.dao.impl;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.portal.dto.ProductWithPriceListIdDTO;
import com.portal.dto.ProposalSearchRulesDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.mapper.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IProposalFormDAO;
import com.portal.dto.ProposalSearchDTO;
import com.portal.dto.form.ProductItemFormDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.Person;
import com.portal.model.Product;
import com.portal.model.ProposalFrontForm;
import com.portal.model.ProposalItem;
import com.portal.model.ProposalItemModel;
import com.portal.model.ProposalItemModelType;
import com.portal.model.ProposalItemType;
import com.portal.model.ProposalProduct;
import com.portal.model.Seller;
import com.portal.model.UserModel;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalFormDao extends BaseDAO implements IProposalFormDAO {

    private final String CREATE_DATE = "creationDate";

    @Override
    public List<ProposalFrontForm> getListProposalFrontForm(ProposalSearchDTO dto, Integer proposalDaysLimit, Seller seller, UserProfileDTO userProfile) throws AppException {

        try {

            String query = "select   "
                    + "	   p.pps_id,  "
                    + "    p.status_cla_id,  "
                    + "    IFNULL(per.name, '-') as `client`,  "
                    + "	   p.num as num_proposta,  "
                    + "	   p.proposal_number,  "
                    + "    p.cod as cod, "
                    + "    p.immediate_delivery,  "
                    + "    slo.order_number, "
                    + "    ifnull( ent_per.name, '' ) as partner,  "
                    + "    sel_pd.sel_id as seller_id, "
                    + "    per_pd.per_id as seller_per_id, "
                    + "    per_pd.name as seller_name, "
                    + "    concat(m.name, \" | \" ,b.name) as brandModel,  "
                    + "    p.create_date,"
                    + "    p.validity_date,  "
                    + "	   pdv.total_amount as total_price, "
        			+ "    statusCla.cla_id as status_cla_id, " 
        			+ "    statusCla.value as status_cla_value, "
        			+ "    statusCla.type as status_cla_type, "
        			+ "    statusCla.label as status_cla_label "
                    + "from proposal p "
        			+ "inner join classifier statusCla on statusCla.cla_id = p.status_cla_id "
                    + "left join sales_order slo on slo.pps_id = p.pps_id "
                    + "left join proposal_person_client ppc on ppc.pps_id = p.pps_id "
                    + " 	and ppc.customer_cla_id = (SELECT cla_id FROM classifier where type = 'CUSTOMER_TYPE' AND value = 'FINANCIADOR') "
                    + "left join person per on per.per_id = ppc.per_id  "
                    + "inner join proposal_detail pd on pd.pps_id = p.pps_id  "
                    + "inner join seller sel_pd on sel_pd.sel_id = pd.sel_id "

                    + "inner join `user` usr on usr.per_id = sel_pd.per_id "
                    + "and ( "
                    + "		(usr.usr_id = :usr_id) "

                    + "		or (fnGetUserHasCheckpoint(:usr_id,'PROPOSAL.VIEW.ALL')) "

                    + "		or (fnGetUserHasCheckpoint(:usr_id,'PROPOSAL.VIEW.PROMPT.DELIVERY') and p.immediate_delivery = 1)"

                    + "		or (fnGetUserHasCheckpoint(:usr_id,'PROPOSAL.VIEW.PREPOSTO') and sel_pd.sel_id in (select pre.sel_id from seller_agent pre where  pre.agent_sel_id = :sel_id)) "

                    + "		or (fnGetUserHasCheckpoint(:usr_id,'PROPOSAL.VIEW.OWNER') and sel_pd.sel_id in (select own.agent_sel_id from seller_agent own where  own.sel_id = :sel_id)) "

                    + "		or (fnGetUserHasCheckpoint(:usr_id,'PROPOSAL.VIEW.TEAM') "
                    + "				and sel_pd.sel_id in (select sts_team.sel_id "
                        + "									from sales_team_seller sts_seller "
                        + "									inner join sales_team_seller sts_team on sts_team.slt_id = sts_seller.slt_id "
                        + "									where sts_seller.sel_id = :sel_id)) "
                    + "		) "

                    + "inner join person per_pd on sel_pd.per_id = per_pd.per_id "
                    + "inner join proposal_detail_vehicle pdv on pdv.ppd_id = pd.ppd_id  "
                    + "inner join price_product pp on pp.ppr_id = pdv.ppr_id "
                    + "left join vehicle v on v.vhe_id = pdv.vhe_id  "
                    + "left join model m on m.mdl_id = v.mdl_id "
                    + "left join brand b on b.brd_id = m.brd_id "
                    + "left join partner par on par.ptn_id = pd.ptn_id  "
                    + "left join person ent_per ON ent_per.per_id = par.entity_per_id  "
                    + "left join partner_group parg ON parg.ptg_id = par.ptg_id "
                    + "where 1=1 ";

            MapSqlParameterSource params = new MapSqlParameterSource();


            if (seller != null) {
                params.addValue("sel_id", seller.getId());
            } else {
            	params.addValue("sel_id", null);
            }
            
            if (userProfile != null) {
                params.addValue("usr_id", userProfile.getUser().getId());
            }


            if (dto != null) {

                if (dto.getStatus() != null) {
                    if (dto.getStatus().size() > 0) {
                        query += " and p.status_cla_id in ( :status ) ";
                        params.addValue("status", dto.getStatus());
                    }
                }

                if (dto.getPartner() != null) {
                    if (dto.getPartner().size() > 0) {
                        query += " and par.ptn_id in ( :partnerId )  ";
                        List<String> partnerList = new ArrayList<>();
                        dto.getPartner().stream().map(item -> {
                            return partnerList.add(item.getId().toString());
                        }).collect(Collectors.toList());
                        params.addValue("partnerId", partnerList);
                    }
                }

                if (dto.getProposalnum() != null) {

                    query += " and p.num = :num ";
                    params.addValue("num", dto.getProposalnum());
                }

                if (dto.getOrdernum() != null && !dto.getOrdernum().isEmpty()) {

                    query += " and slo.order_number = :ordernum ";
                    params.addValue("ordernum", dto.getOrdernum());
                }

                if (dto.getName() != null && !dto.getName().isEmpty()) {

                    query += " and UPPER(per.name) like :name ";
                    params.addValue("name", this.mapLike(dto.getName()).toUpperCase());
                }

                if (dto.getExecutive() != null) {
                    if (dto.getExecutive().size() > 0) {
                        query += " and pd.sel_id in ( :seller )  ";
                        List<String> sellerList = new ArrayList<>();
                        dto.getExecutive().stream().map(item -> {
                            return sellerList.add(item.getId().toString());
                        }).collect(Collectors.toList());
                        params.addValue("seller", sellerList);
                    }
                }

                if (dto.getBrand() != null && dto.getBrand().getId() != null && dto.getBrand().getId() > 0) {

                    query += " and b.brd_id = :brand ";
                    params.addValue("brand", dto.getBrand().getId());
                }

                if (dto.getModel() != null) {
                    if (dto.getModel().size() > 0) {
                        query += " and m.mdl_id in ( :model ) ";
                        List<String> modelList = new ArrayList<>();
                        dto.getModel().stream().map(item -> {
                            return modelList.add(item.getId().toString());
                        }).collect(Collectors.toList());
                        params.addValue("model", modelList);
                    }
                }

                if (dto.getPartnerGroup() != null) {
                    if (dto.getPartnerGroup().size() > 0) {
                        query += " and parg.ptg_id in ( :partnerGroup )";
                        List<String> partnerGroupList = new ArrayList<>();
                        dto.getPartnerGroup().stream().map(item -> {
                            return partnerGroupList.add(item.getId().toString());
                        }).collect(Collectors.toList());
                        params.addValue("partnerGroup", partnerGroupList);
                    }
                }

                if (dto.getDateIni() != null && dto.getDateType() != null) {
                    if (dto.getDateType().equals(CREATE_DATE)) {
                        query += " and p.create_date >= :dateIni ";
                        params.addValue("dateIni", dto.getDateIni().atTime(00, 00, 00));
                    } else {
                        query += " and p.create_date >= :dateIni ";
                        params.addValue("dateIni", dto.getDateIni().minusDays(proposalDaysLimit).atTime(00, 00, 00));
                    }
                }

                if (dto.getDateEnd() != null && dto.getDateType() != null) {
                    if (dto.getDateType().equals(CREATE_DATE)) {
                        query += " and p.create_date <= :dateEnd ";
                        params.addValue("dateEnd", dto.getDateEnd().atTime(23, 59, 59));
                    } else {
                        query += " and p.create_date <= :dateEnd ";
                        params.addValue("dateEnd", dto.getDateEnd().minusDays(proposalDaysLimit).atTime(23, 59, 59));
                    }
                }

                if (dto.getImmediateDelivery() != null) {
                    query += " and p.immediate_delivery = :immediateDelivery ";
                    if (dto.getImmediateDelivery().equals("true")) {
                        params.addValue("immediateDelivery", PortalNumberUtils.booleanToInt(Boolean.TRUE));
                    } else {
                        params.addValue("immediateDelivery", PortalNumberUtils.booleanToInt(Boolean.FALSE));

                    }
                }
            }

            log.trace("[QUERY] ProposalForm.getListProposalFrontForm: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query, params, new ProposalFrontFormMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar o relacionamento entre proposta e documento.", e);
            throw new AppException("Erro ao buscar o lista de proposta para o frontend.", e);
        }
    }

    @Override
    public List<Partner> getListPartnerByChannel(Integer id) throws AppException {

        try {

            String query = "SELECT  ptn.*, " +
                    "sit.cla_id as sit_cla_id," +
                    "sit.value as sit_cla_value," +
                    "sit.type as sit_cla_type," +
                    "sit.label as sit_cla_label," +
                    "chn.name AS chn_name, " +
                    "chn.active AS chn_active, " +
                    "per.name AS per_name, " +
                    "per.job_title AS per_job_title, " +
                    "per.cpf AS per_cpf, " +
                    "per.cnpj AS per_cnpj, " +
                    "per.rg AS per_rg, " +
                    "per.rne AS per_rne, " +
                    "typ.cla_id as per_cla_id," +
                    "typ.value as per_cla_value," +
                    "typ.type as per_cla_type," +
                    "typ.label as per_cla_label," +
                    "per.add_id AS per_add_id, " +
                    "IFNULL(ptg.ptg_id, 0) AS ptg_id, " +
                    "ptg.name AS ptg_name " +
                    "FROM partner ptn " +
                    "INNER JOIN classifier sit ON ptn.situation_cla = sit.cla_id " +
                    "INNER JOIN person per ON per.per_id = ptn.entity_per_id " +
                    "INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " +
                    "INNER JOIN channel chn ON chn.chn_id = ptn.chn_id " +
                    "LEFT JOIN partner_group ptg on ptg.ptg_id = ptn.ptg_id " +
                    "WHERE ptn.chn_id = :id ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] proposalform.getListPartnerByChannel: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query, params, new PartnerMapper());

        } catch (Exception e) {
            log.error("Erro ao consultar o parceiro.", e);
            throw new AppException("Erro ao consultar o parceiro.", e);
        }
    }

    @Override
    public List<Partner> getListPartner() throws AppException {

        try {

            String query = "SELECT  ptn.*, " +
                    "sit.cla_id as sit_cla_id," +
                    "sit.value as sit_cla_value," +
                    "sit.type as sit_cla_type," +
                    "sit.label as sit_cla_label," +
                    "chn.name AS chn_name, " +
                    "chn.active AS chn_active, " +
                    "per.name AS per_name, " +
                    "per.job_title AS per_job_title, " +
                    "per.cpf AS per_cpf, " +
                    "per.cnpj AS per_cnpj, " +
                    "per.rg AS per_rg, " +
                    "per.rne AS per_rne, " +
                    "typ.cla_id as per_cla_id," +
                    "typ.value as per_cla_value," +
                    "typ.type as per_cla_type," +
                    "typ.label as per_cla_label," +
                    "per.add_id AS per_add_id, " +
                    "IFNULL(ptg.ptg_id, 0) AS ptg_id, " +
                    "ptg.name AS ptg_name " +
                    "FROM partner ptn " +
                    "INNER JOIN classifier sit ON ptn.situation_cla = sit.cla_id " +
                    "INNER JOIN person per ON per.per_id = ptn.entity_per_id " +
                    "INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " +
                    "INNER JOIN channel chn ON chn.chn_id = ptn.chn_id " +
                    "LEFT JOIN partner_group ptg on ptg.ptg_id = ptn.ptg_id " +
                    "ORDER BY per.name ASC";

            log.trace("[QUERY] proposalform.getListPartnerByChannel: {} ", query);

            return this.getJdbcTemplatePortal().query(query, new PartnerMapper());

        } catch (Exception e) {
            log.error("Erro ao consultar o parceiro.", e);
            throw new AppException("Erro ao consultar o parceiro.", e);
        }
    }

    @Override
    public List<Seller> getlistExecutiveByPartner(Integer id) throws AppException {

        List<Seller> sellerList = new ArrayList<Seller>();

        String query = "select s.sel_id, per.per_id, per.name, usr.usr_id from seller s "
                + "inner join person per on per.per_id = s.per_id "
                + "inner join user AS usr ON per.per_id = usr.per_id "
                + "inner join seller_partner sp on sp.sel_id = s.sel_id "
                + "where "
                + "	sp.ptn_id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        log.trace("[QUERY] proposalform.getlistExecutiveByPartner: {} [PARAMS]: {}", query, params.getValues());

        sellerList = this.getJdbcTemplatePortal().query(query, params, (ResultSet rs) -> {

            List<Seller> qList = new ArrayList<Seller>();

            while (rs.next()) {

                Seller seller = new Seller();

                Person person = new Person();
                person.setId(rs.getInt("per_id"));
                person.setName(rs.getString("name"));

                seller.setId(rs.getInt("sel_id"));
                seller.setPerson(person);
                seller.setUser(new UserModel(rs.getInt("usr_id")));

                qList.add(seller);
            }

            return qList;
        });

        return sellerList;
    }

    @Override
    public List<Seller> getlistExecutive() throws AppException {

        List<Seller> sellerList = new ArrayList<Seller>();

        String query = "select s.sel_id, per.per_id, per.name, usr.usr_id from seller s "
                + "inner join person per on per.per_id = s.per_id "
                + "inner join user AS usr ON per.per_id = usr.per_id "
                + " order by per.name";

        log.trace("[QUERY] proposalform.getlistExecutive: {} ", query);

        sellerList = this.getJdbcTemplatePortal().query(query, (ResultSet rs) -> {

            List<Seller> qList = new ArrayList<Seller>();

            while (rs.next()) {

                Seller seller = new Seller();

                Person person = new Person();
                person.setId(rs.getInt("per_id"));
                person.setName(rs.getString("name"));

                seller.setId(rs.getInt("sel_id"));
                seller.setPerson(person);
                seller.setUser(new UserModel(rs.getInt("usr_id")));

                qList.add(seller);
            }

            return qList;
        });

        return sellerList;
    }

    @Override
    public List<Seller> getlistSellerByExecutive(Integer id) throws AppException {

        List<Seller> sellerList = new ArrayList<Seller>();

        String query = "select s.sel_id, per.per_id, per.name from seller s "
                + "inner join person per on per.per_id = s.per_id "
                + "inner join user AS usr ON per.per_id = usr.per_id "
                + "inner join seller_agent sa on sa.agent_sel_id = s.sel_id "
                + "where "
                + "	sa.sel_id = :id "
                + " order by per.name";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        log.trace("[QUERY] proposalform.getlistSellerByExecutive: {} [PARAMS]: {}", query, params.getValues());

        sellerList = this.getJdbcTemplatePortal().query(query, params, (ResultSet rs) -> {

            List<Seller> qList = new ArrayList<Seller>();

            while (rs.next()) {

                Seller seller = new Seller();

                Person person = new Person();
                person.setId(rs.getInt("per_id"));
                person.setName(rs.getString("name"));

                seller.setId(rs.getInt("sel_id"));
                seller.setPerson(person);

                qList.add(seller);
            }

            return qList;
        });

        return sellerList;
    }

    @Override
    public List<Brand> getlistBrandByPartner(Integer id) throws AppException, BusException {

        try {

            String query = "select b.* from brand b "
                    + "inner join partner_brand pb on pb.brd_id = b.brd_id "
                    + "where pb.ptn_id = :id ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] proposalform.getlistBrandByPartner: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query, params, new BrandMapper());

        } catch (Exception e) {
            log.error("Erro ao listar as marcas.", e);
            throw new AppException("Erro ao listar as marcas.", e);
        }
    }

    @Override
    public List<Brand> getlistBrandByPartner(String ptnId, String chnId) throws AppException, BusException {

        try {
        	
            String query = "select brd.* " +
                    "from brand brd " +
                    "inner join channel chn on chn.chn_id = :chn_id " +
                    "left join partner_brand pbr on pbr.brd_id= brd.brd_id and pbr.ptn_id = :ptn_id " +
                    "where pbr.ptn_id = :ptn_id or chn.has_partner = 0 ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("ptn_id", ptnId);
            params.addValue("chn_id", chnId);

            log.trace("[QUERY] proposalform.getlistBrandByPartner: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query, params, new BrandMapper());

        } catch (Exception e) {
            log.error("Erro ao listar as marcas.", e);
            throw new AppException("Erro ao listar as marcas.", e);
        }
    }

    @Override
    public List<Product> getlistProductByModel(Integer id, Integer year) throws AppException, BusException {

        try {

            String query = "select p.* from product p "
                    + "inner join product_model pm on pm.prd_id = p.prd_id "
                    + "where p.active = '1' "
                    + "	and mdl_id = :id "
                    + "    and (pm.model_year_start <= :year and pm.model_year_end >= :year)";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            params.addValue("year", year);

            log.trace("[QUERY] proposalform.getlistProductByModel: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query, params, new ProductMapper());

        } catch (Exception e) {
            log.error("Erro ao listar as produtos.", e);
            throw new AppException("Erro ao listar as produtos.", e);
        }
    }

    @Override
    public List<ProductWithPriceListIdDTO> getlistProductByModelV1(Integer mdlId, Integer year, Integer ptnId, Integer chnId) throws AppException, BusException {

        try {

            String query = "SELECT prd.*, " +
                    "prl.prl_id, " +
                    "if (fnGetCustomerPriceList(prm.mdl_id, prm.prd_id,prm.model_year_start) = 0,0, fnGetCustomerPriceList(prm.mdl_id, prm.prd_id,prm.model_year_start) - ppr.price) as over_parceiro " +
                    "FROM product_model prm " +
                    "INNER JOIN product prd ON prd.prd_id = prm.prd_id " +
                    "INNER JOIN price_product ppr ON ppr.prm_id = prm.prm_id " +
                    "INNER JOIN price_list prl ON prl.prl_id = ppr.prl_id " +
                    "INNER JOIN `channel` chn ON chn.chn_id = prl.chn_id " +
                    "LEFT JOIN price_list_partner plp ON plp.prl_id = prl.prl_id " +
                    "WHERE prm.mdl_id = :mdlId " +
                    "AND prm.model_year_start <= :year " +
                    "AND prm.model_year_end >= :year " +
                    "AND prl.chn_id = :chnId " +
                    "AND ( (prl.all_partners = 0 AND chn.has_partner = 0) " +
                    "OR (prl.all_partners = 1 " +
                    "AND prm.mdl_id not in " +
                    "(SELECT prm2.mdl_id " +
                    "FROM price_list prl2 " +
                    "INNER JOIN price_product ppr2 ON ppr2.prl_id = prl2.prl_id " +
                    "INNER JOIN product_model prm2 ON prm2.prm_id = ppr2.prm_id " +
                    "INNER JOIN price_list_partner plp2 ON plp2.prl_id = prl2.prl_id " +
                    "WHERE prm2.mdl_id = prm.mdl_id " +
                    "AND prl2.chn_id = :chnId AND prl2.start_date <= now() " +
                    "AND prl2.end_date >= now() " +
                    "AND plp2.ptn_id = :ptnId )) " +
                    "OR (prl.all_partners = 0 AND plp.ptn_id = :ptnId)) " +
                    "AND prl.start_date <= now() " +
                    "AND prl.end_date >= now() ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("mdlId", mdlId);
            params.addValue("ptnId", ptnId);
            params.addValue("chnId", chnId);
            params.addValue("year", year);

            log.trace("[QUERY] proposalform.getlistProductByModelV1: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query, params, new ProductWithPriceListMapper());

        } catch (Exception e) {
            log.error("Erro ao listar as produtos.", e);
            throw new AppException("Erro ao listar as produtos.", e);
        }
    }


    @Override
    public List<ProposalProduct> getProduct(ProductItemFormDTO productItemFormDTO) throws AppException, BusException {
        try {
            String query = "select  "
                    + "    'CARROCERIA' as name_item_type, "
                    + "    p.prd_id, "
                    + "    null as itm_id, "
                    + "    p.name as name_item, "
                    + "    '' as cod, "
                    + "    '0' as for_free, "
                    + "    pp.price, "
                    + "    pp.ppr_id, "
                    + "    pl.prl_id, "
                    + "    plp.ptn_id "
                    + "from product_model pm "
                    + "inner join product p on p.prd_id = pm.prd_id "
                    + "inner join price_product pp on pp.prm_id = pm.prm_id "
                    + "inner join price_list pl on pl.prl_id = pp.prl_id "
                    + "left join price_list_partner plp on plp.prl_id = pl.prl_id "
                    + "where "
                    + "    pm.prd_id = :prd_id "
                    + "    and pl.chn_id = :chn_id "
                    + "    and pm.mdl_id = :mdl_id "
                    + "    and (pm.model_year_start <= :year and pm.model_year_end >= :year) "
                    + "    and (pl.start_date<= now() and pl.end_date >= now()) ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("prd_id", productItemFormDTO.getPrdId());
            params.addValue("chn_id", productItemFormDTO.getChnId());
            params.addValue("year", productItemFormDTO.getYear());
            params.addValue("mdl_id", productItemFormDTO.getMdlId());

            log.trace("[QUERY] proposalform.getProduct: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query, params, new ProposalProductMapper());

        } catch (Exception e) {
            log.error("Erro ao listar as produtos.", e);
            throw new AppException("Erro ao listar as produtos.", e);
        }
    }

    @Override
    public List<ProposalItemModelType> getListItemModelProduct(ProductItemFormDTO productItemFormDTO) throws AppException, BusException {

        try {

            String query = "select " +
                    "    itt.name as name_item_type, " +
                    "    itt.itt_id, " +
                    "    itm.itm_id, " +
                    "    itm.name as name_item, " +
                    "    itm.cod, " +
                    "    itm.seq, " +
                    "    itm.for_free, " +
                    "    itm.generic, " +
                    "    itm.file,  " +
                    "    itm.icon, " +
                    "    itm.description, " +
                    "    itm.hyperlink, " +
                    "    itm.term, " +
                    "    itm.term_work_day, " +
                    "    itm.highlight, " +
                    "    item_mand.cla_id as mand_cla_id, " +
                    "    item_mand.label as mand_label, " +
                    "    item_mand.value as mand_value, " +
                    "    item_mand.type as mand_type, " +
                    "    item_resp.cla_id as resp_cla_id, " +
                    "    item_resp.label as resp_label, " +
                    "    item_resp.value as resp_value, " +
                    "    item_resp.type as resp_type, " +
                    "    pim.price, " +
                    "    pim.pim_id, " +
                    "    prl.prl_id  " +

                    "from model mdl " +

                    "inner join brand brd on brd.brd_id = mdl.brd_id " +
                    "inner join item_model imd on imd.mdl_id = mdl.mdl_id " +
                    "inner join item itm on	imd.itm_id = itm.itm_id " +
                    "inner join item_type itt on itt.itt_id = itm.itt_id " +
                    "inner join classifier as item_mand on itm.mandatory_cla_id = item_mand.cla_id " +
                    "inner join classifier as item_resp on itm.responsability_cla_id = item_resp.cla_id " +
                    "inner join price_item_model pim " +
                    "	on pim.itm_id = itm.itm_id " +
                    "    and (  " +

                    "			(pim.imd_id = imd.imd_id) " +

                    "			or 	(	pim.brd_id = mdl.brd_id  " +
                    "				and pim.all_models = 1  " +
                    "				and pim.itm_id not in 	(select pim2.itm_id  " +
                    "										from price_item_model pim2  " +
                    "										where pim2.prl_id = pim.prl_id " +
                    "										and pim2.imd_id = imd.imd_id " +
                    "										) " +
                    "				) " +

                    "			or	(	pim.all_brands = 1  " +
                    "				and pim.itm_id not in 	(select pim3.itm_id  " +
                    "										from price_item_model pim3  " +
                    "										where pim3.prl_id = pim.prl_id  " +
                    "										and (	pim3.imd_id = imd.imd_id  " +
                    "											or (	pim3.brd_id = mdl.brd_id  " +
                    "												and pim3.all_models = 1 " +
                    "												) " +
                    "											) " +
                    "										) " +
                    "				) " +
                    "			) " +

                    "inner join (select distinct mdl_id as p_mdl_id, prl_id as p_prl_id  " +
                    "			from product_model prm  " +
                    "            inner join price_product ppr  " +
                    "				on ppr.prm_id = prm.prm_id) as a  " +

                    "				on a.p_mdl_id = mdl.mdl_id  " +


                    "inner join price_list prl  on 	prl.prl_id = pim.prl_id and prl.prl_id = a.p_prl_id " +

                    "where 	prl.prl_id = :prl_id " +
                    "	and mdl.mdl_id = :mdl_id " +
                    "	and imd.model_year_start <= :model_year " +
                    "	and imd.model_year_end >= :model_year ";

            MapSqlParameterSource params = new MapSqlParameterSource();

            params.addValue("model_year", productItemFormDTO.getYear());
            params.addValue("mdl_id", productItemFormDTO.getMdlId());
            params.addValue("prl_id", productItemFormDTO.getPrlId());

            log.trace("[QUERY] proposalform.getListItemModelProduct: {} [PARAMS]: {}", query, params.getValues());

            List<ProposalItemModelType> listProposalItemModelType = this.getJdbcTemplatePortal().query(query, params, (ResultSet rs) -> {

                List<ProposalItemModelType> plistItemType = new ArrayList<ProposalItemModelType>();

                Map<String, List<ProposalItemModel>> mapProposalItem = new HashMap<String, List<ProposalItemModel>>();

                while (rs.next()) {

                    ProposalItemModel proposalItemModel = new ProposalItemModel();
                    proposalItemModel.setItmId(rs.getInt("itm_id"));
                    proposalItemModel.setNameItem(rs.getString("name_item"));
                    proposalItemModel.setCod(rs.getString("cod"));
                    proposalItemModel.setSeq(rs.getInt("seq"));
                    proposalItemModel.setForFree(PortalNumberUtils.intToBoolean(rs.getInt("for_free")));
                    proposalItemModel.setGeneric(PortalNumberUtils.intToBoolean(rs.getInt("generic")));

                    Classifier mandatory = new Classifier();
                    mandatory.setId(rs.getInt("mand_cla_id"));
                    mandatory.setLabel(rs.getString("mand_label"));
                    mandatory.setValue(rs.getString("mand_value"));
                    mandatory.setType(rs.getString("mand_type"));
                    proposalItemModel.setMandatory(mandatory);

                    proposalItemModel.setFile(rs.getString("file"));
                    proposalItemModel.setIcon(rs.getString("icon"));
                    proposalItemModel.setDescription(rs.getString("description"));
                    proposalItemModel.setHyperlink(rs.getString("hyperlink"));

                    proposalItemModel.setTerm(rs.getInt("term"));
                    proposalItemModel.setTermWorkDay(PortalNumberUtils.intToBoolean(rs.getInt("term_work_day")));

                    Classifier responsability = new Classifier();
                    responsability.setId(rs.getInt("resp_cla_id"));
                    responsability.setLabel(rs.getString("resp_label"));
                    responsability.setValue(rs.getString("resp_value"));
                    responsability.setType(rs.getString("resp_type"));
                    proposalItemModel.setResponsability(responsability);

                    proposalItemModel.setPrice(rs.getDouble("price"));
                    proposalItemModel.setPimId(rs.getInt("pim_id"));
                    proposalItemModel.setPrlId(rs.getInt("prl_id"));
                    proposalItemModel.setIttId(rs.getInt("itt_id"));
                    String nameItemType = rs.getString("name_item_type");

                    if (mapProposalItem.containsKey(nameItemType)) {

                        mapProposalItem.get(nameItemType).add(proposalItemModel);

                    } else {

                        List<ProposalItemModel> lProposalItemModel = new ArrayList<ProposalItemModel>();
                        lProposalItemModel.add(proposalItemModel);

                        mapProposalItem.put(nameItemType, lProposalItemModel);
                    }

                }

                mapProposalItem.forEach((nameItemType, proposalItemModel) -> {

                    ProposalItemModelType proposalItemModelType = new ProposalItemModelType();
                    proposalItemModelType.setNameItemType(nameItemType);
                    proposalItemModelType.setProposalItemModels(proposalItemModel);

                    plistItemType.add(proposalItemModelType);
                });

                return plistItemType;
            });

            return listProposalItemModelType;

        } catch (Exception e) {
            log.error("Erro ao listar os itens model com precos.", e);
            throw new AppException("Erro ao listar os itens model com precos.", e);
        }
    }

    @Override
    public List<ProposalItemType> getListItemProduct(ProductItemFormDTO productItemFormDTO) throws AppException, BusException {

        try {

            String query = "select " +
                    "it.name as name_item_type, " +
                    "it.itt_id, " +
                    "i.itm_id, " +
                    "i.name as name_item, " +
                    "i.cod, " +
                    "i.seq, " +
                    "i.for_free, " +
                    "i.generic, " +
                    "i.file, " +
                    "i.icon, " +
                    "i.description, " +
                    "i.hyperlink, " +
                    "i.term, " +
                    "i.term_work_day, " +
                    "i.highlight, " +
                    "item_mand.cla_id as mand_cla_id, " +
                    "item_mand.label as mand_label, " +
                    "item_mand.value as mand_value, " +
                    "item_mand.type as mand_type, " +
                    "item_resp.cla_id as resp_cla_id, " +
                    "item_resp.label as resp_label, " +
                    "item_resp.value as resp_value, " +
                    "item_resp.type as resp_type, " +
                    "pi.price, " +
                    "pi.pci_id, " +
                    "pl.prl_id " +
                    "from item i " +
                    "inner join classifier as item_mand on i.mandatory_cla_id = item_mand.cla_id " +
                    "inner join classifier as item_resp on i.responsability_cla_id = item_resp.cla_id " +
                    "inner join item_type it on it.itt_id = i.itt_id " +
                    "inner join price_item pi on pi.itm_id = i.itm_id " +
                    "inner join price_list pl on pl.prl_id = pi.prl_id " +
                    "where pl.prl_id = :prl_id ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("prl_id", productItemFormDTO.getPrlId());


            log.trace("[QUERY] proposalform.getListItemProduct: {} [PARAMS]: {}", query, params.getValues());

            List<ProposalItemType> listProposalItemType = this.getJdbcTemplatePortal().query(query, params, (ResultSet rs) -> {

                List<ProposalItemType> plistItemType = new ArrayList<ProposalItemType>();

                Map<String, List<ProposalItem>> mapProposalItem = new HashMap<String, List<ProposalItem>>();

                while (rs.next()) {

                    ProposalItem proposalItemModel = new ProposalItem();

                    proposalItemModel.setItmId(rs.getInt("itm_id"));
                    proposalItemModel.setNameItem(rs.getString("name_item"));
                    proposalItemModel.setCod(rs.getString("cod"));
                    proposalItemModel.setSeq(rs.getInt("seq"));
                    proposalItemModel.setForFree(PortalNumberUtils.intToBoolean(rs.getInt("for_free")));
                    proposalItemModel.setGeneric(PortalNumberUtils.intToBoolean(rs.getInt("generic")));

                    Classifier mandatory = new Classifier();
                    mandatory.setId(rs.getInt("mand_cla_id"));
                    mandatory.setLabel(rs.getString("mand_label"));
                    mandatory.setValue(rs.getString("mand_value"));
                    mandatory.setType(rs.getString("mand_type"));
                    proposalItemModel.setMandatory(mandatory);

                    proposalItemModel.setFile(rs.getString("file"));
                    proposalItemModel.setIcon(rs.getString("icon"));
                    proposalItemModel.setDescription(rs.getString("description"));
                    proposalItemModel.setHyperlink(rs.getString("hyperlink"));

                    proposalItemModel.setTerm(rs.getInt("term"));
                    proposalItemModel.setTermWorkDay(PortalNumberUtils.intToBoolean(rs.getInt("term_work_day")));

                    Classifier responsability = new Classifier();
                    responsability.setId(rs.getInt("resp_cla_id"));
                    responsability.setLabel(rs.getString("resp_label"));
                    responsability.setValue(rs.getString("resp_value"));
                    responsability.setType(rs.getString("resp_type"));
                    proposalItemModel.setResponsability(responsability);

                    proposalItemModel.setPrice(rs.getDouble("price"));
                    proposalItemModel.setPciId(rs.getInt("pci_id"));
                    proposalItemModel.setPrlId(rs.getInt("prl_id"));
                    proposalItemModel.setIttId(rs.getInt("itt_id"));

                    String nameItemType = rs.getString("name_item_type");

                    if (mapProposalItem.containsKey(nameItemType)) {
                        mapProposalItem.get(nameItemType).add(proposalItemModel);
                    } else {
                        List<ProposalItem> lProposalItemModel = new ArrayList<ProposalItem>();
                        lProposalItemModel.add(proposalItemModel);
                        mapProposalItem.put(nameItemType, lProposalItemModel);
                    }
                }

                mapProposalItem.forEach((nameItemType, proposalItem) -> {
                    ProposalItemType proposalItemType = new ProposalItemType();
                    proposalItemType.setNameItemType(nameItemType);
                    proposalItemType.setProposalItems(proposalItem);
                    plistItemType.add(proposalItemType);
                });

                return plistItemType;
            });

            return listProposalItemType;

        } catch (Exception e) {
            log.error("Erro ao listar os itens com precos.", e);
            throw new AppException("Erro ao listar os itens com precos.", e);
        }
    }

    @Override
    public List<Seller> getlistInternalSeller() throws AppException {

        String query = "select s.sel_id, per.per_id, per.name, usr.usr_id from seller s "
                + "inner join person per on per.per_id = s.per_id "
                + "inner join user AS usr ON per.per_id = usr.per_id "
                + "inner join job j on j.job_id = s.job_id and UPPER(j.name) = 'VENDA INTERNA' "
                + " order by per.name";

        log.trace("[QUERY] proposalform.getlistSellerByExecutive: {}", query);

        return this.getJdbcTemplatePortal().query(query, (ResultSet rs) -> {

            List<Seller> qList = new ArrayList<Seller>();

            while (rs.next()) {

                Seller seller = new Seller();

                Person person = new Person();
                person.setId(rs.getInt("per_id"));
                person.setName(rs.getString("name"));

                seller.setId(rs.getInt("sel_id"));
                seller.setPerson(person);
                seller.setUser(new UserModel(rs.getInt("usr_id")));

                qList.add(seller);
            }

            return qList;
        });
    }

    @Override
    public List<Seller> getListExecutiveByAgent(Integer userId) throws AppException {

        List<Seller> sellerList = new ArrayList<Seller>();

        String query = "select s.sel_id, per.per_id, per.name from seller s "
                + "inner join person per on per.per_id = s.per_id "
                + "inner join user AS usr ON per.per_id = usr.per_id "
                + "inner join seller_partner sp on sp.sel_id = s.sel_id "
                + "inner join seller_agent sa on sa.sel_id = s.sel_id "
                + "inner join seller a on sa.agent_sel_id = a.sel_id "
                + "inner join person a_per on a_per.per_id = a.per_id "
                + "inner join user AS a_usr ON a_per.per_id = a_usr.per_id "
                + "where a_usr.usr_id = :userId "
                // + "	sp.ptn_id = :partnerId "
                + " order by per.name";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);

        log.trace("[QUERY] proposalform.getListExecutiveByAgent: {} [PARAMS]: {}", query, params.getValues());

        sellerList = this.getJdbcTemplatePortal().query(query, params, (ResultSet rs) -> {

            List<Seller> qList = new ArrayList<Seller>();

            while (rs.next()) {

                Seller seller = new Seller();

                Person person = new Person();
                person.setId(rs.getInt("per_id"));
                person.setName(rs.getString("name"));

                seller.setId(rs.getInt("sel_id"));
                seller.setPerson(person);

                qList.add(seller);
            }

            return qList;
        });

        return sellerList;
    }

    @Override
    public List<Seller> getListExecutiveBySalesTeam(Integer userId) throws AppException {

        List<Seller> sellerList = new ArrayList<Seller>();

        String query = "select s.sel_id, per.per_id, per.name from seller s "
                + "inner join person per on per.per_id = s.per_id "
                + "inner join user AS usr ON per.per_id = usr.per_id "
                + "inner join seller_partner sp on sp.sel_id = s.sel_id "
                + "inner join sales_team_seller sts on sts.sel_id = s.sel_id "
                + "where "
                + "	sts.slt_id in ("
                + "		select distinct slt_id from sales_team_seller sub_sts "
                + "		inner join seller sub_s on sub_s.sel_id = sub_sts.sel_id "
                + "		inner join person sub_per on sub_per.per_id = sub_s.per_id "
                + "		inner join user sub_usr ON sub_per.per_id = sub_usr.per_id and sub_usr.usr_id = :userId "
                + ") "
                + " order by per.name";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);

        log.trace("[QUERY] proposalform.getListExecutiveBySalesTeam: {} [PARAMS]: {}", query, params.getValues());

        sellerList = this.getJdbcTemplatePortal().query(query, params, (ResultSet rs) -> {

            List<Seller> qList = new ArrayList<Seller>();

            while (rs.next()) {

                Seller seller = new Seller();

                Person person = new Person();
                person.setId(rs.getInt("per_id"));
                person.setName(rs.getString("name"));

                seller.setId(rs.getInt("sel_id"));
                seller.setPerson(person);

                qList.add(seller);
            }

            return qList;
        });

        return sellerList;
    }

    @Override
    public List<Partner> getListPartnerByChannelAndSeller(Integer channelId, Integer sellerId) throws AppException {
        try {

            String query = "SELECT  ptn.*, " +
                    "sit.cla_id as sit_cla_id," +
                    "sit.value as sit_cla_value," +
                    "sit.type as sit_cla_type," +
                    "sit.label as sit_cla_label," +
                    "chn.name AS chn_name, " +
                    "chn.active AS chn_active, " +
                    "per.name AS per_name, " +
                    "per.job_title AS per_job_title, " +
                    "per.cpf AS per_cpf, " +
                    "per.cnpj AS per_cnpj, " +
                    "per.rg AS per_rg, " +
                    "per.rne AS per_rne, " +
                    "typ.cla_id as per_cla_id," +
                    "typ.value as per_cla_value," +
                    "typ.type as per_cla_type," +
                    "typ.label as per_cla_label," +
                    "per.add_id AS per_add_id, " +
                    "0 AS ptg_id, " +
                    "null AS ptg_name " +
                    "FROM partner ptn " +
                    "INNER JOIN classifier sit ON ptn.situation_cla = sit.cla_id " +
                    "INNER JOIN person per ON per.per_id = ptn.entity_per_id " +
                    "INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " +
                    "INNER JOIN channel chn ON chn.chn_id = ptn.chn_id " +
                    "INNER JOIN seller_partner sp ON ptn.ptn_id = sp.ptn_id " +
                    "WHERE ptn.chn_id = :channelId " +
                    "AND sp.sel_id = :sellerId " +
                    "ORDER BY per.name ASC";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("channelId", channelId);
            params.addValue("sellerId", sellerId);

            log.trace("[QUERY] proposalform.getListPartnerByChannelAndSeller: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query, params, new PartnerMapper());

        } catch (Exception e) {
            log.error("Erro ao consultar o parceiro.", e);
            throw new AppException("Erro ao consultar o parceiro.", e);
        }
    }
}
