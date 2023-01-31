package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Bank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BankDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private String name;
	
	private String code;
	
	private Boolean active;
	
	public static BankDTO toDTO( Bank model ) {
		
		if( model == null ) {
			return null;
		}
		
		return BankDTO.builder()
							.id( model.getId() )
							.name( model.getName() )
							.code( model.getCode() )
							.active( model.getActive() )
							.build();
	}

	public static List<BankDTO> toDTO( List<Bank> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( BankDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
