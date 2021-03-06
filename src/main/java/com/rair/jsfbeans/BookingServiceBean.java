package com.rair.jsfbeans;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.persistence.OptimisticLockException;

import org.hibernate.StaleObjectStateException;

import com.rair.dao.BookingRepository;
import com.rair.dao.FlightRepository;
import com.rair.domain.Booking;
import com.rair.domain.BookingStatus;
import com.rair.domain.Customer;
import com.rair.domain.Flight;
import com.rair.domain.Payment;
import com.rair.domain.TravelingClass;
import com.rair.mail.MailSender;

@ManagedBean(name = "bookingServiceBean")
@SessionScoped
public class BookingServiceBean {

	private Flight flight;
	private Flight returnFlight;
	private Customer customer;
	private Payment paymentChoice = Payment.CREDITCARD;
	private double priceOfBooking;
	private double priceOfReturnBooking;
	private int nSeatsWanted;
	private int nSeatsWantedReturn;
	private TravelingClass selectedTravelClass;
	private TravelingClass selectedReturnTravelClass;
	private double totalPrice;
	private String cardNumber;

	@Inject
	private BookingRepository bookingRepository;

	@Inject
	private FlightRepository flightRepository;

	public FlightRepository getFlightRepository() {
		return flightRepository;
	}

	public void setFlightRepository(FlightRepository flightRepository) {
		this.flightRepository = flightRepository;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public Flight getFlight() {
		return flight;
	}

	public Flight getReturnFlight() {
		return returnFlight;
	}

	public void setReturnFlight(Flight returnFlight) {
		this.returnFlight = returnFlight;
	}

	public BookingRepository getBookingRepository() {
		return bookingRepository;
	}

	public void setBookingRepository(BookingRepository bookingRepository) {
		this.bookingRepository = bookingRepository;
	}

	public void setFlight(Flight flight) {
		System.out.println("Flight is set: " + flight);
		this.flight = flight;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Payment getPaymentChoice() {
		return paymentChoice;
	}

	public void setPaymentChoice(Payment paymentChoice) {
		System.out.println("Payment choice: " + paymentChoice);
		this.paymentChoice = paymentChoice;
	}

	public double getPriceOfBooking() {
		return priceOfBooking;
	}

	public void setPriceOfBooking(double priceOfBooking) {
		this.priceOfBooking = priceOfBooking;
	}

	public int getnSeatsWanted() {
		return nSeatsWanted;
	}

	public void setnSeatsWanted(int nSeatsWanted) {
		this.nSeatsWanted = nSeatsWanted;
		calculateBookingPrice();
	}

	public void calculateTotalPrice(double priceOfTicket) {

		priceOfBooking = nSeatsWanted * priceOfTicket;
	}

	public Integer maxSeatsOutboundFlight() {
		System.out.println("Flight: " + flight.getFlightNumber());
		System.out.println("Selected travel class: " + selectedTravelClass);
		System.out.println(flight.checkNumberOfSeatsForTravelingClass(selectedTravelClass.toString()));
		return flight.checkNumberOfSeatsForTravelingClass(selectedTravelClass.toString());
	}

	public Integer maxNumberOfSeatsReturnFlight() {
		System.out.println("Return flight: " + returnFlight.getFlightNumber());
		System.out.println("Selected travel class: " + selectedReturnTravelClass);
		System.out.println(returnFlight.checkNumberOfSeatsForTravelingClass(selectedReturnTravelClass.toString()));
		return returnFlight.checkNumberOfSeatsForTravelingClass(selectedReturnTravelClass.toString());
	}

	public TravelingClass getSelectedTravelClass() {
		return selectedTravelClass;
	}

	public void setSelectedTravelClass(TravelingClass selectedTravelClass) {
		this.selectedTravelClass = selectedTravelClass;
	}

	public TravelingClass getSelectedReturnTravelClass() {
		return selectedReturnTravelClass;
	}

	public void setSelectedReturnTravelClass(TravelingClass selectedReturnTravelClass) {
		this.selectedReturnTravelClass = selectedReturnTravelClass;
	}

	public void calculatePriceOfBooking(double priceOfTicket) {
		priceOfBooking = nSeatsWanted * priceOfTicket;
		updateTotalPrice();
	}

	private void updateTotalPrice() {
		totalPrice = priceOfBooking + priceOfReturnBooking;
	}

	public void calculatePriceOfReturnBooking(double priceOfReturnTicket) {
		priceOfReturnBooking = nSeatsWantedReturn * priceOfReturnTicket;
		updateTotalPrice();
	}

	public int getnSeatsWantedReturn() {
		return nSeatsWantedReturn;
	}

	public void setnSeatsWantedReturn(int nSeatsWantedReturn) {
		this.nSeatsWantedReturn = nSeatsWantedReturn;
		calculateBookingPrice();

	}

	public double getPriceOfReturnBooking() {
		return priceOfReturnBooking;
	}

	public void setPriceOfReturnBooking(double priceOfReturnBooking) {
		this.priceOfReturnBooking = priceOfReturnBooking;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getTravelingClassAsString() {
		String str = "";
		switch (selectedTravelClass) {
		case ECONOMY:
			str += "Economy";
			break;
		case BUSINESS:
			str += "Business";
			break;
		default:
			str += "First Class";
			break;
		}
		return str;
	}

	public boolean isCreditCardPayment() {
		return paymentChoice.equals(Payment.CREDITCARD);
	}

	public String makeBooking() {
		flight.adjustAvailableSeats(selectedTravelClass.toString(), nSeatsWanted);
		try {
			flightRepository.update(flight, flight.getId());
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Update failed", "The update of the flight failed. Please refresh your browser and try again."));
			return null;
		}
		Booking booking = new Booking();
		System.out.println(customer);
		booking.setCustomer(customer);
		booking.setFlight(flight);
		booking.setPrice(priceOfBooking);
		booking.setNumberOfSeats(nSeatsWanted);
		booking.setTravelingClass(selectedTravelClass);
		booking.setPaymentChoice(paymentChoice);

		MailSender mailSender = new MailSender();
		if (paymentChoice.equals(Payment.CREDITCARD)) {
			booking.setStatus(BookingStatus.PAYMENT_SUCCES);
			mailSender.setTextMessage("creditcard", priceOfBooking);
		} else {
			booking.setStatus(BookingStatus.PAYMENT_PENDING);
			mailSender.setTextMessage("endorsement", priceOfBooking);
		}
		bookingRepository.createBooking(booking);
		try {
			mailSender.sendMail(customer.getEmailAddress());
		} catch (MessagingException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Mail fail",
					"Something went wrong with our mailing service. We'll send you a mail later. Your booking is fine"));
		}

		if (containsReturnFlight()) {
			returnFlight.adjustAvailableSeats(selectedReturnTravelClass.toString(), nSeatsWantedReturn);
			try {
				flightRepository.update(returnFlight, returnFlight.getId());
			} catch (OptimisticLockException | StaleObjectStateException e) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Update failed",
								"The update of the flight failed. Please refresh your browser and try again."));
			}
			booking = new Booking();
			booking.setCustomer(customer);
			booking.setFlight(returnFlight);
			booking.setPrice(priceOfReturnBooking);
			booking.setNumberOfSeats(nSeatsWantedReturn);
			booking.setTravelingClass(selectedReturnTravelClass);
			booking.setPaymentChoice(paymentChoice);

