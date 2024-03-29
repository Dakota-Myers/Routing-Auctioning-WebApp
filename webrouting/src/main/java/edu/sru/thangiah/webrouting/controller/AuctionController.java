package edu.sru.thangiah.webrouting.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.sru.thangiah.webrouting.domain.Bids;
import edu.sru.thangiah.webrouting.domain.Carriers;
import edu.sru.thangiah.webrouting.domain.Notification;
import edu.sru.thangiah.webrouting.domain.Shipments;
import edu.sru.thangiah.webrouting.domain.User;
import edu.sru.thangiah.webrouting.repository.BidsRepository;
import edu.sru.thangiah.webrouting.repository.ShipmentsRepository;
import edu.sru.thangiah.webrouting.repository.UserRepository;
import edu.sru.thangiah.webrouting.services.NotificationService;
import edu.sru.thangiah.webrouting.services.SecurityService;
import edu.sru.thangiah.webrouting.services.UserService;

/**
 * Handles the Thymeleaf controls for the pages
 * dealing with auctioning
 * @author Thomas Haley tjh1019@sru.edu
 * @since 1/01/2023
 */

@Controller
public class AuctionController {
	
	
	@Autowired
	private UserService userService;


	@Autowired
	private NotificationService notificationService;

	private ShipmentsRepository shipmentsRepository;

	private UserRepository userRepository;

	private BidsRepository bidsRepository;

	private static final Logger Logger = LoggerFactory.getLogger(AuctionController.class);

	/**
	 * Constructor for the AuctionController
	 * @param sr Instantiates the Shipments Repository
	 * @param ur Instantiates the User Repository
	 * @param br Instantiates the Bids Repository
	 */
	public AuctionController (ShipmentsRepository sr, UserRepository ur, BidsRepository br) {
		this.shipmentsRepository = sr;
		this.userRepository = ur;
		this.bidsRepository  = br;
	}

	/**
	 * Finds a given shipment by ID, then redirects to the force end auction confirmation page if the shipment has any bids placed on it
	 * If the shipment has no bids on it, or is not AVAILABLE SHIPMENT, returns the user to their page and alerts the master that the operation has failed
	 * @param id of the shipment that won the auction
	 * @param model used to load attributes into the Thymeleaf model
	 * @param session used to load attributes into the current users HTTP session
	 * @return /force/forceendauctionconfirm 
	 */
	@RequestMapping("/forceendauction/{id}")
	public String forceEndAuction(@PathVariable("id") long id, Model model, HttpSession session) {
		Shipments shipment = shipmentsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid shipment Id:" + id));

		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);
		String redirectLocation = (String) session.getAttribute("redirectLocation");
		List<Bids> bids = shipment.getBids(); 

		if (!user.getRole().toString().equals("MASTERLIST")) {
			System.out.println("Non-Auctioneer user attempeted to force end an auction!");
			Logger.error("{} || (Non-Auctioneer) attempeted to force end an auction with shipment ID {}!", user.getUsername(), shipment.getId());///TODO: Replace this with a proper error message
			return "redirect:" + redirectLocation;
		}

		if (bids.size() < 1) {
			session.setAttribute("message","Error: Cannot force end an auction that has no bids on it.");
			Logger.error("{} || attempted to end an auction without any bids on shipment ID {}", user.getUsername(), shipment.getId());///TODO: Replace this with an html pop in page if possible
			return "redirect:" + redirectLocation;
		}

		model.addAttribute("shipments", shipment);
		model.addAttribute("redirectLocation",redirectLocation);

