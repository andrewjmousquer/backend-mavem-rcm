package com.portal.dto;

import java.time.LocalDateTime;

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
public class DocumentDTO {

    private Integer id;
    private String fileName;
    private String filePath;
    private String contentType;
    private String description;
    private LocalDateTime createDate;
    private Classifier type;
}
