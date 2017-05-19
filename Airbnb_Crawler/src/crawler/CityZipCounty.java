package crawler;

public class CityZipCounty {
	private String _city;
	private String _zipcode;
	private String _county;
	
	public CityZipCounty(String city, String zipcode, String county) {
		_city = city;
		_zipcode = zipcode;
		_county = county;
	}
	
	public String getCity() {
		return _city;
	}
	
	public String getZipcode() {
		return _zipcode;
	}
	
	public String getCounty() {
		return _county;
	}
	
	public void setCity(String value) {
		if (!_city.equals(value)) {
			_city = value;
		}
	}
	
	public void setZipcode(String value) {
		if (!_zipcode.equals(value)) {
			_zipcode = value;
		}
	}
	
	public void setCounty(String value) {
		if (!_county.equals(value)) {
			_county = value;
		}
	}
}
