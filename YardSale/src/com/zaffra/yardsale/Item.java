/**
 * This is a trivial Java object that Gson instantiates with JSON data that's
 * fetched from the server. Gson takes care of all the hard work so long as you
 * follow well-defined naming conventions.
 */

package com.zaffra.yardsale;

public class Item
{

	private float price;
	private String seller;
	private String id;
	private String description;
	private String name;
	
	public float getPrice() {
		return price;
	}
	
	public void setPrice(float price) {
		this.price = price;
	}
	
	public String getSeller() {
		return seller;
	}
	
	public void setSeller(String seller) {
		this.seller = seller;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "<YardSaleItem " + this.id + ">"; 
	}
}