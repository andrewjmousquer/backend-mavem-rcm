package com.portal.dto.form;

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
public class ProductItemFormDTO {
	
	private Integer prdId;
	private Integer chnId;
	private Integer ptnId;
	private Integer mdlId;
	private Integer brdId;
	private Integer year;
	private Integer prlId;
}
