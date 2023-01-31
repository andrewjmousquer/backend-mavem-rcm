package com.portal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
public class ProposalProduct {
	
	private String nameItemType;
    private Integer prdId;
    private String nameItem;
    private String cod;
    private Boolean forFree;
    private Double price;
    private Integer pprId;
    private Integer prlId;
    private Integer ptnId;
}
