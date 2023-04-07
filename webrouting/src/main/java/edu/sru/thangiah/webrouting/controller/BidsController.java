package edu.sru.thangiah.webrouting.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.sru.thangiah.webrouting.domain.Bids;
import edu.sru.thangiah.webrouting.domain.Carriers;
import edu.sru.thangiah.webrouting.domain.Notification;
import edu.sru.thangiah.webrouting.domain.Shipments;
import edu.sru.thangiah.webrouting.domain.User;
import edu.sru.thangiah.webrouting.repository.BidsRepository;
import edu.sru.thangiah.webrouting.repository.CarriersRepository;
import edu.sru.thangiah.webrouting.repository.ShipmentsRepository;
import edu.sru.thangiah.webrouting.services.NotificationService;
import edu.sru.thangiah.webrouting.services.SecurityService;
import edu.sru.thangiah.webrouting.services.UserService;
import edu.sru.thangiah.webrouting.web.UserValidator;

/**
 * Handles the Thymeleaf controls for the pages
 * dealing with carriers.
 * @author Ian Black	imb1007@sru.edu
 * @since 4/8/2022
 */

@Controller
public class BidsController {

	@Autowired
	private UserService userService;

	@Autowired
	private NotificationService notificationService;

	private BidsRepository bidsRepository;

	private ShipmentsRepository shipmentsRepository;

	private CarriersRepository carriersRepository;

	@Autowired
	private UserValidator userValidator;

	private static final Logger Logger = LoggerFactory.getLogger(BidsController.class);
	/**
	 * Constructor for BidsController. <br>
	 * Instantiates the bidsRepository <br>
	 * Instantiates the carriersRepository <br>
	 * Instantiates the shipmentsRepository
	 * @param bidsRepository Used to interact with bids in the database
	 * @param shipmentsRepository Used to interact with shipments in the database
	 * @param carriersRepository Used to interact with carriers in the database
	 */
	public BidsController(BidsRepository bidsRepository, ShipmentsRepository shipmentsRepository, CarriersRepository carriersRepository) {
		this.bidsRepository = bidsRepository;
		this.shipmentsRepository = shipmentsRepository;
		this.carriersRepository = carriersRepository;
	}

	/**
	 * Redirects user to the /add/add-bid page <br>
	 * Adds shipments and carriers to the model
	 * @param id ID of the shipment being found
	 * @param model Used to add data the model
	 * @param bid Holds information for the new bid
	 * @param result Checks entered data to ensure it is valid
	 * @param session stores the current logged in users HTTP session. Attribute "redirectLocation" can store a string containing the last page the user visited.
	 * @return "/add/add-bid"
	 */
	@GetMapping({"/add-bid/{id}"})
	public String showBidList(@PathVariable("id") long id, Model model, Bids bid, BindingResult result, HttpSession session) {
		String redirectLocation = (String) session.getAttribute("redirectLocation");
		model.addAttribute("redirectLocation", redirectLocation);
		Shipments shipment = shipmentsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid shipment Id: " + id));
		model.addAttribute("shipments", shipment);
		model.addAttribute("carriers", carriersRepository.findAll());
		User loggedInUser = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(loggedInUser, model);

		if (!shipment.getFullFreightTerms().toString().equals("AVAILABLE SHIPMENT")) {
			System.out.println("Error: User attempeted to place a bid on a shipment that was not in auction");
			Logger.error("{} || attempted to place a bid on a shipment that was not in auction", loggedInUser.getUsername());
			return (String) session.getAttribute("redirectLocation");
		}

		return "/add/add-bid";
	}

	/**
	 * Adds a bid to the database. Checks if there are errors in the form. <br>
	 * Adds the date, time, and logged in user to the bid. <br>
	 * If there are no errors, the bid is saved in the bidsRepository. and the user is redirect to /bids <br>
	 * If there are errors, the user is redirected to the /add/add-technician page.
	 * @param bid Holds information for the new bid
	 * @param result Checks entered data to ensure it is valid
	 * @param model Used to add data to the model
	 * @return "redirect:/createdshipments" or "/add/add-bid"
	 */
	@RequestMapping({"/addbid"})
	public String addBid(@Validated Bids bid, BindingResult result, Model model, HttpSession session) {
		String redirectLocation = (String) session.getAttribute("redirectLocation");
		model.addAttribute("redirectLocation", redirectLocation);
		userValidator.addition(bid, result);
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);
		if (result.hasErrors()) {
			return "/add/add-bid";
		}

		DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm:ss");


		LocalDateTime now = LocalDateTime.now();

		bid.setCarrier(user.getCarrier());
		bid.setDate(date.format(now));
		bid.setTime(time.format(now));

		boolean deny = false;
		Shipments shipment = bid.getShipment();
		List<Bids> bidsInShipment = shipment.getBids();

