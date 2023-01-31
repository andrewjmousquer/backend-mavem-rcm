package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Channel;

public interface IChannelDAO extends IBaseDAO<Channel> {
	
	public List<Channel> listAll( Pageable pageable ) throws AppException;
	
	public List<Channel> find( Channel model, Pageable pageable ) throws AppException;
	
	public List<Channel> search( Channel model, Pageable pageable ) throws AppException;
	
	public boolean hasPartnerRelationship(Integer chnId) throws AppException;
	
	public boolean hasPriceListRelationship(Integer chnId) throws AppException;
	
	public Optional<Channel> getChannelByProposal(Integer ppsId) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Channel> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Channel, Pageable)}
	 */
	@Deprecated
	public Optional<Channel> find( Channel model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(Channel, Pageable)}
	 */
	@Deprecated
	public List<Channel> search( Channel model ) throws AppException;

	
}
