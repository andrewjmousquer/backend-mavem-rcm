package com.portal.service.imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.portal.dao.IUserCustomerDAO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CustomerModel;
import com.portal.service.IUserCustomerService;

@Service
public class UserCustomerService implements IUserCustomerService {

	@Autowired
	private IUserCustomerDAO dao;

	@Override
	public List<CustomerModel> listUserCustomer(Integer usrId) throws AppException, BusException {
		return dao.listUserCustomer(usrId);
	}

	@Override
	public void saveUserCustomer(Integer usrId, Integer cusId) throws AppException, BusException {
		dao.saveUserCustomer(usrId, cusId);
	}

	@Override
	public void saveUserCustomer(Integer usrId, List<CustomerModel> list) throws AppException, BusException {
		if (list != null) {
			dao.deleteUserCustomer(usrId, list);
			if (!CollectionUtils.isEmpty(list)) {
				for (CustomerModel customer : list) {
					this.saveUserCustomer(usrId, customer.getId());
				}
			}
		}
	}

	@Override
	public void deleteUserCustomer(Integer usrId, Integer cusId) throws AppException, BusException {
		dao.deleteUserCustomer(usrId, cusId);
	}

	@Override
	public void deleteUserCustomer(Integer usrId, List<CustomerModel> list) throws AppException, BusException {
		if (!CollectionUtils.isEmpty(list)) {
			dao.deleteUserCustomer(usrId, list);
		}
	}

}
