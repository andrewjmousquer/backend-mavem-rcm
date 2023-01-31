package com.portal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class ProposalFrontForm implements Comparable<ProposalFrontForm> {

    @EqualsAndHashCode.Include
    private Integer id;

    private Long num;

    private String cod;

    private Boolean immediateDelivery;

    private String proposalNumber;

    private Integer orderNumber;

    private Integer statusId;

    private ProposalState status;
    
    private Classifier statusCla;

    private String client;

    private String partner;

    private Seller executive;

    private String brandModel;

    private LocalDateTime createDate;

    private LocalDate validityDate;

    private Long validityDays;

    private LocalDateTime dateFollowUp;

    private Long daysFollowUp;

    private Double totalPrice;

    private Boolean isEdit;


    @Override
    public int compareTo(ProposalFrontForm proposalFrontForm) {
        if (this.num > proposalFrontForm.getNum()) {
            return -1;
        }
        if (this.num < proposalFrontForm.getNum()) {
            return 1;
        }
        return 0;
    }
}

