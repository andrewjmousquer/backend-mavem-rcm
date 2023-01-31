package com.portal.dao.impl;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.portal.model.SalesTeam;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.ISellerDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.SellerMapper;
import com.portal.model.Seller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SellerDAO extends BaseDAO implements ISellerDAO {

    @Override
    public List<Seller> listAll(Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "sel_id");
            }

            Sort.Order order = Sort.Order.desc("sel_id");
            if (pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("   sel.sel_id AS sel_id, ");
            query.append("   per.per_id AS per_id, ");
            query.append("   per.name AS per_name, ");
            query.append("   per.job_title AS job_title, ");
            query.append("   per.cpf, ");
            query.append("   per.rg, ");
            query.append("   per.cnpj, ");
            query.append("   per.rne, ");
            query.append("   per.add_id, ");
            query.append("   per.birthdate, ");
            query.append("	 usr.usr_id, ");
            query.append("	 usr.username, ");
            query.append("	 usr.enabled, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("   job.job_id, ");
            query.append("   job.name as job_name ");
            query.append("FROM " + schemaName + "seller AS sel   ");
            query.append("INNER JOIN " + schemaName + "person AS per ON sel.per_id = per.per_id   ");
            query.append("INNER JOIN " + schemaName + "user AS usr ON per.per_id = usr.per_id   ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("INNER JOIN " + schemaName + "job AS job ON sel.job_id = job.job_id   ");
            query.append("ORDER BY per.name " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            log.trace("[QUERY] seller.listAll: {}", query);
            return this.getJdbcTemplatePortal().query(query.toString(), new SellerMapper());

        } catch (Exception e) {
            log.error("Erro ao listar seller .", e);
            throw new AppException("Erro ao listar  seller .", e);
        }
    }

    @Override
    public List<Seller> searchForm(String text, Pageable pageable) throws AppException {
        try {

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "sel_id");
            }

            Sort.Order order = Sort.Order.asc("sel_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("   sel.sel_id AS sel_id, ");
            query.append("   per.per_id AS per_id, ");
            query.append("   per.name AS per_name, ");
            query.append("   per.job_title AS job_title, ");
            query.append("   per.cpf, ");
            query.append("   per.rg, ");
            query.append("   per.cnpj, ");
            query.append("   per.rne, ");
            query.append("   per.add_id, ");
            query.append("   per.birthdate, ");
            query.append("	 usr.usr_id, ");
            query.append("	 usr.username, ");
            query.append("	 usr.enabled, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("   job.job_id, ");
            query.append("   job.name as job_name ");
            query.append("FROM " + schemaName + "seller AS sel   ");
            query.append("INNER JOIN " + schemaName + "person AS per ON sel.per_id = per.per_id   ");
            query.append("INNER JOIN " + schemaName + "user AS usr ON per.per_id = usr.per_id   ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("INNER JOIN " + schemaName + "job AS job ON sel.job_id = job.job_id   ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (text != null && !text.equals("")) {
                query.append("WHERE UPPER(per.name) like :text ");
                query.append("      OR UPPER(job.name) like :text ");
                params.addValue("text", this.mapLike(text.toUpperCase()));
            }

            query.append("ORDER BY per.name " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            log.trace("[QUERY] Seller.searchForm: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new SellerMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar seller.", e);
            throw new AppException("Erro ao buscar seller.", e);
        }
    }

    @Override
    public Optional<Seller> find(Seller model) throws AppException {
        List<Seller> models = this.find(model, null);
        return Optional.ofNullable((models != null ? models.get(0) : null));
    }

    @Override
    public List<Seller> find(Seller model, Pageable pageable) throws AppException {
        try {

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "sel_id");
            }

            Sort.Order order = Sort.Order.asc("sel_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("   sel.sel_id AS sel_id, ");
            query.append("   per.per_id AS per_id, ");
            query.append("   per.name AS per_name, ");
            query.append("   per.job_title AS job_title, ");
            query.append("   per.cpf, ");
            query.append("   per.rg, ");
            query.append("   per.cnpj, ");
            query.append("   per.rne, ");
            query.append("   per.add_id, ");
            query.append("   per.birthdate, ");
            query.append("	 usr.usr_id, ");
            query.append("	 usr.username, ");
            query.append("	 usr.enabled, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("   job.job_id, ");
            query.append("   job.name as job_name ");
            query.append("FROM " + schemaName + "seller AS sel   ");
            query.append("INNER JOIN " + schemaName + "person AS per ON sel.per_id = per.per_id   ");
            query.append("INNER JOIN " + schemaName + "user AS usr ON per.per_id = usr.per_id   ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("INNER JOIN " + schemaName + "job AS job ON sel.job_id = job.job_id   ");
            query.append("WHERE true ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND sel.sel_id = :id ");
                    params.addValue("id", model.getId());
                }

                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND sel.per_id = :person ");
                    params.addValue("person", model.getPerson().getId());
                }

                if (model.getJob() != null && model.getJob().getId() != null && model.getJob().getId() > 0) {
                    query.append(" AND sel.job_id = :Job ");
                    params.addValue("jobId", model.getJob().getId());
                }

                if (model.getPerson() != null && !model.getPerson().getName().equals("")) {
                    query.append(" AND per.name = :name ");
                    params.addValue("name", model.getPerson().getName());
                }
            }

            query.append("ORDER BY per.name " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());


            log.trace("[QUERY] seller.find: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new SellerMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar as pessoas.", e);
            throw new AppException("Erro ao buscar as pessoas.", e);
        }


    }

    @Override
    public Optional<Seller> getById(Integer id) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("   sel.sel_id AS sel_id, ");
            query.append("   per.per_id AS per_id, ");
            query.append("   per.name AS per_name, ");
            query.append("   per.job_title AS job_title, ");
            query.append("   per.cpf, ");
            query.append("   per.rg, ");
            query.append("   per.cnpj, ");
            query.append("   per.rne, ");
            query.append("   per.add_id, ");
            query.append("   per.birthdate, ");
            query.append("	 usr.usr_id, ");
            query.append("	 usr.username, ");
            query.append("	 usr.enabled, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("   job.job_id, ");
            query.append("   job.name as job_name ");
            query.append("FROM " + schemaName + "seller AS sel ");
            query.append("INNER JOIN " + schemaName + "person AS per ON sel.per_id = per.per_id   ");
            query.append("INNER JOIN " + schemaName + "user AS usr ON per.per_id = usr.per_id   ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("INNER JOIN " + schemaName + "job AS job ON sel.job_id = job.job_id   ");
            query.append("WHERE sel.sel_id = :id ");
            query.append("LIMIT 1");

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] seller.getById: {} [PARAMS]: {}", query, params.getValues());

            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query.toString(), params, new SellerMapper()));

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        } catch (Exception e) {
            log.error("Erro ao consultar um seller.", e);
            throw new AppException("Erro ao consultar um seller.", e);
        }
    }

    @Override
    public List<Seller> getByAgent(Integer id) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("   sel.sel_id AS sel_id, ");
            query.append("   per.per_id AS per_id, ");
            query.append("   per.name AS per_name, ");
            query.append("   per.job_title AS job_title, ");
            query.append("   per.cpf, ");
            query.append("   per.rg, ");
            query.append("   per.cnpj, ");
            query.append("   per.rne, ");
            query.append("   per.add_id, ");
            query.append("   per.birthdate, ");
            query.append("	 usr.usr_id, ");
            query.append("	 usr.username, ");
            query.append("	 usr.enabled, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("   job.job_id, ");
            query.append("   job.name as job_name ");
            query.append("FROM " + schemaName + "seller AS sel ");
            query.append("INNER JOIN " + schemaName + "person AS per ON sel.per_id = per.per_id   ");
            query.append("INNER JOIN " + schemaName + "user AS usr ON per.per_id = usr.per_id   ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("INNER JOIN " + schemaName + "job AS job ON sel.job_id = job.job_id   ");
            query.append("INNER JOIN " + schemaName + "seller_agent AS agt ON sel.sel_id = agt.sel_id ");
            query.append("WHERE agt.agent_sel_id = :id ");

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] seller.getByAgent: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new SellerMapper());

        }  catch (Exception e) {
            log.error("Erro ao consultar um seller.", e);
            throw new AppException("Erro ao consultar um seller.", e);
        }
    }

    @Override
    public List<Seller> getBySalesTeam(List<SalesTeam> salesTeamList) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("   sel.sel_id AS sel_id, ");
            query.append("   per.per_id AS per_id, ");
            query.append("   per.name AS per_name, ");
            query.append("   per.job_title AS job_title, ");
            query.append("   per.cpf, ");
            query.append("   per.rg, ");
            query.append("   per.cnpj, ");
            query.append("   per.rne, ");
            query.append("   per.add_id, ");
            query.append("   per.birthdate, ");
            query.append("	 usr.usr_id, ");
            query.append("	 usr.username, ");
            query.append("	 usr.enabled, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("   job.job_id, ");
            query.append("   job.name as job_name ");
            query.append("FROM " + schemaName + "seller AS sel ");
            query.append("INNER JOIN " + schemaName + "person AS per ON sel.per_id = per.per_id   ");
            query.append("INNER JOIN " + schemaName + "user AS usr ON per.per_id = usr.per_id   ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("INNER JOIN " + schemaName + "job AS job ON sel.job_id = job.job_id   ");
            query.append("INNER JOIN " + schemaName + "sales_team_seller AS slt ON sel.sel_id = slt.sel_id ");
            query.append("WHERE slt.slt_id in ( :id )");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (salesTeamList != null) {
                if (salesTeamList.size() > 0) {
                    List<String> sellerList = new ArrayList<>();
                    salesTeamList.stream().map(item -> {
                        return sellerList.add(item.getId().toString());
                    }).collect(Collectors.toList());
                    params.addValue("id", sellerList);
                }
            }

            log.trace("[QUERY] seller.getBySalesTeam: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new SellerMapper());

        } catch (Exception e) {
            log.error("Erro ao consultar um seller.", e);
            throw new AppException("Erro ao consultar um seller.", e);
        }
    }

    @Override
    public Optional<Seller> getByUser(Integer id) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("   sel.sel_id AS sel_id, ");
            query.append("   per.per_id AS per_id, ");
            query.append("   per.name AS per_name, ");
            query.append("   per.job_title AS job_title, ");
            query.append("   per.cpf, ");
            query.append("   per.rg, ");
            query.append("   per.cnpj, ");
            query.append("   per.rne, ");
            query.append("   per.add_id, ");
            query.append("   per.birthdate, ");
            query.append("	 usr.usr_id, ");
            query.append("	 usr.username, ");
            query.append("	 usr.enabled, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("   job.job_id, ");
            query.append("   job.name as job_name ");
            query.append("FROM " + schemaName + "seller AS sel ");
            query.append("INNER JOIN " + schemaName + "person AS per ON sel.per_id = per.per_id   ");
            query.append("INNER JOIN " + schemaName + "user AS usr ON per.per_id = usr.per_id   ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("INNER JOIN " + schemaName + "job AS job ON sel.job_id = job.job_id   ");
            query.append("WHERE usr.usr_id = :id ");
            query.append("LIMIT 1");

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] seller.getByUser: {} [PARAMS]: {}", query, params.getValues());

            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query.toString(), params, new SellerMapper()));

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        } catch (Exception e) {
            log.error("Erro ao consultar um seller.", e);
            throw new AppException("Erro ao consultar um seller.", e);
        }
    }

    @Override
    public List<Seller> list() throws AppException {
        return this.listAll(null);
    }

    @Override
    public List<Seller> search(Seller model) throws AppException {
        return this.search(model, null);
    }

    @Override
    public List<Seller> search(Seller model, Pageable pageable) throws AppException {
        try {

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "sel_id");
            }

            Sort.Order order = Sort.Order.asc("sel_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("   sel.sel_id AS sel_id, ");
            query.append("   per.per_id AS per_id, ");
            query.append("   per.name AS per_name, ");
            query.append("   per.job_title AS job_title, ");
            query.append("   per.cpf, ");
            query.append("   per.rg, ");
            query.append("   per.cnpj, ");
            query.append("   per.rne, ");
            query.append("   per.add_id, ");
            query.append("   per.birthdate, ");
            query.append("	 usr.usr_id, ");
            query.append("	 usr.username, ");
            query.append("	 usr.enabled, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("   job.job_id, ");
            query.append("   job.name as job_name ");
            query.append("FROM " + schemaName + "seller AS sel   ");
            query.append("INNER JOIN " + schemaName + "person AS per ON sel.per_id = per.per_id   ");
            query.append("INNER JOIN " + schemaName + "user AS usr ON per.per_id = usr.per_id   ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("INNER JOIN " + schemaName + "job AS job ON sel.job_id = job.job_id   ");
            query.append("WHERE true ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND sel.sel_id = :id ");
                    params.addValue("id", model.getId());
                }

                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND sel.per_id = :person ");
                    params.addValue("person", model.getPerson().getId());
                }

                if (model.getJob() != null && model.getJob().getId() != null && model.getJob().getId() > 0) {
                    query.append(" AND sel.job_id = :Job ");
                    params.addValue("jobId", model.getJob().getId());
                }

                if (model.getPerson() != null && !model.getPerson().getName().equals("")) {
                    query.append(" AND per.name = :name ");
                    params.addValue("name", model.getPerson().getName());

                }
            }

            query.append("ORDER BY per.name " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            log.trace("[QUERY] seller.search: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new SellerMapper());

        } catch (Exception e) {
            log.error("Erro ao procurar seller.", e);
            throw new AppException("Erro ao procurar seller.", e);
        }

    }

    @Override
    public Optional<Seller> save(Seller model) throws AppException {
        try {

            String query = "INSERT INTO seller( per_id, job_id) VALUES (:per_id, :job_id )";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("per_id", model.getPerson().getId());
            params.addValue("job_id", model.getJob().getId());

            log.trace("[QUERY] seller.save {} [PARAMS] :{}", query, params.getValues());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            this.getJdbcTemplatePortal().update(query, params, keyHolder);

            model.setId(this.getKey(keyHolder));

            return Optional.ofNullable(model);

        } catch (Exception e) {
            log.error("Erro ao tentar salvar seller: {}", model, e);
            throw new AppException("Erro ao tentar salvar seller.", e);
        }
    }

    @Override
    public Optional<Seller> update(Seller model) throws AppException {
        try {
            String query = "UPDATE seller SET per_id = :per_id, job_id = :job_id WHERE sel_id = :selId";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("per_id", model.getPerson().getId());
            params.addValue("job_id", model.getJob().getId());
            params.addValue("selId", model.getId());

            log.trace("[QUERY] seller.update {} [PARAMS] :{}", query, params.getValues());
            this.getJdbcTemplatePortal().update(query, params);
            return Optional.ofNullable(model);
        } catch (Exception e) {
            log.error("Erro ao tentar atualizar o seller: {}", model, e);
            throw new AppException("Erro ao tentar atualizar o seller.", e);
        }
    }

    @Override
    public void delete(Integer id) throws AppException {
        try {
            String query = "DELETE FROM seller WHERE sel_id = :id";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUE1RY] seller.delete: {} [PARAMS]: {}", query, params.getValues());

            this.getJdbcTemplatePortal().update(query, params);

        } catch (Exception e) {
            log.error("Erro ao excluir .", e);
            throw new AppException("Erro ao excluir .", e);
        }
    }

    @Override
    public Integer checkSellerDocument(String text) throws AppException {

        try {

            String query = "select  "
                    + "	s.sel_id "
                    + "from person p "
                    + "inner join seller s on s.per_id = p.per_id "
                    + "where  "
                    + "	p.cpf = :text or "
                    + "    p.cnpj = :text or "
                    + "    p.rne = :text "
                    + "LIMIT 1";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("text", text);

            log.trace("[QUERY] seller.checkSellerDocument: {} [PARAMS]: {}", query, params.getValues());

            Integer sellerId = null;

            sellerId = this.getJdbcTemplatePortal().query(query, params, (ResultSet rs) -> {
                while (rs.next()) {
                    return rs.getInt("sel_id");
                }
                return null;
            });

            return sellerId;

        } catch (Exception e) {
            log.error("Erro em seller.checkSellerDocument .", e);
            throw new AppException("Erro em seller.checkSellerDocument .", e);
        }
    }


}
