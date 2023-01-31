package com.portal.dao;

import java.util.List;

import com.portal.dto.ProductWithPriceListIdDTO;
import com.portal.dto.ProposalSearchDTO;
import com.portal.dto.ProposalSearchRulesDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.dto.form.ProductItemFormDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Partner;
import com.portal.model.Product;
import com.portal.model.ProposalFrontForm;
import com.portal.model.ProposalItemModelType;
import com.portal.model.ProposalItemType;
import com.portal.model.ProposalProduct;
import com.portal.model.Seller;

public interface IProposalFormDAO {

	public List<ProposalFrontForm> getListProposalFrontForm(ProposalSearchDTO dto, Integer proposalDaysLimit, Seller seller, UserProfileDTO userProfile  ) throws AppException;

	public List<Partner> getListPartnerByChannel(Integer id) throws AppException;
	
	public List<Seller> getlistExecutiveByPartner(Integer id) throws AppException;
	
	public List<Seller> getlistSellerByExecutive(Integer id) throws AppException;
	
	public List<Brand> getlistBrandByPartner(Integer id) throws AppException, BusException;

	List<Brand> getlistBrandByPartner(String ptnId, String chnId) throws AppException, BusException;

	public List<Product> getlistProductByModel(Integer id, Integer year) throws AppException, BusException;

	public List<ProductWithPriceListIdDTO> getlistProductByModelV1(Integer id, Integer year, Integer mdlId, Integer channelId) throws AppException, BusException;
	
	public List<ProposalProduct> getProduct(ProductItemFormDTO productItemFormDTO) throws AppException, BusException;
	
	public List<ProposalItemType> getListItemProduct(ProductItemFormDTO productItemFormDTO) throws AppException, BusException;

	public List<ProposalItemModelType> getListItemModelProduct(ProductItemFormDTO productItemFormDTO) throws AppException, BusException;

	public List<Seller> getlistExecutive() throws AppException;

	public List<Partner> getListPartner() throws AppException;

	public List<Seller> getlistInternalSeller() throws AppException;

	public List<Seller> getListExecutiveByAgent(Integer userId) throws AppException;

	public List<Seller> getListExecutiveBySalesTeam(Integer userId) throws AppException;

	public List<Partner> getListPartnerByChannelAndSeller(Integer channelId, Integer sellerId) throws AppException;
}
