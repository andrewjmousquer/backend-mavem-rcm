package com.portal.exceptions;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class APIErrorResponse {
    private String status;

    private int statusCode;

    private String message;

    private LocalDateTime timeStamp;
    
    private Object properties;
}