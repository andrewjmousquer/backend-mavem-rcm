package com.portal.model;

import com.portal.enums.LeadEvents;
import com.portal.enums.LeadState;

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
public class LeadWorkflow {

	LeadState from;
	
	LeadState to;
	
	LeadEvents action;
	
}