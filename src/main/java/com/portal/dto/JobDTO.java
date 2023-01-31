package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Job;

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
public class JobDTO {

    @EqualsAndHashCode.Include
    private Integer id;

    private String name;

    private Integer level;


    public static JobDTO toDTO(Job job) {
        if (job == null) {
            return null;
        }
        return JobDTO.builder()
                .id(job.getId())
                .name(job.getName())
                .level(job.getLevel())
                .build();
    }

    public static List<JobDTO> toDTO(List<Job> jobs) {
        if (jobs == null) {
            return null;
        }
        return jobs.stream()
                .map(JobDTO::toDTO)
                .collect(Collectors.toList());

    }

}
