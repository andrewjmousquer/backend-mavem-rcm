package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.ISalesTeamDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.SalesTeamMapper;
import com.portal.model.SalesTeam;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SalesTeamDAO extends BaseDAO implements ISalesTeamDAO {

    @Override
    public Optional<SalesTeam> find(SalesTeam model) throws AppException {
        List<SalesTeam> salesTeams = this.find(model, null);
        return Optional.ofNullable(salesTeams != null ? salesTeams.get(0) : null);
    }

    @Override
    public Optional<SalesTeam> getById(Integer id) throws AppException {
        try {
            String query = "SELECT * FROM sales_team slt WHERE slt.slt_id = :id LIMIT 1";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] sales_team.getById: {} [PARAMS] : {}", query, params.getValues());

            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query, params, new SalesTeamMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.trace("Erro ao consultar uma celula de venda.", e);
            throw new AppException("Erro ao consultar uma celula de venda.", e);
        }
    }

    @Override
    public List<SalesTeam> list() throws AppException {
        return this.listAll(null);
    }

    @Override
    public List<SalesTeam> search(SalesTeam model) throws AppException {
        return this.search(model, null);
    }

    @Override
    public Optional<SalesTeam> save(SalesTeam model) throws AppException {
        try {
            String query = "INSERT INTO sales_team (name) VALUES (:name)";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("name", model.getName().toUpperCase());

            log.trace("[QUERY] sales_team.save {} [PARAMS] : {}", query, params.getValues());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            this.getJdbcTemplatePortal().update(query, params, keyHolder);

            model.setId(this.getKey(keyHolder));

            return Optional.ofNullable(model);
        } catch (Exception e) {
            log.error("Erro ao tentar salvar sales_team: {}", model, e);
            throw new AppException("Erro ao tentar salvar sales_team.", e);
        }
    }

    @Override
    public Optional<SalesTeam> update(SalesTeam model) throws AppException {
        try {
            String query = "UPDATE sales_team SET name = :name WHERE slt_id = :id";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("name", model.getName().toUpperCase());
            params.addValue("id", model.getId());

            this.getJdbcTemplatePortal().update(query, params);
            return Optional.ofNullable(model);
        } catch (Exception e) {
            log.error("Erro ao tentar atualizar a celula de venda : {}", model, e);
            throw new AppException("Erro ao tentar atualizar a celula de venda.", e);
        }
    }

    @Override
    public void delete(Integer id) throws AppException {
        try {
            String query = "DELETE FROM sales_team WHERE slt_id = :id";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            log.trace("[QUERY] sales_team.delete : {} [PARAMS] : {}", query, params.getValues());
            this.getJdbcTemplatePortal().update(query, params);
        } catch (Exception e) {
            log.error("Erro ao tentar excluir.", e);
            throw new AppException("Erro ao excluir.", e);
        }

    }

    @Override
    public Optional<SalesTeam> findBySeller(Integer id) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT slt.slt_id, slt.name FROM sales_team slt " +
                    "INNER JOIN sales_team_seller sts ON sts.slt_id = slt.slt_id " +
                    "INNER JOIN seller sel ON sts.sel_id = sel.sel_id " +
                    "WHERE sel.sel_id = :id");
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] sales_tem.getBySeller: {} [PARAMS]: {}", query, params.getValues());
            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query.toString(), params, new SalesTeamMapper()));

        } catch (Exception e) {
            log.error("Erro ao buscar celula de venda pelo seller.", e);
            throw new AppException("Erro ao buscar celula de venda pelo seller.", e);
        }
    }

    @Override
    public List<SalesTeam> listAll(Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "slt_id");
            }
            Sort.Order order = Sort.Order.desc("slt_id");
            if (pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }
            String query = "SELECT * FROM sales_team " +
                    "ORDER BY slt_id " + order.getDirection().name() + " " +
                    "LIMIT " + pageable.getPageSize() + " " +
                    "OFFSET " + pageable.getPageNumber();

            log.trace("[QUERY] sales_team.listAll: {}", query);
            return this.getJdbcTemplatePortal().query(query, new SalesTeamMapper());
        } catch (Exception e) {
            log.error("Erro ao tentar listar celula de venda", e);
            throw new AppException("Erro ao listar celula de venda. ", e);
        }
    }

    @Override
    public List<SalesTeam> search(SalesTeam model, Pageable pageable) throws AppException {
        try {
            boolean hasFilter = false;

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "slt_id");
            }

            Sort.Order order = Sort.Order.asc("slt_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();

            query.append("SELECT * FROM sales_team slt WHERE :hasFilter ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND slt.slt_id = :id");
                    params.addValue("id", model.getId());
                    hasFilter = true;
                }
                
                if (model.getName() != null && !model.getName().equals("")) {
                    query.append(" AND slt.name = :name ");
                    params.addValue("name", model.getName());
                    hasFilter = true;
                }
            }

            query.append("ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            params.addValue("hasFilter", PortalNumberUtils.booleanToInt(hasFilter));

            log.trace("[QUERY] sales_team.search: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new SalesTeamMapper());
        } catch (Exception e) {
            log.error("Erro ao procurar celula de venda", e);
            throw new AppException("Erro ao procurar celular de venda.", e);
        }
    }

    @Override
    public List<SalesTeam> find(SalesTeam model, Pageable pageable) throws AppException {
        try {
            boolean hasFilter = false;
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "slt_id");
            }

            Sort.Order order = Sort.Order.asc("slt_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();

            query.append("SELECT * FROM sales_team slt WHERE :hasFilter ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND slt.slt_id = :id ");
                    params.addValue("id", model.getId());
                    hasFilter = true;
                }
                
                if (model.getName() != null && !model.getName().equals("")) {
                    query.append(" AND slt.name like :name ");
                    params.addValue("name", this.mapLike(model.getName()));
                    hasFilter = true;
                }
            }
            params.addValue("hasFilter", PortalNumberUtils.booleanToInt(hasFilter));

            query.append("ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            log.trace("[QUERY] sales_team.find {} [PARAMS] : {}", query, params.getValues());
            return this.getJdbcTemplatePortal().query(query.toString(), params, new SalesTeamMapper());
        } catch (Exception e) {
            log.error("Erro ao buscar celula de venda.", e);
            throw new AppException("Erro ao buscar celula de venda.", e);
        }
    }

    @Override
    public List<SalesTeam> searchForm(String searchText, Pageable pageable) throws AppException {

        try {
            boolean hasFilter = false;

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "slt_id");
            }

            Sort.Order order = Sort.Order.asc("slt_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();

            query.append("SELECT  slt.slt_id, slt.name ");
            query.append("FROM sales_team slt ");
            query.append("WHERE :hasFilter ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (searchText != null) {
                query.append(" and slt.name like :text ");
                params.addValue("text", this.mapLike(searchText));
            }

            query.append("ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            params.addValue("hasFilter", PortalNumberUtils.booleanToInt(hasFilter));

            log.trace("[QUERY] sales_team.find: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new SalesTeamMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar os celula de venda.", e);
            throw new AppException("Erro ao buscar os celulas de venda.", e);
        }
    }
}