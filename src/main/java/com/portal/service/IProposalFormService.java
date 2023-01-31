package com.portal.service;

import java.io.File;
import java.util.List;

import org.springframework.core.io.InputStreamResource;

import com.portal.dto.ProductWithPriceListIdDTO;
import com.portal.dto.ProposalSearchDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.dto.form.ProductItemFormDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Partner;
import com.portal.model.Product;
import com.portal.model.ProposalFormProduct;
import com.portal.model.ProposalFrontForm;
import com.portal.model.Seller;

public interface IProposalFormService {
	
	public List<ProposalFrontForm> getListProposalFrontForm(UserProfileDTO userProfile) throws AppException;
	
	public List<ProposalFrontForm>  getListProposalFrontForm(ProposalSearchDTO dto, UserProfileDTO userProfile) throws AppException;
	
	public List<Partner> getListPartnerByChannel(Integer id) throws AppException, BusException;
	
	public List<Seller> getExecutiveList(UserProfileDTO userProfile) throws AppException, BusException;
	
	public List<Seller> getlistSellerByExecutive(Integer id) throws AppException, BusException;
	
	public List<Brand> getlistBrandByPartner(Integer id) throws AppException, BusException;

    List<Brand> getlistBrandByPartner(String ptnId, String chnId) throws AppException, BusException;

    public List<Product> getlistProductByModel(Integer id, Integer year) throws AppException, BusException;

    List<ProductWithPriceListIdDTO> getlistProductByModelV1(Integer id, Integer year, Integer ptnId, Integer chnId) throws AppException, BusException;

    public ProposalFormProduct getProductItems(ProductItemFormDTO productItemFormDTO) throws AppException, BusException;

	public List<Seller> getlistExecutive() throws AppException, BusException;

	public List<Partner> getListPartner() throws AppException, BusException;

	public List<Seller> getlistInternalSeller() throws AppException, BusException;

	public List<Partner> getListPartnerByChannelAndSeller(Integer channelId, Integer sellerId) throws AppException, BusException;
	
    public byte[] generateProposalReport(String proposalNumber) throws AppException, BusException;

}
