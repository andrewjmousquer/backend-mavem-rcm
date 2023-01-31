package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.w3c.dom.DocumentType;

import com.portal.model.Classifier;
import com.portal.model.Document;
import com.portal.model.Person;
import com.portal.model.UserModel;

public class DocumentMapper implements RowMapper<Document> {
	@Override
	public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		int usrId = rs.getInt( "usr_id" );
		UserModel user = null;
		if( usrId != 0 ) {
			user = new UserModel( usrId );
			user.setPerson(new Person(rs.getString("name"), null));
		}
		
		Classifier documentType = Classifier.builder()
				.id(rs.getInt("cla_id"))
				.value(rs.getString("cla_value"))
				.label(rs.getString("cla_label"))
				.type(rs.getString("cla_type"))
				.build();
		
		return Document.builder()
						.id( rs.getInt( "doc_id" ) )
						.fileName( rs.getString( "file_name" ) )
						.contentType( rs.getString( "content_type" ) )
						.description( rs.getString( "description" ) )
						.filePath( rs.getString( "file_path" ) )
						.createDate( rs.getTimestamp( "create_date" ).toLocalDateTime() )
						.type( documentType )
						.user( user )
						.build();
	}
}
