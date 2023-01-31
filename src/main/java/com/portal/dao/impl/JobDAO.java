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
import com.portal.dao.IJobDAO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.mapper.JobMapper;
import com.portal.model.Job;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class JobDAO extends BaseDAO implements IJobDAO {

    @Override
    public Optional<Job> find(Job model) throws AppException {
        List<Job> models = this.find(model, null);
        return Optional.ofNullable((models != null ? models.get(0) : null));
    }

    @Override
    public List<Job> find(Job model, Pageable pageable) throws AppException {
        try {

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "job_id");
            }

            Sort.Order order = Sort.Order.asc("job_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT * ");
            query.append("FROM " + schemaName + "job AS job   ");
            query.append("WHERE true ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND job.job_id = :id ");
                    params.addValue("id", model.getId());
                }

                if (model.getName() != null && !model.getName().equals("")) {
                    query.append(" AND job.name = :name ");
                    params.addValue("name", model.getName());
                }

                if (model.getLevel() != null && model.getLevel() > 0) {
                    query.append(" AND job.level = :level ");
                    params.addValue("level", model.getLevel());
                }
            }

            query.append("ORDER BY job.name " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            log.trace("[QUERY] job.find: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new JobMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar os cargos.", e);
            throw new AppException("Erro ao buscar os cargos.", e);
        }
    }

    @Override
    public Optional<Job> getById(Integer id) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT * ");
            query.append("FROM " + schemaName + "job AS job ");
            query.append("WHERE job.job_id = :id ");
            query.append("LIMIT 1");

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] job.getById: {} [PARAMS]: {}", query, params.getValues());

            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query.toString(), params, new JobMapper()));

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        } catch (Exception e) {
            log.error("Erro ao consultar um cargo.", e);
            throw new AppException("Erro ao consultar um cargo.", e);
        }
    }

    @Override
    public List<Job> list() throws AppException {
        return this.listAll(null);
    }

    @Override
    public List<Job> listAll(Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "job_id");
            }

            Sort.Order order = Sort.Order.desc("job_id");
            if (pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT * ");
            query.append("FROM " + schemaName + "job AS job   ");
            query.append("ORDER BY job.name " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            log.trace("[QUERY] job.listAll: {}", query);

            return this.getJdbcTemplatePortal().query(query.toString(), new JobMapper());

        } catch (Exception e) {
            log.error("Erro ao listar cargo.", e);
            throw new AppException("Erro ao listar cargo.", e);
        }
    }

    @Override
    public List<Job> search(Job model) throws AppException {
        return this.search(model, null);
    }

    @Override
    public List<Job> search(Job model, Pageable pageable) throws AppException {
        try {

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "job_id");
            }

            Sort.Order order = Sort.Order.asc("job_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT * ");
            query.append("FROM " + schemaName + "job AS job   ");
            query.append("WHERE true ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND job.job_id = :id ");
                    params.addValue("id", model.getId());
                }

                if (model.getName() != null && !model.getName().equals("")) {
                    query.append(" AND job.name = :name ");
                    params.addValue("name", model.getName());
                }

                if (model.getLevel() != null && model.getLevel() > 0) {
                    query.append(" AND job.level = :level ");
                    params.addValue("level", model.getLevel());
                }
            }

            query.append("ORDER BY job.name " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            log.trace("[QUERY] job.search: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new JobMapper());

        } catch (Exception e) {
            log.error("Erro ao procurar cargos.", e);
            throw new AppException("Erro ao procurar cargos.", e);
        }
    }

    @Override
    public List<Job> searchForm(String text, Pageable pageable) throws AppException, BusException {
        try {

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "job_id");
            }

            Sort.Order order = Sort.Order.asc("job_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT * ");
            query.append("FROM " + schemaName + "job AS job ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (text != null && !text.equals("")) {
                query.append("WHERE job.name like :text ");
                params.addValue("text", this.mapLike(text));
            }

            query.append("ORDER BY job.name " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            log.trace("[QUERY] Job.searchForm: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new JobMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar job.", e);
            throw new AppException("Erro ao buscar job.", e);
        }
    }

    @Override
    public Optional<Job> save(Job model) throws AppException {
        try {

            String query = "INSERT INTO job( name, level) VALUES (:name, :level )";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("name", model.getName());
            params.addValue("level", model.getLevel());

            log.trace("[QUERY] job.save {} [PARAMS] :{}", query, params.getValues());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            this.getJdbcTemplatePortal().update(query, params, keyHolder);

            model.setId(this.getKey(keyHolder));

            return Optional.ofNullable(model);

        } catch (Exception e) {
            log.error("Erro ao tentar salvar um cargo: {}", model, e);
            throw new AppException("Erro ao tentar salvar um cargo.", e);
        }
    }

    @Override
    public Optional<Job> update(Job model) throws AppException {
        try {
            String query = "UPDATE job SET name = :name, level = :level WHERE job_id = :jobId";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("name", model.getName());
            params.addValue("level", model.getLevel());
            params.addValue("jobId", model.getId());

            log.trace("[QUERY] job.update {} [PARAMS] :{}", query, params.getValues());

            this.getJdbcTemplatePortal().update(query, params);

            return Optional.ofNullable(model);

        } catch (Exception e) {
            log.error("Erro ao tentar atualizar o cargo: {}", model, e);
            throw new AppException("Erro ao tentar atualizar o cargo.", e);
        }
    }

    @Override
    public void delete(Integer id) throws AppException {
        try {
            String query = "DELETE FROM job WHERE job_id = :id";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUE1RY] job.delete: {} [PARAMS]: {}", query, params.getValues());

            this.getJdbcTemplatePortal().update(query, params);

        } catch (Exception e) {
            log.error("Erro ao excluir o cargo .", e);
            throw new AppException("Erro ao excluir o cargo .", e);
        }
    }

}
