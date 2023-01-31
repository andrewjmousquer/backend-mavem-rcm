package com.portal.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProposalFormProduct {

	private ProposalProduct proposalProduct;
	private List<ProposalItemType> proposalItemTypes;
	private List<ProposalItemModelType> proposalItemModelTypes;
}
