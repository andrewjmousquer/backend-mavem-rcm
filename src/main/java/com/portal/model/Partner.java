package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.portal.dto.PartnerDTO;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Partner {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Person person;

	private Integer additionalTerm;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Classifier situation;
		
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Channel channel;

	private PartnerGroup partnerGroup;
	
	private boolean isAssistance;
		
	private List<Brand> brandList;
	
	private List<PartnerPerson> employeeList;
	
	private List<Seller> sellerList;
	
	public Partner( Integer id ) {
		this.id = id;
	}
	
	public Partner( Integer id, Classifier situation, Channel channel, Person person ) {
		this.id = id;
		this.situation = situation;
		this.channel = channel;
		this.person = person;
	}
	
	public Partner( Integer id, Classifier situation, Channel channel, Person person, PartnerGroup group ) {
		this.id = id;
		this.situation = situation;
		this.channel = channel;
		this.person = person;
		this.partnerGroup = group;
	}
	
	public static Partner toEntity( PartnerDTO dto ) {
		if( dto == null ) {
			return null;
		}
		
		return Partner.builder()
							.id( dto.getId() )
							.person( Person.toEntity(dto.getPerson() ))
							.situation( dto.getSituation() )
							.additionalTerm( dto.getAdditionalTerm() )
							.channel( Channel.toEntity( dto.getChannel() ) )
							.partnerGroup( PartnerGroup.toEntity( dto.getPartnerGroup() ) )
							.brandList( Brand.toEntity( dto.getBrandList() ) )
							.employeeList( PartnerPerson.toEntity( dto.getEmployeeList()) )
							.sellerList ( Seller.toEntity( dto.getSellerList() ) )
							.isAssistance(dto.getIsAssistance())
							.build();
	}
	
    public static List<Partner> toEntity(List<PartnerDTO> list) {
        if (list == null) {
            return null;
        }
        return list.stream().map(Partner::toEntity).collect(Collectors.toList());
    }
	
	
}