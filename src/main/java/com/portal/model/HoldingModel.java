package com.portal.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HoldingModel {

	private Integer id;
	private String name;
	private byte[] logo;
	private String cnpj;
	private String label;
	private String socialName;
	private String stateRegistration;
	private String municipalRegistration;
	private AddressModel address;
	private Person person;
	private List<CustomerModel> customers;
	private List<CustomerModel> deletedCustomers;
	private Classifier type;
	
	public HoldingModel(String cnpj) {
		this.cnpj = cnpj;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HoldingModel other = (HoldingModel) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
