package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.model.SalesTeam;
import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Seller;

public interface ISellerService extends IBaseService<Seller> {

    public List<Seller> listAll(Pageable pageable) throws AppException, BusException;

    public List<Seller> searchForm(String text, Pageable pageable) throws AppException;

    public List<Seller> search(Seller model, Pageable pageable) throws AppException, BusException;

    public List<Seller> find(Seller model, Pageable pageable) throws AppException;

    public Optional<Seller> getByUser(Integer id) throws AppException, BusException;

    public List<Seller> getByAgent(Integer id) throws AppException, BusException;

    public List<Seller> getBySalesTeam(List<SalesTeam> salesTeamList) throws AppException;
}
