 package com.portal.model;

import java.util.Objects;

import javax.validation.constraints.Null;

import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

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
public class ProposalDetailVehicleItem {
	
	@EqualsAndHashCode.Include
	@NotNullNotZero(groups = {OnUpdate.class})
	@Null(groups = {OnSave.class})
	private Integer id;
	
	@EqualsAndHashCode.Include
	private ProposalDetailVehicle proposalDetailVehicle;
	
	@EqualsAndHashCode.Include
	private PriceItemModel itemPriceModel;
	
	@EqualsAndHashCode.Include
	private PriceItem itemPrice;
	
	@EqualsAndHashCode.Include
	private Seller seller;
	
	private Double amountDiscount;
	
	private Double percentDiscount;
	
	private Double finalPrice;
	
	private Boolean forFree;

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProposalDetailVehicleItem other = (ProposalDetailVehicleItem) obj;
		return Objects.equals(id, other.id);
	}

}