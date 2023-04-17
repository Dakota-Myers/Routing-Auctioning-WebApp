package edu.sru.thangiah.webrouting.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import edu.sru.thangiah.webrouting.controller.ExcelController;
import edu.sru.thangiah.webrouting.domain.Bids;
import edu.sru.thangiah.webrouting.domain.Carriers;
import edu.sru.thangiah.webrouting.domain.Contacts;
import edu.sru.thangiah.webrouting.domain.Driver;
import edu.sru.thangiah.webrouting.domain.Locations;
import edu.sru.thangiah.webrouting.domain.MaintenanceOrders;
import edu.sru.thangiah.webrouting.domain.Role;
import edu.sru.thangiah.webrouting.domain.Shipments;
import edu.sru.thangiah.webrouting.domain.Technicians;
import edu.sru.thangiah.webrouting.domain.User;
import edu.sru.thangiah.webrouting.domain.VehicleTypes;
import edu.sru.thangiah.webrouting.domain.Vehicles;
import edu.sru.thangiah.webrouting.repository.BidsRepository;
import edu.sru.thangiah.webrouting.repository.CarriersRepository;
import edu.sru.thangiah.webrouting.repository.ContactsRepository;
import edu.sru.thangiah.webrouting.repository.DriverRepository;
import edu.sru.thangiah.webrouting.repository.LocationsRepository;
import edu.sru.thangiah.webrouting.repository.MaintenanceOrdersRepository;
import edu.sru.thangiah.webrouting.repository.RoleRepository;
import edu.sru.thangiah.webrouting.repository.ShipmentsRepository;
import edu.sru.thangiah.webrouting.repository.TechniciansRepository;
import edu.sru.thangiah.webrouting.repository.UserRepository;
import edu.sru.thangiah.webrouting.repository.VehicleTypesRepository;
import edu.sru.thangiah.webrouting.repository.VehiclesRepository;
import edu.sru.thangiah.webrouting.services.SecurityService;
import edu.sru.thangiah.webrouting.services.UserService;
import edu.sru.thangiah.webrouting.services.ValidationServiceImp;
import edu.sru.thangiah.webrouting.web.UserValidator;



@Controller
public class EditAndAddController {


	@Autowired
	private UserService userService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private UserValidator userValidator;

	private CarriersRepository carriersRepository;

	private ShipmentsRepository shipmentsRepository;

	private VehiclesRepository vehiclesRepository;

	private BidsRepository bidsRepository;

	private VehicleTypesRepository vehicleTypesRepository;

	private LocationsRepository	locationsRepository;

	private ContactsRepository contactsRepository;

	private UserRepository userRepository;

	private TechniciansRepository techniciansRepository;

	private DriverRepository driverRepository;

	private RoleRepository roleRepository;

	private MaintenanceOrdersRepository maintenanceOrdersRepository;

	private ValidationServiceImp validationServiceImp;

	private static final Logger Logger = LoggerFactory.getLogger(EditAndAddController.class);


	public EditAndAddController (BidsRepository bidsRepository, ShipmentsRepository shipmentsRepository, CarriersRepository carriersRepository, VehiclesRepository vehiclesRepository, 
			VehicleTypesRepository vehicleTypesRepository,LocationsRepository	locationsRepository, ContactsRepository contactsRepository, TechniciansRepository techniciansRepository,
			DriverRepository driverRepository, MaintenanceOrdersRepository maintenanceOrdersRepository, ValidationServiceImp validationServiceImp, UserRepository userRepository, RoleRepository roleRepository) {
		this.shipmentsRepository = shipmentsRepository;
		this.carriersRepository = carriersRepository;
		this.vehiclesRepository = vehiclesRepository;
		this.bidsRepository = bidsRepository;
		this.vehicleTypesRepository = vehicleTypesRepository;
		this.locationsRepository = locationsRepository;
		this.contactsRepository = contactsRepository;
		this.techniciansRepository = techniciansRepository;
		this.driverRepository = driverRepository;
		this.maintenanceOrdersRepository = maintenanceOrdersRepository;
		this.validationServiceImp = validationServiceImp;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;

	}
	
	
	@GetMapping("/add-contact")
	public String showContactAddForm(Contacts contact,Model model, HttpSession session) {
		model.addAttribute("redirectLocation", (String) session.getAttribute("redirectLocation"));
		model.addAttribute("currentPage","/contacts");
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);

