package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Person;
import com.portal.model.UserModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
	
	private Integer id;
	private String username;
	private Boolean enabled;
	private Person person;
	
	public static UserDTO toDTO(UserModel entity) {

		if (entity == null) {
	        return null;
	    }
	
	    return UserDTO.builder()
	            .id(entity.getId())
	            .username(entity.getUsername())
	            .enabled(entity.getEnabled())
	            .person(entity.getPerson())
	            .build();
	}

  public static List<UserDTO> toDTO(List<UserModel> list) {
      if (list == null) {
          return null;
      }
      return list.stream().map(UserDTO::toDTO).collect(Collectors.toList());
  }

}
