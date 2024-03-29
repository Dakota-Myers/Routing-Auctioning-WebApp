package edu.sru.thangiah.webrouting.services;

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
import org.springframework.stereotype.Service;

import edu.sru.thangiah.webrouting.controller.ExcelController;
import edu.sru.thangiah.webrouting.domain.Contacts;
import edu.sru.thangiah.webrouting.domain.Driver;
import edu.sru.thangiah.webrouting.domain.Locations;
import edu.sru.thangiah.webrouting.domain.MaintenanceOrders;
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
import edu.sru.thangiah.webrouting.repository.ShipmentsRepository;
import edu.sru.thangiah.webrouting.repository.TechniciansRepository;
import edu.sru.thangiah.webrouting.repository.UserRepository;
import edu.sru.thangiah.webrouting.repository.VehicleTypesRepository;
import edu.sru.thangiah.webrouting.repository.VehiclesRepository;
import edu.sru.thangiah.webrouting.web.UserValidator;


/**
 * Handles all of the validation for the database interactions
 * @author Dakota Myers drm1022@sru.edu
 * @since 1/01/2023
 */
@Service
public class ValidationServiceImp {


	@Autowired
	private UserService userService;
	
	@Autowired
	private ApiServiceImpl apiServiceImp;

	private CarriersRepository carriersRepository;

	private ShipmentsRepository shipmentsRepository;

	private VehiclesRepository vehiclesRepository;

	private BidsRepository bidsRepository;

	private VehicleTypesRepository vehicleTypesRepository;

	private LocationsRepository	locationsRepository;

	private ContactsRepository contactsRepository;

	private TechniciansRepository techniciansRepository;

	private DriverRepository driverRepository;

	private MaintenanceOrdersRepository maintenanceOrdersRepository;

	private static final Logger Logger = LoggerFactory.getLogger(ValidationServiceImp.class);


	/**
	 * Constructor for the ValidationServiceImp
	 * @param bidsRepository holds the bid sRepository
	 * @param shipmentsRepository holds the shipments Repository
	 * @param carriersRepository holds the carriers Repository
	 * @param vehiclesRepository holds the vehicles Repository
	 * @param vehicleTypesRepository holds the vehicleTypes Repository
	 * @param locationsRepository holds the locations Repository
	 * @param contactsRepository holds the contacts Repository
	 * @param techniciansRepository holds the technicians Repository
	 * @param driverRepository holds the driver Repository
	 * @param maintenanceOrdersRepository holds the maintenanceOrders Repository
	 */
	
	public ValidationServiceImp (BidsRepository bidsRepository, ShipmentsRepository shipmentsRepository, CarriersRepository carriersRepository, VehiclesRepository vehiclesRepository, 
			VehicleTypesRepository vehicleTypesRepository,LocationsRepository	locationsRepository, ContactsRepository contactsRepository, TechniciansRepository techniciansRepository,
			DriverRepository driverRepository, MaintenanceOrdersRepository maintenanceOrdersRepository) {
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

	}


	/**
	 * Validates the shipment excel sheet, returns a list of valid shipments
	 * @param worksheet holds the shipment worksheet
	 * @param session used to load attributes into the current users HTTP session
	 * @return result
	 */
	
