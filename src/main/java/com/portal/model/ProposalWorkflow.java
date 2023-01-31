package com.portal.model;

import com.portal.enums.ProposalEvents;
import com.portal.enums.ProposalState;

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
public class ProposalWorkflow {

	ProposalState from;
	
	ProposalState to;
	
	ProposalEvents action;
	
}