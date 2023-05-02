package roppy.dq10.seraphysearcher.service;


public class TweetData {
	private long id;
	private long time;
	private long userId;
	private String screenName;
	private String text;
	public TweetData(long id, long time, long userId,
			String screenName, String text) {
		super();
		this.id = id;
		this.time = time;
		this.userId = userId;
		this.screenName = screenName;
		this.text = text;
	}
	public long getId() {
		return id;
	}
	public long getTime() {
		return time;
	}
	public long getUserId() {
		return userId;
	}
	public String getScreenName() {
		return screenName;
	}
	public String getText() {
		return text;
	}

}
