package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Job;

public interface IJobService extends IBaseService<Job> {

    public List<Job> listAll(Pageable pageable) throws AppException, BusException;

    public List<Job> searchForm(String text, Pageable pageable) throws AppException, BusException;

    public List<Job> find(Job model, Pageable pageable) throws AppException, BusException;

    public List<Job> search(Job model, Pageable pageable) throws AppException, BusException;
}