		return "/force/forceendauctionconfirm";
	}
	/**
	 * Finds given shipment by ID, then redirects the user to confirm or deny the push of the shipment to auction
	 * @param id of the shipment that won the auction
	 * @param model used to load attributes into the Thymeleaf model
	 * @param session used to load attributes into the current users HTTP session
	 * @return /push/pushshipmentconfirm
	 */
	@RequestMapping("/pushshipment/{id}")
	public String pushShipment(@PathVariable("id") long id, Model model, HttpSession session) {
		Shipments shipment = shipmentsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid shipment Id:" + id));
		User user = userService.getLoggedInUser();
		String redirectLocation = (String) session.getAttribute("redirectLocation");
		model.addAttribute("redirectLocation", redirectLocation);
		model = NotificationController.loadNotificationsIntoModel(user, model);

		if (!shipment.getFullFreightTerms().equals("PENDING")) {
			System.out.println("Error: Non-pending shipment attempted to be moved to auction.");
			Logger.error("{} || attempted to moved to auction a non-pending shipment.", user.getUsername());
			return redirectLocation;
		}

		model.addAttribute("shipments", shipment);

		return "/push/pushshipmentconfirm";
	}

	/**
	 * Allows the user to confirm or deny the shipment to move into the auction
	 * @param id of the shipment that is being pushed to auction
	 * @param model used to load attributes into the Thymeleaf model
	 * @param session used to load attributes into the current users HTTP session
	 * @return redirects the user to the base shipments page
	 */
	@RequestMapping("/pushshipmentconfirmation/{id}")
	public String pushShipmentConfirmation(@PathVariable("id") long id, Model model, HttpSession session) {
		String redirectLocation = (String) session.getAttribute("redirectLocation");
		model.addAttribute("redirectLocation", redirectLocation);
		Shipments shipment = shipmentsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Shipment Id:" + id));
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);

		if (user.getRole().toString().equals("CARRIER") || (user.getRole().toString().equals("SHIPPER") && !user.getShipments().contains(shipment))) {
			System.out.println("Error: Invalid permissions for pushing shipment");
			Logger.error("{} || attempted to push a shipment and they do not have permission", user.getUsername());
			return "redirect:" + redirectLocation;
		}

		if(shipment.getUser().getId() != user.getId()) {
			notificationService.addNotification(shipment.getUser(), 
					"ALERT: Your shipment with ID " + shipment.getId() + " and Client " + shipment.getClient() + " was pushed to auction by " + user.getUsername(), false);
		}

		shipment.setFullFreightTerms("AVAILABLE SHIPMENT");
		shipmentsRepository.save(shipment);
		Logger.info("{} || successfully pushed the shipment with ID {} to auction.", user.getUsername(), shipment.getId());

		return "redirect:" + redirectLocation;
	}

	/**
	 * Finds the shipment by ID and removes that shipment from the auction
	 * @param id of shipment being removed from auction
	 * @param model used to load attributes into the Thymeleaf model
	 * @param session used to load attributes into the current users HTTP session
	 * @return /reset/removefromauctionconfirm
	 */
	@RequestMapping("/removefromauction/{id}")
	public String removeFromAuction(@PathVariable("id") long id, Model model, HttpSession session) {
		Shipments shipment = shipmentsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Shipment Id:" + id));
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);
		String redirectLocation = (String) session.getAttribute("redirectLocation");
		model.addAttribute("redirectLocation",redirectLocation);

		if (!user.getRole().toString().equals("MASTERLIST") && !user.getRole().toString().equals("SHIPPER")) {
			System.out.println("User with invalid role " + user.getRole().toString() + " attempted to remove shipment from auction");
			return "redirect:" + redirectLocation;
		}

		if(user.getRole().toString().equals("SHIPPER")) {
			if (shipment.getUser().getId() != user.getId()) {
				System.out.println("Shipper attempted to remove a shipment that wasn't their own from auction");
				return "redirect:" + redirectLocation;
			}
		}

		model.addAttribute("shipments",shipment);
		return "/reset/removefromauctionconfirm";
	}

	/**
	 * Finds the shipment by the ID and then redirects the user back to the base page if they confirm or deny the confirmation
	 * @param id of shipment being removed from auction
	 * @param model used to load attributes into the Thymeleaf model
	 * @param session used to load attributes into the current users HTTP session
	 * @return redirects the user back to the base shipments page
	 */
	@RequestMapping("/removefromauctionconfirmation/{id}")
	public String removeFromAuctionConfirmation(@PathVariable("id") long id, Model model, HttpSession session) {
		Shipments shipment = shipmentsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Shipment Id:" + id));
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);

		if(user.getRole().toString().equals("MASTERLIST")) {
			notificationService.addNotification(shipment.getUser(), "Your shipment with id " + shipment.getId() + " was removed from auction by " + user.getUsername(), false);
		}

		List<Bids> bids = shipment.getBids();

		while (!bids.isEmpty()) {
			Bids bid = bids.get(0);
			notificationService.addNotification(CarriersController.getUserFromCarrier(bid.getCarrier()), 
					"Your bid with id " + bid.getId() + " on shipment with id " + shipment.getId() + " was deleted as the shipment was removed from auction", false);
			bidsRepository.delete(bid);
			bids.remove(bid);
		}

		shipment.setFullFreightTerms("PENDING");
		shipmentsRepository.save(shipment);

		return "redirect:" + (String) session.getAttribute("redirectLocation");
	}

	/**
	 * Finds the shipment by the ID, then allows the user to confirm or deny the ending of auction for the shipment
	 * @param id of shipment being removed from auction
	 * @param model used to load attributes into the Thymeleaf model
	 * @param session used to load attributes into the current users HTTP session
	 * @return redirects the user back to the base shipments page
	 */
	@RequestMapping("/forceendauctionconfirmation/{id}")
	public String forceEndAuctionConfirmation(@PathVariable("id") long id, Model model, HttpSession session) {
		Shipments shipment = shipmentsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid shipment Id:" + id));
		String redirectLocation = (String) session.getAttribute("redirectLocation");
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);

		User bidUser;
		Bids winningBid = shipment.getLowestBid();

		if (winningBid == null) {
			System.out.println("ERROR: Failed to end auction on auction with no bids");
			Logger.error("{} || Failed to end auction on auction with no bids!", user.getUsername()); //TODO: Make errors like this display on the page for user end to see (follow method used in database.html
			return redirectLocation;
		}

		Carriers carrier = winningBid.getCarrier();

		shipment.setCarrier(carrier);
		shipment.setPaidAmount(winningBid.getPrice());
		shipment.setScac(carrier.getScac());
		shipment.setFullFreightTerms("BID ACCEPTED");

		if(shipment.getUser().getId() != user.getId()) {
			notificationService.addNotification(shipment.getUser(), 
					"ALERT: Your shipment with ID " + shipment.getId() + " and Client " + shipment.getClient() + " had its auction forcefully ended. It was given to carrer" + shipment.getCarrier().getCarrierName(), false);

		}

		bidUser = CarriersController.getUserFromCarrier(winningBid.getCarrier());
		notificationService.addNotification(bidUser, 
				"ALERT: You have won the auction on shipment with ID " + shipment.getId() + " with a final bid value of " + winningBid.getPrice(), true);

		shipmentsRepository.save(shipment);
		Logger.info("{} || has successfully ended the auction for shipment with ID {}.", user.getUsername(), shipment.getId());

		return "redirect:" + redirectLocation;
	}

	/**
	 * Finds the user by ID, then returns the user to a confirmation page for toggling auctioning
	 * @param id of the user the auctioning is being toggled on
	 * @param model used to load attributes into the Thymeleaf model
	 * @param session used to load attributes into the current users HTTP session
	 * @return /toggle/toggleauctioningconfirm
	 */
	@GetMapping("/toggleauctioning/{id}")
	public String toggleAuctioning(@PathVariable("id") long id, Model model, HttpSession session) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid shipment Id:" + id));
		User loggedinuser = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(loggedinuser, model);
		String redirectLocation = (String) session.getAttribute("redirectLocation");

		if(!loggedinuser.getRole().toString().equals("ADMIN")) {
			System.out.println("ERROR: Non admin user tried to toggle auctioning for another user!");
			Logger.error("{} || (Non-Admin) tried to toggle auctioning for another user!", loggedinuser.getUsername());
			return redirectLocation;
		}

		model.addAttribute("user",user);
		model.addAttribute("redirectLocation",redirectLocation);

		return "/toggle/toggleauctioningconfirm";
	}


	/**
	 * Finds the user by ID, then returns the user to the base users page if the confirm or deny toggling auctioning
	 * @param id of the user the auctioning is being toggled on
	 * @param model used to load attributes into the Thymeleaf model
	 * @param session used to load attributes into the current users HTTP session
	 * @return redirects the user back to the base users page
	 */
	@GetMapping("/toggleauctioningconfirmation/{id}")
	public String toggleAuctioningconfirmation(@PathVariable("id") long id, Model model, HttpSession session) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid shipment Id:" + id));
		User loggedInUser = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(loggedInUser, model);

		if (user.getAuctioningAllowed()) {
			user.setAuctioningAllowed(false);
			Logger.info("{} || Auctioning is not allowed.", user.getUsername());
			notificationService.addNotification(user, 
					"ALERT: Your auctioning abilites have been disabled. Please contact your system Auctioneer to regain access.", true);
		}
		else {
			user.setAuctioningAllowed(true);
			Logger.info("{} || Auctioning is allowed.", user.getUsername());
			notificationService.addNotification(user, 
					"ALERT: Your auctioning abilites have been re-eneabled. Thank you!", true);
		}
		Logger.info("{} || has successfully changed auctioning permissions for {}.", loggedInUser.getUsername() , user.getUsername());
		userRepository.save(user);

		return "redirect:" + session.getAttribute("redirectLocation");
	}

}
