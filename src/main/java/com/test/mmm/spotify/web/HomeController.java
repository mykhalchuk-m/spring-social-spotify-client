package com.test.mmm.spotify.web;

import org.springframework.social.spotify.api.Spotify;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

/**
 * Created by Marian_Mykhalchuk on 9/8/2016.
 */
@Controller
public class HomeController {

	private final Spotify spotify;

	@Inject
	public HomeController(Spotify spotify) {
		this.spotify = spotify;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/")
	public String spotifyHome(Model model) {
		model.addAttribute("spotifyProfileName", spotify.me().getProfile().getDisplayName());
		model.addAttribute("following", spotify.me().getFollowing().getArtists().getItems());
		return "home";
	}

}
