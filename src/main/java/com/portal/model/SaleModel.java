package com.portal.model;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Setter
@Getter
public class SaleModel {

	private Long id;

	private String customer;

	private String contact;

	private Date date;
	
	private Date dateEnd;

	private BigDecimal value;
	
	private BigDecimal firstPayment;
	
	private BigDecimal tax;

	private Integer portion;
	
	private String paymentType;

	private String comments;
	
	private UserModel user;
}
