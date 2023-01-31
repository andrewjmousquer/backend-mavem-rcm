package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IPersonDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PersonMapper;
import com.portal.model.Person;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PersonDAO extends BaseDAO implements IPersonDAO {

    @Override
    public List<Person> find(Person model, Pageable pageable) throws AppException {
        try {
            boolean hasFilter = false;

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "per_id");
            }

            Order order = Order.asc("per_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();

            query.append("SELECT ");
            query.append("	 per.*, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("	 ifnull(negList.cla_id,0) as neg_list_cla_id, ");
            query.append("	 negList.value as neg_list_value, ");
            query.append("	 negList.type as neg_list_type, ");
            query.append("	 negList.label as neg_list_label ");
            query.append("FROM person per ");
            query.append("INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("LEFT JOIN classifier negList ON per.negative_list_cla_id = negList.cla_id ");

            query.append("WHERE :hasFilter ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND per.per_id = :id ");
                    params.addValue("id", model.getId());
                    hasFilter = true;
                }

                if (model.getName() != null && !model.getName().equals("")) {
                    query.append(" AND per.name = :name ");
                    params.addValue("name", model.getName());
                    hasFilter = true;
                }


                if (model.getCorporateName() != null && !model.getCorporateName().equals("")) {
                    query.append(" AND per.corporate_name = :corporateName ");
                    params.addValue("corporateName", model.getCorporateName());
                    hasFilter = true;
                }

                if (model.getJobTitle() != null && !model.getJobTitle().equals("")) {
                    query.append(" AND per.job_title = :jobTitle ");
                    params.addValue("jobTitle", model.getJobTitle());
                    hasFilter = true;
                }

                if (model.getCpf() != null && !model.getCpf().equals("")) {
                    query.append(" AND per.cpf = :cpf ");
                    params.addValue("cpf", model.getCpf());
                    hasFilter = true;
                }

                if (model.getRg() != null && !model.getRg().equals("")) {
                    query.append(" AND per.rg = :rg ");
                    params.addValue("rg", model.getRg());
                    hasFilter = true;
                }

                if (model.getIe() != null && !model.getIe().equals("")) {
                    query.append(" AND per.ie = :ie ");
                    params.addValue("ie", model.getIe());
                    hasFilter = true;
                }

                if (model.getCnpj() != null && !model.getCnpj().equals("")) {
                    query.append(" AND per.cnpj = :cnpj ");
                    params.addValue("cnpj", model.getCnpj());
                    hasFilter = true;
                }

                if (model.getRne() != null && !model.getRne().equals("")) {
                    query.append(" AND per.rne = :rne ");
                    params.addValue("rne", model.getRne());
                    hasFilter = true;
                }

                if (model.getClassification() != null) {
                    query.append(" AND per.classification_cla_id = :classification ");
                    params.addValue("classification", model.getClassification().getId());
                    hasFilter = true;
                }

                if (model.getBirthdate() != null) {
                    query.append(" AND per.birthdate = :birthdate ");
                    params.addValue("birthdate", PortalTimeUtils.localDateFormat(model.getBirthdate(), "yyyy-MM-dd"));
                    hasFilter = true;
                }
            }

            params.addValue("hasFilter", PortalNumberUtils.booleanToInt(hasFilter));

            query.append("ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());


            log.trace("[QUERY] person.find: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new PersonMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar as pessoas.", e);
            throw new AppException("Erro ao buscar as pessoas.", e);
        }
    }

    /**
     * @deprecated Usar a função {@link #find(Person, Pageable)}
     */
    @Override
    public Optional<Person> find(Person model) throws AppException {
        List<Person> models = this.find(model, null);
        return Optional.ofNullable((models != null ? models.get(0) : null));
    }

    @Override
    public List<Person> search(Person model, Pageable pageable) throws AppException {
        try {
            boolean hasFilter = false;

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "per_id");
            }

            Order order = Order.asc("per_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();

            query.append("SELECT ");
            query.append("	 per.*, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("	 ifnull(negList.cla_id,0) as neg_list_cla_id, ");
            query.append("	 negList.value as neg_list_value, ");
            query.append("	 negList.type as neg_list_type, ");
            query.append("	 negList.label as neg_list_label ");
            query.append("FROM person per ");
            query.append("INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("LEFT JOIN classifier negList ON per.negative_list_cla_id = negList.cla_id ");
            query.append("WHERE :hasFilter ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND per.per_id = :id ");
                    params.addValue("id", model.getId());
                    hasFilter = true;
                }

                if (model.getName() != null && !model.getName().equals("")) {
                    query.append(" AND per.name LIKE :name ");
                    params.addValue("name", this.mapLike(model.getName()));
                    hasFilter = true;
                }

                if (model.getCorporateName() != null && !model.getCorporateName().equals("")) {
                    query.append(" AND per.corporate_name = :corporateName ");
                    params.addValue("corporateName", model.getCorporateName());
                    hasFilter = true;
                }

                if (model.getJobTitle() != null && !model.getJobTitle().equals("")) {
                    query.append(" AND per.job_title LIKE :jobTitle ");
                    params.addValue("jobTitle", this.mapLike(model.getJobTitle()));
                    hasFilter = true;
                }

                if (model.getCpf() != null && !model.getCpf().equals("")) {
                    query.append(" AND per.cpf LIKE :cpf ");
                    params.addValue("cpf", this.mapLike(model.getCpf()));
                    hasFilter = true;
                }

                if (model.getRg() != null && !model.getRg().equals("")) {
                    query.append(" AND per.rg LIKE :rg ");
                    params.addValue("rg", this.mapLike(model.getRg()));
                    hasFilter = true;
                }

                if (model.getIe() != null && !model.getIe().equals("")) {
                    query.append(" AND per.ie LIKE :ie ");
                    params.addValue("ie", this.mapLike(model.getIe()));
                    hasFilter = true;
                }

                if (model.getCnpj() != null && !model.getCnpj().equals("")) {
                    query.append(" AND per.cnpj LIKE :cnpj ");
                    params.addValue("cnpj", this.mapLike(model.getCnpj()));
                    hasFilter = true;
                }

                if (model.getRne() != null && !model.getRne().equals("")) {
                    query.append(" AND per.rne LIKE :rne ");
                    params.addValue("rne", this.mapLike(model.getRne()));
                    hasFilter = true;
                }

                if (model.getClassification() != null) {
                    query.append(" AND per.classification_cla_id = :classification ");
                    params.addValue("classification", model.getClassification().getId());
                    hasFilter = true;
                }

                if (model.getBirthdate() != null) {
                    query.append(" AND per.birthdate = :birthdate ");
                    params.addValue("birthdate", PortalTimeUtils.localDateFormat(model.getBirthdate(), "yyyy-MM-dd"));
                    hasFilter = true;
                }
            }

            query.append("ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            params.addValue("hasFilter", PortalNumberUtils.booleanToInt(hasFilter));

            log.trace("[QUERY] person.search: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new PersonMapper());

        } catch (Exception e) {
            log.error("Erro ao procurar as pessoas.", e);
            throw new AppException("Erro ao procurar as pessoas.", e);
        }
    }

    /**
     * @deprecated Usar a função {@link #    search(Bank, Pageable)}
     */
    @Override
    public List<Person> search(Person model) throws AppException {
        return this.search(model, null);
    }

    @Override
    public List<Person> searchForm(String searchText, Pageable pageable) throws AppException {
        try {
            boolean haFilter = true;

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "name");
            }

            Order order = Order.asc("name");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();

            query.append("SELECT ");
            query.append("	 per.*, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("	 ifnull(negList.cla_id,0) as neg_list_cla_id, ");
            query.append("	 negList.value as neg_list_value, ");
            query.append("	 negList.type as neg_list_type, ");
            query.append("	 negList.label as neg_list_label ");
            query.append("FROM person as per ");
            query.append("INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("LEFT JOIN classifier negList ON per.negative_list_cla_id = negList.cla_id ");
            query.append("LEFT JOIN contact c on c.per_id = per.per_id ");
            query.append("WHERE :hasFilter ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (searchText != null) {
                query.append(" AND ( per.name like :text ");
                query.append(" or per.corporate_name like :text ");
                query.append(" or per.rg like :text ");
                query.append(" or per.ie like :text ");
                query.append(" or per.cpf like :text ");
                query.append(" or per.cnpj like :text ");
                query.append(" or per.rne like :text ");
                query.append(" or c.value like :text ) ");

                params.addValue("text", this.mapLike(searchText));
            }

            query.append("GROUP BY per.per_id ");
            query.append("ORDER BY per." + order.getProperty() + " " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            params.addValue("hasFilter", PortalNumberUtils.booleanToInt(haFilter));

            log.trace("[QUERY] person.find: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new PersonMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar os person.", e);
            throw new AppException("Erro ao buscar os person.", e);
        }
    }
    
    @Override
    public List<Person> findByContact(String contact) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("	 per.*, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("	 ifnull(negList.cla_id,0) as neg_list_cla_id, ");
            query.append("	 negList.value as neg_list_value, ");
            query.append("	 negList.type as neg_list_type, ");
            query.append("	 negList.label as neg_list_label ");
            query.append("FROM person as per ");
            query.append("INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("LEFT JOIN classifier negList ON per.negative_list_cla_id = negList.cla_id ");
            query.append("LEFT JOIN contact c on c.per_id = per.per_id ");
            query.append("WHERE c.value like :text  ");
            query.append("GROUP BY per.per_id ");
            query.append("ORDER BY per.name ASC ");

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("text", this.mapLike(contact));


            log.trace("[QUERY] person.findByContact: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new PersonMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar pessoas pelo contato.", e);
            throw new AppException("Erro ao buscar pessoas pelo contato.", e);
        }
    }
    
    @Override
    public Optional<Person> searchByDocument(String searchText) throws AppException {
        try {

            StringBuilder query = new StringBuilder();

            query.append("SELECT ");
            query.append("	 per.*, ");
            query.append("	 typ.cla_id as per_cla_id, ");
            query.append("	 typ.value as per_cla_value, ");
            query.append("	 typ.type as per_cla_type, ");
            query.append("	 typ.label as per_cla_label, ");
            query.append("	 ifnull(negList.cla_id,0) as neg_list_cla_id, ");
            query.append("	 negList.value as neg_list_value, ");
            query.append("	 negList.type as neg_list_type, ");
            query.append("	 negList.label as neg_list_label ");
            query.append("FROM person as per ");
            query.append("INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id ");
            query.append("LEFT JOIN classifier negList ON per.negative_list_cla_id = negList.cla_id ");
            query.append("LEFT JOIN contact c on c.per_id = per.per_id ");
            query.append("WHERE 1=1 ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (searchText != null) {
                query.append(" and per.rg = :text ");
                query.append(" or per.ie = :text ");
                query.append(" or per.cpf = :text ");
                query.append(" or per.cnpj = :text ");
                query.append(" or per.rne = :text ");

                params.addValue("text", searchText);
            }
            query.append(" LIMIT 1 ");

            log.trace("[QUERY] person.find: {} [PARAMS]: {}", query, params.getValues());

            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query.toString(), params, new PersonMapper()));

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        } catch (Exception e) {
            log.error("Erro ao consultar uma pessoa.", e);
            throw new AppException("Erro ao consultar uma pessoa.", e);
        }
    }

    @Override
    public List<Person> listAll(Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "per_id");
            }

            Order order = Order.asc("per_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            String query = "SELECT " +
                    "per.*, " +
                    "typ.cla_id as per_cla_id, " +
                    "typ.value as per_cla_value, " +
                    "typ.type as per_cla_type, " +
                    "typ.label as per_cla_label, " +
                    "ifnull(negList.cla_id,0) as neg_list_cla_id, " +
                    "negList.value as neg_list_value, " +
                    "negList.type as neg_list_type, " +
                    "negList.label as neg_list_label " +
                    "FROM person per " +
                    "INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " +
                    "LEFT JOIN classifier negList ON per.negative_list_cla_id = negList.cla_id " +
                    "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
                    "LIMIT " + pageable.getPageSize() + " " +
                    "OFFSET " + pageable.getPageNumber();

            log.trace("[QUERY] person.listAll: {} [PARAMS]: {}", query);

            return this.getJdbcTemplatePortal().query(query, new PersonMapper());

        } catch (Exception e) {
            log.error("Erro ao listar as pessoas.", e);
            throw new AppException("Erro ao listar as pessoas.", e);
        }
    }

    /**
     * @deprecated Usar a função {@link #listAll(Pageable)}
     */
    @Override
    public List<Person> list() throws AppException {
        return this.listAll(null);
    }

    @Override
    public Optional<Person> getById(Integer id) throws AppException {
        try {
            String query = "SELECT " +
                    "	per.*, " +
                    "	typ.cla_id as per_cla_id, " +
                    "	typ.value as per_cla_value, " +
                    "	typ.type as per_cla_type, " +
                    "	typ.label as per_cla_label, " +
                    "	ifnull(negList.cla_id,0) as neg_list_cla_id, " +
                    "	negList.value as neg_list_value, " +
                    "	negList.type as neg_list_type, " +
                    "	negList.label as neg_list_label " +
                    "FROM person per " +
                    "INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id  " +
                    "LEFT JOIN classifier negList ON per.negative_list_cla_id = negList.cla_id " +
                    "WHERE per.per_id = :id " +
                    "LIMIT 1";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] person.getById: {} [PARAMS]: {}", query, params.getValues());

            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query, params, new PersonMapper()));

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        } catch (Exception e) {
            log.error("Erro ao consultar uma pessoa.", e);
            throw new AppException("Erro ao consultar uma pessoa.", e);
        }
    }

	@Override
	public Optional<Person> save( Person model ) throws AppException {
		try {
			String query = "INSERT INTO person (name, corporate_name, job_title, cpf, rg, ie, cnpj, rne, add_id, classification_cla_id, birthdate, negative_list_cla_id) " +
					"VALUES (:name, :corporate_name, :jobTitle, :cpf, :rg, :ie, :cnpj, :rne, :addId, :classification, :birthdate, :negative_list_cla_id)";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("name", model.getName().toUpperCase());
            params.addValue("corporate_name", (model.getCorporateName() != null ? model.getCorporateName().toUpperCase() : null));
            params.addValue("jobTitle", (model.getJobTitle() != null ? model.getJobTitle().toUpperCase() : null));
            params.addValue("cpf", model.getCpf());
            params.addValue("rg", model.getRg());
            params.addValue("ie", model.getIe());
            params.addValue("cnpj", model.getCnpj());
            params.addValue("rne", model.getRne());
            params.addValue("addId", (model.getAddress() != null ? model.getAddress().getId() : null));
            params.addValue("classification", (model.getClassification() != null ? model.getClassification().getId() : null));
            params.addValue("birthdate", PortalTimeUtils.localDateFormat(model.getBirthdate(), "yyyy-MM-dd"));
            params.addValue("negative_list_cla_id", (model.getNegativeList() != null ? model.getNegativeList().getId() : null));

            log.trace("[QUERY] person.save: {} [PARAMS]: {}", query, params.getValues());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            this.getJdbcTemplatePortal().update(query, params, keyHolder);

            model.setId(this.getKey(keyHolder));

            return Optional.ofNullable(model);

        } catch (Exception e) {
            log.error("Erro ao tentar salvar a pessoa: {}", model, e);
            throw new AppException("Erro ao tentar salvar a pessoa.", e);
        }
    }

	@Override
	public Optional<Person> update(Person model) throws AppException {
		try {
			String query = "UPDATE person SET name=:name, corporate_name= :corporate_name, job_title=:jobTitle, cpf=:cpf, rg=:rg, ie=:ie, cnpj=:cnpj, rne=:rne, add_id=:addId, " +
					"classification_cla_id=:classification, birthdate=:birthdate, negative_list_cla_id=:negative_list_cla_id " +
					"WHERE per_id = :perId";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("name", model.getName().toUpperCase());
            params.addValue("corporate_name", (model.getCorporateName() != null ? model.getCorporateName().toUpperCase() : null));
            params.addValue("jobTitle", (model.getJobTitle() != null ? model.getJobTitle().toUpperCase() : null));
            params.addValue("cpf", model.getCpf());
            params.addValue("rg", model.getRg());
            params.addValue("ie", model.getIe());
            params.addValue("cnpj", model.getCnpj());
            params.addValue("rne", model.getRne());
            params.addValue("addId", (model.getAddress() != null ? model.getAddress().getId() : null));
            params.addValue("classification", model.getClassification().getId());
            params.addValue("birthdate", PortalTimeUtils.localDateFormat(model.getBirthdate(), "yyyy-MM-dd"));
            params.addValue("negative_list_cla_id", (model.getNegativeList() != null ? model.getNegativeList().getId() : null));
            params.addValue("perId", model.getId());

            this.getJdbcTemplatePortal().update(query, params);
            return Optional.ofNullable(model);
        } catch (Exception e) {
            log.error("Erro ao tentar atualizar a pessoa: {}", model, e);
            throw new AppException("Erro ao tentar atualizar a pessoa.", e);
        }
    }

    @Override
    public void delete(Integer id) throws AppException {
        try {
            String query = "DELETE FROM person WHERE per_id = :id";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] person.delete: {} [PARAMS]: {}", query, params.getValues());

            this.getJdbcTemplatePortal().update(query, params);

        } catch (Exception e) {
            log.error("Erro ao excluir a pessoa.", e);
            throw new AppException("Erro ao excluir a pessoa.", e);
        }
    }

    @Override
    public boolean hasPartnerRelationship(Integer perId) throws AppException {
        try {
            String query = "SELECT CASE WHEN EXISTS ( " +
                    "	SELECT entity_per_id FROM partner WHERE entity_per_id = :perId LIMIT 1 " +
                    ") " +
                    "THEN TRUE " +
                    "ELSE FALSE " +
                    "END AS `exists` ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("perId", perId);

            log.trace("[QUERY] person.hasPartnerRelationship: {} [PARAMS]: {}", query, params.getValues());

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );

        } catch (Exception e) {
            log.error("Erro ao verificar a existência de relacionamento com parceiro.", e);
            throw new AppException("Erro ao verificar a existência de relacionamento com parceiro.", e);
        }
    }

    @Override
    public boolean hasPartnerPersonRelationship(Integer perId) throws AppException {
        try {
            String query = "SELECT CASE WHEN EXISTS ( " +
                    "SELECT per_id FROM partner_person WHERE per_id = :perId LIMIT 1 " +
                    ") " +
                    "THEN TRUE " +
                    "ELSE FALSE " +
                    "END AS `exists` ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("perId", perId);

            log.trace("[QUERY] person.hasPartnerPersonRelationship: {} [PARAMS]: {}", query, params.getValues());

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );

        } catch (Exception e) {
            log.error("Erro ao verificar a existência de relacionamento com parceiros.", e);
            throw new AppException("Erro ao verificar a existência de relacionamento com parceiros.", e);
        }
    }

    @Override
    public boolean hasProposalRelationship(Integer perId) throws AppException {
        try {
            String query = "SELECT CASE WHEN EXISTS ( " +
                    "SELECT per_id FROM proposal_person_client WHERE per_id = :perId LIMIT 1 " +
                    ") " +
                    "THEN TRUE " +
                    "ELSE FALSE " +
                    "END AS `exists` ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("perId", perId);

            log.trace("[QUERY] person.hasProposalRelationship: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().queryForObject(query, params, (rs, rowNum) -> rs.getBoolean("exists"));

        } catch (Exception e) {
            log.error("Erro ao verificar a existência de relacionamento com proposta.", e);
            throw new AppException("Erro ao verificar a existência de relacionamento com proposta.", e);
        }
    }

    @Override
    public boolean hasProposalDetailRelationship(Integer perId) throws AppException {
        try {
            String query = "SELECT CASE WHEN EXISTS ( " +
                    "SELECT per_id " +
                    "FROM seller " +
                    "WHERE sel_id = :perId " +
                    "OR intern_sale_sel_id = :perId LIMIT 1 " +
                    ") " +
                    "THEN TRUE " +
                    "ELSE FALSE " +
                    "END AS `exists` ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("perId", perId);

            log.trace("[QUERY] person.hasProposalDetailRelationship: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().queryForObject(query, params, (rs, rowNum) -> rs.getBoolean("exists"));

        } catch (Exception e) {
            log.error("Erro ao verificar a existência de relacionamento com detalhes da proposta.", e);
            throw new AppException("Erro ao verificar a existência de relacionamento com detalhes da proposta.", e);
        }
    }

    @Override
    public boolean hasCommissionRelationship(Integer perId) throws AppException {
        try {
            String query = "SELECT CASE WHEN EXISTS ( " +
                    "SELECT per_id FROM proposal_commission WHERE per_id = :perId LIMIT 1 " +
                    ") " +
                    "THEN TRUE " +
                    "ELSE FALSE " +
                    "END AS `exists` ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("perId", perId);

            log.trace("[QUERY] person.hasCommissionRelationship: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().queryForObject(query, params, (rs, rowNum) -> rs.getBoolean("exists"));

        } catch (Exception e) {
            log.error("Erro ao verificar a existência de relacionamento com a comissão.", e);
            throw new AppException("Erro ao verificar a existência de relacionamento com a comissão.", e);
        }
    }

    @Override
    public boolean hasLeadRelationship(Integer perId) throws AppException {
        try {
            String query = "SELECT CASE WHEN EXISTS ( " +
                    "SELECT client_per_id " +
                    "FROM lead " +
                    "WHERE client_per_id = :perId " +
                    "LIMIT 1 " +
                    ") " +
                    "THEN TRUE " +
                    "ELSE FALSE " +
                    "END AS `exists` ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("perId", perId);

            log.trace("[QUERY] person.hasLeadRelationship: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().queryForObject(query, params, (rs, rowNum) -> rs.getBoolean("exists"));

        } catch (Exception e) {
            log.error("Erro ao verificar a existência de relacionamento com lead.", e);
            throw new AppException("Erro ao verificar a existência de relacionamento com lead.", e);
        }
    }

    @Override
    public boolean hasHoldingRelationship(Integer perId) throws AppException {
        try {
            String query = "SELECT CASE WHEN EXISTS ( " +
                    "SELECT per_id FROM holding WHERE per_id = :perId LIMIT 1 " +
                    ") " +
                    "THEN TRUE " +
                    "ELSE FALSE " +
                    "END AS `exists` ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("perId", perId);

            log.trace("[QUERY] person.hasHoldingRelationship: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().queryForObject(query, params, (rs, rowNum) -> rs.getBoolean("exists"));

        } catch (Exception e) {
            log.error("Erro ao verificar a existência de relacionamento com conglomerado.", e);
            throw new AppException("Erro ao verificar a existência de relacionamento com conglomerado.", e);
        }
    }

    @Override
    public boolean hasUserRelationship(Integer perId) throws AppException {
        try {
            String query = "SELECT CASE WHEN EXISTS ( " +
                    "SELECT per_id FROM user WHERE per_id = :perId LIMIT 1 " +
                    ") " +
                    "THEN TRUE " +
                    "ELSE FALSE " +
                    "END AS `exists` ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("perId", perId);

            log.trace("[QUERY] person.hasUserRelationship: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().queryForObject(query, params, (rs, rowNum) -> rs.getBoolean("exists"));

        } catch (Exception e) {
            log.error("Erro ao verificar a existência de relacionamento com usuário do sistema.", e);
            throw new AppException("Erro ao verificar a existência de relacionamento com usuário do sistema.", e);
        }
    }

    @Override
    public boolean hasSellerRelationship(Integer perId) throws AppException {
        try {
            String query = "SELECT CASE WHEN EXISTS ( " +
                    "SELECT per_id FROM seller WHERE per_id = :perId LIMIT 1 " +
                    ") " +
                    "THEN TRUE " +
                    "ELSE FALSE " +
                    "END AS `exists` ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("perId", perId);

            log.trace("[QUERY] person.hasSellerRelationship: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().queryForObject(query, params, (rs, rowNum) -> rs.getBoolean("exists"));

        } catch (Exception e) {
            log.error("Erro ao verificar a existência de relacionamento com seller do sistema.", e);
            throw new AppException("Erro ao verificar a existência de relacionamento com seller do sistema.", e);
        }
    }
}
