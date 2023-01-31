package com.portal.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.portal.dto.PriceListDTO;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PriceList {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotBlank(groups = {OnUpdate.class, OnSave.class})
	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String name;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private LocalDateTime start;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private LocalDateTime end;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Channel channel;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean allPartners;
	
	public static PriceList toEntity( PriceListDTO dto ) {
		
		if( dto == null ) {
			return null;
		}
		
		return PriceList.builder()
							.id( dto.getId() )
							.name( dto.getName() )
							.start( dto.getStart() )
							.end( dto.getEnd() )
							.channel( Channel.toEntity( dto.getChannel() ) )
							.allPartners( dto.getAllPartners() )
							.build();
	}
	
	public static List<PriceList> toEntity( List<PriceListDTO> dtos ) {
		
		if( dtos == null ) {
			return null;
		}
		
		return dtos.stream()
						.map( PriceList::toEntity )
						.collect( Collectors.toList() );	
		
	}
}
