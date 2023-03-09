package com.niranad.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
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

	private static enum CsvData {
		CONFIRMED, RECOVERED, DEATH
	};

	private List<LocationStats> allStats = new ArrayList<>();

	@PostConstruct
	@Scheduled(fixedRate = 60 * 360 * 1000)
	public void fetchVirusData() throws IOException, InterruptedException {
		Map<String, LocationStats> statsMap = new HashMap<>();
		List<LocationStats> newStats = new ArrayList<>();

		HttpResponse<String> confCasesResponse = makeRequest(GLOBAL_CONFIRMED_CASES_DATA_URL);
		HttpResponse<String> deathCasesResponse = makeRequest(GLOBAL_DEATHS_DATA_URL);
		HttpResponse<String> recovCasesResponse = makeRequest(GLOBAL_RECOVERED_DATA_URL);

		Iterable<CSVRecord> confCasesRecords = parseCSVResponse(confCasesResponse.body());
		Iterable<CSVRecord> deathCasesRecords = parseCSVResponse(deathCasesResponse.body());
		Iterable<CSVRecord> recovCasesRecords = parseCSVResponse(recovCasesResponse.body());

		// Format today's date and previous day's date in mm/dd/yy
		LocalDate today = LocalDate.now();
		String[] todayYYMMDD = today.toString().split("-");
		String todayDateFormat = todayYYMMDD[1] + "/" + todayYYMMDD[2] + "/" + todayYYMMDD[0].substring(2);
		String[] prevDayYYMMDD = today.minusDays(1).toString().split("-");
		String prevDateFormat = prevDayYYMMDD[1] + "/" + prevDayYYMMDD[2] + "/" + prevDayYYMMDD[0].substring(2);

		// Process confirmed cases records into statsMap
		readRecordsIntoMap(confCasesRecords, statsMap, CsvData.CONFIRMED, todayDateFormat, prevDateFormat);

		// Process death cases records into statsMap
		readRecordsIntoMap(deathCasesRecords, statsMap, CsvData.DEATH, todayDateFormat, prevDateFormat);

		// Process recovered cases records into statsMap
		readRecordsIntoMap(recovCasesRecords, statsMap, CsvData.RECOVERED, todayDateFormat, prevDateFormat);

		newStats = Arrays.asList(statsMap.values().toArray(new LocationStats[statsMap.size()]));
		Collections.sort(newStats);
		allStats = newStats;
	}

	private void readRecordsIntoMap(Iterable<CSVRecord> records, Map<String, LocationStats> statsMap, CsvData csvData,
			String dateNow, String datePrev) {
		for (CSVRecord record : records) {
			LocationStats locationStats;

			if (csvData != CsvData.CONFIRMED) {
				if (!record.get("Province/State").isBlank()) {
					locationStats = statsMap.get(record.get("Province/State"));
				} else {
					locationStats = statsMap.get(record.get("Country/Region"));
				}

				if (locationStats == null) {
					locationStats = new LocationStats();
					locationStats.setState(record.get("Province/State"));
					locationStats.setCountry(record.get("Country/Region"));
					statsMap.put(
							locationStats.getState().isBlank() ? locationStats.getCountry() : locationStats.getState(),
							locationStats);
				}
			} else {
				locationStats = new LocationStats();
				locationStats.setState(record.get("Province/State"));
				locationStats.setCountry(record.get("Country/Region"));

				if (!locationStats.getState().isBlank()) {
					statsMap.put(locationStats.getState(), locationStats);
				} else {
					statsMap.put(locationStats.getCountry(), locationStats);
				}
			}

			int latestTotal = Integer.parseInt(record.get(record.size() - 1));

			if (csvData == CsvData.RECOVERED) {
				locationStats.setTotalRecovered(latestTotal);
			} else if (csvData == CsvData.DEATH) {
				locationStats.setTotalDeaths(latestTotal);
			} else {
				locationStats.setLatestTotalCases(latestTotal);
			}

			int todayTotalCases = record.isMapped(dateNow) ? Integer.parseInt(record.get(datePrev)) : 0;
			int prevDayTotalCases = record.isMapped(datePrev) ? Integer.parseInt(record.get(datePrev)) : 0;

			if (csvData == CsvData.RECOVERED) {
				locationStats.setRecoveredSincePrevDay(prevDayTotalCases > 0 && todayTotalCases > prevDayTotalCases
						? todayTotalCases - prevDayTotalCases
						: 0);
			} else if (csvData == CsvData.DEATH) {
				locationStats.setDeathsSincePrevDay(prevDayTotalCases > 0 && todayTotalCases > prevDayTotalCases
						? todayTotalCases - prevDayTotalCases
						: 0);
			} else {
				locationStats.setDiffFromPrevDay(prevDayTotalCases > 0 && todayTotalCases > prevDayTotalCases
						? todayTotalCases - prevDayTotalCases
						: 0);
			}
		}
	}

	private HttpResponse<String> makeRequest(String url) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private Iterable<CSVRecord> parseCSVResponse(String resBody) throws IOException {
		StringReader reader = new StringReader(resBody);
		return Builder.create().setHeader().setSkipHeaderRecord(true).build().parse(reader);
	}

	public List<LocationStats> getAllStats() {
		return allStats;
	}
}
