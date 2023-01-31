package com.portal.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.portal.dto.ClassifierDTO;

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
public class Classifier {

	private Integer id;
	private String value;
	private String type;
	private String label;
	private String description;
		
	public Classifier(Integer id) {
		this.id = id;
	}
	
	public Classifier(String type) {
		this.type = type;
	}
	
	public Classifier(int id, String value) {
		if(id > 0) {
			this.id = id;
		}
		this.value = value;
	}
	
	public Classifier(String value, String type) {
		if(value != null) this.value = value;
		if(type != null) this.type = type;
	}
	
	public Classifier(int id, String value, String type) {
		if(id > 0) this.id = id;
		if(value != null) this.value = value;
		if(type != null) this.type = type;
	}
	
	public Classifier(int id, String value, String type, String label) {
		if(id > 0) this.id = id;
		if(value != null) this.value = value;
		if(type != null) this.type = type;
		if(label != null) this.label = label;
	}
	
	public Classifier(String value, String type, String label) {
		if(value != null) this.value = value;
		if(type != null) this.type = type;
		if(label != null) this.label = label;
	}
	
    public static Classifier toEntity(ClassifierDTO dto) {
        if (dto == null) {
            return null;
        }

        return Classifier.builder()
                .id(dto.getId())
                .value(dto.getValue())
                .type(dto.getType())
                .label(dto.getLabel())
                .description(dto.getDescription())
                .build();
    }

    public static List<Classifier> toEntity(List<ClassifierDTO> list) {
        if (list == null) {
            return null;
        }
        return list.stream().map(Classifier::toEntity).collect(Collectors.toList());
    }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Classifier other = (Classifier) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
}
