package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IPriceListPartnerDAO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Partner;
import com.portal.model.PriceList;
import com.portal.service.IPartnerService;
import com.portal.service.IPriceListPartnerService;
import com.portal.service.IPriceListService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PriceListPartnerService implements IPriceListPartnerService {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private IPartnerService partnerService;
	
	@Autowired
	private IPriceListPartnerDAO dao; 
	
	@Autowired
	private IPriceListService priceListService;
	
	@Override
	public void save(Integer priceListId, Integer partnerId) throws AppException, BusException {
		try {
			
			// REGRA: PTN-PRL-I1, PTN-PRL-U1
			if( priceListId == null ) {
				throw new BusException( "Não é possível salvar o relacionamento entre lista e preço e parceiro com a lista e preço inválida." );
			}
			
			// REGRA: PTN-PRL-I3, PTN-PRL-U3
			if( partnerId == null ) {
				throw new BusException( "Não é possível salvar o relacionamento entre lista e preço e parceiro com o parceiro inválido." );
			}
			
			// REGRA: PTN-PRL-I2, PTN-PRL-U2
			Optional<PriceList> priceListDB = this.priceListService.getById( priceListId );
			if( !priceListDB.isPresent() ) {
				throw new BusException( "Não é possível salvar o relacionamento entre lista e preço e parceiro com a lista e preço inválida ou inexistente." );
				
			} else if( priceListDB.get().getAllPartners().booleanValue() ) { // REGRA: PTN-PRL-I6, PTN-PRL-U6
				throw new BusException( "Não é permitido salvar um relacionamendo com parceiro com a lista de preço marcada como TODOS OS PARCEIROS." );
			}
			
			// REGRA: PTN-PRL-I4, PTN-PRL-U4
			Optional<Partner> partnerDB = this.partnerService.getById( partnerId );
			if( !partnerDB.isPresent() ) {
				throw new BusException( "Não é possível salvar o relacionamento entre lista e preço e parceiro com o parceiro inválido ou inexistente." );
			}

			this.dao.save( priceListId, partnerId );
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo salvar o relacionamento entre lista e preço e parceiro.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PriceListPartnerService.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}		
	}

	@Override
	public void delete(Integer priceListId, Integer partnerId) throws AppException, BusException {
		if( priceListId == null || priceListId.equals( 0 ) || partnerId == null || partnerId.equals( 0 ) ) {
			throw new BusException( "Não é possível excluir o relacionamento entre lista e preço e parceiro com o ID da lista e preço e/ou parceiro inválidos." );
		}
		
		this.dao.delete(priceListId, partnerId);
	}

	@Override
	public void deleteByPriceList(Integer priceListId) throws AppException, BusException {
		if( priceListId == null || priceListId.equals( 0 ) ) {
			throw new BusException( "Não é possível excluir o relacionamento entre lista e preço e parceiro com o ID da lista de preço inválido." );
		}
		
		this.dao.deleteByPriceList(priceListId);		
	}

	@Override
	public Optional<PriceList> getPriceList(Integer priceListId, Integer partnerId) throws AppException, BusException {
		if( partnerId == null || partnerId.equals( 0 ) || priceListId == null || priceListId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro ou da lista e preço inválido." );
		}
		
		return this.dao.getPriceList( priceListId, partnerId);
	}

	@Override
	public Optional<Partner> getPartner(Integer priceListId, Integer partnerId) throws AppException, BusException {
		if( partnerId == null || partnerId.equals( 0 ) || priceListId == null || priceListId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro ou da lista e preço inválido." );
		}
		
		return this.dao.getPartner( priceListId, partnerId);
	}

	@Override
	public List<PriceList> findByPartner(Integer partnerId) throws AppException, BusException {
		if( partnerId == null || partnerId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro inválido." );
		}
		
		return this.dao.findByPartner(partnerId);
	}
	
	@Override
	public List<Partner> findByPriceList(Integer priceListId) throws AppException, BusException {
		if( priceListId == null || priceListId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID da lista e preço inválido." );
		}
		
		return this.dao.findByPriceList(priceListId);
	}
}
