package com.rair.jsfbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

import com.rair.dao.FlightRepository;
import com.rair.domain.Airport;
import com.rair.domain.Employee;
import com.rair.domain.Flight;
import com.rair.domain.TravelingClass;

@ManagedBean
@ViewScoped
public class FlightSearchBean implements Serializable {

	private static final long serialVersionUID = 8792693292832076782L;

	@ManagedProperty("#{airportServiceBean}")
	private AirportServiceBean airportServiceBean;

	@ManagedProperty("#{bookingServiceBean}")
	private BookingServiceBean bookingServiceBean;

	@ManagedProperty("#{loginBean}")
	private LoginBean loginBean;

	@Inject
	private FlightRepository flightRepository;

	private Airport arrivalAirport;
	private Airport departureAirport;
	private List<Flight> flightsForArrival;

	private boolean singleFlight;
	private double priceOfTicket;

	private String customerID;

	@PostConstruct
	public void init() {
		// RequestContext.getCurrentInstance().reset("flightsearchWizard");
		arrivalAirport = airportServiceBean.getAirports().get(0);
		flightsForArrival = flightRepository.retrieveFlightsByDestination(arrivalAirport);
	}

	public double getPriceOfTicket() {
		return priceOfTicket;
	}

	public void setPriceOfTicket(double priceOfTicket) {
		this.priceOfTicket = priceOfTicket;
	}

	public Airport getArrivalAirport() {
		System.out.println("Arrival airport: " + this.arrivalAirport);
		return arrivalAirport;
	}

	public void setArrivalAirport(Airport arrivalAirport) {
		this.arrivalAirport = arrivalAirport;
	}

	public Airport getDepartureAirport() {
		return departureAirport;
	}

	public void setDepartureAirport(Airport departureAirport) {
		this.departureAirport = departureAirport;
	}

	public List<Airport> completeAirports(String query) {
		System.out.println("Query for aiports: " + query);
		List<Airport> filteredAirports = new ArrayList<>();
		for (Airport airport : airportServiceBean.getAirports()) {
			if (airport.getCountry().toLowerCase().contains(query.toLowerCase())
					|| airport.getCity().toLowerCase().contains(query.toLowerCase())) {
				filteredAirports.add(airport);
			}
		}
		return filteredAirports;
	}

	public AirportServiceBean getAirportServiceBean() {
		return airportServiceBean;
	}

	public boolean isSingleFlight() {
		return singleFlight;
	}

	public void setSingleFlight(boolean singleFlight) {
		this.singleFlight = singleFlight;
	}

	public void setAirportServiceBean(AirportServiceBean airportServiceBean) {
		this.airportServiceBean = airportServiceBean;
	}

	public BookingServiceBean getBookingServiceBean() {
		return bookingServiceBean;
	}

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	public void setBookingServiceBean(BookingServiceBean bookingServiceBean) {
		this.bookingServiceBean = bookingServiceBean;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public FlightRepository getFlightRepository() {
		return flightRepository;
	}

	public void setFlightRepository(FlightRepository flightRepository) {
		this.flightRepository = flightRepository;
	}

	public List<Flight> getFlightsForArrival() {
		flightsForArrival = flightRepository.retrieveFlightsByDestination(arrivalAirport);
		System.out.println("Flights for the arrival airport: " + this.flightsForArrival);
		return flightsForArrival;
	}

	public void setFlightsForArrival(List<Flight> flightsForArrival) {
		this.flightsForArrival = flightsForArrival;
	}

	public String onFlowProcess(FlowEvent event) {
		System.out.println("Ols Step: " + event.getOldStep());
		if (event.getNewStep().equals("flights")) {
			if (event.getOldStep().equals("ticketChoice")) {
				bookingServiceBean.setFlight(null);
				bookingServiceBean.setnSeatsWanted(0);
				bookingServiceBean.setPriceOfBooking(0);
				bookingServiceBean.setSelectedTravelClass(TravelingClass.ECONOMY);
			}
			flightsForArrival = flightRepository.retrieveFlightsByDestination(arrivalAirport);
			for (Flight flight : flightsForArrival) {
				System.out.println(flight.getFlightNumber());
				for (Entry<TravelingClass, Integer> entry : flight.getAvailableSeats().entrySet()) {
					System.out.println(entry.getKey() + ": " + entry.getValue());
				}
			}
		}
		return event.getNewStep();
	}

	public void onRowSelect(SelectEvent event) {
		bookingServiceBean.setFlight((Flight) event.getObject());
		priceOfTicket = bookingServiceBean.getFlight().getBasePrice() * Employee.RAIR_PERCENTAGE;
		bookingServiceBean.setSelectedTravelClass(TravelingClass.ECONOMY);
		FacesMessage msg = new FacesMessage("Flight Selected");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public void onRowUnselect(UnselectEvent event) {
		bookingServiceBean.setFlight(null);
		FacesMessage msg = new FacesMessage("Flight Unselected");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public String bookFlight() {

		System.out.println(loginBean.getPerson());
		if (loginBean.getPerson() == null) {
			return "toLogin";
		}

		return "toLogin";
	}

	public void changePriceOfOneTicket() {
		switch (bookingServiceBean.getSelectedTravelClass()) {
		case BUSINESS:
			priceOfTicket = bookingServiceBean.getFlight().getTicketPriceBusinessClass();
			break;
		case FIRST_CLASS:
			priceOfTicket = bookingServiceBean.getFlight().getTicketPriceFirstClass();
			break;
		default:
			priceOfTicket = bookingServiceBean.getFlight().getTicketPriceEconomyClass();
			break;
		}
		priceOfTicket *= Employee.RAIR_PERCENTAGE;
	}

	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	public void openRegisteredQuestion() {
		System.out.println("Hello");
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("modal", true);
		RequestContext.getCurrentInstance().openDialog("registerQuestion", options, null);
	}

	public void onReturnFromRegisterQuestion(SelectEvent event) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage("Data Returned", event.getObject().toString()));
	}

}
