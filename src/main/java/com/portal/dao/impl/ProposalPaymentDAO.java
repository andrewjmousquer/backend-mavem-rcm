package com.portal.dao.impl;

import java.math.BigDecimal;
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
import com.portal.dao.IProposalPaymentDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProposalPaymentMapper;
import com.portal.model.ProposalPayment;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalPaymentDAO extends BaseDAO implements IProposalPaymentDAO {

	@Override
	public List<ProposalPayment> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "ASC" ), "position");
			}
			
			Order order = Order.desc( "ppy_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT * " +
							"FROM proposal_payment ppy " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			
			log.trace( "[QUERY] proposalPaymentlistAll: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ProposalPaymentMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar o detalhe de pagamento.", e );
			throw new AppException( "Erro ao listar o detalhe de pagamento.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<ProposalPayment> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<ProposalPayment> find(ProposalPayment model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "ASC" ), "position");
			}
			
			Order order = Order.asc( "ppy_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT ppy.*, ");
			query.append("	cla_p.cla_id as payer_cla_id, ");
			query.append("	cla_p.value as payer_value, ");
			query.append("	cla_p.type as payer_type, ");
			query.append("	cla_p.label as payer_label, ");
			query.append("	cla_e.cla_id as event_cla_id, ");
			query.append("	cla_e.value as event_value, ");
			query.append("	cla_e.type as event_type, ");
			query.append("	cla_e.label as event_label ");
			query.append("FROM proposal_payment ppy ");
			query.append("INNER JOIN classifier cla_p on cla_p.cla_id = ppy.payer_cla_id ");
			query.append("INNER JOIN classifier cla_e on cla_e.cla_id = ppy.event_cla_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND ppy.ppy_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}

				if( model.getDueDate() != null ) {
					query.append(" AND ppy.due_date = :dueDate ");
					params.addValue("dueDate", PortalTimeUtils.dateFormat( model.getDueDate(), "yyyy-MM-dd HH:mm:ss" ) );
					hasFilter = true;
				}
				
				if( model.getProposalDetail() != null && model.getProposalDetail().getId() != null ) {
					query.append(" AND ppy.ppd_id = :ppdId " );
					params.addValue("ppdId", model.getProposalDetail().getId());
					hasFilter = true;
				}
				
				if( model.getPaymentMethod() != null && model.getPaymentMethod().getId() != null ) {
					query.append(" AND ppy.pym_id = :pymId " );
					params.addValue("pymId", model.getPaymentMethod().getId());
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] proposalPaymentfind: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProposalPaymentMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar o detalhe de pagamento.", e );
			throw new AppException( "Erro ao buscar o detalhe de pagamento.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(ProposalPayment, Pageable)}
	 */
	@Override
	public Optional<ProposalPayment> find(ProposalPayment model) throws AppException {
		List<ProposalPayment> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<ProposalPayment> search(ProposalPayment model, Pageable pageable) throws AppException {
		return this.find(model, pageable);
	}
	
	/**
	 * @deprecated Usar a função {@link #search(ProposalPayment, Pageable)}
	 */
	@Override
	public List<ProposalPayment> search(ProposalPayment model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<ProposalPayment> getById(Integer id) throws AppException {
		try {
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT ppy.*, ");
			query.append("	cla_p.cla_id as payer_cla_id, ");
			query.append("	cla_p.value as payer_value, ");
			query.append("	cla_p.type as payer_type, ");
			query.append("	cla_p.label as payer_label, ");
			query.append("	cla_e.cla_id as event_cla_id, ");
			query.append("	cla_e.value as event_value, ");
			query.append("	cla_e.type as event_type, ");
			query.append("	cla_e.label as event_label ");
			query.append("FROM proposal_payment ppy ");
			query.append("INNER JOIN classifier cla_p on cla_p.cla_id = ppy.payer_cla_id ");
			query.append("INNER JOIN classifier cla_e on cla_e.cla_id = ppy.event_cla_id ");
			query.append("WHERE ppy.ppy_id = :id ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposalPaymentgetById: {} [PARAMS]: {}", query.toString(), params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ProposalPaymentMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o detalhe do pagamento.", e );
			throw new AppException( "Erro ao consultar o detalhe do pagamento.", e );
		}
	}

	@Override
	public Optional<ProposalPayment> save(ProposalPayment model) throws AppException {
		try {
			String query = "INSERT INTO proposal_payment (ppy_id, payment_amount, due_date, installment_amount, ppd_id, pym_id, pyr_id, interest, payer_cla_id, event_cla_id, pre_approved, antecipated_billing, position, quantity_days, carbon_billing ) " +
					 	   "VALUES ( NULL, :paymentAmount, :dueDate, :installment_amount, :ppdId, :pymId, :pyrId, :interest, :payerclaid, :eventclaid, :preapproved, :antecipatedbilling, :position , :quantityDays, :carbonBilling ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "paymentAmount", model.getPaymentAmount() );
			params.addValue( "dueDate", model.getDueDate() != null ? PortalTimeUtils.dateFormat( model.getDueDate(), "yyyy-MM-dd HH:mm:ss") : null);
			params.addValue( "installment_amount", model.getInstallmentAmount() != null ? model.getInstallmentAmount() : BigDecimal.ZERO);
			params.addValue( "ppdId", ( model.getProposalDetail() == null ? null : model.getProposalDetail().getId() ) );
			params.addValue( "pymId", ( model.getPaymentMethod() == null ? null : model.getPaymentMethod().getId() ) );
			params.addValue( "pyrId", (model.getPaymentRule() == null ? null : model.getPaymentRule().getId()) );
			params.addValue( "interest", model.getInterest() != null ? model.getInterest() : BigDecimal.ZERO );
			params.addValue( "payerclaid", model.getPayer().getId() );
			params.addValue( "eventclaid", model.getEvent().getId() );
			params.addValue( "preapproved", PortalNumberUtils.booleanToInt( model.getPreApproved() ) );
			params.addValue( "antecipatedbilling", PortalNumberUtils.booleanToInt( model.getAntecipatedBilling() ) );
			params.addValue("quantityDays", model.getQuantityDays() == null ? null  : model.getQuantityDays());
			params.addValue("carbonBilling", PortalNumberUtils.booleanToInt(model.getCarbonBilling()));
			params.addValue( "position", model.getPosition());
			
			log.trace( "[QUERY] proposalPayment.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o detalhe do pagamento: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o detalhe do pagamento.", e);
		}
	}

	@Override
	public Optional<ProposalPayment> update(ProposalPayment model) throws AppException {
		try {
			String query = "UPDATE proposal_payment " +
					"SET payment_amount=:paymentAmount, " +
					"due_date=:dueDate, " +
					"installment_amount=:installment_amount, " +
					"ppd_id=:ppdId, " +
					"pym_id=:pymId, " +
					"pyr_id=:pyrId, " +
					"interest=:interest, " +
					"payer_cla_id=:payerclaid, " +
					"event_cla_id=:eventclaid, " +
					"pre_approved=:preapproved, " +
					"antecipated_billing=:antecipatedbilling, " +
					"position=:position, " +
					"quantity_days=:quantityDays, " +
					"carbon_billing=:carbonBilling " +
					" WHERE ppy_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "paymentAmount", model.getPaymentAmount() );
			params.addValue( "dueDate", model.getDueDate() != null ? PortalTimeUtils.dateFormat( model.getDueDate(), "yyyy-MM-dd HH:mm:ss") : null);
			params.addValue( "installment_amount", model.getInstallmentAmount() );
			params.addValue( "ppdId", ( model.getProposalDetail() == null ? null : model.getProposalDetail().getId() ) );
			params.addValue( "pymId", ( model.getPaymentMethod() == null ? null : model.getPaymentMethod().getId() ) );
			params.addValue( "pyrId", (model.getPaymentRule() == null ? null : model.getPaymentRule().getId()) );
			params.addValue( "interest", model.getInterest() );
			params.addValue( "payerclaid", model.getPayer().getId() );
			params.addValue( "eventclaid", model.getEvent().getId() );
			params.addValue( "preapproved", PortalNumberUtils.booleanToInt( model.getPreApproved() ) );
			params.addValue( "antecipatedbilling", PortalNumberUtils.booleanToInt( model.getAntecipatedBilling() ) );
			params.addValue("quantityDays", model.getQuantityDays() == null ? null  : model.getQuantityDays());
			params.addValue("carbonBilling", PortalNumberUtils.booleanToInt(model.getCarbonBilling()));
			params.addValue( "position", model.getPosition() );
			
			log.trace( "[QUERY] proposalPayment.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o detalhe do pagamento: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o detalhe do pagamento.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM proposal_payment WHERE ppy_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposalPaymentdelete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o detalhe do pagamento" , e );
			throw new AppException( "Erro ao excluir o detalhe do pagamento.", e );
		}
	}
}
