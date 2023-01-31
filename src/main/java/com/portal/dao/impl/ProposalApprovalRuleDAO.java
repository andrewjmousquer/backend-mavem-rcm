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
import com.portal.dao.IProposalApprovalRuleDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProposalApprovalRuleMapper;
import com.portal.model.ProposalApprovalRule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalApprovalRuleDAO extends BaseDAO implements IProposalApprovalRuleDAO {
    
	@Override
    public Optional<ProposalApprovalRule> find(ProposalApprovalRule model) throws AppException {
        List<ProposalApprovalRule> models = this.find(model, null);
        return Optional.ofNullable(((models != null && !models.isEmpty() )? models.get(0) : null));
    }
    
    @Override
    public List<ProposalApprovalRule> find(ProposalApprovalRule model, Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "par_id");
            }
            
            Sort.Order order = Sort.Order.asc("par_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("par.par_id  as par_id, ");
            query.append("par.value  as value, ");
            query.append("job.job_id  as job_id, ");
            query.append("job.name  as job_name, ");
            query.append("job.level  as job_level ");
            query.append("FROM " + schemaName + "proposal_approval_rule AS par ");
            query.append("INNER JOIN " + schemaName + "job AS job ON par.job_id = job.job_id ");
            query.append("WHERE par_id > 0 ");
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND par.par_id = :id ");
                    params.addValue("id", model.getId());
                }
                if (model.getValue() != null && model.getValue() > 0) {
                    query.append(" AND par.value = :value ");
                    params.addValue("value", model.getValue());
                }
                if (model.getJob() != null && model.getJob().getId() > 0) {
                    query.append("AND par.job_id  = :jobId ");
                    params.addValue("jobId", model.getJob().getId());
                }
            }
            
            query.append("ORDER BY job.level " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());
            
            log.trace("[QUERY] proposal_approval_rule.find: {} [PARAMS]: {}", query, params.getValues());
            
            return this.getJdbcTemplatePortal().query(query.toString(), params, new ProposalApprovalRuleMapper());
            
        } catch (Exception e) {
            log.error("Erro ao buscar as alçadas de aprovação.", e);
            throw new AppException("Erro ao buscar as alçadas de aprovação.", e);
        }
    }
    @Override
    public Optional<ProposalApprovalRule> getById(Integer id) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("par.par_id  as par_id, ");
            query.append("par.value  as value, ");
            query.append("job.job_id  as job_id, ");
            query.append("job.name  as job_name, ");
            query.append("job.level  as job_level ");
            query.append("FROM " + schemaName + "proposal_approval_rule AS par ");
            query.append("INNER JOIN " + schemaName + "job AS job ON par.job_id = job.job_id ");
            query.append("WHERE par.par_id = :id ");
            query.append("LIMIT 1");
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            log.trace("[QUERY] proposal_approval_rule.getById: {} [PARAMS]: {}", query, params.getValues());
            
            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query.toString(), params, new ProposalApprovalRuleMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro ao consultar uma alçada de aprovação.", e);
            throw new AppException("Erro ao consultar uma alçada de aprovação.", e);
        }
    }


    @Override
    public Optional<ProposalApprovalRule> getByJob(Integer id) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("par.par_id  as par_id, ");
            query.append("par.value  as value, ");
            query.append("job.job_id  as job_id, ");
            query.append("job.name  as job_name, ");
            query.append("job.level  as job_level ");
            query.append("FROM " + schemaName + "proposal_approval_rule AS par ");
            query.append("INNER JOIN " + schemaName + "job AS job ON par.job_id = job.job_id ");
            query.append("WHERE par.job_id = :id ");
            query.append("LIMIT 1");

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            log.trace("[QUERY] proposal_approval_rule.getByJob: {} [PARAMS]: {}", query, params.getValues());

            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query.toString(), params, new ProposalApprovalRuleMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro ao consultar uma alçada de aprovação.", e);
            throw new AppException("Erro ao consultar uma alçada de aprovação.", e);
        }
    }
    
    @Override
    public List<ProposalApprovalRule> list() throws AppException {
        return this.listAll(null);
    }
    
    @Override
    public List<ProposalApprovalRule> listAll(Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "par_id");
            }
            
            Sort.Order order = Sort.Order.desc("par_id");
            if (pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }
           
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("par.par_id  as par_id, ");
            query.append("par.value  as value, ");
            query.append("job.job_id  as job_id, ");
            query.append("job.name  as job_name, ");
            query.append("job.level  as job_level ");
            query.append("FROM " + schemaName + "proposal_approval_rule AS par ");
            query.append("INNER JOIN " + schemaName + "job AS job ON par.job_id = job.job_id ");
            query.append("ORDER BY job.level " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());
            
            log.trace("[QUERY] proposal_approval_rule.listAll: {}", query);
            
            return this.getJdbcTemplatePortal().query(query.toString(), new ProposalApprovalRuleMapper());
        } catch (Exception e) {
            log.error("Erro ao listar uma alçada comercial.", e);
            throw new AppException("Erro ao listar uma alçada comercial.", e);
        }
    }
    
    @Override
    public List<ProposalApprovalRule> search(ProposalApprovalRule model) throws AppException {
        return this.search(model, null);
    }
    
    @Override
    public List<ProposalApprovalRule> search(ProposalApprovalRule model, Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "par_id");
            }
            
            Sort.Order order = Sort.Order.asc("par_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("par.par_id  as par_id, ");
            query.append("par.value  as value, ");
            query.append("job.job_id  as job_id, ");
            query.append("job.name  as job_name, ");
            query.append("job.level  as job_level ");
            query.append("FROM " + schemaName + "proposal_approval_rule AS par ");
            query.append("INNER JOIN " + schemaName + "job AS job ON par.job_id = job.job_id ");
            query.append("WHERE true ");
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND par.par_id = :id ");
                    params.addValue("id", model.getId());
                }
                if (model.getValue() != null && model.getValue() > 0) {
                    query.append(" AND par.value = :value ");
                    params.addValue("value", model.getValue());
                }
                if (model.getJob() != null && model.getJob().getId() > 0) {
                    query.append("AND par.job_id  = :jobId");
                    params.addValue("jobId", model.getJob().getId());
                }
            }
            query.append("ORDER BY job.level " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());
            
            log.trace("[QUERY] proposal_approval_rule.search: {} [PARAMS]: {}", query, params.getValues());
            
            return this.getJdbcTemplatePortal().query(query.toString(), params, new ProposalApprovalRuleMapper());
        } catch (Exception e) {
            log.error("Erro ao procurar alçadas comercial.", e);
            throw new AppException("Erro ao procurar  alçadas comercial.", e);
        }
    }
    
    @Override
    public List<ProposalApprovalRule> searchForm(String text, Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "par_id");
            }
            
            Sort.Order order = Sort.Order.asc("par_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }
            
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("par.par_id  as par_id, ");
            query.append("par.value  as value, ");
            query.append("job.job_id  as job_id, ");
            query.append("job.name  as job_name, ");
            query.append("job.level  as job_level ");
            query.append("FROM " + schemaName + "proposal_approval_rule AS par ");
            query.append("INNER JOIN " + schemaName + "job AS job ON par.job_id = job.job_id ");
           
            MapSqlParameterSource params = new MapSqlParameterSource();
            if (text != null && !text.equals("")) {
                query.append("WHERE par.value like :text OR job.name like :text ");
                params.addValue("text", this.mapLike(text));
            }
            
            query.append("ORDER BY par.par_id " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());
            
            log.trace("[QUERY] proposal_approval_rule.searchForm: {} [PARAMS]: {}", query, params.getValues());
            
            return this.getJdbcTemplatePortal().query(query.toString(), params, new ProposalApprovalRuleMapper());
        } catch (Exception e) {
            log.error("Erro ao buscar alçada comercial.", e);
            throw new AppException("Erro ao buscar alçada comercial.", e);
        }
    }
    
    @Override
    public Optional<ProposalApprovalRule> save(ProposalApprovalRule model) throws AppException {
        try {
            String query = "INSERT INTO proposal_approval_rule( value, job_id) VALUES (:value, :jobId )";
           
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("value", model.getValue());
            params.addValue("jobId", model.getJob().getId());
            
            log.trace("[QUERY] proposal_approval_rule.save {} [PARAMS] :{}", query, params.getValues());
            KeyHolder keyHolder = new GeneratedKeyHolder();
           
            this.getJdbcTemplatePortal().update(query, params, keyHolder);
            
            model.setId(this.getKey(keyHolder));
            
            return Optional.ofNullable(model);
        } catch (Exception e) {
            log.error("Erro ao tentar salvar uma alçada comercial: {}", model, e);
            throw new AppException("Erro ao tentar salvar uma alçada comercial.", e);
        }
    }
    
    @Override
    public Optional<ProposalApprovalRule> update(ProposalApprovalRule model) throws AppException {
        try {
            String query = "UPDATE proposal_approval_rule SET value = :value, job_id = :jobId WHERE par_id = :id";
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("value", model.getValue());
            params.addValue("jobId", model.getJob().getId());
            params.addValue("id", model.getId());
            
            log.trace("[QUERY] proposal_approval_rule.update {} [PARAMS] :{}", query, params.getValues());
            
            this.getJdbcTemplatePortal().update(query, params);
            return Optional.ofNullable(model);
        } catch (Exception e) {
            log.error("Erro ao tentar atualizar a alçada comercial: {}", model, e);
            throw new AppException("Erro ao tentar atualizar a alçada comercial.", e);
        }
    }
    
    @Override
    public void delete(Integer id) throws AppException {
        try {
            String query = "DELETE FROM proposal_approval_rule WHERE par_id = :id";
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            
            log.trace("[QUE1RY] proposal_approval_rule.delete: {} [PARAMS]: {}", query, params.getValues());
            
            this.getJdbcTemplatePortal().update(query, params);
        } catch (Exception e) {
            log.error("Erro ao excluir a alçada comercial .", e);
            throw new AppException("Erro ao excluir a alçada comercial .", e);
        }
    }
}