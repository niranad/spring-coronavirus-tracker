package com.niranad.coronavirustracker.models;

public class LocationStats implements Comparable<LocationStats> {

	private String state;
	private String country;
	private int latestTotalCases;
	private int diffFromPrevDay;
	private int totalDeaths;
	private int deathsSincePrevDay;
	private int totalRecovered;
	private int recoveredSincePrevDay;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getLatestTotalCases() {
		return latestTotalCases;
	}

	public void setLatestTotalCases(int latestTotalCases) {
		this.latestTotalCases = latestTotalCases;
	}

	public int getDiffFromPrevDay() {
		return diffFromPrevDay;
	}

	public void setDiffFromPrevDay(int diffFromPrevDay) {
		this.diffFromPrevDay = diffFromPrevDay;
	}

	public int getTotalRecovered() {
		return totalRecovered;
	}

	public void setTotalRecovered(int totalRecovered) {
		this.totalRecovered = totalRecovered;
	}

	public int getRecoveredSincePrevDay() {
		return recoveredSincePrevDay;
	}

	public void setRecoveredSincePrevDay(int recoveredSincePrevDay) {
		this.recoveredSincePrevDay = recoveredSincePrevDay;
	}

	public int getTotalDeaths() {
		return totalDeaths;
	}

	public void setTotalDeaths(int totalDeaths) {
		this.totalDeaths = totalDeaths;
	}

	public int getDeathsSincePrevDay() {
		return deathsSincePrevDay;
	}

	public void setDeathsSincePrevDay(int deathsSincePrevDay) {
		this.deathsSincePrevDay = deathsSincePrevDay;
	}

	@Override
	public String toString() {
		return String.format("State: %s, Country: %s, LTC: %d", getState(), getCountry(), getLatestTotalCases());
	}

	@Override
	public int compareTo(LocationStats o) {
		return getState().compareTo(o.getState()) < 0 ? -1
				: getState().compareTo(o.getState()) > 0 ? 1
						: getCountry().compareTo(o.getCountry()) < 0 ? -1
								: getCountry().compareTo(o.getCountry()) > 0 ? 1 : 0;
	}
}
