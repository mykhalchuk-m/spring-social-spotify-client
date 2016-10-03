package com.test.mmm.spotify.config;

import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.spotify.api.Spotify;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Marian_Mykhalchuk on 9/8/2016.
 */
public class UserInterceptor extends HandlerInterceptorAdapter {

	private final UsersConnectionRepository usersConnectionRepository;
	private final UserCookieGenerator userCookieGenerator = new UserCookieGenerator();


	public UserInterceptor(UsersConnectionRepository usersConnectionRepository) {
		this.usersConnectionRepository = usersConnectionRepository;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		rememberUser(request, response);
		handleSignOut(request, response);
		if (SecurityContext.userSignedIn() || requestForSignIn(request)) {
			return true;
		} else {
			return requireSignIn(request, response);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		SecurityContext.remove();
	}

	private boolean requestForSignIn(HttpServletRequest request) {
		return request.getServletPath().startsWith("/signin");
	}

	private boolean requireSignIn(HttpServletRequest request, HttpServletResponse response) throws Exception {
		new RedirectView("/signin", true).render(null, request, response);
		return false;
	}

	private void handleSignOut(HttpServletRequest request, HttpServletResponse response) {
		if (SecurityContext.userSignedIn() && request.getServletPath().startsWith("/signout")) {
			usersConnectionRepository.createConnectionRepository(SecurityContext.getCurrentUser().getId())
					.removeConnections("spotify");
			userCookieGenerator.removeCookie(response);
			SecurityContext.remove();
		}
	}

	private void rememberUser(HttpServletRequest request, HttpServletResponse response) {
		String userId = userCookieGenerator.readCookieValue(request);
		if (userId == null) {
			return;
		}
		if (!userNotFound(userId)) {
			userCookieGenerator.removeCookie(response);
			return;
		}
		SecurityContext.setCurrentUser(new User(userId));
	}

	private boolean userNotFound(String userId) {
		return usersConnectionRepository.createConnectionRepository(userId).findPrimaryConnection(Spotify.class) !=	null;
	}
}