		for (Bids b: bidsInShipment) {
			if (b.getCarrier().getCarrierName().equals(bid.getCarrier().getCarrierName())
					&& b.getPrice().equals(bid.getPrice())) {
				deny = true;
			}
		}

		if (deny == true) {
			model.addAttribute("error", "Error: Bid with the same carrier and price has already been placed.");
			Logger.error("{} || failed to add bid due to a bid with the same carrier and price already being place.", user.getUsername());
			model.addAttribute("shipments", bid.getShipment());
			model.addAttribute("carriers", carriersRepository.findAll());
			return "/add/add-bid";
		}

		bidsRepository.save(bid);
		Logger.info("{} || successfully created a new bid with ID {}", user.getUsername(), bid.getId());
		notificationService.addNotification(bid.getShipment().getUser(), 
				"ALERT: A new bid as been added on your shipment with ID " + bid.getShipment().getId() + " and Client " + bid.getShipment().getClient(), false);

		return "redirect:" + redirectLocation;
	}

	/**
	 * Finds a bid using the id parameter and if found, redirects user to delete confirmation page
	 * @param id Holds the ID of the bid to be deleted
	 * @param model Used to add data to the model
	 * @param session stores the current logged in users HTTP session. Attribute "redirectLocation" can store a string containing the last page the user visited.
	 * @return "/delete/deletebidconfirm" if succesful, "redirect: + redirectLocation" otherwise
	 */
	@GetMapping("/deletebid/{id}")
	public String deleteBid(@PathVariable("id") long id, Model model, HttpSession session) {
		Bids bid = bidsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid bid Id:" + id));
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);
		String redirectLocation = (String) session.getAttribute("redirectLocation");

		if (bid.getShipment().getFullFreightTerms().toString().equals("FROZEN") && !user.getRole().toString().equals("MASTERLIST")) {
			System.out.println("User attempeted to delete a bid on a frozen shipment");
			Logger.error("{} || attempted to delete a bid on a frozen shipment", user.getUsername());//Replace this with a proper error message 
			return "redirect:" + redirectLocation;
		}

		if (bid.getCarrier().equals(user.getCarrier()) || user.getRole().toString().equals("MASTERLIST")) {
			model.addAttribute("bids", bid);
			model.addAttribute("redirectLocation",redirectLocation);

			return "/delete/deletebidconfirm";
		}

		return "redirect:" + redirectLocation;
	}

	/**
	 * Finds a bid using the id parameter and if found, deletes the bid and redirects to created shipments page
	 * @param id ID of the bid being deleted
	 * @param model Used to add data to the model
	 * @param session stores the current logged in users HTTP session. Attribute "redirectLocation" can store a string containing the last page the user visited.
	 * @return "redirect: + redirectLocation" (redirectLocation is stored in the HttpSession and set during page loads for critical pages)
	 */
	@GetMapping("/deletebidconfirmation/{id}")
	public String deleteUserConfirm(@PathVariable("id") long id, Model model, HttpSession session) {
		Bids bid = bidsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid bid Id:" + id));
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);
		User bidUser = CarriersController.getUserFromCarrier(bid.getCarrier());

		if(user.getId() != bidUser.getId()) {
			notificationService.addNotification(bidUser,
					"ALERT: Your bid with ID " + bid.getId() + " placed on shipment with ID " + bid.getShipment().getId() + " was deleted by " + user.getUsername(), false);
		}

		User loggedInUser = userService.getLoggedInUser();
		Logger.info("{} || successfully deleted Bid with ID {}.", loggedInUser.getUsername(), bid.getId());
		bidsRepository.delete(bid);
		return "redirect:" + session.getAttribute("redirectLocation");
	}

	/**
	 * Finds a shipment using the id parameter and if found, redirects users to the reset shipment bids confirmation page
	 * @param id : Holds the ID of the shipment to be reset
	 * @param model Used to add data to the model
	 * @param session stores the current logged in users HTTP session. Attribute "redirectLocation" can store a string containing the last page the user visited.
	 * @return "reset/resetshipmentbidsconfirm"
	 */
	@GetMapping("/resetshipmentbids/{id}")
	public String resetShipmentBids(@PathVariable("id") long id, Model model, HttpSession session) {
		Shipments shipment = shipmentsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid shipment Id:" + id));

		model.addAttribute("shipment",shipment);
		model.addAttribute("redirectLocation",session.getAttribute("redirectLocation"));

		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);

		return "/reset/resetshipmentbidsconfirm";
	}

	/**
	 * Finds a shipment using the id parameter and if found, gets all of that shipments bids then removes then. Then, returns the user to their redirectlocation
	 * @param id : Holds the ID of the shipment to be reset
	 * @param model Used to add data to the model
	 * @param session stores the current logged in users HTTP session. Attribute "redirectLocation" can store a string containing the last page the user visited.
	 * @return "redirect: + redirectLocation" (redirectLocation is stored in the HttpSession and set during page loads for critical pages)
	 */
	@GetMapping("/resetshipmentbidsconfirmation/{id}")
	public String resetShipmentBidsConfirmation(@PathVariable("id") long id, Model model, HttpSession session) {
		Shipments shipment = shipmentsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid shipment Id:" + id));

		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);
		User bidUser;

		if (!user.getRole().toString().equals("MASTERLIST")) {
			System.out.println("Error: Non Auctioneer tried to reset shipment!");
			Logger.error("{} || (Non-Auctioneer) tried to reset shipment!", user.getUsername());
			return "redirect:" + session.getAttribute("redirectLocation");
		}

		for (Bids bid : shipment.getBids()) {
			bidUser = CarriersController.getUserFromCarrier(bid.getCarrier());
			notificationService.addNotification(bidUser,
					"ALERT: Your bid with ID " + bid.getId() + " placed on shipment with ID " + bid.getShipment().getId() + " was deleted by " + user.getUsername(), false);

			bidsRepository.delete(bid);
		}

		return "redirect:" + session.getAttribute("redirectLocation");
	}

	/**
	 * Accepts a bid from the carrier. <br>
	 * Assigns a carrier, price, scac, and changes the full freight terms of a shipment. <br>
	 * Updates the shipment using the shipmentsRepository.
	 * @param id Holds the ID of the bid to be accepted
	 * @param model Used to add data to the model
	 * @return "redirect:/shipmentshomeshipper"
	 */
	@GetMapping("/acceptbid/{id}")
	public String acceptBid(@PathVariable("id") long id, Model model, HttpSession session) {
		Bids bid = bidsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid bid Id: " + id));


		String redirectLocation = (String) session.getAttribute("redirectLocation");
		model.addAttribute("redirectLocation", redirectLocation);
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);
		User bidUser;

		if (bid.getShipment().getFullFreightTerms().toString().equals("FROZEN") && !user.getRole().toString().equals("MASTERLIST")) {
			System.out.println("User attempted to accept a bid on a frozen shipment");
			Logger.error("{} || attempted to accept a bid on a frozen shipment.", user.getUsername());//Replace this with a proper error message and redirect, for now it just dumps shippers out of the accept menu
			return "redirect:" + redirectLocation;
		}

		Carriers carrier = bid.getCarrier();
		Shipments shipment = bid.getShipment();

		shipment.setCarrier(carrier);
		shipment.setPaidAmount(bid.getPrice());
		shipment.setScac(carrier.getScac());
		shipment.setFullFreightTerms("BID ACCEPTED");

		bidUser = CarriersController.getUserFromCarrier(carrier);
		notificationService.addNotification(bidUser, 
				"ALERT: You have won the auction on shipment with ID " + shipment.getId() + " with a final bid value of " + bid.getPrice(), false);

		shipmentsRepository.save(shipment);
		Logger.info("{} || successfully created a new bid with ID {}", user.getUsername(), bid.getId());

		return "redirect:" + redirectLocation;
	}


	@GetMapping("/editbids/{id}")
	public String showBidsEditForm(@PathVariable("id") long id, Model model, HttpSession session ) {
		Bids bid = bidsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid bid Id:" + id));
		User user = userService.getLoggedInUser();


		model = NotificationController.loadNotificationsIntoModel(user, model);

		model.addAttribute("currentPage","/shipments");


		model.addAttribute("bid", bid);
		model.addAttribute("redirectLocation", (String) session.getAttribute("redirectLocation"));

		try{
			model.addAttribute("message", (String) session.getAttribute("message"));
		}
		catch(Exception e){
			//do nothing
		}

		session.removeAttribute("message");

		return "/edit/edit-bids";
	}

	
	@PostMapping("/editbids/{id}")
	public String updateBid(@PathVariable("id") Long id, Bids bid, Model model, HttpSession session) {

		String redirectLocation = (String) session.getAttribute("redirectLocation");
		model.addAttribute("redirectLocation", session.getAttribute("redirectLocation"));

		Bids temp = bidsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Bid Id:" + id));

		User user = userService.getLoggedInUser();
		
		
		String price = bid.getPrice().strip();
		
		if (!(price.length() <= 12 && price.length() > 0) || !(price.matches("^[0-9.]+$"))) {
			Logger.error("{} || attempted to edit a bid but the price was not between 1 and 16 numeric characters long.",user.getUsername());
			session.setAttribute("message", "Price was not between 1 and 16 numeric characters.");
			return "redirect:/editbids/"+id.toString() ;	
		}
		
		Bids result = new Bids();
		
		result.setId(temp.getId());
		result.setDate(temp.getDate());
		result.setTime(temp.getTime());
		result.setPrice(price);
		result.setCarrier(temp.getCarrier());
		result.setShipment(temp.getShipment());


		bidsRepository.save(result);
		Logger.info("{} || successfully updated the bid with ID {}",user.getUsername(), result.getId());
		return "redirect:" + redirectLocation;

	}

}
