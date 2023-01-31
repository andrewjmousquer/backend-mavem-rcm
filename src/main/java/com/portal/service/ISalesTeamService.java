package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.SalesTeam;

public interface ISalesTeamService extends IBaseService<SalesTeam> {

    public Optional<SalesTeam> findBySeller (Integer id) throws AppException;

    public List<SalesTeam> find(SalesTeam model, Pageable pageable) throws AppException;

    public List<SalesTeam> listAll(Pageable pageable) throws AppException, BusException;

    public List<SalesTeam> search(SalesTeam model, Pageable pageable) throws AppException;
}
