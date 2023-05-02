package roppy.dq10.seraphysearcher.service;


import twitter4j.conf.ConfigurationBuilder;

public class CredentialConfig {
	public static ConfigurationBuilder getConfigurationBuilder(){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("XXXX")
		  .setOAuthConsumerSecret("XXXX")
		  .setOAuthAccessToken("XXXX")
		  .setOAuthAccessTokenSecret("XXXX");
		return cb;
	}

	public static String getServerKey() {
		return "XXXX";
	}

}
