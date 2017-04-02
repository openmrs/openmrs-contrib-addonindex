package org.openmrs.addonindex.legacy;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;

public class LegacyFindModulesResponse {
	
	private Integer sEcho;
	
	private Integer iTotalRecords;
	
	private List<String[]> aaData = new ArrayList<>();
	
	@JsonGetter("sColumns")
	public String getsColumns() {
		return "Action,Name,Version,Author,Description";
	}
	
	public void addRow(String url, String name, String version, String owner, String description) {
		aaData.add(new String[] { url, name, version, owner, description });
	}
	
	public Integer getsEcho() {
		return sEcho;
	}
	
	public void setsEcho(Integer sEcho) {
		this.sEcho = sEcho;
	}
	
	public Integer getiTotalRecords() {
		return iTotalRecords;
	}
	
	public void setiTotalRecords(Integer iTotalRecords) {
		this.iTotalRecords = iTotalRecords;
	}
	
	public Integer getiTotalDisplayRecords() {
		return aaData.size();
	}
	
	public List<String[]> getAaData() {
		return aaData;
	}
	
	public void setAaData(List<String[]> aaData) {
		this.aaData = aaData;
	}
}