		session.removeAttribute("message");
		model.addAttribute("contactForm", new Contacts());

		return "/add/add-contact";
	}


	@PostMapping("submit-add-contact")
	public String contactAddForm(@ModelAttribute("contactForm") Contacts contact, Model model, HttpSession session) {
		model.addAttribute("redirectLocation", (String) session.getAttribute("redirectLocation"));
		model.addAttribute("currentPage","/contacts");
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);

		Hashtable<String, String> hashtable = new Hashtable<>();

		hashtable.put("firstName", contact.getFirstName().strip());
		hashtable.put("lastName", contact.getLastName().strip());
		hashtable.put("middleInitial", contact.getMiddleInitial().strip());
		hashtable.put("emailAddress", contact.getEmailAddress().strip());
		hashtable.put("streetAddress1", contact.getStreetAddress1().strip());
		hashtable.put("streetAddress2", contact.getStreetAddress2().strip());
		hashtable.put("contactCity", contact.getCity().strip()); 
		hashtable.put("contactState", contact.getState().strip());
		hashtable.put("contactZip", contact.getZip().strip());
		hashtable.put("primaryPhone", contact.getPrimaryPhone().strip());
		hashtable.put("workPhone", contact.getWorkPhone().strip());

		Contacts result;

		result = validationServiceImp.validateContact(hashtable, session);
		
		
		if (result == null) {
			Logger.error("{} || attempted to add a new Contact but "+ session.getAttribute("message") ,user.getUsername());
			model.addAttribute("message", session.getAttribute("message"));
			return "/add/add-contact";
		}

		contactsRepository.save(result);
		Logger.info("{} || successfully added a new Contact with ID {}.", user.getUsername(), result.getId());

		return "redirect:" + (String) session.getAttribute("redirectLocation");
	}

	
	@GetMapping("/add-vehicletype")
	public String showVehicleTypeAddForm(VehicleTypes vehicletype,Model model, HttpSession session) {
		model.addAttribute("redirectLocation", (String) session.getAttribute("redirectLocation"));
		model.addAttribute("currentPage","/vehicletypes");
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);

		session.removeAttribute("message");
		model.addAttribute("vehicleTypeForm", new VehicleTypes());

		return "/add/add-vehicletype";
	}


	@PostMapping("submit-add-vehicletype")
	public String vehicleTypeAddForm(@ModelAttribute("vehicleTypeForm") VehicleTypes vehicleType, Model model, HttpSession session) {
		model.addAttribute("redirectLocation", (String) session.getAttribute("redirectLocation"));
		model.addAttribute("currentPage","/vehicletypes");
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);

		Hashtable<String, String> hashtable = new Hashtable<>();

		hashtable.put("type", vehicleType.getType().strip());
		hashtable.put("subType", vehicleType.getSubType());
		hashtable.put("description", vehicleType.getDescription().strip());
		hashtable.put("make", vehicleType.getMake().strip());
		hashtable.put("model", vehicleType.getModel().strip());
		hashtable.put("minimumWeight", vehicleType.getMinimumWeight());
		hashtable.put("maximumWeight", vehicleType.getMaximumWeight());
		hashtable.put("capacity", vehicleType.getCapacity());
		hashtable.put("maximumRange", vehicleType.getMaximumRange());
		hashtable.put("restrictions", vehicleType.getRestrictions().strip());
		hashtable.put("height", vehicleType.getHeight());
		hashtable.put("emptyWeight", vehicleType.getEmptyWeight());
		hashtable.put("length", vehicleType.getLength());
		hashtable.put("minimumCubicWeight", vehicleType.getMinimumCubicWeight());
		hashtable.put("maximumCubicWeight", vehicleType.getMaximumCubicWeight());

		VehicleTypes result;

		result = validationServiceImp.validateVehicleTypes(hashtable, session);
		
		
		if (result == null) {
			Logger.error("{} || attempted to add a new Vehicle Type but "+ session.getAttribute("message") ,user.getUsername());
			model.addAttribute("message", session.getAttribute("message"));
			return "/add/add-vehicletype";
		}

		vehicleTypesRepository.save(result);
		Logger.info("{} || successfully added a new Vehicle Type with ID {}.", user.getUsername(), result.getId());

		return "redirect:" + (String) session.getAttribute("redirectLocation");
	}
	
	
	@GetMapping("/add-location")
	public String showLocationAddForm(Locations location, Model model, HttpSession session) {
		model.addAttribute("redirectLocation", (String) session.getAttribute("redirectLocation"));
		model.addAttribute("currentPage","/locations");
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);

		session.removeAttribute("message");
		model.addAttribute("locationForm", new Locations());

		return "/add/add-location";
	}


	@PostMapping("submit-add-location")
	public String locationAddForm(@ModelAttribute("locationForm") Locations location, Model model, HttpSession session) {
		model.addAttribute("redirectLocation", (String) session.getAttribute("redirectLocation"));
		model.addAttribute("currentPage","/locations");
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);

		Hashtable<String, String> hashtable = new Hashtable<>();

		hashtable.put("locationName", location.getName().strip());
		hashtable.put("streetAddress1", location.getStreetAddress1().strip());
		hashtable.put("streetAddress2", location.getStreetAddress2().strip());
		hashtable.put("locationCity", location.getCity().strip()); 
		hashtable.put("locationState", location.getState().strip());
		hashtable.put("locationZip", location.getZip().strip());
		hashtable.put("locationLatitude", "");
		hashtable.put("locationLongitude", "");
		hashtable.put("locationType", location.getLocationType().strip());

		Locations result;

		result = validationServiceImp.validateLocations(hashtable, session);

		if (result == null) {
			Logger.error("{} || attempted to add a new Location but "+ session.getAttribute("message") ,user.getUsername());
			model.addAttribute("message", session.getAttribute("message"));
			return "/add/add-location";
		}

		locationsRepository.save(result);
		Logger.info("{} || successfully added a new Location with ID {}.", user.getUsername(), result.getId());

		return "redirect:" + (String) session.getAttribute("redirectLocation");
	}
	
	@GetMapping("/add-vehicle")
	public String showVehicleAddForm(Vehicles vehicle, Model model, HttpSession session) {
		model.addAttribute("redirectLocation", (String) session.getAttribute("redirectLocation"));
		model.addAttribute("currentPage","/vehicles");
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);
		model.addAttribute("vehicleTypes", user.getCarrier().getVehicleTypes()); 
		model.addAttribute("locations", user.getCarrier().getLocations());

		session.removeAttribute("message");
		model.addAttribute("vehicleForm", new Vehicles());

		return "/add/add-vehicle";
	}


	@PostMapping("submit-add-vehicle")
	public String vehicleAddForm(@ModelAttribute("vehicleForm") Vehicles vehicle, Model model, HttpSession session) {
		model.addAttribute("redirectLocation", (String) session.getAttribute("redirectLocation"));
		model.addAttribute("currentPage","/vehicles");
		User user = userService.getLoggedInUser();
		model = NotificationController.loadNotificationsIntoModel(user, model);
		model.addAttribute("vehicleTypes", user.getCarrier().getVehicleTypes()); 
		model.addAttribute("locations", user.getCarrier().getLocations());

		Hashtable<String, String> hashtable = new Hashtable<>();

		hashtable.put("plate", vehicle.getPlateNumber().strip());
		hashtable.put("vin", vehicle.getVinNumber().strip());
		hashtable.put("manufacturedYear", vehicle.getManufacturedYear().strip());
		hashtable.put("vehicleTypeMakeModel", vehicle.getVehicleType().getMake() + " " + vehicle.getVehicleType().getModel());
		hashtable.put("locationName", vehicle.getLocation().getName());
		
		Vehicles result;

		result = validationServiceImp.validateVehicles(hashtable, session);

		if (result == null) {
			Logger.error("{} || attempted to add a new Vehicle but "+ session.getAttribute("message") ,user.getUsername());
			model.addAttribute("message", session.getAttribute("message"));
			return "/add/add-vehicle";
		}
		
		vehiclesRepository.save(result);
		Logger.info("{} || successfully added a new Vehicle with ID {}.", user.getUsername(), result.getId());

		return "redirect:" + (String) session.getAttribute("redirectLocation");
	}
	
	

}