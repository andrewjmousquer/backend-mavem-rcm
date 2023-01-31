package com.portal.dto;

import com.portal.model.ProposalDetailVehicle;
import com.portal.model.Seller;
import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProposalApprovalListDTO implements Comparable<ProposalApprovalListDTO> {

    @EqualsAndHashCode.Include
    private Integer id;
    private Long num;
    private String proposalNumber;
    private ProposalDetailVehicle proposalDetailVehicle;
    private Integer orderNumber;
    private String client;
    private String partner;
    private Seller executive;
    private String brandModel;
    private LocalDate createDate;
    private LocalDate validityDate;
    private Double discount;
    private Double totalPrice;

    @Override
    public int compareTo(ProposalApprovalListDTO proposalApprovalListDTO) {
        if (this.num > proposalApprovalListDTO.getNum()) {
            return -1;
        }
        if (this.num < proposalApprovalListDTO.getNum()) {
            return 1;
        }
        return 0;
    }
}
