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
public class ProposalItemModel {
	
    private Integer itmId;
    private String nameItem;
    private String cod;
    private Integer seq;
    private Boolean forFree;
    private Boolean generic;
    private Classifier mandatory;
    private String file;
    private String icon;
    private String description;
    private String hyperlink;
    private Integer term;
    private Boolean termWorkDay;
    private Boolean highlight;
    private Classifier responsability;
    private Double price;
    private Integer pimId;
    private Integer prlId;
    private Integer ittId;
    private ItemType itemType;
}
