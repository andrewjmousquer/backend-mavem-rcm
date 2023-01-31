package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.portal.dto.proposal.ProposalDTO;
import com.portal.model.ProposalApprovalRule;
import com.portal.model.ProposalDetail;
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
public class ProposalDetailDTO {

	@EqualsAndHashCode.Include
	@NotNullNotZero(groups = {OnUpdate.class})
	@Null(groups = {OnSave.class})
	private Integer id;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ProposalDTO proposal;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private SellerDTO seller;
	
	private SellerDTO internSale;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ChannelDTO channel;
	
	private PartnerDTO partner;

	private String purchaseOrderService;

	private String purchaseOrderProduct;

	private String purchaseOrderDocumentation;
	
	public static ProposalDetailDTO toDTO(ProposalDetail entity) {
        if (entity == null) {
            return null;
        }
        return ProposalDetailDTO.builder()
                .id(entity.getId())
                .proposal(ProposalDTO.toDTO(entity.getProposal()))
                .seller(SellerDTO.toDTO(entity.getSeller()))
                .internSale(SellerDTO.toDTO(entity.getInternSale()))
                .channel(ChannelDTO.toDTO(entity.getChannel()))
                .partner(PartnerDTO.toDTO(entity.getPartner()))
				.purchaseOrderService(entity.getPurchaseOrderService())
				.purchaseOrderProduct(entity.getPurchaseOrderProduct())
				.purchaseOrderDocumentation(entity.getPurchaseOrderDocumentation())
                .build();
    }

	public static List<ProposalApprovalRuleDTO> toDTO(List<ProposalApprovalRule> proposalApprovalRules) {
	    if (proposalApprovalRules == null) {
	        return null;
	    }
	    return proposalApprovalRules.stream()
	            .map(ProposalApprovalRuleDTO::toDTO)
	            .collect(Collectors.toList());
	}
}
