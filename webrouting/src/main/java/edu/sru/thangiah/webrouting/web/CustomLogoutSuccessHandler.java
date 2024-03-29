package edu.sru.thangiah.webrouting.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;



/**
 * Extends the SimpleUrlLogoutSuccessHandler to log interaction
 * @author Dakota Myers drm1022@sru.edu
 * @since 1/01/2023
 */

public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {
	
	
	private static final Logger Logger = LoggerFactory.getLogger(CustomLogoutSuccessHandler.class);

	/**
	 * Logs the logout interaction
	 */
	@Override
    public void onLogoutSuccess(
      HttpServletRequest request, 
      HttpServletResponse response, 
      Authentication authentication) 
      throws IOException, ServletException {
 
        Logger.info("{} || logged out.", authentication.getName());

        super.onLogoutSuccess(request, response, authentication);
    }
}
