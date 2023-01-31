package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.portal.dto.ProposalDetailDTO;
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
public class ProposalDetail {

	@EqualsAndHashCode.Include
	@NotNullNotZero(groups = {OnUpdate.class})
	@Null(groups = {OnSave.class})
	private Integer id;

	@JsonBackReference
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Proposal proposal;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Seller seller;
	
	private Seller internSale;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Channel channel;
	
	private Partner partner;

	private UserModel user;

	private String purchaseOrderService;

	private String purchaseOrderProduct;

	private String purchaseOrderDocumentation;

	public static ProposalDetail toEntity(ProposalDetailDTO dto) {
        if (dto == null) {
            return null;
        }
        return ProposalDetail.builder()
	                .id(dto.getId())
	                .proposal(Proposal.builder().id(dto.getProposal().getProposal().getId()).build())
	                .seller(Seller.toEntity(dto.getSeller()))
	                .internSale(Seller.toEntity(dto.getInternSale()))
	                .channel(Channel.toEntity(dto.getChannel()))
	                .partner(Partner.toEntity(dto.getPartner()))
					.purchaseOrderService(dto.getPurchaseOrderService())
				 	.purchaseOrderProduct(dto.getPurchaseOrderProduct())
					.purchaseOrderDocumentation(dto.getPurchaseOrderDocumentation())
	                .build();
	 }

    public static List<ProposalDetail> toEntity(List<ProposalDetailDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(ProposalDetail::toEntity)
                .collect(Collectors.toList());

    }
	
}