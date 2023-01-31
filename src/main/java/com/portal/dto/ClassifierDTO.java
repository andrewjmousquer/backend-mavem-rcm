package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Classifier;

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
public class ClassifierDTO {

	private Integer id;
	private String value;
	private String type;
	private String label;
	private String description;
		
    public static ClassifierDTO toDto(Classifier entity) {
        if (entity == null) {
            return null;
        }

        return ClassifierDTO.builder()
                .id(entity.getId())
                .value(entity.getValue())
                .type(entity.getType())
                .label(entity.getLabel())
                .description(entity.getDescription())
                .build();
    }

    public static List<ClassifierDTO> toDTO(List<Classifier> list) {
        if (list == null) {
            return null;
        }
        return list.stream().map(ClassifierDTO::toDto).collect(Collectors.toList());
    }
}
