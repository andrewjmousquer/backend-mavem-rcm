package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.dto.SummaryReportMealDTO;
import com.portal.dto.SummaryReportTypeDTO;

public class SummaryReportMapper implements ResultSetExtractor<List<SummaryReportMealDTO>> {

	@Override
	public List<SummaryReportMealDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<SummaryReportMealDTO> list = new ArrayList<>();

		if (rs != null) {
			SummaryReportMealDTO mealDTO = null;
			
			while (rs.next()) {
				String typeName = rs.getString("name");
				Integer total = rs.getInt("total");

				SummaryReportTypeDTO item = new SummaryReportTypeDTO();
				item.setName(typeName);
				item.setTotal(total);
				
				String mealName = rs.getString("meal");
				
				if (mealDTO == null || !mealDTO.getName().equalsIgnoreCase(mealName)) {
					mealDTO = new SummaryReportMealDTO();
					mealDTO.setName(mealName);
					mealDTO.setList(new ArrayList<SummaryReportTypeDTO>());
					mealDTO.getList().add(item);
					list.add(mealDTO);
				} else {
					mealDTO.getList().add(item);
				}
			}
		}

		return list;
	}

}
