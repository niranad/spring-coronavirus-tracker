package com.niranad.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.niranad.coronavirustracker.models.LocationStats;

import jakarta.annotation.PostConstruct;

@Service
public class CoronaVirusDataService {

	private static final String GLOBAL_CONFIRMED_CASES_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
	private static final String GLOBAL_DEATHS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";
	private static final String GLOBAL_RECOVERED_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_recovered_global.csv";
	private List<LocationStats> allStats = new ArrayList<>();

	@PostConstruct
	@Scheduled(fixedRate=60*60*1000)
	public void fetchVirusData() throws IOException, InterruptedException {
		Map<String, LocationStats> statsMap = new HashMap<>();
		List<LocationStats> newStats = new ArrayList<>();

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest confCasesRequest = HttpRequest.newBuilder().uri(URI.create(GLOBAL_CONFIRMED_CASES_DATA_URL))
				.build();
		HttpResponse<String> confCasesResponse = client.send(confCasesRequest, HttpResponse.BodyHandlers.ofString());

		HttpRequest deathCasesRequest = HttpRequest.newBuilder().uri(URI.create(GLOBAL_DEATHS_DATA_URL)).build();
		HttpResponse<String> deathCasesResponse = client.send(deathCasesRequest, HttpResponse.BodyHandlers.ofString());

		HttpRequest recovCasesRequest = HttpRequest.newBuilder().uri(URI.create(GLOBAL_RECOVERED_DATA_URL)).build();
		HttpResponse<String> recovCasesResponse = client.send(recovCasesRequest, HttpResponse.BodyHandlers.ofString());

		StringReader confCasesCsvBodyReader = new StringReader(confCasesResponse.body());
		Iterable<CSVRecord> confCasesRecords = Builder.create().setHeader().setSkipHeaderRecord(true).build()
				.parse(confCasesCsvBodyReader);

		StringReader deathCasesCsvBodyReader = new StringReader(deathCasesResponse.body());
		Iterable<CSVRecord> deathCasesRecords = Builder.create().setHeader().setSkipHeaderRecord(true).build()
				.parse(deathCasesCsvBodyReader);

		StringReader recovCasesCsvBodyReader = new StringReader(recovCasesResponse.body());
		Iterable<CSVRecord> recovCasesRecords = Builder.create().setHeader().setSkipHeaderRecord(true).build()
				.parse(recovCasesCsvBodyReader);

		// Process confirmed cases records into statsMap
		for (CSVRecord record : confCasesRecords) {
			LocationStats locationStats = new LocationStats();

			locationStats.setState(record.get("Province/State"));
			locationStats.setCountry(record.get("Country/Region"));

			if (!locationStats.getState().isBlank()) {
				statsMap.put(locationStats.getState(), locationStats);
			} else {
				statsMap.put(locationStats.getCountry(), locationStats);
			}

			int latestCases = Integer.parseInt(record.get(record.size() - 1));
			int prevDayCases = Integer.parseInt(record.get(record.size() - 2));

			locationStats.setLatestTotalCases(latestCases);
			locationStats.setDiffFromPrevDay(latestCases - prevDayCases);
		}

		// Process death cases records into statsMap
		for (CSVRecord record : deathCasesRecords) {
			LocationStats locationStats;
			if (!record.get("Province/State").isBlank()) {
				locationStats = statsMap.get(record.get("Province/State"));
			} else {
				locationStats = statsMap.get(record.get("Country/Region"));
			}

			if (locationStats == null) {
				locationStats = new LocationStats();
				locationStats.setState(record.get("Province/State"));
				locationStats.setCountry(record.get("Country/Region"));
				statsMap.put(locationStats.getState().isBlank() ? locationStats.getCountry() : locationStats.getState(),
						locationStats);
			}

			int latestTotalDeaths = Integer.parseInt(record.get(record.size() - 1));
			int prevDayTotalDeaths = Integer.parseInt(record.get(record.size() - 2));
			locationStats.setTotalDeaths(latestTotalDeaths);
			locationStats.setDeathsSincePrevDay(latestTotalDeaths - prevDayTotalDeaths);
		}

		// Process recovered cases records into statsMap
		for (CSVRecord record : recovCasesRecords) {
			LocationStats locationStats;
			if (!record.get("Province/State").isBlank()) {
				locationStats = statsMap.get(record.get("Province/State"));
			} else {
				locationStats = statsMap.get(record.get("Country/Region"));
			}

			if (locationStats == null) {
				locationStats = new LocationStats();
				locationStats.setState(record.get("Province/State"));
				locationStats.setCountry(record.get("Country/Region"));
				statsMap.put(locationStats.getState().isBlank() ? locationStats.getCountry() : locationStats.getState(),
						locationStats);
			}

			int latestTotalRecovered = Integer.parseInt(record.get(record.size() - 1));
			int prevDayTotalRecovered = Integer.parseInt(record.get(record.size() - 2));
			locationStats.setTotalRecovered(latestTotalRecovered);
			locationStats.setRecoveredSincePrevDay(latestTotalRecovered - prevDayTotalRecovered);
		}

		newStats = Arrays.asList(statsMap.values().toArray(new LocationStats[statsMap.size()]));
		Collections.sort(newStats);
		allStats = newStats;
	}

	public List<LocationStats> getAllStats() {
		return allStats;
	}

}
