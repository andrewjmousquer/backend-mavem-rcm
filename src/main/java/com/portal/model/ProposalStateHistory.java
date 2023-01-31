package com.portal.model;

import com.portal.enums.ProposalState;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProposalStateHistory {

    @EqualsAndHashCode.Include
    @Null(groups = {OnSave.class})
    @NotNullNotZero(groups = {OnUpdate.class})
    private Integer id;

    @NotNull(groups = {OnUpdate.class, OnSave.class})
    private Proposal proposal;

    private Classifier statusOld;

    private Classifier statusNew;

    private SalesOrder salesOrder;

    @NotNull(groups = {OnUpdate.class, OnSave.class})
    private UserModel user;

    @NotNull(groups = {OnUpdate.class, OnSave.class})
    private LocalDateTime statusDate;
}