	public List<Shipments> validateShipmentSheet(XSSFSheet worksheet, HttpSession session){

		List <Shipments> result = new ArrayList<>();

		try {
			User user = userService.getLoggedInUser();

			for(int i=1; i<worksheet.getPhysicalNumberOfRows(); i++) {
				Boolean isNull = false;

				Shipments shipment = new Shipments();
				XSSFRow row = worksheet.getRow(i);

				for (int j = 0; j<11; j++) {
					if (row.getCell(j)== null || row.getCell(j).toString().equals("")) {
						isNull = true;
					}
				}
				if (isNull == true) {
					break;
				}


				String scac = "";											//Not Set By Upload
				String freightBillNumber = "0.00";							//Not Set By Upload
				String paidAmount = "0.00";									//Not Set By Upload
				String fullFreightTerms = "PENDING"; 						//Not Set By Upload (ALWAYS PENDING)
				String shipperLatitude = "";
				String shipperLongitude = "";
				String consigneeLatitude = "";
				String consigneeLongitude = "";


				String clientName = row.getCell(0).toString().strip();
				String clientMode = row.getCell(1).toString().strip();
				String date = row.getCell(2).toString();
				String commodityClass = row.getCell(3).toString().strip();
				String commodityPieces = row.getCell(4).toString().strip();
				String commodityPaidWeight = row.getCell(5).toString().strip();
				String shipperCity = row.getCell(6).toString().strip();
				String shipperState = row.getCell(7).toString().strip();
				String shipperZip = row.getCell(8).toString().strip();
				String consigneeCity = row.getCell(9).toString().strip();
				String consigneeState = row.getCell(10).toString().strip();
				String consigneeZip = row.getCell(11).toString().strip();



				commodityClass = commodityClass.substring(0, commodityClass.length() - 2);
				commodityPieces = commodityPieces.substring(0, commodityPieces.length() - 2);
				commodityPaidWeight = commodityPaidWeight.substring(0, commodityPaidWeight.length() - 2);
				shipperZip = shipperZip.substring(0, shipperZip.length() - 2);
				consigneeZip = consigneeZip.substring(0, consigneeZip.length() - 2);



				Hashtable<String, String> hashtable = new Hashtable<>();

				hashtable.put("clientName", clientName);
				hashtable.put("clientMode", clientMode);
				hashtable.put("date", date);
				hashtable.put("commodityClass", commodityClass);
				hashtable.put("commodityPieces", commodityPieces);
				hashtable.put("commodityPaidWeight", commodityPaidWeight);
				hashtable.put("shipperCity", shipperCity);
				hashtable.put("shipperState", shipperState);
				hashtable.put("shipperZip", shipperZip);
				hashtable.put("shipperLatitude", shipperLatitude);
				hashtable.put("shipperLongitude", shipperLongitude);
				hashtable.put("consigneeCity", consigneeCity);
				hashtable.put("consigneeState", consigneeState);
				hashtable.put("consigneeZip", consigneeZip);
				hashtable.put("consigneeLatitude", consigneeLatitude);
				hashtable.put("consigneeLongitude", consigneeLongitude);


				hashtable.put("scac", scac);
				hashtable.put("freightBillNumber", freightBillNumber);
				hashtable.put("paidAmount", paidAmount);
				hashtable.put("fullFreightTerms", fullFreightTerms);


				shipment = validateShipment(hashtable, session);
				if (shipment == null) {
					return null;								//Change this to continue if you want it to continue after one is invalid
				}

				shipment.setCarrier(null);					//THIS IS DEFAULT
				shipment.setVehicle(null);					//THIS IS DEFAULT
				shipment.setUser(user);						//THIS USER

				result.add(shipment);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			session.setAttribute("message", "Excel File was incorrectly formatted.");
			return null;
		}
		return result;
	}

	/**
	 * Validates all of the shipment object attributes
	 * Returns valid shipment
	 * @param hashtable holds the attributes of the shipment
	 * @param session used to load attributes into the current users HTTP session
	 * @return shipment
	 */
	
	public Shipments validateShipment(Hashtable<String, String> hashtable, HttpSession session) {

		List<String> acceptedFreightTerms = Arrays.asList("PENDING", "AVAILABLE SHIPMENT", "AWAITING ACCEPTANCE", "BID ACCEPTED", "FROZEN");
		List<String> states = Arrays.asList("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming");
		List<String> stateAbbreviations = Arrays.asList("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY");

		User user = userService.getLoggedInUser();

		String clientName = (String) hashtable.get("clientName");
		String clientMode = (String)hashtable.get("clientMode");
		String date = (String)hashtable.get("date");
		String commodityClass = (String)hashtable.get("commodityClass");
		String commodityPieces = (String)hashtable.get("commodityPieces");
		String commodityPaidWeight = (String)hashtable.get("commodityPaidWeight");
		String shipperCity = (String)hashtable.get("shipperCity");
		String shipperState = (String)hashtable.get("shipperState");
		String shipperZip = (String)hashtable.get("shipperZip");
		String shipperLatitude = (String)hashtable.get("shipperLatitude");
		String shipperLongitude = (String)hashtable.get("shipperLongitude");
		String consigneeCity = (String)hashtable.get("consigneeCity");
		String consigneeState = (String)hashtable.get("consigneeState");
		String consigneeZip = (String)hashtable.get("consigneeZip");
		String consigneeLatitude = (String)hashtable.get("consigneeLatitude");
		String consigneeLongitude = (String)hashtable.get("consigneeLongitude");
		String freightBillNumber = (String)hashtable.get("freightBillNumber");
		String paidAmount = (String)hashtable.get("paidAmount");
		String scac = (String)hashtable.get("scac");
		String fullFreightTerms = (String)hashtable.get("fullFreightTerms");


		if(!(acceptedFreightTerms.contains(fullFreightTerms))) {
			session.setAttribute("message", "Full Freight Term must be an accepted term.");
			return null;
		}

		if(!(scac.length() <= 4 && scac.length() >= 2) || !(scac.matches("^[a-zA-Z0-9]+$"))) {
			if (!(scac.equals("") || scac == null)) {
				session.setAttribute("message", "SCAC must be between 2 and 4 characters long or empty.");
				return null;
			}	
		}

		if(!(paidAmount.length() <= 16 && paidAmount.length() > 0) || !(freightBillNumber.matches("^[0-9]*\\.?[0-9]+$"))) {
			session.setAttribute("message", "Paid Amount must be between 1 and 16 numbers long.");
			return null;
		}

		if(!(freightBillNumber.length() <= 32 && freightBillNumber.length() > 0) || !(freightBillNumber.matches("^[0-9]*\\.?[0-9]+$"))) {
			session.setAttribute("message", "Freight Bill Number must be between 1 and 32 numbers long.");
			return null;
		}

		if (!(clientName.length() <= 64 && clientName.length() > 0)) {
			session.setAttribute("message", "Client Name must be between 1 and 64 characters.");
			return null;
		}

		if(!(clientMode.equals("LTL") || clientMode.equals("FTL"))) {
			session.setAttribute("message", "Client Mode must be LTL or FTL.");
			return null;
		}

		if(!(date.length() <= 12 && date.length() > 0 && date.matches("^\\d{2}-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4}$"))) { 
			session.setAttribute("message", "Date must be between 1 and 12 characters and formated MM/DD/YYYY.");
			return null;
		}

		if(!(commodityClass.length() <= 12 && commodityClass.length() > 0) || !(commodityClass.matches("^[a-zA-Z0-9.]+$"))) {
			session.setAttribute("message", "Commodity Class must be between 1 and 12 characters and alphanumeric.");
			return null;
		}

		if(!(commodityPieces.length() <= 64 && commodityPieces.length() > 0) || !(commodityPieces.matches("^[0-9.]+$"))) {
			session.setAttribute("message", "Commodity Pieces must be between 1 and 64 characters long and numeric.");
			return null;
		}

		if(!(commodityPaidWeight.length() <= 16 && commodityPaidWeight.length() > 0) || !(commodityPaidWeight.matches("^[0-9.]*\\.?[0-9.]+$"))) {
			session.setAttribute("message", "Commodity Paid Weight must be between 1 and 16 characters long and numeric.");
			return null;
		}

		if(!(shipperCity.length() <= 64 && shipperCity.length() > 0) || !(shipperCity.matches("^[A-Za-z]+(?:[\\s-][A-Za-z]+)*$"))) {
			session.setAttribute("message", "Shipper City must be between 1 and 64 characters and alphabetic.");
			return null;
		}

		if(!(states.contains(shipperState) || stateAbbreviations.contains(shipperState))) {
			session.setAttribute("message", "Shipper State must be a state or state abbreviation.");
			return null;
		}

		if(!(shipperZip.length() <= 12 && shipperZip.length() > 0) || !(shipperZip.matches("^[0-9.]+$"))){
			session.setAttribute("message", "Shipper Zip must be between 1 and 12 characters and numeric.");
			return null;
		}

		if(!(consigneeCity.length() <= 64 && consigneeCity.length() > 0) || !( consigneeCity.matches("^[A-Za-z]+(?:[\\s-][A-Za-z]+)*$"))) {
			session.setAttribute("message", "Consignee City must be between 1 and 64 characters and alphabetic.");
			return null;
		}

		if(!(states.contains(consigneeState) || stateAbbreviations.contains(consigneeState))) {
			session.setAttribute("message", "Consignee State must be a state or state abbreviation.");
			return null;
		}

		if(!(consigneeZip.length() <= 12 && consigneeZip.length() > 0) || !(consigneeZip.matches("^[0-9.]+$"))){
			session.setAttribute("message", "Consignee Zip must be between 1 and 12 characters and is alphabetic.");
			return null;
		}

		String shipperLatAndLong = apiServiceImp.fetchLatitudeAndLongitude(shipperCity, shipperState);
		String consigneeLatAndLong = apiServiceImp.fetchLatitudeAndLongitude(consigneeCity, consigneeState);

		if (!shipperLatAndLong.equals("")){
			String[] parts = shipperLatAndLong.split("\\|");
			shipperLatitude = parts[0];
			shipperLongitude = parts[1];
		}
		
		if (!consigneeLatAndLong.equals("")){
			String[] parts = consigneeLatAndLong.split("\\|");
			consigneeLatitude = parts[0];
			consigneeLongitude = parts[1];
		}
		
		Shipments shipment = new Shipments();
		
		
		shipment.setClient(clientName);
		shipment.setClientMode(clientMode);
		shipment.setShipDate(date);
		shipment.setCommodityClass(commodityClass);
		shipment.setCommodityPieces(commodityPieces);
		shipment.setCommodityPaidWeight(commodityPaidWeight);
		shipment.setShipperCity(shipperCity);
		shipment.setShipperState(shipperState);
		shipment.setShipperZip(shipperZip);
		shipment.setShipperLatitude(shipperLatitude);
		shipment.setShipperLongitude(shipperLongitude);
		shipment.setConsigneeCity(consigneeCity);
		shipment.setConsigneeState(consigneeState);
		shipment.setConsigneeZip(consigneeZip);
		shipment.setConsigneeLatitude(consigneeLatitude);
		shipment.setConsigneeLongitude(consigneeLongitude);

		shipment.setFreightbillNumber(freightBillNumber);
		shipment.setPaidAmount(paidAmount);
		shipment.setFullFreightTerms(fullFreightTerms);
		shipment.setScac(scac);

		return shipment;	

	}


	/**
	 * Validates the vehicles types excel sheet, returns a list of valid vehicle types
	 * @param worksheet holds the vehicle types worksheet
	 * @param session used to load attributes into the current users HTTP session
	 * @return result
	 */
	
	public List<VehicleTypes> validateVehicleTypesSheet(XSSFSheet worksheet, HttpSession session){
		List <VehicleTypes> result = new ArrayList<>();
		User user = userService.getLoggedInUser();
		try {
			for(int i=1; i<worksheet.getPhysicalNumberOfRows(); i++) {
				ArrayList <String> allVehicleTypes = new ArrayList<>();
				VehicleTypes VehicleType = new VehicleTypes();
				XSSFRow row = worksheet.getRow(i);

				if ((row.getCell(0)== null || row.getCell(0).toString().equals("")) || (row.getCell(1)== null || row.getCell(1).toString().equals("")) || (row.getCell(3)== null || row.getCell(3).toString().equals("")) 
						|| (row.getCell(4)== null || row.getCell(4).toString().equals("")) || (row.getCell(5)== null || row.getCell(5).toString().equals("")) || (row.getCell(6)== null || row.getCell(6).toString().equals(""))
						|| (row.getCell(7)== null || row.getCell(7).toString().equals("")) || (row.getCell(8)== null || row.getCell(8).toString().equals("")) || (row.getCell(10)== null || row.getCell(10).toString().equals("")) 
						|| (row.getCell(11)== null || row.getCell(11).toString().equals("")) || (row.getCell(12)== null || row.getCell(12).toString().equals("")) || (row.getCell(13)== null || row.getCell(13).toString().equals(""))
						|| (row.getCell(14)== null || row.getCell(14).toString().equals(""))) {
					break;
				}

				String type = row.getCell(0).toString().strip();
				String subType = row.getCell(1).toString().strip();
				String description = row.getCell(2).toString().strip();
				String make = row.getCell(3).toString().strip();
				String model = row.getCell(4).toString().strip();
				String minimumWeight = row.getCell(5).toString().strip();
				String maximumWeight = row.getCell(6).toString().strip();
				String capacity = row.getCell(7).toString().strip();
				String maximumRange = row.getCell(8).toString().strip();
				String restrictions = row.getCell(9).toString().strip();
				String height = row.getCell(10).toString().strip();
				String emptyWeight = row.getCell(11).toString().strip();
				String length = row.getCell(12).toString().strip();
				String minimumCubicWeight = row.getCell(13).toString().strip();
				String maximumCubicWeight = row.getCell(14).toString().strip();


				minimumWeight = minimumWeight.substring(0, minimumWeight.length() - 2);
				maximumWeight = maximumWeight.substring(0, maximumWeight.length() - 2);
				capacity = capacity.substring(0, capacity.length() - 2);
				maximumRange = maximumRange.substring(0, maximumRange.length() - 2);
				height = height.substring(0, height.length() - 2);
				emptyWeight = emptyWeight.substring(0, emptyWeight.length() - 2);
				length = length.substring(0, length.length() - 2);
				minimumCubicWeight = minimumCubicWeight.substring(0, minimumCubicWeight.length() - 2);
				maximumCubicWeight = maximumCubicWeight.substring(0, maximumCubicWeight.length() - 2);


				Hashtable<String, String> hashtable = new Hashtable<>();

				hashtable.put("type", type);
				hashtable.put("subType", subType);
				hashtable.put("description", description);
				hashtable.put("make", make);
				hashtable.put("model", model);
				hashtable.put("minimumWeight", minimumWeight);
				hashtable.put("maximumWeight", maximumWeight);
				hashtable.put("capacity", capacity);
				hashtable.put("maximumRange", maximumRange);
				hashtable.put("restrictions", restrictions);
				hashtable.put("height", height);
				hashtable.put("emptyWeight", emptyWeight);
				hashtable.put("length", length);
				hashtable.put("minimumCubicWeight", minimumCubicWeight);
				hashtable.put("maximumCubicWeight", maximumCubicWeight);

				VehicleType = validateVehicleTypes(hashtable, session);
				if (VehicleType == null) {
					return null;								
				}  

				for(VehicleTypes v : result) {
					allVehicleTypes.add(v.getMake() + " " + v.getModel());	
				}

				if(allVehicleTypes.contains(make + " " + model)) {
					session.setAttribute("message", "There was mulitple vehicle types with the same make and model within the sheet.");
					return null;
				}
				result.add(VehicleType);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			session.setAttribute("message", "Excel File was incorrectly formatted.");
			return null;
		}
		return result;
	}

	/**
	 * Validates all of the vehicle type object attributes
	 * Returns valid vehicle type
	 * @param hashtable holds the attributes of the vehicle types
	 * @param session used to load attributes into the current users HTTP session
	 * @return vehicletype
	 */
	
	public VehicleTypes validateVehicleTypes(Hashtable<String, String> hashtable, HttpSession session) {

		User user = userService.getLoggedInUser();
		List <VehicleTypes> repoVehicleTypes =  user.getCarrier().getVehicleTypes();
		ArrayList <String> allVehicleTypes = new ArrayList<>();

		for(VehicleTypes v : repoVehicleTypes) {
			allVehicleTypes.add(v.getMake() + " " + v.getModel());	
		}

		String type = (String) hashtable.get("type");
		String subType = (String) hashtable.get("subType");
		String description = (String) hashtable.get("description");
		String make = (String) hashtable.get("make");
		String model = (String) hashtable.get("model");
		String minimumWeight = (String) hashtable.get("minimumWeight");
		String maximumWeight = (String) hashtable.get("maximumWeight");
		String capacity = (String) hashtable.get("capacity");
		String maximumRange = (String) hashtable.get("maximumRange");
		String restrictions = (String) hashtable.get("restrictions");
		String height = (String) hashtable.get("height");
		String emptyWeight = (String) hashtable.get("emptyWeight");
		String length = (String) hashtable.get("length");
		String minimumCubicWeight = (String) hashtable.get("minimumCubicWeight");
		String maximumCubicWeight = (String) hashtable.get("maximumCubicWeight");


		if(allVehicleTypes.contains(make + " " + model)) {
			session.setAttribute("message", "Vehicle Type make and model aleady exists.");
			return null;
		}

		if (!(type.length() <= 32 && type.length() > 0) || !(type.matches("^[a-zA-Z ]+$"))) {
			session.setAttribute("message", "Type was not between 1 and 32 alphabetic characters.");
			return null;	
		}

		if (!(subType.length() <= 32 && subType.length() > 0) || !(subType.matches("^[a-zA-Z ]+$"))) {
			session.setAttribute("message", "Sub Type was not between 1 and 32 characters.");
			return null;	
		}

		if (!(description == null || description.equals(""))) {
			if (!(description.length() <= 64 && description.length() > 0)) {
				session.setAttribute("message", "Description was not between 1 and 64 characters.");
				return null;	
			}
		}

		if(!(make.length() <= 32 && make.length() > 0)) {
			session.setAttribute("message", "Make was not between 1 and 32 characters.");
			return null;
		}

		if(!(model.length() <= 32 && model.length() > 0)) {
			session.setAttribute("message", "Model was not between 1 and 32 characters.");
			return null;
		}

		if (!(minimumWeight.length() <= 16 && minimumWeight.length() > 0) || !(minimumWeight.matches("^[0-9.]+$"))) {
			session.setAttribute("message", "Minimum Weight was not between 1 and 16 numeric characters.");
			return null;
		}

		if (!(maximumWeight.length() <= 16 && maximumWeight.length() > 0) || !(maximumWeight.matches("^[0-9.]+$"))) { 
			session.setAttribute("message", "Maximum Weight was not between 1 and 16 numeric characters.");
			return null;
		}

		if (!(capacity == null || capacity.equals(""))) {
			if(!(capacity.length() <= 16 && capacity.length() > 0) || !(capacity.matches("^[0-9-.]+$"))) {
				session.setAttribute("message", "Capacity was not between 1 and 16 numeric characters.");
				return null;
			}
		}

		if(!(maximumRange.length() <= 16 && maximumRange.length() > 0) || !(maximumRange.matches("^[0-9.]+$"))) {
			session.setAttribute("message", "Maximum Range was not between 1 and 16 numeric characters.");
			return null;
		}

		if (!(restrictions == null || restrictions.equals(""))) {
			if(!(restrictions.length() <= 128 && restrictions.length() > 0)) {
				session.setAttribute("message", "Restrictions was not between 1 and 128 characters.");
				return null;
			}
		}

		if(!(height.length() <= 16 && height.length() > 0) || !(height.matches("^[0-9.]+$"))) {
			session.setAttribute("message", "Height was not between 1 and 16 numeric characters.");
			return null;
		}

		if(!(emptyWeight.length() <= 16 && emptyWeight.length() > 0) || !(emptyWeight.matches("^[0-9.]+$"))) {
			session.setAttribute("message", "Empty Weight was not between 1 and 16 numeric characters.");
			return null;
		}

		if(!(length.length() <= 16 && length.length() > 0) || !( length.matches("^[0-9.]+$"))) {
			session.setAttribute("message", "Length was not between 1 and 16 numeric characters.");
			return null;
		}

		if(!(minimumCubicWeight.length() <= 16 && minimumCubicWeight.length() > 0) || !(minimumCubicWeight.matches("^[0-9.]+$"))){
			session.setAttribute("message", "Minimum Cubic Weight was not between 1 and 16 numeric characters.");
			return null;
		}

		if(!(maximumCubicWeight.length() <= 16 && maximumCubicWeight.length() > 0) || !(maximumCubicWeight.matches("^[0-9.]+$"))){
			session.setAttribute("message", "Maximum Cubic Weight was not between 1 and 16 numeric characters.");
			return null;
		}


		VehicleTypes vehicleType = new VehicleTypes();

		try {
			vehicleType.setType(type);
			vehicleType.setSubType(subType);
			vehicleType.setDescription(description);
			vehicleType.setMake(make);
			vehicleType.setModel(model);
			vehicleType.setMinimumWeight(minimumWeight);
			vehicleType.setMaximumWeight(maximumWeight);
			vehicleType.setCapacity(capacity);
			vehicleType.setMaximumRange(maximumRange);
			vehicleType.setRestrictions(restrictions);
			vehicleType.setHeight(height);
			vehicleType.setEmptyWeight(emptyWeight);
			vehicleType.setLength(length);
			vehicleType.setMinimumCubicWeight(minimumCubicWeight);
			vehicleType.setMaximumCubicWeight(maximumCubicWeight);
			vehicleType.setCarrier(user.getCarrier());
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return vehicleType;

	}

	/**
	 * Validates the locations excel sheet, returns a list of valid locations
	 * @param worksheet holds the locations worksheet
	 * @param session used to load attributes into the current users HTTP session
	 * @return result
	 */
	
	public List<Locations> validateLocationsSheet(XSSFSheet worksheet, HttpSession session){
		List <Locations> result = new ArrayList<>();
		User user = userService.getLoggedInUser();
		try {
			for(int i=1; i<worksheet.getPhysicalNumberOfRows(); i++) {
				List<String> allLocations = new ArrayList<>();
				Locations location = new Locations();
				XSSFRow row = worksheet.getRow(i);

				if ((row.getCell(0)== null || row.getCell(0).toString().equals("")) || (row.getCell(1)== null || row.getCell(1).toString().equals("")) || (row.getCell(3)== null || row.getCell(3).toString().equals("")) 
						|| (row.getCell(4)== null || row.getCell(4).toString().equals("")) || (row.getCell(5)== null || row.getCell(5).toString().equals("")) || (row.getCell(6)== null || row.getCell(6).toString().equals(""))) {
					break;
				}

				String locationName = row.getCell(0).toString().strip();
				String streetAddress1 = row.getCell(1).toString().strip();
				String streetAddress2 = row.getCell(2).toString().strip();
				String locationCity = row.getCell(3).toString().strip();
				String locationState = row.getCell(4).toString().strip();
				String locationZip = row.getCell(5).toString().strip();
				String locationType = row.getCell(6).toString().strip();
				String locationLatitude = "";
				String locationLongitude = "";

				locationZip = locationZip.substring(0, locationZip.length() - 2);

				Hashtable<String, String> hashtable = new Hashtable<>();

				hashtable.put("locationName", locationName);
				hashtable.put("streetAddress1", streetAddress1);
				hashtable.put("streetAddress2", streetAddress2);
				hashtable.put("locationCity", locationCity); 
				hashtable.put("locationState", locationState);
				hashtable.put("locationZip", locationZip);
				hashtable.put("locationLatitude", locationLatitude);
				hashtable.put("locationLongitude", locationLongitude);
				hashtable.put("locationType", locationType);


				location = validateLocations(hashtable, session);
				if (location == null) {
					return null;
				}

				for(Locations l: result) {
					allLocations.add(l.getName());	
				}

				if(allLocations.contains(locationName)) {
					session.setAttribute("message", "There was mulitple locations with the same name within the sheet.");
					return null;
				}

				result.add(location);	
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			session.setAttribute("message", "Excel File was incorrectly formatted.");
			return null;
		}

		return result;
	}
	
	/**
	 * Validates all of the location object attributes
	 * Returns valid location
	 * @param hashtable holds the attributes of the location
	 * @param session used to load attributes into the current users HTTP session
	 * @return location
	 */
	
	public Locations validateLocations(Hashtable<String, String> hashtable, HttpSession session) {
		User user = userService.getLoggedInUser();
		List <Locations> repoLocations = user.getCarrier().getLocations();
		ArrayList <String> allLocations = new ArrayList<>();
		for(Locations l: repoLocations) {
			allLocations.add(l.getName());	
		}

		List<String> states = Arrays.asList("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming");
		List<String> stateAbbreviations = Arrays.asList("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY");


		String locationName = (String) hashtable.get("locationName");
		String streetAddress1 = (String)hashtable.get("streetAddress1");
		String streetAddress2 = (String)hashtable.get("streetAddress2");
		String locationCity = (String)hashtable.get("locationCity");
		String locationState = (String)hashtable.get("locationState");
		String locationZip = (String)hashtable.get("locationZip");
		String locationLatitude = (String)hashtable.get("locationLatitude");
		String locationLongitude = (String)hashtable.get("locationLongitude");
		String locationType = (String)hashtable.get("locationType");

		if (!(locationName.length() <= 32 && locationName.length() > 0) || !(locationName.matches("^[a-zA-Z ']+$"))) {
			session.setAttribute("message", "Name must be between 1 and 32 characters and alphabetic.");
			return null;
		}

		if (allLocations.contains(locationName)) { 
			session.setAttribute("message", "Location with the same name already exists.");
			return null;
		}

		if(!(streetAddress1.length() <= 64 && streetAddress1.length() > 0) || !(streetAddress1.matches("\\d+\\s+([a-zA-Z.]+\\s?)+"))) { 
			session.setAttribute("message", "Address must be between 1 and 64 characters that are alphanumeric.");
			return null;
		}

		if (!(streetAddress2 == null || streetAddress2.equals(""))) {
			if(!(streetAddress2.length() <= 64 && streetAddress2.length() > 0) || !(streetAddress2.matches("^[A-Za-z0-9./-]+(?:[\\s-][A-Za-z0-9.-]+)*$"))) {
				session.setAttribute("message", "Street Address 2 must be between 1 and 64 characters that are alphanumeric.");
				return null;
			}
		}

		if(!(locationCity.length() <= 64 && locationCity.length() > 0) || !(locationCity.matches("^[A-Za-z]+(?:[\\s-][A-Za-z]+)*$"))) { 
			session.setAttribute("message", "City must be between 1 and 64 characters and is alphabetic.");
			return null;
		}

		if(!(states.contains(locationState) || stateAbbreviations.contains(locationState))) {  
			session.setAttribute("message", "State must be a state or state abbreviation.");
			return null;
		}

		if(!(locationZip.length() <= 12 && locationZip.length() > 0) || !(locationZip.matches("^[0-9.]+$"))){ 
			session.setAttribute("message", "Zip must be between 1 and 12 characters and is numeric.");
			return null;
		}

		if(!(locationType.length() <= 64 && locationType.length() > 0) || !(locationType.matches("^[a-zA-Z ]+$"))){ 
			session.setAttribute("message", "Type must be 1 to 32 alphabetic characters.");
			return null;
		}

		String latAndLong = apiServiceImp.fetchLatitudeAndLongitude(locationCity, locationState);

		if (!latAndLong.equals("")){
			String[] parts = latAndLong.split("\\|");
			locationLatitude = parts[0];
			locationLongitude = parts[1];
		}
		
		Locations location = new Locations();


		location.setName(locationName);
		location.setStreetAddress1(streetAddress1);
		location.setStreetAddress2(streetAddress2);
		location.setCity(locationCity);
		location.setState(locationState);
		location.setZip(locationZip);
		location.setLatitude(locationLatitude);
		location.setLongitude(locationLongitude);
		location.setCarrier(user.getCarrier());
		location.setLocationType(locationType);
		location.setCarrier(user.getCarrier());

		return location;
	}

	/**
	 * Validates the contacts excel sheet, returns a list of valid contacts
	 * @param worksheet holds the contacts worksheet
	 * @param session used to load attributes into the current users HTTP session
	 * @return result
	 */
	
	public List<Contacts> validateContactsSheet(XSSFSheet worksheet, HttpSession session){
		List <Contacts> result = new ArrayList<>();
		User user = userService.getLoggedInUser();
		try {
			for(int i=1; i<worksheet.getPhysicalNumberOfRows(); i++) {
				
				
				List<String> allContacts = new ArrayList<>();
				Contacts contact = new Contacts();
				XSSFRow row = worksheet.getRow(i);

				if ((row.getCell(0)== null || row.getCell(0).toString().equals("")) || (row.getCell(1)== null || row.getCell(1).toString().equals("")) || (row.getCell(3)== null || row.getCell(3).toString().equals("")) 
						|| (row.getCell(4)== null || row.getCell(4).toString().equals("")) || (row.getCell(6)== null || row.getCell(6).toString().equals("")) || (row.getCell(7)== null || row.getCell(7).toString().equals(""))
						|| (row.getCell(8)== null || row.getCell(8).toString().equals("")) || (row.getCell(9)== null || row.getCell(9).toString().equals(""))) {
					break;
				}

				String firstName = row.getCell(0).toString().strip();
				String lastName = row.getCell(1).toString().strip();
				String middleInitial = row.getCell(2).toString().strip();
				String emailAddress = row.getCell(3).toString().strip();
				String streetAddress1 = row.getCell(4).toString().strip();
				String streetAddress2 = row.getCell(5).toString().strip();
				String contactCity = row.getCell(6).toString().strip();
				String contactState = row.getCell(7).toString().strip();
				String contactZip = row.getCell(8).toString().strip();
				String primaryPhone = row.getCell(9).toString().strip();
				String workPhone = row.getCell(10).toString().strip();


				contactZip = contactZip.substring(0, contactZip.length() - 2);

				Hashtable<String, String> hashtable = new Hashtable<>();

				hashtable.put("firstName", firstName);
				hashtable.put("lastName", lastName);
				hashtable.put("middleInitial", middleInitial);
				hashtable.put("emailAddress", emailAddress);
				hashtable.put("streetAddress1", streetAddress1);
				hashtable.put("streetAddress2", streetAddress2);
				hashtable.put("contactCity", contactCity); 
				hashtable.put("contactState", contactState);
				hashtable.put("contactZip", contactZip);
				hashtable.put("primaryPhone", primaryPhone);
				hashtable.put("workPhone", workPhone);

				contact = validateContact(hashtable, session);
				if (contact == null) {
					return null;
				}
				for(Contacts c: result) {
					allContacts.add(c.getFirstName() + " " + c.getLastName());	
				}

				if(allContacts.contains(firstName + " " + lastName)) {
					session.setAttribute("message", "There was mulitple contacts with the same first name and last name within the sheet.");
					return null;
				}
				result.add(contact);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			session.setAttribute("message", "Excel File was incorrectly formatted.");
			return null;
		}
		
		return result;
	}
	
	/**
	 * Validates all of the contact object attributes
	 * Returns valid contact
	 * @param hashtable holds the attributes of the contact
	 * @param session used to load attributes into the current users HTTP session
	 * @return contact
	 */
	
	public Contacts validateContact(Hashtable<String, String> hashtable, HttpSession session) {
		User user = userService.getLoggedInUser();
		List <Contacts> repoContacts =  user.getCarrier().getContacts();
		ArrayList <String> allContacts = new ArrayList<>();

		for(Contacts c: repoContacts) {
			allContacts.add(c.getFirstName() + " " + c.getLastName());
		}


		List<String> states = Arrays.asList("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming");
		List<String> stateAbbreviations = Arrays.asList("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY");

		String firstName = (String) hashtable.get("firstName");
		String lastName = (String)hashtable.get("lastName");
		String middleInitial = (String)hashtable.get("middleInitial");
		String emailAddress = (String)hashtable.get("emailAddress");
		String streetAddress1 = (String)hashtable.get("streetAddress1");
		String streetAddress2 = (String)hashtable.get("streetAddress2");
		String contactCity = (String)hashtable.get("contactCity");
		String contactState = (String)hashtable.get("contactState");
		String contactZip = (String)hashtable.get("contactZip");
		String primaryPhone = (String)hashtable.get("primaryPhone");
		String workPhone = (String)hashtable.get("workPhone");

		for(Contacts c: repoContacts) {
			if((c.getFirstName() + " " + c.getLastName()).equals(firstName + " " + lastName)) {
				session.setAttribute("message", "There is already a contact with the same first name and last name.");
				return null;
			}
		}

		if (!(firstName.length() <= 32 && firstName.length() > 0) || !(firstName.matches("^[a-zA-Z ']+$"))) { 
			session.setAttribute("message", "First name field must be between 1 and 32 characters and alphabetic.");
			return null;
		}

		if(!(lastName.length() <= 32 && lastName.length() > 0) || !(lastName.matches("^[a-zA-Z ']+$"))) {
			session.setAttribute("message", "Last name field must be between 1 and 32 characters and alphbetic.");
			return null;
		}
		
		if (!(middleInitial == null || middleInitial.equals(""))) {
			if(!(middleInitial.length() <= 16 && middleInitial.length() > 0) || !(middleInitial.matches("^[A-Za-z]{1}$"))) {
				session.setAttribute("message", "Middle initial must be 1 character and alphabetic.");
				return null;
			}
		}

		if(!(emailAddress.length() <= 64 && emailAddress.length() > 0) || !(emailAddress.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))){
			session.setAttribute("message", "Email address must be between 1 and 64 characters that are alpahnumeric.");
			return null;
		}

		if(!(streetAddress1.length() <= 64 && streetAddress1.length() > 0)) { 
			session.setAttribute("message", "Street address 1 must be between 1 and 128 characters.");
			return null;
		}

		if (!(streetAddress2 == null || streetAddress2.equals(""))) {
			if(!(streetAddress2.length() <= 64 && streetAddress2.length() > 0)) { 
				session.setAttribute("message", "Street address 2 must be between 1 and 64 characters.");
				return null;
			}
		}
		if(!(contactCity.length() <= 64 && contactCity.length() > 0) || !(contactCity.matches("^[A-Za-z]+(?:[\\s-][A-Za-z]+)*$"))) {
			session.setAttribute("message", "City must be between 1 and 64 characters and is alphabetic.");
			return null;
		}

		if(!(states.contains(contactState) || stateAbbreviations.contains(contactState))) {  
			session.setAttribute("message", "State must be a state or state abbreviation.");
			return null;
		}

		if(!(contactZip.length() <= 12 && contactZip.length() > 0) || !(contactZip.matches("^[0-9.]+$"))){ 
			session.setAttribute("message", "Zip must be between 1 and 12 numeric characters.");
			return null;
		}

		if(!(primaryPhone.length() <= 13 && primaryPhone.length() > 0) || !(primaryPhone.matches("\\d{3}-\\d{3}-\\d{4}"))){ 
			session.setAttribute("message", "Primary phone must be in format ###-###-####.");
			return null;
		}

		if (!(workPhone == null || workPhone.equals(""))) {
			if(!(workPhone.length() <= 13 && workPhone.length() > 0) || !(workPhone.matches("\\d{3}-\\d{3}-\\d{4}"))){ 
				session.setAttribute("message", "Work phone must be must be in format ###-###-####.");
				return null;
			}
		}

		Contacts contact = new Contacts();


		contact.setFirstName(firstName);
		contact.setLastName(lastName);
		contact.setMiddleInitial(middleInitial);
		contact.setEmailAddress(emailAddress);
		contact.setStreetAddress1(streetAddress1);
		contact.setStreetAddress2(streetAddress2);
		contact.setCity(contactCity);
		contact.setState(contactState);
		contact.setZip(contactZip);
		contact.setPrimaryPhone(primaryPhone);
		contact.setWorkPhone(workPhone);
		contact.setCarrier(user.getCarrier());

		return contact;
	}


	/**
	 * Validates the technicians excel sheet, returns a list of valid technicians
	 * @param worksheet holds the technicians worksheet
	 * @param session used to load attributes into the current users HTTP session
	 * @return result
	 */
	
	public List<Technicians> validateTechniciansSheet(XSSFSheet worksheet, HttpSession session){
		List <Technicians> result = new ArrayList<>();
		User user = userService.getLoggedInUser();
		{
			try {	

				for(int i=1; i<worksheet.getPhysicalNumberOfRows(); i++) {
					List<String> allTechnicians = new ArrayList<>();
					Technicians technician = new Technicians();
					XSSFRow row = worksheet.getRow(i);

					if ((row.getCell(0)== null || row.getCell(0).toString().equals("")) || (row.getCell(1)== null || row.getCell(1).toString().equals(""))) {
						break;
					}

					String skillGrade = row.getCell(0).toString().strip();
					String contactFullName = row.getCell(1).toString().strip();

					Hashtable<String, String> hashtable = new Hashtable<>();

					hashtable.put("skillGrade", skillGrade);
					hashtable.put("contactFullName", contactFullName );

					technician = validateTechnicians(hashtable, session);
					if (technician == null) {
						return null;
					}

					for(Technicians t: result) {
						allTechnicians.add(t.getContact().getFirstName() + " " + t.getContact().getLastName());	
					}

					if(allTechnicians.contains(contactFullName)) {
						Logger.error("{} || attempted to upload multiple Technicians with the same Contact name.",user.getUsername());
						session.setAttribute("message", "Can not upload multiple Technicians with the same Contact name.");
						return null;
					}

					result.add(technician);
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				session.setAttribute("message", "Excel File was incorrectly formatted.");
				return null;
			}
		}
		return result;
	}


	/**
	 * Validates all of the technician object attributes
	 * Returns valid technicians
	 * @param hashtable holds the attributes of the technician
	 * @param session used to load attributes into the current users HTTP session
	 * @return technician
	 */
	
	public Technicians validateTechnicians(Hashtable<String, String> hashtable, HttpSession session) {
		User user = userService.getLoggedInUser();
		List <Contacts> repoContacts = user.getCarrier().getContacts();
		List <Technicians> repoTechnicians = user.getCarrier().getTechnicians();

		Long contactId = null;

		String skillGrade = (String) hashtable.get("skillGrade");
		String contactFullName = (String) hashtable.get("contactFullName");

		for(Contacts c : repoContacts) {
			if (contactFullName.equals(c.getFirstName() + " " + c.getLastName())) {
				contactId = c.getId();
			}
		}

		for(Technicians t : repoTechnicians) {
			if ((t.getContact().getFirstName() + " " + t.getContact().getLastName()).equals(contactFullName)) {
				Logger.error("{} || attempted to upload a Technician but the Contact already exists.",user.getUsername());
				session.setAttribute("message", "Technician already exists with this Contact name.");
				return null;
			}
		}

		if (!(skillGrade.length() <= 12 && skillGrade.length() > 0) || !(skillGrade.matches("^[a-zA-Z0-9.]+$"))) {
			Logger.error("{} || attempted to upload a Technician but the Skill Grade was not between 1 and 12 alphanumeric characters long.",user.getUsername());
			session.setAttribute("message", "Skill Grade was not between 1 and 12 alphanumeric characters long.");
			return null;	
		}

		if (contactId == null) {
			Logger.error("{} || attempted to upload a Technician but the Contact did not exist.",user.getUsername());
			session.setAttribute("message", "Contact did not exist. (Contacts must be uploaded first, did you upload them yet?)");
			return null;	
		}

		Technicians technician = new Technicians();

		try {

			technician.setCarrier(user.getCarrier());
			technician.setSkill_grade(skillGrade);

			technician.setContact(contactsRepository.findById(contactId).orElseThrow(() -> new IllegalArgumentException("Invalid Contact Id")));

		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}

		return technician;
	}
	
	/**
	 * Validates the vehicles excel sheet, returns a list of valid vehicles
	 * @param worksheet holds the vehicles worksheet
	 * @param session used to load attributes into the current users HTTP session
	 * @return result
	 */
	
	public List<Vehicles> validateVehiclesSheet(XSSFSheet worksheet, HttpSession session){
		User user = userService.getLoggedInUser();
		List <Vehicles> result = new ArrayList<>();
		try {
			for(int i=1; i<worksheet.getPhysicalNumberOfRows(); i++) {
				List <String> allVehicles = new ArrayList<>();
				Vehicles vehicle = new Vehicles();
				XSSFRow row = worksheet.getRow(i);

				if ((row.getCell(0)== null || row.getCell(0).toString().equals("")) || (row.getCell(1)== null || row.getCell(1).toString().equals("")) || (row.getCell(2)== null || row.getCell(2).toString().equals("")) 
						|| (row.getCell(3)== null || row.getCell(3).toString().equals("")) || (row.getCell(4)== null || row.getCell(4).toString().equals(""))) {
					break;
				}

				String plate = row.getCell(0).toString().strip();
				String vin = row.getCell(1).toString().strip();
				String manufacturedYear = row.getCell(2).toString().strip();
				String vehicleTypeMakeModel = row.getCell(3).toString().strip();
				String locationName = row.getCell(4).toString().strip();

				manufacturedYear = manufacturedYear.substring(0, manufacturedYear.length() - 2);
				
				Hashtable<String, String> hashtable = new Hashtable<>();

				hashtable.put("plate", plate);
				hashtable.put("vin", vin);
				hashtable.put("manufacturedYear", manufacturedYear);
				hashtable.put("vehicleTypeMakeModel", vehicleTypeMakeModel);
				hashtable.put("locationName", locationName);


				vehicle = validateVehicles(hashtable, session);
				if (vehicle == null) {
					return null;								
				}

				for(Vehicles v: result) {
					allVehicles.add(v.getPlateNumber() + " " + v.getVinNumber());
				}

				if(allVehicles.contains(plate + " " + vin)) {
					session.setAttribute("message", "There was mulitple vehicles with the same plate and vin within the sheet.");
					return null;
				}

				result.add(vehicle);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			session.setAttribute("message", "Excel File was incorrectly formatted.");
			return null;
		}
		return result;
	}

	/**
	 * Validates all of the vehicle object attributes
	 * Returns valid vehicle 
	 * @param hashtable holds the attributes of the vehicle 
	 * @param session used to load attributes into the current users HTTP session
	 * @return vehicle
	 */
	
	public Vehicles validateVehicles(Hashtable<String, String> hashtable, HttpSession session) {
		User user = userService.getLoggedInUser();
		List <VehicleTypes> repoVehicleTypes =  user.getCarrier().getVehicleTypes();
		List <Locations> repoLocations = user.getCarrier().getLocations();
		List <Vehicles> repoVehicles = user.getCarrier().getVehicles();
		Long vehicleTypeId = null;
		Long locationId = null;

		String plate = (String) hashtable.get("plate");
		String vin = (String) hashtable.get("vin");
		String manufacturedYear = (String) hashtable.get("manufacturedYear");
		String vehicleTypeMakeModel = (String) hashtable.get("vehicleTypeMakeModel");
		String locationName = (String) hashtable.get("locationName");

		for(VehicleTypes vt: repoVehicleTypes) {
			if (vehicleTypeMakeModel.equals(vt.getMake() + " " + vt.getModel())){
				vehicleTypeId = vt.getId();
			}
		}

		for(Locations l: repoLocations) {
			if (l.getName().equals(locationName)) {
				locationId = l.getId();
			}
		}

		for(Vehicles v: repoVehicles) {
			if ((v.getPlateNumber()+ " " + v.getVinNumber()).equals(plate + " " + vin)) {
				session.setAttribute("message", "Vehicle already exists.");
				return null;
			}
		}

		if(vehicleTypeId == null) {
			session.setAttribute("message", "Vehicle Type did not exist.");
			session.setAttribute("excelMessage", "Vehicle Types must be uploaded first, did you upload them yet?");
			return null;	
		}

		if(locationId == null) {
			session.setAttribute("message", "Location did not exist.");
			session.setAttribute("excelMessage", "Locations must be uploaded first, did you upload them yet?");
			return null;	
		}

		if (!(plate.length() <= 12 && plate.length() > 0) || !(plate.matches("^[a-zA-Z0-9. -]+$"))) {
			session.setAttribute("message", "Plate was not between 1 and 12 alphanumeric characters.");
			return null;	
		}

		if (!(vin.length() <= 17 && vin.length() > 0) || !(vin.matches("^[a-zA-Z0-9.]+$"))) {
			session.setAttribute("message", "Vin was not between 1 and 17 alphanumeric characters.");
			return null;	
		}

		if (!(manufacturedYear.length() <= 4 && manufacturedYear.length() > 0) || !(manufacturedYear.matches("^[0-9]+$"))) {
			session.setAttribute("message", "Year was not between 1950 and 2023.");
			return null;	
		}
	
		if (manufacturedYear.matches("^[0-9]+$")) {
			int year = Integer.parseInt(manufacturedYear);
			if (!(year >= 1950 && year <= 2023)) {
				session.setAttribute("message", "Year was not between 1950 and 2023.");
				return null;
	        } 
		}

		Vehicles vehicle = new Vehicles();

		try {

			vehicle.setPlateNumber(plate);
			vehicle.setVinNumber(vin);
			vehicle.setManufacturedYear(manufacturedYear);
			vehicle.setCarrier(user.getCarrier());

			vehicle.setVehicleType(vehicleTypesRepository.findById(vehicleTypeId).orElseThrow(() -> new IllegalArgumentException("Invalid Vehicle Type Id")));
			vehicle.setLocation(locationsRepository.findById(locationId).orElseThrow(() -> new IllegalArgumentException("Invalid Location Id")));

		}

		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return vehicle;
	}

	/**
	 * Validates the driver excel sheet, returns a list of valid driver
	 * @param worksheet holds the driver worksheet
	 * @param session used to load attributes into the current users HTTP session
	 * @return result
	 */
	
	public List<Driver> validateDriverSheet(XSSFSheet worksheet, HttpSession session){
		List <Driver> result = new ArrayList<>();
		User user = userService.getLoggedInUser();
		{
			try {	

				for(int i=1; i<worksheet.getPhysicalNumberOfRows(); i++) {
					List<String> allDrivers = new ArrayList<>();
					Driver driver = new Driver();
					XSSFRow row = worksheet.getRow(i);

					if ((row.getCell(0)== null || row.getCell(0).toString().equals("")) || (row.getCell(1)== null || row.getCell(1).toString().equals("")) || (row.getCell(2)== null || row.getCell(2).toString().equals("")) 
							|| (row.getCell(3)== null || row.getCell(3).toString().equals("")) || (row.getCell(4)== null || row.getCell(4).toString().equals(""))) {
						break;
					}

					String licenseNumber = row.getCell(0).toString().strip();
					String licenseExpiration = row.getCell(1).toString().strip();
					String licenseClass = row.getCell(2).toString().strip();
					String contactFullName = row.getCell(3).toString().strip();
					String vehiclePlateAndVin = row.getCell(4).toString().strip();

					Hashtable<String, String> hashtable = new Hashtable<>();

					hashtable.put("contactFullName", contactFullName);
					hashtable.put("vehiclePlateAndVin", vehiclePlateAndVin );
					hashtable.put("licenseNumber", licenseNumber);
					hashtable.put("licenseExpiration", licenseExpiration);
					hashtable.put("licenseClass", licenseClass);

					driver = validateDriver(hashtable, session);
					if (driver == null) {
						return null;
					}

					for(Driver d: result) {
						allDrivers.add(d.getContact().getFirstName() + " " + d.getContact().getLastName());	
					}

					if(allDrivers.contains(contactFullName)) {
						session.setAttribute("message", "There was mulitple Drivers with the same Contact name within the sheet.");
						return null;
					}

					result.add(driver);	
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				session.setAttribute("message", "Excel File was incorrectly formatted.");
				return null;
			}
		}
		return result;
	}

	/**
	 * Validates all of the driver object attributes
	 * Returns valid driver
	 * @param hashtable holds the attributes of the driver
	 * @param session used to load attributes into the current users HTTP session
	 * @return driver
	 */
	
	public Driver validateDriver(Hashtable<String, String> hashtable, HttpSession session) {
		User user = userService.getLoggedInUser();
		List <Contacts> repoContacts = user.getCarrier().getContacts();
		List <Driver> repoDrivers =  user.getCarrier().getDrivers();
		List <Vehicles> repoVehicles = user.getCarrier().getVehicles();


		Long contactId = null;
		Long vehicleId = null;

		String licenseNumber = (String)hashtable.get("licenseNumber");
		String licenseExpiration = (String)hashtable.get("licenseExpiration");
		String licenseClass = (String)hashtable.get("licenseClass");
		String contactFullName = (String)hashtable.get("contactFullName");
		String vehiclePlateAndVin = (String)hashtable.get("vehiclePlateAndVin");

		for(Vehicles v: repoVehicles) {
			if (vehiclePlateAndVin.equals(v.getPlateNumber() + " " + v.getVinNumber())){
				vehicleId = v.getId();
			}
		}

		for(Contacts c: repoContacts) {
			if (contactFullName.equals(c.getFirstName() + " " + c.getLastName())) {
				contactId = c.getId();
			}
		}

		for(Driver d : repoDrivers) {
			if ((d.getContact().getFirstName() + " " + d.getContact().getLastName()).equals(contactFullName)) {
				session.setAttribute("message", "Driver with this Contact already exists.");
				return null;
			}
		}

		if(vehicleId == null) {
			session.setAttribute("message", "Vehicle did not exist.");
			session.setAttribute("excelMessage","Vehicles must be uploaded first, did you upload them yet?");
			return null;
		}

		if(contactId == null) {;
			session.setAttribute("message", "Contact did not exist.");
			session.setAttribute("excelMessage","Contacts must be uploaded first, did you upload them yet?");
			return null;
		}

		if(!(licenseNumber.length() <= 32 && licenseNumber.length() > 0)){
			session.setAttribute("message", "License number must be between 1 and 32 characters.");
			return null;
		}

		if(!(licenseExpiration.length() <= 12 && licenseExpiration.length() > 0) || !(licenseExpiration.matches("^\\d{2}-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4}$"))){ 
			session.setAttribute("message", "Date must be between 1 and 12 characters and formated MM/DD/YYYY.");
			return null;
		}

		if(!(licenseClass.length() <= 12 && licenseClass.length() > 0) || !(licenseClass.matches("^[a-zA-Z-/]+$"))) { 
			session.setAttribute("message", "License class must be between 1 and 12 alphabetic characters.");
			return null;
		}


		Driver driver = new Driver();


		driver.setLisence_number(licenseNumber);
		driver.setLisence_expiration(licenseExpiration);
		driver.setLisence_class(licenseClass);
		driver.setCarrier(user.getCarrier());

		driver.setVehicle(vehiclesRepository.findById(vehicleId).orElseThrow(() -> new IllegalArgumentException("Invalid Vehicle Id")));
		driver.setContact(contactsRepository.findById(contactId).orElseThrow(() -> new IllegalArgumentException("Invalid Contact Id")));

		return driver;
	}

	/**
	 * Validates the maintenance orders excel sheet, returns a list of valid maintenance orders
	 * @param worksheet holds the maintenance orders worksheet
	 * @param session used to load attributes into the current users HTTP session
	 * @return result
	 */
	
	public List<MaintenanceOrders> validateMaintenanceOrdersSheet(XSSFSheet worksheet, HttpSession session){
		List <MaintenanceOrders> result = new ArrayList<>();
		try {

			for(int i=1; i<worksheet.getPhysicalNumberOfRows(); i++) {

				MaintenanceOrders maintenanceOrder = new MaintenanceOrders();
				XSSFRow row = worksheet.getRow(i);


				if ((row.getCell(2)== null || row.getCell(2).toString().equals("")) || (row.getCell(4)== null || row.getCell(4).toString().equals("")) || (row.getCell(5)== null || row.getCell(5).toString().equals("")) 
						|| (row.getCell(6)== null || row.getCell(6).toString().equals("")) || (row.getCell(7)== null || row.getCell(7).toString().equals(""))) {
					break;
				}

				String date = row.getCell(0).toString();
				String details = row.getCell(1).toString().strip();
				String serviceType = row.getCell(2).toString().strip();
				String cost = row.getCell(3).toString().strip();
				String status = row.getCell(4).toString().strip();
				String type = row.getCell(5).toString().strip();
				String vehiclePlateAndVin = row.getCell(6).toString().strip();
				String techniciansContactFullName = row.getCell(7).toString().strip();

				cost = cost.substring(0, cost.length() - 2);


				Hashtable<String, String> hashtable = new Hashtable<>();

				hashtable.put("date", date);
				hashtable.put("details", details);
				hashtable.put("serviceType", serviceType);
				hashtable.put("cost", cost);
				hashtable.put("status", status);
				hashtable.put("type", type);
				hashtable.put("vehiclePlateAndVin", vehiclePlateAndVin);
				hashtable.put("techniciansContactFullName", techniciansContactFullName);



				maintenanceOrder = validateMaintenanceOrder(hashtable, session);
				if (maintenanceOrder == null) {
					return null;								
				}

				result.add(maintenanceOrder);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			session.setAttribute("message", "Excel File was incorrectly formatted.");
			return null;
		}
		return result;
	}
	
	/**
	 * Validates all of the maintenance order object attributes
	 * Returns valid maintenance order
	 * @param hashtable holds the attributes of the maintenance order
	 * @param session used to load attributes into the current users HTTP session
	 * @return maintenaceOrder
	 */
	
	public MaintenanceOrders validateMaintenanceOrder(Hashtable<String, String> hashtable, HttpSession session) {
		User user = userService.getLoggedInUser();
		List <Technicians> repoTechnicians = user.getCarrier().getTechnicians();
		List <Vehicles> repoVehicles = user.getCarrier().getVehicles();

		Long vehicleId = null;
		Long technicianId = null;


		String date = hashtable.get("date");
		String details = (String)hashtable.get("details");
		String serviceType = (String)hashtable.get("serviceType");
		String cost = (String)hashtable.get("cost");
		String status = (String)hashtable.get("status");
		String type = hashtable.get("type");
		String vehiclePlateAndVin = hashtable.get("vehiclePlateAndVin");
		String techniciansContactFullName = hashtable.get("techniciansContactFullName");


		for(Vehicles v: repoVehicles) {
			if (vehiclePlateAndVin.equals(v.getPlateNumber() + " " + v.getVinNumber())){
				vehicleId = v.getId();
			}
		}

		for(Technicians t: repoTechnicians) {
			if (techniciansContactFullName.equals(t.getContact().getFirstName() + " " + t.getContact().getLastName())){
				technicianId = t.getId();
			}
		}

		if(vehicleId == null) {
			session.setAttribute("message", "Vehicle did not exist.");
			session.setAttribute("excelMessage","Vehicles must be uploaded first, did you upload them yet?");
			return null;
		}

		if(technicianId == null) {
			session.setAttribute("message", "Technician did not exist.");
			session.setAttribute("excelMessage","Technicians must be uploaded first, did you upload them yet?");
			return null;
		}

		if (!(date == null || date.equals(""))) {
			if(!(date.length() <= 12 && date.length() > 0 && date.matches("^\\d{2}-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4}$"))) { 
				session.setAttribute("message", "Date must be between 1 and 12 characters and formated DD-MMM-YYYY.");
				return null;
			}
		}

		if (!(details == null || details.equals(""))) {
			if(!(details.length() <= 128 && details.length() > 0)) {
				session.setAttribute("message", "Details must be between 1 and 128 characters.");
				return null;
			}
		}

		if(!(serviceType.length() <= 12 && serviceType.length() > 0) || !(serviceType.matches("^[a-zA-Z0-9.]+$"))) {
			session.setAttribute("message", "Service Type must be between 1 and 12 alphanumeric characters.");
			return null;
		}

		if (!(cost == null || cost.equals(""))) {
			if(!(cost.length() <= 16 && cost.length() > 0) || !(cost.matches("^[0-9.]+$"))) {
				session.setAttribute("message", "Cost must be between 1 and 16 numeric characters.");
				return null;
			}
		}

		if(!(status.length() <= 64 && status.length() > 0)) {
			session.setAttribute("message", "Status must be between 1 and 64 characters.");
			return null;
		}

		if(!(type.length() <= 64 && type.length() > 0) || !(type.matches("^[a-zA-Z0-9. ]+$"))) {
			session.setAttribute("message", "Type must be between 1 and 64 characters.");
			return null;
		}


		MaintenanceOrders maintenanceOrder = new MaintenanceOrders();

		maintenanceOrder.setCost(cost);
		maintenanceOrder.setScheduled_date(date);
		maintenanceOrder.setStatus_key(status);
		maintenanceOrder.setDetails(details);
		maintenanceOrder.setService_type_key(serviceType);
		maintenanceOrder.setMaintenance_type(type);
		maintenanceOrder.setCarrier(user.getCarrier());


		maintenanceOrder.setVehicle(vehiclesRepository.findById(vehicleId).orElseThrow(() -> new IllegalArgumentException("Invalid Vehicle Id")));
		maintenanceOrder.setTechnician(techniciansRepository.findById(technicianId).orElseThrow(() -> new IllegalArgumentException("Invalid Technician Id")));

		return maintenanceOrder;
	}


	/**
	 * Validates a contact form for the edit page
	 * Once valid returns contact
	 * @param hashtable holds the attributes of the contact
	 * @param session used to load attributes into the current users HTTP session
	 * @return contact
	 */
	public Contacts validateContactForm(Hashtable<String, String> hashtable, HttpSession session) {

		List<String> states = Arrays.asList("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming");
		List<String> stateAbbreviations = Arrays.asList("DC", "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY");

		User user = userService.getLoggedInUser();

		String firstName = (String) hashtable.get("firstName");
		String lastName = (String)hashtable.get("lastName");
		String middleInitial = (String)hashtable.get("middleInitial");
		String emailAddress = (String)hashtable.get("emailAddress");
		String streetAddress1 = (String)hashtable.get("streetAddress1");
		String streetAddress2 = (String)hashtable.get("streetAddress2");
		String contactCity = (String)hashtable.get("contactCity");
		String contactState = (String)hashtable.get("contactState");
		String contactZip = (String)hashtable.get("contactZip");
		String primaryPhone = (String)hashtable.get("primaryPhone");
		String workPhone = (String)hashtable.get("workPhone");


		if (!(firstName.length() <= 32 && firstName.length() > 0) || !(firstName.matches("^[a-zA-Z ']+$"))) { 
			Logger.error("{} || attempted to edit a contact but Contact first name field must be between 1 and 32 characters and alphabetic.",user.getUsername());
			session.setAttribute("message", "First name field must be between 1 and 32 characters and alphabetic.");
			return null;
		}

		if(!(lastName.length() <= 32 && lastName.length() > 0) || !(lastName.matches("^[a-zA-Z ']+$"))) {
			Logger.error("{} || attempted to edit a contact but Contact last name field must be between 1 and 32 characters and alphbetic.",user.getUsername());
			session.setAttribute("message", "Last name field must be between 1 and 32 characters and alphbetic.");
			return null;
		}
		if (!(middleInitial == null || middleInitial.equals(""))) {
			if(!(middleInitial.length() <= 16 && middleInitial.length() > 0) || !(middleInitial.matches("^[A-Za-z]{1}$"))) {
				Logger.error("{} || attempted to edit a contact but Contact Middle initial must be 1 character and alphabetic.",user.getUsername());
				session.setAttribute("message", "Middle initial must be 1 character and alphabetic.");
				return null;
			}
		}

		if(!(emailAddress.length() <= 64 && emailAddress.length() > 0) || !(emailAddress.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))){
			Logger.error("{} || attempted to edit a contact but Contact email address must be between 1 and 64 characters that are alpahnumeric.",user.getUsername());
			session.setAttribute("message", "Email address must be between 1 and 64 characters that are alpahnumeric.");
			return null;
		}

		if(!(streetAddress1.length() <= 64 && streetAddress1.length() > 0)) { 
			Logger.error("{} || attempted to edit a contact but Contact street address 1 must be between 1 and 128 characters.",user.getUsername());
			session.setAttribute("message", "Street address 1 must be between 1 and 128 characters.");
			return null;
		}

		if (!(streetAddress2 == null || streetAddress2.equals(""))) {
			if(!(streetAddress2.length() <= 64 && streetAddress2.length() > 0)) { 
				Logger.error("{} || attempted to edit a contact but Contact street address 2 must be between 1 and 64 characters.",user.getUsername());
				session.setAttribute("message", "Street address 2 must be between 1 and 64 characters.");
				return null;
			}
		}
		if(!(contactCity.length() <= 64 && contactCity.length() > 0) || !(contactCity.matches("^[A-Za-z]+(?:[\\s-][A-Za-z]+)*$"))) {
			Logger.error("{} || attempted to edit a contact but Contact City must be between 1 and 64 characters and is alphabetic.",user.getUsername());
			session.setAttribute("message", "City must be between 1 and 64 characters and is alphabetic.");
			return null;
		}

		if(!(states.contains(contactState) || stateAbbreviations.contains(contactState))) {  
			Logger.error("{} || attempted to edit a contact but Contact state must be a state or state abbreviation.",user.getUsername());
			session.setAttribute("message", "State must be selected.");
			return null;
		}

		if(!(contactZip.length() == 5) || !(contactZip.matches("^[0-9.]+$"))){ 
			Logger.error("{} || attempted to edit a contact but Contact Zip must 5 numeric characters",user.getUsername());
			session.setAttribute("message", "Zip must be 5 numeric characters.");
			return null;
		}

		if(!(primaryPhone.length() <= 13 && primaryPhone.length() > 0) || !(primaryPhone.matches("\\d{3}-\\d{3}-\\d{4}"))){ 
			Logger.error("{} || attempted to edit a contact but Contact primary phone must be in format ###-###-####.",user.getUsername());
			session.setAttribute("message", "Primary phone must be in format ###-###-####.");
			return null;
		}

		if (!(workPhone == null || workPhone.equals(""))) {
			if(!(workPhone.length() <= 13 && workPhone.length() > 0) || !(workPhone.matches("\\d{3}-\\d{3}-\\d{4}"))){ 
				Logger.error("{} || attempted to edit a contact but Contact work phone must be must be in format ###-###-####.",user.getUsername());
				session.setAttribute("message", "Work phone must be must be in format ###-###-####.");
				return null;
			}
		}

		Contacts contact = new Contacts();


		contact.setFirstName(firstName);
		contact.setLastName(lastName);
		contact.setMiddleInitial(middleInitial);
		contact.setEmailAddress(emailAddress);
		contact.setStreetAddress1(streetAddress1);
		contact.setStreetAddress2(streetAddress2);
		contact.setCity(contactCity);
		contact.setState(contactState);
		contact.setZip(contactZip);
		contact.setPrimaryPhone(primaryPhone);
		contact.setWorkPhone(workPhone);
		contact.setCarrier(user.getCarrier());

		return contact;
	}

	/**
	 * Validates a vehicle types form for the edit page
	 * Once valid returns vehicle type
	 * @param hashtable holds the attributes of the vehicle type
	 * @param session used to load attributes into the current users HTTP session
	 * @return vehicle type
	 */
	
	public VehicleTypes validateVehicleTypesForm(Hashtable<String, String> hashtable, HttpSession session) {

		User user = userService.getLoggedInUser();			

		String type = (String) hashtable.get("type");
		String subType = (String) hashtable.get("subType");
		String description = (String) hashtable.get("description");
		String make = (String) hashtable.get("make");
		String model = (String) hashtable.get("model");
		String minimumWeight = (String) hashtable.get("minimumWeight");
		String maximumWeight = (String) hashtable.get("maximumWeight");
		String capacity = (String) hashtable.get("capacity");
		String maximumRange = (String) hashtable.get("maximumRange");
		String restrictions = (String) hashtable.get("restrictions");
		String height = (String) hashtable.get("height");
		String emptyWeight = (String) hashtable.get("emptyWeight");
		String length = (String) hashtable.get("length");
		String minimumCubicWeight = (String) hashtable.get("minimumCubicWeight");
		String maximumCubicWeight = (String) hashtable.get("maximumCubicWeight");


		if (!(type.length() <= 32 && type.length() > 0) || !(type.matches("^[a-zA-Z ]+$"))) {
			Logger.error("{} || attempted to edit a Vehicle Type but the Type was not between 1 and 32 alphabetic characters long.",user.getUsername());
			session.setAttribute("message", "Type was not between 1 and 32 alphabetic characters long.");
			return null;	
		}

		if (!(subType.length() <= 32 && subType.length() > 0) || !(subType.matches("^[a-zA-Z ]+$"))) {
			Logger.error("{} || attempted to edit a Vehicle Type but the Sub Type was not between 1 and 32 characters long.",user.getUsername());
			session.setAttribute("message", "Sub Type was not between 1 and 32 characters long.");
			return null;	
		}

		if (!(description == null || description.equals(""))) {
			if (!(description.length() <= 64 && description.length() > 0)) {
				Logger.error("{} || attempted to edit a Vehicle Type but the Description was not between 1 and 64 characters long.",user.getUsername());
				session.setAttribute("message", "Description was not between 1 and 64 characters long.");
				return null;	
			}
		}

		if(!(make.length() <= 32 && make.length() > 0)) {
			Logger.error("{} || attempted to edit a Vehicle Type but the Make was not between 1 and 32 characters long.",user.getUsername());
			session.setAttribute("message", "Make was not between 1 and 32 characters long.");
			return null;
		}

		if(!(model.length() <= 32 && model.length() > 0)) {
			Logger.error("{} || attempted to edit a Vehicle Type but the Model was not between 1 and 32 characters long.",user.getUsername());
			session.setAttribute("message", "Model was not between 1 and 32 characters long.");
			return null;
		}

		if (!(minimumWeight.length() <= 16 && minimumWeight.length() > 0) || !(minimumWeight.matches("^[0-9.]+$"))) {
			Logger.error("{} || attempted to edit a Vehicle Type but the Minimum Weight was not between 1 and 16 numeric characters long.",user.getUsername());
			session.setAttribute("message", "Minimum Weight was not between 1 and 16 numeric characters long.");
			return null;
		}

		if (!(maximumWeight.length() <= 16 && maximumWeight.length() > 0) || !(maximumWeight.matches("^[0-9.]+$"))) { 
			Logger.error("{} || attempted to edit a Vehicle Type but the Maximum Weight was not between 1 and 16 numeric characters long.",user.getUsername());
			session.setAttribute("message", "Maximum Weight was not between 1 and 16 numeric characters long.");
			return null;
		}

		if (!(capacity == null || capacity.equals(""))) {
			if(!(capacity.length() <= 16 && capacity.length() > 0) || !(capacity.matches("^[0-9-.]+$"))) {
				Logger.error("{} || attempted to edit a Vehicle Type but the Capacity was not between 1 and 16 numeric characters long.",user.getUsername());
				session.setAttribute("message", "Capacity was not between 1 and 16 numeric characters long.");
				return null;
			}
		}

		if(!(maximumRange.length() <= 16 && maximumRange.length() > 0) || !(maximumRange.matches("^[0-9.]+$"))) {
			Logger.error("{} || attempted to edit a Vehicle Type but the Maximum Range was not between 1 and 16 numeric characters long.",user.getUsername());
			session.setAttribute("message", " Maximum Range was not between 1 and 16 numeric characters long.");
			return null;
		}

		if (!(restrictions == null || restrictions.equals(""))) {
			if(!(restrictions.length() <= 128 && restrictions.length() > 0)) {
				Logger.error("{} || attempted to edit a Vehicle Type but the Restrictions was not between 1 and 128 characters long.",user.getUsername());
				session.setAttribute("message", "Restrictions was not between 1 and 128 characters long.");
				return null;
			}
		}

		if(!(height.length() <= 16 && height.length() > 0) || !(height.matches("^[0-9.]+$"))) {
			Logger.error("{} || attempted to edit a Vehicle Type but the Height was not between 1 and 16 numeric characters long.",user.getUsername());
			session.setAttribute("message", "Height was not between 1 and 16 numeric characters long.");
			return null;
		}

		if(!(emptyWeight.length() <= 16 && emptyWeight.length() > 0) || !(emptyWeight.matches("^[0-9.]+$"))) {
			Logger.error("{} || attempted to edit a Vehicle Type but the Empty Weight was not between 1 and 16 numeric characters long.",user.getUsername());
			session.setAttribute("message", "Empty Weight was not between 1 and 16 numeric characters long.");
			return null;
		}

		if(!(length.length() <= 16 && length.length() > 0) || !( length.matches("^[0-9.]+$"))) {
			Logger.error("{} || attempted to edit a Vehicle Type but the Length was not between 1 and 16 numeric characters long.",user.getUsername());
			session.setAttribute("message", "Length was not between 1 and 16 numeric characters long.");
			return null;
		}

		if(!(minimumCubicWeight.length() <= 16 && minimumCubicWeight.length() > 0) || !(minimumCubicWeight.matches("^[0-9.]+$"))){
			Logger.error("{} || attempted to edit a Vehicle Type but the Minimum Cubic Weight was not between 1 and 16 numeric characters long.",user.getUsername());
			session.setAttribute("message", "Minimum Cubic Weight was not between 1 and 16 numeric characters long.");
			return null;
		}

		if(!(maximumCubicWeight.length() <= 16 && maximumCubicWeight.length() > 0) || !(maximumCubicWeight.matches("^[0-9.]+$"))){
			Logger.error("{} || attempted to edit a Vehicle Type but the Maximum Cubic Weight was not between 1 and 16 numeric characters long.",user.getUsername());
			session.setAttribute("message", "Maximum Cubic Weight was not between 1 and 16 numeric characters long.");
			return null;
		}


		VehicleTypes vehicleType = new VehicleTypes();

		try {
			vehicleType.setType(type);
			vehicleType.setSubType(subType);
			vehicleType.setDescription(description);
			vehicleType.setMake(make);
			vehicleType.setModel(model);
			vehicleType.setMinimumWeight(minimumWeight);
			vehicleType.setMaximumWeight(maximumWeight);
			vehicleType.setCapacity(capacity);
			vehicleType.setMaximumRange(maximumRange);
			vehicleType.setRestrictions(restrictions);
			vehicleType.setHeight(height);
			vehicleType.setEmptyWeight(emptyWeight);
			vehicleType.setLength(length);
			vehicleType.setMinimumCubicWeight(minimumCubicWeight);
			vehicleType.setMaximumCubicWeight(maximumCubicWeight);
			vehicleType.setCarrier(user.getCarrier());
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return vehicleType;

	}

	/**
	 * Validates a location form for the edit page
	 * Once valid returns location
	 * @param hashtable holds the attributes of the location
	 * @param session used to load attributes into the current users HTTP session
	 * @return location
	 */
	
	public Locations validateLocationsForm(Hashtable<String, String> hashtable, HttpSession session) {


		List<String> states = Arrays.asList("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming");
		List<String> stateAbbreviations = Arrays.asList("DC","AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY");

		User user = userService.getLoggedInUser();

		String locationName = (String) hashtable.get("locationName");
		String streetAddress1 = (String)hashtable.get("streetAddress1");
		String streetAddress2 = (String)hashtable.get("streetAddress2");
		String locationCity = (String)hashtable.get("locationCity");
		String locationState = (String)hashtable.get("locationState");
		String locationZip = (String)hashtable.get("locationZip");
		String locationLatitude = (String)hashtable.get("locationLatitude");
		String locationLongitude = (String)hashtable.get("locationLongitude");
		String locationType = (String)hashtable.get("locationType");

		if (!(locationName.length() <= 32 && locationName.length() > 0) || !(locationName.matches("^[a-zA-Z ']+$"))) { 
			Logger.error("{} || attempted to update a location but the name field must be between 1 and 32 characters and alphabetic.",user.getUsername());
			session.setAttribute("message", "Name must be between 1 and 32 characters and alphabetic.");
			return null;
		}

		if(!(streetAddress1.length() <= 64 && streetAddress1.length() > 0) || !(streetAddress1.matches("\\d+\\s+([a-zA-Z.]+\\s?)+"))) { 
			Logger.error("{} || attempted to update a location but location address must be between 1 and 64 characters that are alphanumeric.",user.getUsername());
			session.setAttribute("message", "Address must be between 1 and 64 characters that are alphanumeric.");
			return null;
		}

		if (!(streetAddress2 == null || streetAddress2.equals(""))) {
			if(!(streetAddress2.length() <= 64 && streetAddress2.length() > 0) || !(streetAddress2.matches("^[A-Za-z0-9./-]+(?:[\\s-][A-Za-z0-9.-]+)*$"))) {
				Logger.error("{} || attempted to update a location street address 2 must be between 1 and 64 characters that are alphanumeric.",user.getUsername());
				session.setAttribute("message", "Street Address 2 must be between 1 and 64 characters that are alphanumeric.");
				return null;
			}
		}

		if(!(locationCity.length() <= 64 && locationCity.length() > 0) || !(locationCity.matches("^[A-Za-z]+(?:[\\s-][A-Za-z]+)*$"))) { 
			Logger.error("{} || attempted to update a location city but location city must be between 1 and 64 characters and is alphabetic.",user.getUsername());
			session.setAttribute("message", "City must be between 1 and 64 characters and is alphabetic.");
			return null;
		}

		if(!(states.contains(locationState) || stateAbbreviations.contains(locationState))) {  
			Logger.error("{} || attempted to update a location state but location state must be a state or state abbreviation.",user.getUsername());
			session.setAttribute("message", "State must be a state or state abbreviation.");
			return null;
		}

		if(!(locationZip.length() <= 12 && locationZip.length() > 0) || !(locationZip.matches("^[0-9.]+$"))){
			Logger.error("{} || attempted to update a location state but zip must be between 1 and 12 numeric charcaters.",user.getUsername());
			session.setAttribute("message", "Zip must be between 1 and 12 characters and is numeric.");
			return null;
		}

		if(!(locationType.length() <= 64 && locationType.length() > 0) || !(locationType.matches("^[a-zA-Z ]+$"))){ 
			Logger.error("{} || attempted to update a location type must be 1 to 32 alphabetic characters.",user.getUsername());
			session.setAttribute("message", "Type must be 1 to 32 alphabetic characters.");
			return null;
		}

		Locations location = new Locations();

		String latAndLong = apiServiceImp.fetchLatitudeAndLongitude(locationCity, locationState);

		if (!latAndLong.equals("")){
			String[] parts = latAndLong.split("\\|");
			locationLatitude = parts[0];
			locationLongitude = parts[1];
		}

		location.setName(locationName);
		location.setStreetAddress1(streetAddress1);
		location.setStreetAddress2(streetAddress2);
		location.setCity(locationCity);
		location.setState(locationState);
		location.setZip(locationZip);
		location.setLatitude(locationLatitude);
		location.setLongitude(locationLongitude);
		location.setCarrier(user.getCarrier());
		location.setLocationType(locationType);
		location.setCarrier(user.getCarrier());

		return location;
	}

	/**
	 * Validates a vehicle form for the edit page
	 * Once valid returns vehicle
	 * @param hashtable holds the attributes of the vehicle
	 * @param session used to load attributes into the current users HTTP session
	 * @return vehicle
	 */
	
	public Vehicles validateVehiclesForm(Hashtable<String, String> hashtable, HttpSession session) {
		User user = userService.getLoggedInUser();
		String plate = (String) hashtable.get("plate");
		String vin = (String) hashtable.get("vin");
		String manufacturedYear = (String) hashtable.get("manufacturedYear");



		if (!(plate.length() <= 12 && plate.length() > 0) || !(plate.matches("^[a-zA-Z0-9. -]+$"))) {
			Logger.error("{} || attempted to upload a Vehicle but the Plate was not between 1 and 12 alphanumeric characters long.",user.getUsername());
			session.setAttribute("message", "Plate was not between 1 and 12 alphanumeric characters long.");
			return null;	
		}

		if (!(vin.length() <= 17 && vin.length() > 0) || !(vin.matches("^[a-zA-Z0-9.]+$"))) {
			Logger.error("{} || attempted to upload a Vehicle but the Vin was not between 1 and 17 alphanumeric characters long.",user.getUsername());
			session.setAttribute("message", "Vin was not between 1 and 17 alphanumeric characters long.");
			return null;	
		}

		if (!(manufacturedYear.length() <= 4 && manufacturedYear.length() > 0) || !(manufacturedYear.matches("^[0-9]+$"))) {
			Logger.error("{} || attempted to upload a Vehicle but the Year was not between 1 and 4 numeric characters long.",user.getUsername());
			session.setAttribute("message", "Year was not between 1 and 4 numeric characters long.");
			return null;	
		}	

		Vehicles vehicle = new Vehicles();

		try {

			vehicle.setPlateNumber(plate);
			vehicle.setVinNumber(vin);
			vehicle.setManufacturedYear(manufacturedYear);
			vehicle.setCarrier(user.getCarrier());
		}

		catch(Exception e) {
			e.printStackTrace();
			return null;
		}

		return vehicle;
	}

	/**
	 * Validates a driver form for the edit page
	 * Once valid returns driver
	 * @param hashtable holds the attributes of the driver
	 * @param session used to load attributes into the current users HTTP session
	 * @return driver
	 */
	
	public Driver validateDriverForm(Hashtable<String, String> hashtable, HttpSession session) {
		User user = userService.getLoggedInUser();


		String licenseNumber = (String)hashtable.get("licenseNumber");
		String licenseExpiration = (String)hashtable.get("licenseExpiration");
		String licenseClass = (String)hashtable.get("licenseClass");



		if(!(licenseNumber.length() <= 32 && licenseNumber.length() > 0)){
			session.setAttribute("message", "License number must be between 1 and 32 characters.");
			return null;
		}

		if(!(licenseExpiration.length() <= 12 && licenseExpiration.length() > 0) || !(licenseExpiration.matches("^\\d{2}-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4}$"))){ 
			session.setAttribute("message", "Date must be between 1 and 12 characters and formated DD-MMM-YYYY.");
			return null;
		}

		if(!(licenseClass.length() <= 12 && licenseClass.length() > 0) || !(licenseClass.matches("^[a-zA-Z-/]+$"))) { 
			session.setAttribute("message", "License class must be between 1 and 12 alphabetic characters.");
			return null;
		}


		Driver driver = new Driver();


		driver.setLisence_number(licenseNumber);
		driver.setLisence_expiration(licenseExpiration);
		driver.setLisence_class(licenseClass);
		driver.setCarrier(user.getCarrier());


		return driver;
	}

	/**
	 * Validates a maintenance order form for the edit page
	 * Once valid returns maintenance order
	 * @param hashtable holds the attributes of the maintenance order
	 * @param session used to load attributes into the current users HTTP session
	 * @return maintenanceOrder
	 */
	
	public MaintenanceOrders validateMaintenanceOrderForm(Hashtable<String, String> hashtable, HttpSession session) {

		User user = userService.getLoggedInUser();

		String date = hashtable.get("date");
		String details = (String)hashtable.get("details");
		String serviceType = (String)hashtable.get("serviceType");
		String cost = (String)hashtable.get("cost");
		String status = (String)hashtable.get("status");
		String type = hashtable.get("type");



		if (!(date == null || date.equals(""))) {
			if(!(date.length() <= 12 && date.length() > 0 && date.matches("^\\d{2}-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4}$"))) { 
				Logger.error("{} || attempted to edit a Maintenance Order but the Date must be between 1 and 12 characters and formated DD-MMM-YYYY.",user.getUsername());
				session.setAttribute("message", "Date must be between 1 and 12 characters and formated DD-MMM-YYYY.");
				return null;
			}
		}

		if (!(details == null || details.equals(""))) {
			if(!(details.length() <= 128 && details.length() > 0)) {
				Logger.error("{} || attempted to edit a Maintenance Order but the Details must be between 1 and 128 characters.",user.getUsername());
				session.setAttribute("message", "Details must be between 1 and 128 characters.");
				return null;
			}
		}

		if(!(serviceType.length() <= 12 && serviceType.length() > 0) || !(serviceType.matches("^[a-zA-Z0-9.]+$"))) {
			Logger.error("{} || attempted to edit a Maintenance Order but the Service Type must be between 1 and 12 alphanumeric characters.",user.getUsername());
			session.setAttribute("message", "Service Type must be between 1 and 12 alphanumeric characters.");
			return null;
		}

		if (!(cost == null || cost.equals(""))) {
			if(!(cost.length() <= 16 && cost.length() > 0) || !(cost.matches("^[0-9.]+$"))) {
				Logger.error("{} || attempted to edit a Maintenance Order but the Cost must be between 1 and 16 numeric characters.",user.getUsername());
				session.setAttribute("message", "Cost must be between 1 and 16 numeric characters.");
				return null;
			}
		}

		if(!(status.length() <= 64 && status.length() > 0)) {
			Logger.error("{} || attempted to edit a Maintenance Order but the Status must be between 1 and 64 characters.",user.getUsername());
			session.setAttribute("message", "Status must be between 1 and 64 characters.");
			return null;
		}

		if(!(type.length() <= 64 && type.length() > 0) || !(type.matches("^[a-zA-Z0-9. ]+$"))) {
			Logger.error("{} || attempted to edit a Maintenance Order but the Maintenance Type must be between 1 and 64 characters.",user.getUsername());
			session.setAttribute("message", "Type must be between 1 and 64 characters.");
			return null;
		}


		MaintenanceOrders maintenanceOrder = new MaintenanceOrders();

		maintenanceOrder.setCost(cost);
		maintenanceOrder.setScheduled_date(date);
		maintenanceOrder.setStatus_key(status);
		maintenanceOrder.setDetails(details);
		maintenanceOrder.setService_type_key(serviceType);
		maintenanceOrder.setMaintenance_type(type);
		maintenanceOrder.setCarrier(user.getCarrier());


		return maintenanceOrder;
	}

	/**
	 * Validates a shipment form for the edit page
	 * Once valid returns shipment
	 * @param hashtable holds the attributes of the shipment
	 * @param session used to load attributes into the current users HTTP session
	 * @return shipment
	 */
	
	public Shipments validateShipmentForm(Hashtable<String, String> hashtable, HttpSession session) {

		List<String> states = Arrays.asList("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming");
		List<String> stateAbbreviations = Arrays.asList("DC", "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY");

		User user = userService.getLoggedInUser();

		String clientName = (String) hashtable.get("clientName");
		String clientMode = (String)hashtable.get("clientMode");
		String date = (String)hashtable.get("date");
		String commodityClass = (String)hashtable.get("commodityClass");
		String commodityPieces = (String)hashtable.get("commodityPieces");
		String commodityPaidWeight = (String)hashtable.get("commodityPaidWeight");
		String shipperCity = (String)hashtable.get("shipperCity");
		String shipperState = (String)hashtable.get("shipperState");
		String shipperZip = (String)hashtable.get("shipperZip");
		String shipperLatitude = (String)hashtable.get("shipperLatitude");
		String shipperLongitude = (String)hashtable.get("shipperLongitude");
		String consigneeCity = (String)hashtable.get("consigneeCity");
		String consigneeState = (String)hashtable.get("consigneeState");
		String consigneeZip = (String)hashtable.get("consigneeZip");
		String consigneeLatitude = (String)hashtable.get("consigneeLatitude");
		String consigneeLongitude = (String)hashtable.get("consigneeLongitude");



		if (!(clientName.length() <= 64 && clientName.length() > 0)) {
			session.setAttribute("message", "Client Name must be between 1 and 64 characters.");
			return null;
		}

		if(!(clientMode.equals("LTL") || clientMode.equals("FTL"))) {
			session.setAttribute("message", "Client Mode must be LTL or FTL.");
			return null;
		}


		if(!(date.length() <= 12 && date.length() > 0 && date.matches("^\\d{2}-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4}$"))) { 
			session.setAttribute("message", "Date must be between 1 and 12 characters and formated DD/MMM/YYYY");
			return null;
		}


		if(!(commodityClass.length() <= 12 && commodityClass.length() > 0) || !(commodityClass.matches("^[a-zA-Z0-9.]+$"))) {
			session.setAttribute("message", "Commodity Class must be between 1 and 12 characters and alphanumeric.");
			return null;
		}

		if(!(commodityPieces.length() <= 64 && commodityPieces.length() > 0) || !(commodityPieces.matches("^[0-9.]+$"))) {
			session.setAttribute("message", "Commodity Pieces must be between 1 and 64 characters long and numeric.");
			return null;
		}

		if(!(commodityPaidWeight.length() <= 16 && commodityPaidWeight.length() > 0) || !(commodityPaidWeight.matches("^[0-9.]*\\.?[0-9.]+$"))) {
			session.setAttribute("message", "Commodity Paid Weight must be between 1 and 16 characters long and numeric.");
			return null;
		}

		if(!(shipperCity.length() <= 64 && shipperCity.length() > 0) || !(shipperCity.matches("^[A-Za-z]+(?:[\\s-][A-Za-z]+)*$"))) {
			session.setAttribute("message", "Shipper City must be between 1 and 64 characters and is alphabetic.");
			return null;
		}

		if(!(states.contains(shipperState) || stateAbbreviations.contains(shipperState))) {
			session.setAttribute("message", "Shipper State must be a state or state abbreviation.");
			return null;
		}

		if(!(shipperZip.length() <= 12 && shipperZip.length() > 0) || !(shipperZip.matches("^[0-9.]+$"))){
			session.setAttribute("message", "Shipper Zip must be between 1 and 12 characters and is numeric.");
			return null;
		}

		if(!(consigneeCity.length() <= 64 && consigneeCity.length() > 0) || !( consigneeCity.matches("^[A-Za-z]+(?:[\\s-][A-Za-z]+)*$"))) {
			session.setAttribute("message", "Consignee City must be between 1 and 64 characters and is alphabetic.");
			return null;
		}

		if(!(states.contains(consigneeState) || stateAbbreviations.contains(consigneeState))) {
			session.setAttribute("message", "Consignee State must be a state or state abbreviation.");
			return null;
		}

		if(!(consigneeZip.length() <= 12 && consigneeZip.length() > 0) || !(consigneeZip.matches("^[0-9.]+$"))){
			session.setAttribute("message", "Consignee Zip must be between 1 and 12 characters and is alphabetic.");
			return null;
		}
		
		Shipments shipment = new Shipments();

		String shipperLatAndLong = apiServiceImp.fetchLatitudeAndLongitude(shipperCity, shipperState);
		String consigneeLatAndLong = apiServiceImp.fetchLatitudeAndLongitude(consigneeCity, consigneeState);

		if (!shipperLatAndLong.equals("")){
			String[] parts = shipperLatAndLong.split("\\|");
			shipperLatitude = parts[0];
			shipperLongitude = parts[1];
		}
		
		if (!consigneeLatAndLong.equals("")){
			String[] parts = consigneeLatAndLong.split("\\|");
			consigneeLatitude = parts[0];
			consigneeLongitude = parts[1];
		}
		
		shipment.setClient(clientName);
		shipment.setClientMode(clientMode);
		shipment.setShipDate(date);
		shipment.setCommodityClass(commodityClass);
		shipment.setCommodityPieces(commodityPieces);
		shipment.setCommodityPaidWeight(commodityPaidWeight);
		shipment.setShipperCity(shipperCity);
		shipment.setShipperState(shipperState);
		shipment.setShipperZip(shipperZip);
		shipment.setShipperLatitude(shipperLatitude);
		shipment.setShipperLongitude(shipperLongitude);
		shipment.setConsigneeCity(consigneeCity);
		shipment.setConsigneeState(consigneeState);
		shipment.setConsigneeZip(consigneeZip);
		shipment.setConsigneeLatitude(consigneeLatitude);
		shipment.setConsigneeLongitude(consigneeLongitude);

		return shipment;	

	}

}