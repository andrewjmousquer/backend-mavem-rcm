package com.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProposalApprovalCheckpointRules {


    boolean proposalComercialApprovalAll;
    boolean proposalComercialApprovalSalesTeam;


    public ProposalApprovalCheckpointRules(boolean b) {
        this.proposalComercialApprovalAll = b;
        this.proposalComercialApprovalSalesTeam = b;
    }
}
