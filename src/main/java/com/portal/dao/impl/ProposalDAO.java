package com.portal.dao.impl;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import com.portal.service.IParameterService;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.portal.dao.IProposalDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProposalFrontFormMapper;
import com.portal.mapper.ProposalMapper;
import com.portal.model.Proposal;
import com.portal.model.ProposalFrontForm;
import com.portal.model.VehicleModel;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalDAO extends BaseDAO implements IProposalDAO {

	@Autowired
	private IParameterService parameterService;

	@Override
	public List<Proposal> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pps_id");	
			}
			
			Order order = Order.desc( "pps_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT  pps.*, "+ 
							"slo.order_number as order_number, " +
							"statusCla.cla_id as status_cla_id, " +
							"statusCla.value as status_cla_value, " +
							"statusCla.type as status_cla_type, " +
							"statusCla.label as status_cla_label, " +
							"riskCla.cla_id as risk_cla_id, " +
							"riskCla.value as risk_cla_value, " +
							"riskCla.type as risk_cla_type, " +
							"riskCla.label as risk_cla_label " +
							"FROM proposal pps " +
							"inner join classifier statusCla on statusCla.cla_id = pps.status_cla_id " +
							"inner join classifier riskCla on riskCla.cla_id = pps.risk_cla_id " +
							"left join sales_order slo on slo.pps_id = pps.pps_id  " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] proposal.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ProposalMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar as propostas.", e );
			throw new AppException( "Erro ao listar as propostas.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<Proposal> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<Proposal> find(Proposal model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pps_id");	
			}
			
			Order order = Order.asc( "pps_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pps.*, ");
			query.append("slo.order_number as order_number, ");
			query.append("statusCla.cla_id as status_cla_id, ");
			query.append("statusCla.value as status_cla_value, ");
			query.append("statusCla.type as status_cla_type, ");
			query.append("statusCla.label as status_cla_label, ");
			query.append("riskCla.cla_id as risk_cla_id, ");
			query.append("riskCla.value as risk_cla_value, ");
			query.append("riskCla.type as risk_cla_type, ");
			query.append("riskCla.label as risk_cla_label ");
			query.append("FROM proposal pps ");
			query.append("inner join classifier statusCla on statusCla.cla_id = pps.status_cla_id ");
			query.append("inner join classifier riskCla on riskCla.cla_id = pps.risk_cla_id ");
			query.append("inner join proposal_detail ppd on pps.pps_id = ppd.pps_id ");
			query.append("inner join proposal_detail_vehicle pdv on ppd.ppd_id = pdv.ppd_id ");
			query.append("left join sales_order slo on slo.pps_id = pps.pps_id ");
			query.append("WHERE pps.pps_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND pps.pps_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if( model.getCreateDate() != null ) {
					query.append(" AND pps.create_date = :createDate ");
					params.addValue("createDate", PortalTimeUtils.localDateTimeFormat( model.getCreateDate(), "yyyy-MM-dd HH:mm:ss" ) );
					hasFilter = true;
				} 
				
				if( model.getStatus() != null ) {
					query.append(" AND pps.status_cla_id = :status ");
					params.addValue("status", model.getStatus().getType().getId());
					hasFilter = true;
				}
				
				if( model.getNum() != null ) {
					query.append(" AND pps.num = :num ");
					params.addValue("num", model.getNum());
					hasFilter = true;
				}

				if( model.getCod() != null ) {
					query.append(" AND pps.cod = :cod ");
					params.addValue("cod", model.getCod());
					hasFilter = true;
				}
				
				if(model.getLead() != null && model.getLead().getId() != null) {
					query.append(" AND pps.led_id = :ledId ");
					params.addValue("ledId", model.getLead().getId());
					hasFilter = true;
				}
				if(model.getProposalDetailVehicle() != null && model.getProposalDetailVehicle().getVehicle().getId() != null){
					query.append(" AND pdv.vhe_id = :vhe_id ");
					params.addValue("vhe_id", model.getProposalDetailVehicle().getVehicle().getId());
					hasFilter = true;
				}

			}

			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] proposal.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProposalMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar as propostas.", e );
			throw new AppException( "Erro ao buscar as propostas.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Proposal, Pageable)}
	 */
	@Override
	public Optional<Proposal> find(Proposal model) throws AppException {
		List<Proposal> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	/**
	 * @deprecated Usar a função {@link #search(Proposal, Pageable)}
	 */
	@Override
	public List<Proposal> search(Proposal model) throws AppException {
		return this.find(model, null);
	}

	@Override
	public List<ProposalFrontForm> getByVehicle(VehicleModel vehicleModel, Pageable defaultPagination) throws AppException {
		try {

			String query =  "select   "
					+ "	   p.pps_id,  "
					+ "    p.status_cla_id,  "
					+ "    IFNULL(per.name, '-') as `client`,  "
					+ "	   p.num as num_proposta,  "
					+ "    p.cod as cod,  "
					+ "    p.proposal_number, "
					+ "	   p.immediate_delivery, "
					+ "    slo.order_number, "
					+ "    ifnull( ent_per.name, '' ) as partner,  "
					+ "    sel_pd.sel_id as seller_id, "
					+ "    per_pd.per_id as seller_per_id, "
					+ "    per_pd.name as seller_name, "
					+ "    concat(m.name, \" | \" ,b.name) as brandModel,  "
					+ "    p.create_date, "
					+ "    p.validity_date,  "
					+ "	   pdv.total_amount as total_price, "
        			+ "    statusCla.cla_id as status_cla_id, " 
        			+ "    statusCla.value as status_cla_value, "
        			+ "    statusCla.type as status_cla_type, "
        			+ "    statusCla.label as status_cla_label "
					+ "from proposal p  "
        			+ "inner join classifier statusCla on statusCla.cla_id = p.status_cla_id "
					+ "left join proposal_person_client ppc on ppc.pps_id = p.pps_id  "
					+ "left join person per on per.per_id = ppc.per_id  "
					+ "left join sales_order slo on slo.pps_id = p.pps_id  "
					+ "inner join proposal_detail pd on pd.pps_id = p.pps_id  "
					+ "inner JOIN seller sel_pd on sel_pd.sel_id = pd.sel_id "
					+ "inner JOIN person per_pd on per_pd.per_id = sel_pd.per_id "
					+ "inner join proposal_detail_vehicle pdv on pdv.ppd_id = pd.ppd_id  "
					+ "inner join price_product pp on pp.ppr_id = pdv.ppr_id "
					+ "left join vehicle v on v.vhe_id = pdv.vhe_id  "
					+ "inner join model m on m.mdl_id = pdv.mdl_id "
					+ "inner join brand b on b.brd_id = m.brd_id "
					+ "left JOIN partner par on par.ptn_id = pd.ptn_id  "
					+ "left JOIN person ent_per ON ent_per.per_id = par.entity_per_id  "
					+ "left JOIN partner_group parg ON parg.ptg_id = par.ptg_id "
					+ "where 1=1 ";

			MapSqlParameterSource params = new MapSqlParameterSource();

			if(vehicleModel != null) {
				if(vehicleModel.getId() != null) {
					query += " and v.vhe_id = :vheId ";
					params.addValue("vheId", vehicleModel.getId());
				}
			}

			log.trace( "[QUERY] ProposalForm.getByVehicle: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().query( query, params, new ProposalFrontFormMapper() );

		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre proposta e vehicle.", e );
			throw new AppException( "Erro ao buscar o lista de proposta para o frontend.", e );
		}
	}

	@Override
	public Optional<Proposal> getById(Integer id) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pps.*, ");
			query.append("slo.order_number, ");
			query.append("statusCla.cla_id as status_cla_id, ");
			query.append("statusCla.value as status_cla_value, ");
			query.append("statusCla.type as status_cla_type, ");
			query.append("statusCla.label as status_cla_label, ");
			query.append("riskCla.cla_id as risk_cla_id, ");
			query.append("riskCla.value as risk_cla_value, ");
			query.append("riskCla.type as risk_cla_type, ");
			query.append("riskCla.label as risk_cla_label ");
			query.append("FROM proposal pps ");
			query.append("inner join classifier statusCla on statusCla.cla_id = pps.status_cla_id ");
			query.append("inner join classifier riskCla on riskCla.cla_id = pps.risk_cla_id ");
			query.append("left join sales_order slo on slo.pps_id = pps.pps_id ");
			query.append("WHERE pps.pps_id = :id ");
			query.append("LIMIT 1 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposal.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ProposalMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar a proposta.", e );
			throw new AppException( "Erro ao consultar a proposta.", e );
		}
	}

	@Override
	public Optional<Proposal> save(Proposal model) throws AppException {
		try {

			String daysLimit = this.parameterService.getValueOf("PROPOSAL_DAYS_LIMIT");

			String query = "INSERT INTO proposal (num, proposal_number, cod, create_date, validity_date, status_cla_id, led_id, risk_cla_id, immediate_delivery, contract, "
													+ "finantial_contact, finantial_contact_name, finantial_contact_email, finantial_contact_phone, "
													+ "document_contact, document_contact_name, document_contact_email, document_contact_phone,  "
													+ "commercial_contact_name, commercial_contact_email, commercial_contact_phone ) " +
						   "VALUES (:num, :proposal_number, :cod, now(), date_add(now(), interval :daysLimit day), :status, :ledId, :riskclaid, :immediatedelivery, :contract, "
						   		+ ":finantialcontact, :finantialcontactname,:finantialcontactemail, :finantialcontactphone, "
						   		+ ":documentcontact, :documentcontactname,:documentcontactemail, :documentcontactphone, "
						   		+ ":commercialContactName, :commercialContactEmail, :commercialContactPhone) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("num", model.getNum());
			params.addValue("proposal_number", model.getProposalNumber());
			params.addValue("createDate", PortalTimeUtils.localDateTimeFormat( model.getCreateDate(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue("daysLimit", daysLimit);
			params.addValue("validityDate", PortalTimeUtils.localDateTimeFormat(model.getValidityDate(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue("status", model.getStatus().getType().getId());
			params.addValue("cod", (model.getCod() != null ? model.getCod().toUpperCase() : null));
			params.addValue("ledId", (model.getLead() == null ? null : model.getLead().getId() ) );
			params.addValue("finantialcontact", PortalNumberUtils.booleanToInt(model.getFinantialContact()));
			params.addValue("finantialcontactname", (model.getFinantialContactName() != null ? model.getFinantialContactName().toUpperCase() : null)); 
			params.addValue("finantialcontactemail", model.getFinantialContactEmail());
			params.addValue("finantialcontactphone", model.getFinantialContactPhone());
			params.addValue("documentcontact", PortalNumberUtils.booleanToInt(model.getDocumentContact()));
			params.addValue("documentcontactname", (model.getDocumentContactName() != null ? model.getDocumentContactName().toUpperCase() : null)); 
			params.addValue("documentcontactemail", model.getDocumentContactEmail());
			params.addValue("documentcontactphone", model.getDocumentContactPhone());
			params.addValue("commercialContactName", model.getCommercialContactName());
			params.addValue("commercialContactEmail", model.getCommercialContactEmail());
			params.addValue("commercialContactPhone", model.getCommercialContactPhone());
			params.addValue("riskclaid", model.getRisk().getType().getId());
			params.addValue("immediatedelivery", PortalNumberUtils.booleanToInt( model.getImmediateDelivery() ) );
			params.addValue("contract", model.getContract());

			log.trace( "[QUERY] proposal.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar a proposta: {}", model, e );
			throw new AppException( "Erro ao tentar salvar a proposta.", e);
		}
	}

	@Override
	public Optional<Proposal> update(Proposal model) throws AppException {
		try {
			String query = "UPDATE proposal SET  " +
					 	   	"					num=:num, " +
					 	   	"					cod=:cod, " +
					 	   	"					status_cla_id=:status, " +
					 	   	"					led_id = :ledId, " +
							"					finantial_contact = :finantialcontact, " +
							"					finantial_contact_name = :finantialcontactname, " +
							"					finantial_contact_email = :finantialcontactemail, " +
							"					finantial_contact_phone = :finantialcontactphone, " +
							"					document_contact = :documentcontact, " +
							"					document_contact_name = :documentcontactname, " +
							"					document_contact_email = :documentcontactemail, " +
							"					document_contact_phone = :documentcontactphone, " +
							"					commercial_contact_name = :commercialContactName, " +
							"					commercial_contact_email = :commercialContactEmail, " +
							"					commercial_contact_phone = :commercialContactPhone, " +
							"					risk_cla_id = :riskclaid, " +
							"					immediate_delivery = :immediatedelivery, " +
							"					contract = :contract " +
					 	   "WHERE pps_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", model.getId() );
			params.addValue("status", model.getStatus().getType().getId());
			params.addValue("num", model.getNum());
			params.addValue("cod", model.getCod());
			params.addValue("ledId", ( model.getLead() == null ? null : model.getLead().getId() ) );
			params.addValue("finantialcontact", PortalNumberUtils.booleanToInt(model.getFinantialContact()));
			params.addValue("finantialcontactname", model.getFinantialContactName());
			params.addValue("finantialcontactemail", model.getFinantialContactEmail());
			params.addValue("finantialcontactphone", model.getFinantialContactPhone());
			params.addValue("documentcontact", PortalNumberUtils.booleanToInt(model.getDocumentContact()));
			params.addValue("documentcontactname", model.getDocumentContactName());
			params.addValue("documentcontactemail", model.getDocumentContactEmail());
			params.addValue("documentcontactphone", model.getDocumentContactPhone());
			params.addValue("commercialContactName", model.getCommercialContactName());
			params.addValue("commercialContactEmail", model.getCommercialContactEmail());
			params.addValue("commercialContactPhone", model.getCommercialContactPhone());
			params.addValue("riskclaid", model.getRisk().getType().getId());
			params.addValue("immediatedelivery", PortalNumberUtils.booleanToInt( model.getImmediateDelivery() ) );
			params.addValue("contract", model.getContract());

			log.trace( "[QUERY] proposal.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar a proposta: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar a proposta.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM proposal WHERE pps_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposal.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir a proposta" , e );
			throw new AppException( "Erro ao excluir a proposta.", e );
		}
	}
	
	@Override
	public Long getLastProposalNumber() throws AppException {
		try {
			MapSqlParameterSource params = new MapSqlParameterSource();
			StringBuilder query = new StringBuilder();
			query.append("SELECT IFNULL(MAX(num),0) as num FROM proposal ");
			
			Long num = this.getJdbcTemplatePortal().query(query.toString(), params, (ResultSet rs) -> {
            	while (rs.next()) {
            		return rs.getLong("num");
            	}
            	return null;
            });
			
			return num;
		} catch (Exception e) {
            log.error("Erro em seller.checkSellerDocument .", e);
            throw new AppException("Erro em seller.checkSellerDocument .", e);
        }
	}
}
