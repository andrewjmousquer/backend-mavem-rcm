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
import com.portal.dao.IVehicleDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.VehicleMapper;
import com.portal.model.VehicleModel;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class VehicleDAO extends BaseDAO implements IVehicleDAO {

    @Override
    public List<VehicleModel> listAll(Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "vhe_id");
            }

            Order order = Order.desc("vhe_id");
            if (pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            String query = "SELECT  vhe.vhe_id, " +
                    "vhe.chassi, " +
                    "vhe.plate, " +
                    "mdl.mdl_id, " +
                    "mdl.name, " +
                    "brd.brd_id, " +
                    "brd.name as brand_name, " +
                    "vhe.version," +
                    "vhe.model_year," +
                    "vhe.purchase_date," +
                    "vhe.purchase_value, " +
                    "cla.cla_id, " +
                    "cla.value, " +
                    "cla.type, " +
                    "cla.label, " +
                    "cla.description " +
                    "FROM vehicle vhe " +
                    "INNER JOIN model mdl ON (vhe.mdl_id = mdl.mdl_id) " +
                    "INNER JOIN brand brd ON (mdl.brd_id = brd.brd_id) " +
                    "LEFT JOIN  classifier cla ON (vhe.color_cla = cla.cla_id) " +
                    "ORDER BY vhe_id " + order.getDirection().name() + " " +
                    "LIMIT " + pageable.getPageSize() + " " +
                    "OFFSET " + pageable.getPageNumber();


            log.trace("[QUERY] vehicle.listAll: {}", query);

            return this.getJdbcTemplatePortal().query(query, new VehicleMapper());

        } catch (Exception e) {
            log.error("Erro ao listar os veículos.", e);
            throw new AppException("Erro ao listar os veículos.", e);
        }
    }

    /**
     * @deprecated Usar a função {@link #listAll(Pageable)}
     */
    @Override
    public List<VehicleModel> list() throws AppException {
        return this.listAll(null);
    }

    @Override
    public List<VehicleModel> find(VehicleModel model, Pageable pageable) throws AppException {
        try {
        	boolean hasFilter = false;
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "vhe_id");
            }

            Order order = Order.asc("vhe_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();

            query.append("SELECT vhe.vhe_id, " +
                    "vhe.chassi, " +
                    "vhe.plate, " +
                    "mdl.mdl_id, " +
                    "mdl.name, " +
                    "brd.brd_id, " +
                    "brd.name as brand_name, " +
                    "vhe.version," +
                    "vhe.model_year," +
                    "vhe.purchase_date," +
                    "vhe.purchase_value, " +
                    "cla.cla_id, " +
                    "cla.value, " +
                    "cla.type, " +
                    "cla.label, " +
                    "cla.description ");
            query.append("FROM vehicle vhe " +
                    "INNER JOIN model mdl ON (vhe.mdl_id = mdl.mdl_id) " +
                    "INNER JOIN brand brd ON (mdl.brd_id = brd.brd_id) " +
                    "LEFT JOIN classifier cla ON (vhe.color_cla = cla.cla_id) ");
            query.append("WHERE vhe.vhe_id > 0 ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                	query.append(!hasFilter ? " AND " : "OR");
                    query.append(" vhe.vhe_id = :id ");
                    params.addValue("id", model.getId());
                    hasFilter = true;
                }

                if (model.getChassi() != null) {
                	query.append(!hasFilter ? " AND " : "OR");
                    query.append(" vhe.chassi = :chassi ");
                    params.addValue("chassi", model.getChassi());
                    hasFilter = true;
                }

                if (model.getPlate() != null) {
                	query.append(!hasFilter ? " AND " : "OR");
                    query.append(" vhe.plate = :plate ");
                    params.addValue("plate", model.getPlate());
                    hasFilter = true;
                }

                if (model.getModel() != null && model.getModel().getId() != null) {
                	query.append(!hasFilter ? " AND " : "OR");
                    query.append(" vhe.mdl_id = :mdlId ");
                    params.addValue("mdlId", model.getModel().getId());
                    hasFilter = true;
                }

                if (model.getModelYear() != null) {
                	query.append(!hasFilter ? " AND " : "OR");
                    query.append(" vhe.model_year = :modelYear ");
                    params.addValue("modelYear", model.getModelYear());
                    hasFilter = true;
                }

                if (model.getPurchaseDate() != null) {
                	query.append(!hasFilter ? " AND " : "OR");
                    query.append(" vhe.purchase_date = :purchaseDate ");
                    params.addValue("purchaseDate", PortalTimeUtils.localDateFormat(model.getPurchaseDate(), "yyyy-MM-dd"));
                    hasFilter = true;
                }

                if (model.getPurchaseValue() != null) {
                	query.append(!hasFilter ? " AND " : "OR");
                    query.append(" vhe.purchase_value = :purchaseValue ");
                    params.addValue("purchaseValue", model.getPurchaseValue());
                    hasFilter = true;
                }
            }

            query.append("ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            params.addValue("hasFilter", PortalNumberUtils.booleanToInt(hasFilter));

            log.trace("[QUERY] vehicle.find: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new VehicleMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar os veículos.", e);
            throw new AppException("Erro ao buscar os veículos.", e);
        }
    }

    /**
     * @deprecated Usar a função {@link #find(VehicleModel, Pageable)}
     */
    @Override
    public Optional<VehicleModel> find(VehicleModel model) throws AppException {
        List<VehicleModel> models = this.find(model, null);
        return Optional.ofNullable((models != null ? models.get(0) : null));
    }

    @Override
    public List<VehicleModel> search(VehicleModel model, Pageable pageable) throws AppException {
        try {
            boolean hasFilter = false;

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "vhe_id");
            }

            Order order = Order.asc("vhe_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();

            query.append("SELECT  vhe.vhe_id, ");
            query.append("vhe.chassi, vhe.plate, mdl.mdl_id, mdl.name, brd.brd_id, brd.name as brand_name, vhe.model_year, ");
            query.append("vhe.version, vhe.purchase_date, vhe.purchase_value, ");
            query.append("cla.cla_id, cla.value, cla.type, cla.label, cla.description ");
            query.append("FROM vehicle vhe ");
            query.append("INNER JOIN model mdl ON (vhe.mdl_id = mdl.mdl_id) ");
            query.append("INNER JOIN brand brd ON (mdl.brd_id = brd.brd_id)  ");
            query.append("LEFT JOIN classifier cla ON (vhe.color_cla = cla.cla_id)  ");
            query.append("WHERE :hasFilter ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (model != null) {
                if (model.getId() != null && model.getId() > 0) {
                    query.append(" AND vhe.vhe_id = :id ");
                    params.addValue("id", model.getId());
                    hasFilter = true;
                }

                if (model.getChassi() != null) {
                    query.append(" AND vhe.chassi LIKE :chassi ");
                    params.addValue("chassi", this.mapLike(model.getChassi()));
                    hasFilter = true;
                }

                if (model.getPlate() != null) {
                    query.append(" AND vhe.plate LIKE :plate ");
                    params.addValue("plate", this.mapLike(model.getPlate()));
                    hasFilter = true;
                }

                if (model.getModel() != null && model.getModel().getId() != null) {
                    query.append(" AND mdl_id = :mdlId ");
                    params.addValue("mdlId", model.getModel().getId());
                    hasFilter = true;
                }

                if (model.getModelYear() != null) {
                    query.append(" AND vhe.model_year = :modelYear ");
                    params.addValue("modelYear", model.getModelYear());
                    hasFilter = true;
                }

                if (model.getPurchaseDate() != null) {
                    query.append(" AND vhe.purchase_date = :purchaseDate ");
                    params.addValue("purchaseDate", PortalTimeUtils.localDateFormat(model.getPurchaseDate(), "yyyy-MM-dd"));
                    hasFilter = true;
                }

                if (model.getPurchaseValue() != null) {
                    query.append(" AND vhe.purchase_value = :purchaseValue ");
                    params.addValue("purchaseValue", model.getPurchaseValue());
                    hasFilter = true;
                }
            }

            query.append("ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            params.addValue("hasFilter", PortalNumberUtils.booleanToInt(hasFilter));

            log.trace("[QUERY] vehicle.find: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new VehicleMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar os veículos.", e);
            throw new AppException("Erro ao buscar os veículos.", e);
        }
    }

    /**
     * @deprecated Usar a função {@link #search(VehicleModel, Pageable)}
     */
    @Override
    public List<VehicleModel> search(VehicleModel model) throws AppException {
        return this.search(model, null);
    }

    @Override
    public List<VehicleModel> searchForm(String searchText, Pageable pageable) throws AppException {
        try {
            boolean hasFilter = false;

            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "vhe_id");
            }

            Order order = Order.asc("vhe_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();

            query.append("SELECT  vhe.vhe_id, ");
            query.append("vhe.chassi, vhe.plate, mdl.mdl_id, mdl.name, brd.brd_id, brd.name as brand_name, vhe.model_year, ");
            query.append("vhe.version, vhe.purchase_date, vhe.purchase_value, vhe.color_cla, ");
            query.append("cla.cla_id, cla.value, cla.type, cla.label, cla.description ");
            query.append("FROM vehicle vhe ");
            query.append("INNER JOIN model mdl ON (vhe.mdl_id = mdl.mdl_id) ");
            query.append("INNER JOIN brand brd ON (mdl.brd_id = brd.brd_id)  ");
            query.append("LEFT JOIN classifier cla ON (vhe.color_cla = cla.cla_id)  ");
            query.append("WHERE :hasFilter ");

            MapSqlParameterSource params = new MapSqlParameterSource();

            if (searchText != null) {

                query.append(" or vhe.plate like :text ");
                query.append(" or mdl.name like :text ");
                query.append(" or brd.name like :text ");
                query.append(" or vhe.chassi like :text ");

                params.addValue("text", this.mapLike(searchText));
            }

            query.append("ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());

            params.addValue("hasFilter", PortalNumberUtils.booleanToInt(hasFilter));

            log.trace("[QUERY] vehicle.find: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().query(query.toString(), params, new VehicleMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar os veículos.", e);
            throw new AppException("Erro ao buscar os veículos.", e);
        }
    }

    @Override
    public List<VehicleModel> getByBrand(String brand, Pageable pageable) throws AppException {
        try {
            if (pageable == null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "vhe_id");
            }

            Order order = Order.asc("vhe_id");
            if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
                order = pageable.getSort().get().findFirst().orElse(order);
            }

            StringBuilder query = new StringBuilder();

            query.append("SELECT  vhe.vhe_id, ");
            query.append("vhe.chassi, vhe.plate, mdl.mdl_id, mdl.name, brd.brd_id, brd.name as brand_name, vhe.model_year, ");
            query.append("vhe.version, vhe.purchase_date, vhe.purchase_value, ");
            query.append("cla.cla_id, cla.value, cla.type, cla.label, cla.description ");
            query.append("FROM vehicle vhe ");
            query.append("INNER JOIN model mdl ON (vhe.mdl_id = mdl.mdl_id) ");
            query.append("INNER JOIN brand brd ON (mdl.brd_id = brd.brd_id)  ");
            query.append("LEFT JOIN classifier cla ON (vhe.color_cla = cla.cla_id)  ");
            query.append("WHERE brd.name =  '" + brand + "' ");
            query.append(" ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " ");
            query.append("LIMIT " + pageable.getPageSize() + " ");
            query.append("OFFSET " + pageable.getPageNumber());


            log.trace("[QUERY] vehicle.find: {} [PARAMS]: {}", query);

            return this.getJdbcTemplatePortal().query(query.toString(), new VehicleMapper());

        } catch (Exception e) {
            log.error("Erro ao buscar os veículos.", e);
            throw new AppException("Erro ao buscar os veículos.", e);
        }
    }

    @Override
    public Optional<VehicleModel> getByChassi(String chassi) throws AppException {
        try {

            String query = "SELECT  vhe.vhe_id, " +
                    "vhe.chassi, " +
                    "vhe.plate, " +
                    "mdl.mdl_id, " +
                    "mdl.name, " +
                    "brd.brd_id, " +
                    "brd.name as brand_name, " +
                    "vhe.version," +
                    "vhe.model_year," +
                    "vhe.purchase_date," +
                    "vhe.purchase_value, " +
                    "cla.cla_id, " +
                    "cla.value, " +
                    "cla.type, " +
                    "cla.label, " +
                    "cla.description " +
                    "FROM vehicle vhe " +
                    "INNER JOIN model mdl ON (vhe.mdl_id = mdl.mdl_id) " +
                    "INNER JOIN brand brd ON (mdl.brd_id = brd.brd_id) " +
                    "LEFT JOIN classifier cla ON (vhe.color_cla = cla.cla_id) " +
                    "WHERE vhe.chassi = :chassi " +
                    "LIMIT 1";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("chassi", chassi);

            log.trace("[QUERY] vehicle.getByChassi: {} [PARAMS]: {}", query, params.getValues());

            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query, params, new VehicleMapper()));

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        } catch (Exception e) {
            log.error("Erro ao consultar o veículo.", e);
            throw new AppException("Erro ao consultar o veículo.", e);
        }
    }

    @Override
    public Optional<VehicleModel> getById(Integer id) throws AppException {
        try {

            String query = "SELECT  vhe.vhe_id, " +
                    "vhe.chassi, " +
                    "vhe.plate, " +
                    "mdl.mdl_id, " +
                    "mdl.name, " +
                    "brd.brd_id, " +
                    "brd.name as brand_name, " +
                    "vhe.version," +
                    "vhe.model_year," +
                    "vhe.purchase_date," +
                    "vhe.purchase_value, " +
                    "cla.cla_id, " +
                    "cla.value, " +
                    "cla.type, " +
                    "cla.label, " +
                    "cla.description " +
                    "FROM vehicle vhe " +
                    "INNER JOIN model mdl ON (vhe.mdl_id = mdl.mdl_id) " +
                    "INNER JOIN brand brd ON (mdl.brd_id = brd.brd_id) " +
                    "LEFT JOIN classifier cla ON (vhe.color_cla = cla.cla_id) " +
                    "WHERE vhe.vhe_id = :id " +
                    "LIMIT 1";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] vehicle.getById: {} [PARAMS]: {}", query, params.getValues());

            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query, params, new VehicleMapper()));

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        } catch (Exception e) {
            log.error("Erro ao consultar o veículo.", e);
            throw new AppException("Erro ao consultar o veículo.", e);
        }
    }

    @Override
    public Optional<VehicleModel> save(VehicleModel model) throws AppException {
        try {
            String query = "INSERT INTO vehicle (vhe_id, chassi, plate, mdl_id, version, model_year, purchase_date, purchase_value, color_cla) " +
                    "VALUES ( NULL, :chassi, :plate, :mdlId, :version, :modelYear, :purchaseDate, :purchaseValue, :colorCla ) ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("chassi", model.getChassi().toUpperCase());
            params.addValue("plate", model.getPlate() != null ? model.getPlate().toUpperCase() : null);
            params.addValue("mdlId", model.getModel() != null ? model.getModel().getId() : null);
            params.addValue("version", model.getVersion() != null ? model.getVersion() : null);
            params.addValue("modelYear", model.getModelYear());
            params.addValue("purchaseDate", model.getPurchaseDate() != null ? PortalTimeUtils.localDateFormat(model.getPurchaseDate(), "yyyy-MM-dd") : null);
            params.addValue("purchaseValue", model.getPurchaseValue() != null ? model.getPurchaseValue() : null);
            params.addValue("colorCla", model.getColor() != null ? model.getColor().getId() : null);

            log.trace("[QUERY] vehicle.save: {} [PARAMS]: {}", query, params.getValues());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);

            model.setId(this.getKey(keyHolder));

            return Optional.ofNullable(model);

        } catch (Exception e) {
            log.error("Erro ao tentar salvar o veículo: {}", model, e);
            throw new AppException("Erro ao tentar salvar o veículo.", e);
        }
    }

    @Override
    public Optional<VehicleModel> update(VehicleModel model) throws AppException {
        try {
            String query = "UPDATE vehicle SET chassi=:chassi, plate=:plate, mdl_id=:mdlId, version = :version, model_year=:modelYear, purchase_date=:purchaseDate, purchase_value=:purchaseValue, color_cla=:colorCla " +
                    "WHERE vhe_id = :id";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", model.getId());
            params.addValue("chassi", model.getChassi().toUpperCase());
            params.addValue("plate", model.getPlate() != null ? model.getPlate().toUpperCase() : null);
            params.addValue("mdlId", (model.getModel() != null ? model.getModel().getId() : null));
            params.addValue("version", model.getVersion() != null ? model.getVersion() : null);
            params.addValue("modelYear", model.getModelYear());
            params.addValue("purchaseDate", model.getPurchaseDate() != null ? PortalTimeUtils.localDateFormat(model.getPurchaseDate(), "yyyy-MM-dd") : null);
            params.addValue("purchaseValue", model.getPurchaseValue() != null ? model.getPurchaseValue() : null);
            params.addValue("colorCla", model.getColor() != null ? model.getColor().getId() : null);

            log.trace("[QUERY] vehicle.update: {} [PARAMS]: {}", query, params.getValues());
            this.getNamedParameterJdbcTemplate().update(query.toString(), params);

            return Optional.ofNullable(model);

        } catch (Exception e) {
            log.error("Erro ao tentar atualizar o veículo: {}", model, e);
            throw new AppException("Erro ao tentar atualizar o veículo.", e);
        }
    }

    @Override
    public void delete(Integer id) throws AppException {
        try {
            String query = "DELETE FROM vehicle WHERE vhe_id = :id";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] vehicle.delete: {} [PARAMS]: {}", query, params.getValues());

            this.getJdbcTemplatePortal().update(query, params);

        } catch (Exception e) {
            log.error("Erro ao excluir o veículo", e);
            throw new AppException("Erro ao excluir o veículo.", e);
        }

    }

    @Override
    public boolean hasProposalDetailRelationship(Integer vheId) throws AppException {
        try {
            String query = "SELECT CASE WHEN EXISTS ( " +
                    "SELECT vhe_id FROM proposal_detail_vehicle WHERE vhe_id = :vheId LIMIT 1 " +
                    ") " +
                    "THEN TRUE " +
                    "ELSE FALSE " +
                    "END AS `exists` ";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("vheId", vheId);

            log.trace("[QUERY] vehicle.hasProposalDetailRelationship: {} [PARAMS]: {}", query, params.getValues());

            return this.getJdbcTemplatePortal().queryForObject(query, params, (rs, rowNum) -> rs.getBoolean("exists"));

        } catch (Exception e) {
            log.error("Erro ao verificar a existência de relacionamento com detalhe da proposta.", e);
            throw new AppException("Erro ao verificar a existência de relacionamento com detalhe da proposta.", e);
        }
    }
}
