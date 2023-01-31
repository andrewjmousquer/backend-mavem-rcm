package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.SalesTeam;

public interface ISalesTeamDAO extends IBaseDAO<SalesTeam> {

    public Optional<SalesTeam> findBySeller(Integer id) throws AppException;

    public List<SalesTeam> listAll(Pageable pageable) throws AppException;

    public List<SalesTeam> search(SalesTeam model, Pageable pageable) throws AppException;

    public List<SalesTeam> find(SalesTeam model, Pageable pageable) throws AppException;

    List<SalesTeam> searchForm(String searchText, Pageable pageable) throws AppException;
}
