package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.model.SalesTeam;
import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Seller;

public interface ISellerDAO extends IBaseDAO<Seller> {

    public List<Seller> listAll(Pageable pageable) throws AppException;

    public List<Seller> searchForm(String text, Pageable pageable) throws AppException;

    public List<Seller> find(Seller model, Pageable pageable) throws AppException;

    public List<Seller> search(Seller model, Pageable pageable) throws AppException;

    public Optional<Seller> getByUser(Integer id) throws AppException;
    
    public Integer checkSellerDocument(String text) throws AppException;

    public List<Seller> getByAgent(Integer id) throws AppException;

    public List<Seller> getBySalesTeam(List<SalesTeam> salesTeamList) throws AppException;
}
