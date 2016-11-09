package com.rair.jsfbeans;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import com.rair.domain.Employee;
import com.rair.domain.Person;

@ManagedBean(name = "loginServiceBean")
@ApplicationScoped
public class LoginServiceBean {

	private Map<String, Boolean> loginMap;

	@PostConstruct
	public void init() {
		loginMap = new HashMap<>();
	}

	public Map<String, Boolean> getLoginMap() {
		return loginMap;
	}

	public void setLoginMap(Map<String, Boolean> loginMap) {
		this.loginMap = loginMap;
	}

	public void login(String email) {
		System.out.println("Loggin in: " + email);
		loginMap.put(email, true);
	}

	public void logout(Person person) {
		System.out.println("Logout: " + person.getEmailAddress());
		loginMap.put(person.getEmailAddress(), false);
	}

}
