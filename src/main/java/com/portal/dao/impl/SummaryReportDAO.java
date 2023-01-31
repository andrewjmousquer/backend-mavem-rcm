package com.portal.dao.impl;

import java.util.Calendar;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.ISummaryReportDAO;
import com.portal.dto.SummaryReportMealDTO;
import com.portal.exceptions.AppException;
import com.portal.mapper.SummaryReportMapper;
import com.portal.utils.PortalTimeUtils;

@Repository
public class SummaryReportDAO extends BaseDAO implements ISummaryReportDAO {

	@Override
	public List<SummaryReportMealDTO> getPacientList(Calendar date) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			MapSqlParameterSource params = new MapSqlParameterSource();

			query.append("SELECT ");
			query.append("	mel.mel_id id, ");
			query.append("	mel.name meal, ");
			query.append("	ope.name, ");
			query.append("	count(0) total ");
			query.append("FROM transaction tra ");
			query.append("INNER JOIN meal mel ON tra.mel_id = mel.mel_id ");
			query.append("INNER JOIN beneficiary ben ON tra.ben_id = ben.ben_id ");
			query.append("INNER JOIN operator ope ON ben.ope_id = ope.ope_id ");
			query.append("INNER JOIN classifier status_cla ON tra.status_cla = status_cla.cla_id AND status_cla.value = 'APROVADO' ");
			query.append("WHERE DATE_FORMAT(tra.date,'%Y-%m-%d') = :date ");
			query.append("GROUP BY DATE_FORMAT(tra.date,'%Y-%m-%d'), mel.mel_id, ope.name");

			params.addValue("date", PortalTimeUtils.dateToSQLDate(date.getTime(), "yyyy-MM-dd"));

			return this.getJdbcTemplatePortal().query( query.toString(), params, new SummaryReportMapper() );
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppException(e);
		}
	}

	@Override
	public List<SummaryReportMealDTO> getEmployeeList(Calendar date) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			MapSqlParameterSource params = new MapSqlParameterSource();

			query.append("SELECT ");
			query.append("	mel.mel_id id, ");
			query.append("	mel.name meal, ");
			query.append("	cla.value name, ");
			query.append("	count(0) total ");
			query.append("FROM transaction tra ");
			query.append("INNER JOIN meal mel ON tra.mel_id = mel.mel_id ");
			query.append("INNER JOIN beneficiary ben ON tra.ben_id = ben.ben_id ");
			query.append("INNER JOIN classifier cla on ben.employee_cla = cla.cla_id ");
			query.append("INNER JOIN classifier status_cla ON tra.status_cla = status_cla.cla_id AND status_cla.value = 'APROVADO' ");
			query.append("WHERE DATE_FORMAT(tra.date,'%Y-%m-%d') = :date ");
			query.append("GROUP BY DATE_FORMAT(tra.date,'%Y-%m-%d'), mel.mel_id, cla.value");

			params.addValue("date", PortalTimeUtils.dateToSQLDate(date.getTime(), "yyyy-MM-dd"));

			return this.getJdbcTemplatePortal().query( query.toString(), params, new SummaryReportMapper() );
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppException(e);
		}
	}
}
