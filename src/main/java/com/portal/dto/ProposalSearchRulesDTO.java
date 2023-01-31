package com.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProposalSearchRulesDTO {

    boolean proposalViewAll;
    boolean proposalViewPromptDelivery;
    boolean proposalViewOwner;
    boolean proposalViewPreposto;
    boolean proposalViewTeam;

    public ProposalSearchRulesDTO(boolean b) {
        this.proposalViewAll = b;
        this.proposalViewPromptDelivery = b;
        this.proposalViewOwner = b;
        this.proposalViewPreposto = b;
        this.proposalViewTeam = b;
    }
}
