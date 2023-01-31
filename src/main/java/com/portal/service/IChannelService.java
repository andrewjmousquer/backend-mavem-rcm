package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Channel;

public interface IChannelService extends IBaseService<Channel> {
	
	public List<Channel> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<Channel> find( Channel model, Pageable pageable ) throws AppException, BusException;
	
	public List<Channel> search( Channel model, Pageable pageable ) throws AppException, BusException;
	
	public  Optional<Channel> getChannelByProposal(Integer ppsId) throws AppException, BusException;
	
}
