package com.smart.service;

//import com.smart.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smart.dao.MainDao;

import java.util.List;

@Service
public class MainService {
	private MainDao mainDao;



	@Autowired
	public void setMainDao(MainDao mainDao) {
		this.mainDao = mainDao;
	}

}
