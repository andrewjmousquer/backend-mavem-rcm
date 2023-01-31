package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.AddressModel;
import com.portal.model.Classifier;
import com.portal.model.Job;
import com.portal.model.Person;
import com.portal.model.Seller;
import com.portal.model.UserModel;
import com.portal.utils.PortalNumberUtils;

public class SellerMapper implements RowMapper<Seller> {

    @Override
    public Seller mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Classifier personType = Classifier.builder()
				.id(rs.getInt("per_cla_id"))
				.value(rs.getString("per_cla_value"))
				.type(rs.getString("per_cla_type"))
				.label(rs.getString("per_cla_label"))
				.build();
    	
        Person person = Person.builder()
                .id(rs.getInt("per_id"))
                .name(rs.getString("per_name"))
                .jobTitle(rs.getString("job_title"))
                .cpf(rs.getString("cpf"))
                .cnpj(rs.getString("cnpj"))
                .rg(rs.getString("rg"))
                .rne(rs.getString("rne"))
                .classification(personType)
                .address(new AddressModel(rs.getInt("add_id")))
                .build();

        UserModel user = UserModel.builder()
        		.id(rs.getInt("usr_id"))
        		.username(rs.getString("username"))
        		.person(person)
        		.enabled(PortalNumberUtils.intToBoolean( rs.getInt( "enabled" )))
        		.build();
        
        Job job = Job.builder()
        		 .id(rs.getInt("job_id"))
                 .name(rs.getString("job_name"))
                 .build();
        
        return Seller.builder()
                .id(rs.getInt("sel_id"))
                .person(person)
                .user(user)
                .job(job)
                .build();
    }
}
