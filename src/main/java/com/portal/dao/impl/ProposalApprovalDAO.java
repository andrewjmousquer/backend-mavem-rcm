package com.portal.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IProposalApprovalDAO;
import com.portal.dto.ProposalApprovalDetailDTO;
import com.portal.dto.ProposalApprovalFilterDTO;
import com.portal.dto.ProposalApprovalListDTO;
import com.portal.enums.ProposalState;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProposalApprovalDetailMapper;
import com.portal.mapper.ProposalApprovalFilterMapper;
import com.portal.mapper.ProposalApprovalMapper;
import com.portal.model.ProposalApproval;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalApprovalDAO extends BaseDAO implements IProposalApprovalDAO {

    private final String CREATE_DATE = "creationDate";


    @Override
    public Optional<ProposalApproval> find(ProposalApproval model) throws AppException {
        List<ProposalApproval> models = this.find(model, null);
        return Optional.ofNullable((models != null ? models.get(0) : null));
    }

    @Override
    public Optional<ProposalApproval> getById(Integer id) throws AppException {
        return Optional.empty();
    }

    @Override
    public List<ProposalApproval> find(ProposalApproval model, Pageable pageable) throws AppException {
        try {

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "pps_id");
            }

            Sort.Order order = Sort.Order.asc("pps_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("pps.pps_id as pps_id, ");
            query.append("pps.num as pps_num, ");
            query.append("pps.led_id  as led_id, ");
            query.append("per.per_id, ");
            query.append("per.name, ");
            query.append("per.job_title, ");
            query.append("per.cpf, ");
            query.append("per.rg, ");
            query.append("per.cnpj, ");
            query.append("per.rne, ");
            query.append("per.birthdate, ");
            query.append("per.add_id, ");
            query.append("per.classification_cla_id, ");
            query.append("cla.cla_id, ");
            query.append("apr.date AS date, ");
            query.append("apr.comment AS comment, ");
            query.append("sel.sel_id as seller_id, ");
            query.append("pdv.pdv_id, ");
            query.append("pdv.vhe_id, ");
            query.append("pdv.product_final_price, ");
            query.append("mdl.mdl_id as mdl_id, ");
            query.append("mdl.name as mdl_name, ");
            query.append("brd.brd_id as brd_id, ");
            query.append("brd.name  as brd_name, ");
            query.append("vhe.vhe_id as vhe_id ");
            query.append("FROM " + schemaName + "proposal_approval AS apr ");
            query.append("INNER JOIN " + schemaName + "proposal AS pps ON apr.pps_id = pps.pps_id ");
            query.append("INNER JOIN " + schemaName + "person AS per ON apr.per_id = per.per_id ");
            query.append("INNER JOIN " + schemaName + "classifier as cla ON apr.status_cla_id = cla.cla_id ");
            query.append("INNER JOIN " + schemaName + "proposal_detail AS ppd ON ppd.pps_id = pps.pps_id ");
            query.append("INNER JOIN " + schemaName + "seller AS sel on ppd.sel_id = sel.sel_id ");
            query.append("INNER JOIN " + schemaName + "proposal_detail_vehicle AS pdv on ppd.ppd_id = pdv.ppd_id ");
            query.append("LEFT JOIN " + schemaName + "vehicle as vhe  on pdv.vhe_id = vhe.vhe_id ");
            query.append("INNER JOIN " + schemaName + "model as mdl on vhe.mdl_id = pdv.mdl_id ");
            query.append("INNER JOIN " + schemaName + "brand as brd on mdl.brd_id = brd.brd_id ");
            query.append("WHERE pps.status_cla_id = :status ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            params.addValue("status", model.getStatus().getType().getId());

            if (model != null) {
                if (model.getProposal() != null && model.getProposal().getId() > 0) {
                    query.append(" AND apr.pps_id = :ppsId ");
                    params.addValue("ppsId", model.getProposal().getId());
                }

                if (model.getPerson() != null && model.getPerson().getId() > 0) {
                    query.append(" AND apr.per_id = :perId ");
                    params.addValue("perId", model.getPerson().getId());
                }

                if (model.getDate() != null) {
                    query.append(" AND apr.date = :date ");
                    params.addValue("date", PortalTimeUtils.localDateTimeFormat(model.getDate(), "yyyy-MM-dd HH:mm:ss"));
                }

                if (model.getStatus() != null) {
                    query.append("AND apr.status_cla_id  = :claID");
                    params.addValue("claID", model.getStatus().getType().getId());
                }

                if (model.getComment() != null) {
                    query.append("AND apr.comment  = :comment");
                    params.addValue("comment", model.getComment());
                }
            }

            query.append("ORDER BY apr.pps_id " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            log.trace("[QUERY] proposal_approval.find: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new ProposalApprovalMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar as aprovações comerciais.", e);
            throw new AppException("Erro ao buscar as aprovações comercias.", e);
        }
    }

    @Override
    public Optional<ProposalApprovalDetailDTO> getByIdProposalAppoval(Integer id) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("prc.per_id as client_id, ");
            query.append("prc.name as client_name, ");
            query.append("prc.cpf as client_cpf, ");
            query.append("prc.cnpj as client_cnpj, ");
            query.append("prc.rg as client_rg, ");
            query.append("prc.rne as clien_rne, ");
            query.append("prc.classification_cla_id , ");
            query.append("chn.chn_id as channel_id, ");
            query.append("chn.name as channel_name, ");
            query.append("rsk.cla_id as risk_id, ");
            query.append("rsk.value as risk_value, ");
            query.append("rsk.type as risk_type, ");
            query.append("pps.pps_id as pps_id, ");
            query.append("pps.num as pps_num, ");
            query.append("pps.create_date as pps_create_date, ");
            query.append("slo.order_number as order_number, ");
            query.append("ptn.ptn_id as ptn_id, ");
            query.append("pptn.per_id as pptn_id, ");
            query.append("pptn.name as pptn_name, ");
            query.append("psel.per_id as seller_person_id, ");
            query.append("psel.name as seller_person_name, ");
            query.append("sel.sel_id as sel_id, ");
            query.append("mdl.mdl_id as mdl_id, ");
            query.append("mdl.name as mdl_name, ");
            query.append("brd.brd_id as brd_id, ");
            query.append("brd.name as brd_name, ");
            query.append("pdv.pdv_id, ");
            query.append("pdv.total_amount, ");
            query.append("vhe.vhe_id as vhe_id, ");
            query.append("vhe.model_year, ");
            query.append("vhe.plate , ");
            query.append("vhe.chassi , ");
            query.append("vhe.purchase_date , ");
            query.append("vhe.purchase_value ");
            query.append("FROM " + schemaName + "proposal AS pps ");
            query.append("LEFT JOIN sales_order slo on slo.pps_id = p.pps_id  ");
            query.append("LEFT JOIN " + schemaName + "proposal_approval AS apr ON apr.pps_id = pps.pps_id ");
            query.append("INNER JOIN " + schemaName + "proposal_person_client AS ppc ON pps.pps_id  = ppc.pps_id ");
            query.append("INNER JOIN " + schemaName + "person AS prc ON ppc.per_id  = prc.per_id ");
            query.append("INNER JOIN " + schemaName + "proposal_detail AS ppd ON pps.pps_id  = ppd.pps_id ");
            query.append("INNER JOIN " + schemaName + "proposal_detail_vehicle AS pdv on ppd.ppd_id = pdv.ppd_id ");
            query.append("INNER JOIN " + schemaName + "price_product AS ppr ON pdv.ppr_id  = ppr.ppr_id ");
            query.append("INNER JOIN " + schemaName + "price_list AS prl ON ppr.prl_id  = prl.prl_id ");
            query.append("INNER JOIN " + schemaName + "price_list_partner AS plp ON prl.prl_id  = plp.prl_id ");
            query.append("INNER JOIN " + schemaName + "partner AS ptn ON plp.ptn_id  = ptn.ptn_id ");
            query.append("INNER JOIN " + schemaName + "person AS pptn ON ptn.entity_per_id  = pptn.per_id ");
            query.append("LEFT JOIN " + schemaName + "seller_partner AS spt ON ptn.ptn_id  = spt.ptn_id ");
            query.append("LEFT JOIN " + schemaName + "seller AS sel ON spt.sel_id  = sel.sel_id ");
            query.append("LEFT JOIN " + schemaName + "person AS psel ON sel.per_id  = psel.per_id ");
            query.append("INNER JOIN " + schemaName + "vehicle AS vhe ON pdv.vhe_id  = vhe.vhe_id ");
            query.append("INNER JOIN " + schemaName + "model AS mdl ON vhe.mdl_id  = pdv.mdl_id ");
            query.append("INNER JOIN " + schemaName + "brand AS brd ON mdl.brd_id  = brd.brd_id ");
            query.append("INNER JOIN " + schemaName + "classifier AS rsk ON pps.risk_cla_id  = rsk.cla_id ");
            query.append("INNER JOIN " + schemaName + "channel AS chn ON chn.chn_id  = ppd.chn_id ");
            query.append("WHERE pps.pps_id = :id and pps.status_cla_id = :status  ");
            query.append("LIMIT 1");


            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            params.addValue("status", 62);


            log.trace("[QUERY] proposal_approval.getById: {} [PARAMS]: {}", query, params.getValues());

            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query.toString(), params, new ProposalApprovalDetailMapper()));

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        } catch (Exception e) {
            log.error("Erro ao consultar uma aprovação comercial", e);
            throw new AppException("Erro ao consultar uma aprovação comercial", e);
        }
    }

    @Override
    public List<ProposalApproval> list() throws AppException {
        return null;
    }

    @Override
    public List<ProposalApprovalListDTO> listAll(Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "pps_id");
            }

            Sort.Order order = Sort.Order.desc("pps_id");
            if (pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            String query = "select   "
                    + "	   p.pps_id,  "
                    + "    p.status_cla_id,  "
                    + "    IFNULL(per.name, '-') as `client`,  "
                    + "	   p.num as num_proposta,  "
                    + "	   p.proposal_number,  "
                    + "    p.cod as cod,  "
                    + "	   pdv.pdv_id ,"
                    + "	   pdv.price_discount_amount, "
                    + "	   pdv.product_amount_discount, "
                    + "    slo.order_number, "
                    + "    ifnull( ent_per.name, '' ) as partner,  "
                    + "    sel_pd.sel_id as seller_id, "
                    + "    per_pd.per_id as seller_per_id, "
                    + "    per_pd.name as seller_name, "
                    + "    concat(m.name, \" | \" ,b.name) as brandModel,  "
                    + "    p.create_date,  "
                    + "    p.validity_date, "
                    + "	   pdv.total_amount as total_price "
                    + "from proposal p  "
                    + "left join sales_order slo on slo.pps_id = p.pps_id  "
                    + "left join proposal_person_client ppc on ppc.pps_id = p.pps_id "
                    + " 	and ppc.customer_cla_id = (SELECT cla_id FROM classifier where type = 'CUSTOMER_TYPE' AND value = 'FINANCIADOR') "
                    + "left join person per on per.per_id = ppc.per_id  "
                    + "inner join proposal_detail pd on pd.pps_id = p.pps_id  "
                    + "inner join seller sel_pd on pd.sel_id = sel_pd.sel_id  "
                    + "inner join person per_pd on sel_pd.per_id = per_pd.per_id "
                    + "inner join proposal_detail_vehicle pdv on pdv.ppd_id = pd.ppd_id  "
                    + "inner join price_product pp on pp.ppr_id = pdv.ppr_id "
                    + "left join vehicle v on v.vhe_id = pdv.vhe_id  "
                    + "left join model m on m.mdl_id = pdv.mdl_id "
                    + "left join brand b on b.brd_id = m.brd_id "
                    + "left join partner par on par.ptn_id = pd.ptn_id  "
                    + "left join person ent_per ON ent_per.per_id = par.entity_per_id  "
                    + "left join partner_group parg ON parg.ptg_id = par.ptg_id "
                    + "where p.status_cla_id = :status ";
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            ProposalState proposalState = ProposalState.getByValue("IN_COMMERCIAL_APPROVAL");
            params.addValue("status", proposalState.getType().getId());

            log.trace("[QUERY] proposal_approval.listAll: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new ProposalApprovalFilterMapper());

        } catch (Exception e) {
            log.error("Erro ao listar uma aprovação comercial.", e);
            throw new AppException("Erro ao listar uma aprovação comercial.", e);
        }
    }

    @Override
    public List<ProposalApproval> search(ProposalApproval model) throws AppException {
        return null;
    }

    @Override
    public List<ProposalApprovalListDTO> search(ProposalApprovalFilterDTO dto, Integer proposalDaysLimit, Pageable pageable) throws AppException {
        try {

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "pps_id");
            }

            Sort.Order order = Sort.Order.asc("pps_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            String query = "select   "
                    + "	   p.pps_id,  "
                    + "    p.status_cla_id,  "
                    + "    IFNULL(per.name, '-') as `client`,  "
                    + "	   p.num as num_proposta,  "
                    + "	   p.proposal_number,  "
                    + "    p.cod as cod,  "
                    + "	   pdv.pdv_id ,"
                    + "	   pdv.price_discount_amount, "
                    + "	   pdv.product_amount_discount, "
                    + "    slo.order_number, "
                    + "    ifnull( ent_per.name, '' ) as partner,  "
                    + "    sel_pd.sel_id as seller_id, "
                    + "    per_pd.per_id as seller_per_id, "
                    + "    per_pd.name as seller_name, "
                    + "    concat(m.name, \" | \" ,b.name) as brandModel,  "
                    + "    p.create_date,  "
                    + "    p.validity_date, "
                    + "	   pdv.total_amount as total_price "
                    + "from proposal p  "
                    + "left join sales_order slo on slo.pps_id = p.pps_id  "
                    + "left join proposal_person_client ppc on ppc.pps_id = p.pps_id "
                    + " 	and ppc.customer_cla_id = (SELECT cla_id FROM classifier where type = 'CUSTOMER_TYPE' AND value = 'FINANCIADOR') "
                    + "left join person per on per.per_id = ppc.per_id  "
                    + "inner join proposal_detail pd on pd.pps_id = p.pps_id  "
                    + "inner join seller sel_pd on pd.sel_id = sel_pd.sel_id  "
                    + "inner join person per_pd on sel_pd.per_id = per_pd.per_id "
                    + "inner join proposal_detail_vehicle pdv on pdv.ppd_id = pd.ppd_id  "
                    + "inner join price_product pp on pp.ppr_id = pdv.ppr_id "
                    + "left join vehicle v on v.vhe_id = pdv.vhe_id  "
                    + "left join model m on m.mdl_id = pdv.mdl_id "
                    + "left join brand b on b.brd_id = m.brd_id "
                    + "left join partner par on par.ptn_id = pd.ptn_id  "
                    + "left join person ent_per ON ent_per.per_id = par.entity_per_id  "
                    + "left join partner_group parg ON parg.ptg_id = par.ptg_id "
                    + "where p.status_cla_id = :status ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("status", dto.getStatus().getType().getId());

            if (dto != null) {

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

                if (dto.getProposalNum() != null) {

                    query += " and p.num = :num ";
                    params.addValue("num", dto.getProposalNum());
                }

                if (dto.getOrderNum() != null && !dto.getOrderNum().isEmpty()) {

                    query += " and slo.order_number = :ordernum ";
                    params.addValue("ordernum", dto.getOrderNum());
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
            }

            log.trace("[QUERY] proposal_approval.search: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new ProposalApprovalFilterMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar as aprovações comerciais.", e);
            throw new AppException("Erro ao buscar as aprovações comercias.", e);
        }
    }

    @Override
    public Optional<ProposalApproval> save(ProposalApproval model) throws AppException {
        try {

            String query = "INSERT INTO proposal_approval ( pps_id, per_id, date, status_cla_id, comment ) VALUES (:ppsId, :perId, :date, :statusId, :comment )";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("ppsId", model.getProposal().getId());
            params.addValue("perId", model.getPerson().getId());
            params.addValue("date", PortalTimeUtils.localDateTimeFormat(model.getDate(), "yyyy-MM-dd HH:mm:ss"));
            params.addValue("statusId", model.getStatus().getType().getId());
            params.addValue("comment", model.getComment());


            log.trace("[QUERY] proposal_approval.save {} [PARAMS] :{}", query, params.getValues());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            this.getJdbcTemplatePortal().update(query, params, keyHolder);

            return Optional.ofNullable(model);

        } catch (Exception e) {
            log.error("Erro ao tentar salvar uma aprovação comercial: {}", model, e);
            throw new AppException("Erro ao tentar salvar umaaprovação comercial.", e);
        }
    }

    @Override
    public Optional<ProposalApproval> update(ProposalApproval model) throws AppException {
        try {
            String query = "UPDATE proposal_approval " +
                    "SET pps_id = :ppsId, per_id = :perId, date = :date, status_cla_id = :statusId, comment = :comment " +
                    "WHERE pps_id = :ppsId ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("perId", model.getPerson().getId());
            params.addValue("date", PortalTimeUtils.localDateTimeFormat(model.getDate(), "yyyy-MM-dd HH:mm:ss"));
            params.addValue("statusId", model.getStatus().getType().getId());
            params.addValue("comment", model.getComment());
            params.addValue("ppsId", model.getProposal().getId());

            log.trace("[QUERY] proposal_approval.update {} [PARAMS] :{}", query, params.getValues());

            this.getJdbcTemplatePortal().update(query, params);

            return Optional.ofNullable(model);

        } catch (Exception e) {
            log.error("Erro ao tentar atualizar a aprovação comercial: {}", model, e);
            throw new AppException("Erro ao tentar atualizar a aprovação comercial.", e);
        }
    }

    @Override
    public void delete(Integer id) throws AppException {
        try {
            String query = "DELETE FROM proposal_approval WHERE pps_id = :id";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUE1RY] proposal_approval.delete: {} [PARAMS]: {}", query, params.getValues());

            this.getJdbcTemplatePortal().update(query, params);

        } catch (Exception e) {
            log.error("Erro ao excluir a aprovação comercial .", e);
            throw new AppException("Erro ao excluir a aprovação comercial.", e);
        }
    }

}