			mailSender = new MailSender();
			if (paymentChoice.equals(Payment.CREDITCARD)) {
				booking.setStatus(BookingStatus.PAYMENT_SUCCES);
				mailSender.setTextMessage("creditcard", priceOfReturnBooking);
			} else {
				booking.setStatus(BookingStatus.PAYMENT_PENDING);
				mailSender.setTextMessage("endorsement", priceOfReturnBooking);
			}

			bookingRepository.createBooking(booking);
			try {
				mailSender.sendMail(customer.getEmailAddress());
			} catch (MessagingException e) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Mail fail",
						"Something went wrong with our mailing service. We'll send you a mail later. Your booking is fine"));
			}

		}
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		return "succes.xhtml?faces-redirect=true";
	}

	public void calculateBookingPrice() {
		this.priceOfBooking = nSeatsWanted * flight.getTicketPriceByTravelclass(selectedTravelClass.toString());

		if (returnFlight != null) {
			this.priceOfReturnBooking = nSeatsWantedReturn
					* returnFlight.getTicketPriceByTravelclass(selectedReturnTravelClass.toString());
			this.totalPrice = priceOfBooking + priceOfReturnBooking;
		} else {
			this.totalPrice = priceOfBooking;
		}

	}

	public boolean containsReturnFlight() {
		return returnFlight != null;
	}

}
