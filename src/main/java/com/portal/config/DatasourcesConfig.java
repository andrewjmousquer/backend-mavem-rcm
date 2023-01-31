package com.portal.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DatasourcesConfig {
	
	@Primary
	@Bean(name = "dataSourcePortal")
	@ConfigurationProperties(prefix="spring.datasource-portal")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}
	@Bean(name = "namedTemplatePortal")
	public NamedParameterJdbcTemplate namedTemplateFlat(@Qualifier("dataSourcePortal") DataSource datasource) {
		return new NamedParameterJdbcTemplate( datasource );
	}

}