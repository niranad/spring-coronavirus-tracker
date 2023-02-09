package com.niranad.coronavirustracker.controllers;

import java.text.NumberFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.niranad.coronavirustracker.models.LocationStats;
import com.niranad.coronavirustracker.services.CoronaVirusDataService;

@Controller
public class HomeController {
	
	@Autowired
	CoronaVirusDataService coronavirusDataService;
	
	@GetMapping("/")
	public String home(Model model) {
		List<LocationStats> allStats = coronavirusDataService.getAllStats();
		int totalReportedCases = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
		int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
		int totalDeaths = allStats.stream().mapToInt(stat -> stat.getTotalDeaths()).sum();
		int totalRecovered = allStats.stream().mapToInt(stat -> stat.getTotalRecovered()).sum();
		
		NumberFormat format = NumberFormat.getNumberInstance();
		
		model.addAttribute("locationStats", allStats);
		model.addAttribute("totalReportedCases", format.format(totalReportedCases));
		model.addAttribute("totalNewCases", format.format(totalNewCases));
		model.addAttribute("totalDeaths", format.format(totalDeaths));
		model.addAttribute("totalRecovered", format.format(totalRecovered));
		
		return "home";
	}
}
