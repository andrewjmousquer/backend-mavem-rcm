package com.portal.dto;

import java.time.LocalDate;
import java.util.List;

import com.portal.enums.ProposalState;
import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.model.Partner;
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
public class ProposalApprovalFilterDTO {

    private Integer order;
    private List<Partner> partner;
    private String proposalNum;
    private String orderNum;
    private String name;
    private List<Seller> executive;
    private Brand brand;
    private List<Model> model;
    private String dateType;
    private LocalDate dateIni;
    private LocalDate dateEnd;
    private ProposalState status;
}
