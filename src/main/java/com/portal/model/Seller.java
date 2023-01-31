package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.Null;

import com.portal.dto.SellerDTO;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Seller {

    @EqualsAndHashCode.Include
    @Null(groups = {ValidationHelper.OnSave.class})
    @NotNullNotZero(groups = {ValidationHelper.OnUpdate.class})
    private Integer id;
    private Person person;
    private UserModel user;
    private Job job;
    private List<SalesTeam> salesTeamList;
    private List<Partner> partnerList;
    private List<Seller> agentList;
    
    public Seller( Integer id, String person) {
    	this.id = id;
    	this.person.getName();
    }

    public static Seller toEntity(SellerDTO sellerDto) {

        if (sellerDto == null) {
            return null;
        }

        return Seller.builder()
                .id(sellerDto.getId())
                .job(Job.toEntity(sellerDto.getJob()))
                .person(Person.toEntity(sellerDto.getPerson()))
                .user(UserModel.toEntity(sellerDto.getUser()))
                .salesTeamList(SalesTeam.toEntity(sellerDto.getSalesTeamList()))
                .partnerList(Partner.toEntity(sellerDto.getPartnerList()))
                .agentList(Seller.toEntity(sellerDto.getAgentList()))
                .build();
    }

    public static List<Seller> toEntity(List<SellerDTO> list) {
        if (list == null) {
            return null;
        }
        return list.stream().map(Seller::toEntity).collect(Collectors.toList());
    }
}
