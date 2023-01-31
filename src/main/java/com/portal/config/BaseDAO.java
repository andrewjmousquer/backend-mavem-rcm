package com.portal.config;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@Configuration
public class BaseDAO extends NamedParameterJdbcDaoSupport{

	@Value("${spring.datasource-portal.defaultSchema}")
	public String schemaName;
	
	@Autowired
	@Qualifier("namedTemplatePortal")
	private NamedParameterJdbcTemplate jdbcTemplatePortal;
	
	public NamedParameterJdbcTemplate getJdbcTemplatePortal() {
		return jdbcTemplatePortal;
	}

	@Autowired
	private DataSource dataSource;
    
	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
    }

	@SuppressWarnings("rawtypes")
	public Integer getKey(KeyHolder keyHolder) throws InvalidDataAccessApiUsageException, DataRetrievalFailureException {
		Integer returnInteger = null;
		List keyList = keyHolder.getKeyList();
		
		if(keyList.size() == 1) {
			returnInteger = keyHolder.getKey().intValue();
		} else if (keyList.size() > 1 || ((Map) keyList.get(0)).size() > 1) {
			Iterator keyIter = ((Map) keyList.get(0)).values().iterator();
			if (keyIter.hasNext()) {
				Object key = keyIter.next();
				returnInteger = ((Number) key).intValue();
			}
		} else {
			throw new DataRetrievalFailureException("Erro ao obter chave.");
		}
		
		return returnInteger;
		
	}
	
	public String mapLike(String value) {
		if(value != null) {
			return "%" + value + "%";
		}
		
		return null;
	}
	
}
