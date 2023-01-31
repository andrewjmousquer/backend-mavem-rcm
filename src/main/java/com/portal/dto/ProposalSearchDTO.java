package com.portal.dto;

import java.time.LocalDate;
import java.util.List;

import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.model.Partner;
import com.portal.model.PartnerGroup;
import com.portal.model.Seller;

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
public class ProposalSearchDTO {

	private List<String> status;
    private List<Partner> partner;
    private String proposalnum;
	private String ordernum;
    private String name;
    private List<Seller> executive;
	private Brand brand;
    private List<Model> model;
    private List<PartnerGroup> partnerGroup;
    private String immediateDelivery;
    private String dateType;
    private LocalDate dateIni;
    private LocalDate dateEnd;
}
