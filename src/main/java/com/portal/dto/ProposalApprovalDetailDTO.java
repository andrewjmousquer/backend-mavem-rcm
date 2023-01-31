package com.portal.dto;

import com.portal.enums.ProposalRisk;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Model;
import com.portal.model.Partner;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.ProposalDetailVehicle;
import com.portal.model.Seller;
import com.portal.model.VehicleModel;

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
public class ProposalApprovalDetailDTO {

    private Person client;

    private Proposal proposal;

    private Partner partner;

    private Seller seller;

    private Brand brand;

    private Model model;

    private ProposalDetailVehicle proposalDetailVehicle;

    private VehicleModel vehicle;

    private ProposalRisk risk;

    private Channel channel;
}
