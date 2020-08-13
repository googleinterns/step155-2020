package com.google.sps.data;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public final class Authenticator {
  private static final UserService userService = UserServiceFactory.getUserService();

  /**
   * Determines whether a user is logged in. Returns false if the user is not logged in and a
   * redirection occurs. Otherwise, returns true.
   */
  public static boolean isLoggedIn(HttpServletResponse response, String redirection)
      throws IOException {
    if (!userService.isUserLoggedIn()) {
      String loginUrl = userService.createLoginURL(redirection);
      response.sendRedirect(loginUrl);
      return false;
    }
    return true;
  }
}
