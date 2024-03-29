package edu.sru.thangiah.webrouting.controller;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import edu.sru.thangiah.webrouting.domain.User;
import edu.sru.thangiah.webrouting.mailsending.Emailing;
import edu.sru.thangiah.webrouting.mailsending.MailSending;
import edu.sru.thangiah.webrouting.services.SecurityService;
import edu.sru.thangiah.webrouting.services.UserService;
import edu.sru.thangiah.webrouting.web.UserValidator;

/**
 * Controller class for the forgot password functionality
 * @author Josh Gearhart 	jjg1018@sru.edu
 */

@Controller
public class ForgottenPasswordController {

	@Autowired
	private Emailing emailImpl;

	private User user;

	private String email;

	@Autowired
	private UserService userService;

	private String webUrl;

	private String otpCode;

	private String password;

	@Autowired
	private HttpServletRequest mailRequest; 

	@Autowired
	private UserValidator userValidator;
	
	private String redirect;

	private static final Logger Logger = LoggerFactory.getLogger(ForgottenPasswordController.class);
	
	/**
	 * Adds all of the attributes to the model to render the forgot password form page
	 * @param model used to load attributes into the Thymeleaf model
	 * @return /forgotpasswordform
	 */
	
	@GetMapping("/forgotpassword")
	public String forgotPassword(Model model) {
		model.addAttribute("currentPage","/login");
		return "forgotpasswordform";
	}

	/**
	 * Handles the input from the user and sending the user a reset password email
	 * @param model used to load attributes into the Thymeleaf model
	 * @param request - used to get the email from the form
	 * @return /forgotpasswordform - this will ensure that the model displays the message
	 * @throws UnsupportedEncodingException exception
	 * @throws MessagingException exception
	 */

	@PostMapping("/forgotpassword")
	public String forgotPasswordForm(Model model,HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
		email = request.getParameter("email");
		userService.assignOtpCode(email);
		webUrl = MailSending.getUrl(mailRequest);
		emailImpl.forgotPassword(email, webUrl);
		User user = userService.getLoggedInUser();
		model.addAttribute("message", "Your reset password link has been sent to your email");
		Logger.info("{} || sent themselves a password reset link.", user.getUsername());
		return "forgotpasswordform";
	}

	/**
	 * method that finds the user by their OTP code, if the user does not have the same code
	 * they will not be able to proceed
	 * @param otpCode - passed in the URL, used for finding the user
	 * @param model used to load attributes into the Thymeleaf model
	 * @return /forgotpassword or /resetpaswordform
	 */
	@GetMapping("/resetpassword")
	public String resetPassword(@RequestParam(required=true, value="code") String otpCode, Model model) {
		user = userService.findByOtp(otpCode);
		model.addAttribute("otpCode", otpCode);
		if(user == null ) {
			model.addAttribute("message", "Invalid OTP Code");
			return "forgotpassword";
		}
		if(!model.containsAttribute("userForm")) {
			model.addAttribute("userForm", new User());
		}
		return "resetpasswordform";
	}

	/**
	 * Method that takes in the password and code and resets the users password
	 * @param userForm - Used to hold the users password
	 * @param bindingResult - Used to validate the password held from userForm
	 * @param request - used to recieve the otpCode and the password
	 * @param model - used to send messages back to the user
	 * @param redirectAttr - Used to add flash Attributes for binding result and userForm
	 * @return /resetpasswordform 
	 */
	@PostMapping("/resetpassword")
	public String resetPasswordForm(@ModelAttribute("userForm") User userForm, BindingResult bindingResult, HttpServletRequest request, Model model, RedirectAttributes redirectAttr) {
		userValidator.validatePassword(userForm, bindingResult);
		otpCode = request.getParameter("otpCode");
		password = request.getParameter("password");
		user = userService.findByOtp(otpCode);
		User loggedInUser = userService.getLoggedInUser();
		if(user == null) {
			model.addAttribute("message", "Invalid OTP Code");
		} 
		else if (bindingResult.hasErrors()) {
			redirectAttr.addFlashAttribute("org.springframework.validation.BindingResult.userForm", bindingResult);
			redirectAttr.addFlashAttribute("userForm", userForm);
			redirect = request.getHeader("Referer");
			Logger.error("{} || failed to reset password.", user.getUsername());
			return "redirect:" + redirect;
		}
		else { 
			userService.resetPassword(user,password);
			model.addAttribute("message", "Your password has been changed");
			Logger.info("{} || changed thier password.", loggedInUser.getUsername());
		}
		return "resetpasswordform";
	}

}
