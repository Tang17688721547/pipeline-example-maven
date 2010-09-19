package com.springsource.greenhouse.oauth;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth.consumer.token.HttpSessionBasedTokenServices;
import org.springframework.security.oauth.consumer.token.OAuthConsumerToken;
import org.springframework.security.oauth.consumer.token.OAuthConsumerTokenServices;
import org.springframework.social.oauth.AccessToken;
import org.springframework.social.oauth.AccessTokenServices;

import com.springsource.greenhouse.account.Account;

/**
 * Implementation of SS-OAuth's session-oriented
 * {@link OAuthConsumerTokenServices} interface that delegates to an
 * implementation of Spring Social's AccessTokenServices. This enables SS-OAuth
 * to deal with consumer tokens through a token services implementation that is
 * per-session while Spring Social can deal with them through a service that is
 * a singleton.
 * 
 * This adapter also extends SS-OAuth's {@link HttpSessionBasedTokenServices} to
 * take advantage of session storage of access tokens.
 * 
 * @author Craig Walls
 */
public class OAuthConsumerTokenServicesAdapter extends HttpSessionBasedTokenServices {
	private Account account;
	private HttpSession session;
	private AccessTokenServices accessTokenServices;

	public OAuthConsumerTokenServicesAdapter(HttpSession session, AccessTokenServices accessTokenServices,
			Account account) {
		super(session);
		this.account = account;
		this.session = session;
		this.accessTokenServices = accessTokenServices;
	}

	public OAuthConsumerToken getToken(String resourceId) throws AuthenticationException {
        OAuthConsumerToken token = super.getToken(resourceId);
		if (token == null) {
			AccessToken accessToken = accessTokenServices.getToken(resourceId, account);

			if (accessToken != null) {
				token = new OAuthConsumerToken();
				token.setAccessToken(true);
				token.setValue(accessToken.getValue());
				token.setSecret(accessToken.getSecret());
				token.setResourceId(accessToken.getProviderId());
				super.storeToken(resourceId, token);
			}
		}
		return token;
	}

	public void storeToken(String resourceId, OAuthConsumerToken token) {
		if (token.isAccessToken()) {
			accessTokenServices.storeToken(resourceId, account, new AccessToken(token.getValue(), token.getSecret(),
					resourceId));
		}
		super.storeToken(resourceId, token);
	}
	
	public void removeToken(String resourceId) {
		accessTokenServices.removeToken(resourceId, account);
	    session.removeAttribute(KEY_PREFIX + "#" + resourceId);
	}
}
