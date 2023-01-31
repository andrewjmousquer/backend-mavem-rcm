package com.portal.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Item;

public interface IItemService extends IBaseService<Item> {
	
	public void updateFile(Integer id, String column, String value) throws AppException;
	
	public List<Item> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<Item> find( Item model, Pageable pageable ) throws AppException, BusException;
	
	public List<Item> search( Item model, Pageable pageable ) throws AppException, BusException;

	public Boolean store(MultipartFile file, Integer id, String type, UserProfileDTO userProfile) throws AppException, BusException;
	
	public String getItemImage(String id) throws AppException, IOException;
	
	public byte[] getImageIcon(Integer id) throws AppException, BusException;

}
