package com.portal.dto;

import java.util.List;

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
public class QualificationTreePathDTO {

	@EqualsAndHashCode.Include
	private Integer id;

	private Integer seq;

	private String name;
	
	private Boolean active;
	
	private Boolean required;
	
	private Integer level;

	private String breadcrumbIdPath;
	
	private String breadcrumbNamePath;

	private Integer parentId;
	
	private List<QualificationTreePathDTO> childrens;
}
