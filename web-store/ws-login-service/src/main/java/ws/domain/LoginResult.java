package ws.domain;

public record LoginResult(Boolean succesful, SessionCookie cookie, String message) {
	public static String DEFAULT_FAILURE_MESSAGE = "Failure to log in: make sure you have the right username and password.";
}
