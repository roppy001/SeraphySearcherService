package roppy.dq10.seraphysearcher.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class MainService {
	private static Log log = LogFactory.getLog(MainService.class);

	public final static String TOPIC = "/topics/all-2.0";

	public final static String DATA_DIR_KEY = "data.dir";
	public final static String USER_LIST_PATH_KEY = "userlist.path";
	public final static String DEBUG_TOKEN_KEY = "debug.token";

	public final static long VALID_CREATED_TIME = 28L * 24L * 60L * 60L * 1000L;
	public final static long VALID_FOLLOWING_COUNT = 10;
	public final static long VALID_FOLLOWER_COUNT = 10;
	public final static long VALID_TWEET_COUNT = 10;

	public final static long ACCESS_INTERVAL_TIME = 2L * 60L * 1000L;

	public final static int QUERY_STATUS_NUM = 40;
	public final static int CACHE_STATUS_NUM = 120;

	public static void main(String[] args) {
		if (args.length != 2) {
			log.warn("usage: ");
			log.warn("java (jar) (config.txt) release|debug");
		}

		Path configPath = Paths.get(args[0]);

		Properties propertiesConfig = new Properties();
		try {
			propertiesConfig.load(Files.newBufferedReader(configPath));
		} catch (IOException ioe) {
			log.warn("cannot load configuration file.");
			return;
		}

		// リリースモードかどうかの取得
		boolean isRelease = "release".equals(args[1]);

		log.info("LAUNCH AS: " + (isRelease ? "release" : "debug"));

		// ツイートデータキャッシュの置き場所パスの取得
		String dataFileDir = propertiesConfig.getProperty(DATA_DIR_KEY);
		if (dataFileDir == null) {
			log.warn("Please set data.dir in configuration file.");
			return;
		}

		// デバッグ時の送り先トークンの指定
		String tokenKey = propertiesConfig.getProperty(DEBUG_TOKEN_KEY);
		if (tokenKey == null) {
			log.warn("Please set data.token in configuration file.");
			return;
		}

		// ユーザリストファイルの読み込み
		// IDから開始する場合はIdによる除外
		// @から開始する場合はScreenNameによる除外
		// ただし+、*から開始する場合は除外せず
		// *から開始する場合は攻略サイトユーザとして登録される。
		Set<Long> blackIdSet = new HashSet<Long>();
		Set<String> blackNameSet = new HashSet<String>();
		Set<String> whiteNameSet = new HashSet<String>();
		Set<String> premiumNameSet = new HashSet<String>();

		String userListFilePath = propertiesConfig
				.getProperty(USER_LIST_PATH_KEY);

		if (userListFilePath != null) {
			Path userListPath = Paths.get(userListFilePath);

			try (BufferedReader bufferedReader = Files
					.newBufferedReader(userListPath)) {
				while (true) {
					String lineStr = bufferedReader.readLine();
					if (lineStr == null) {
						break;
					}

					if (!"".equals(lineStr)) {
						if (lineStr.startsWith("@")) {
							blackNameSet.add(lineStr.substring(1));
						} else if (lineStr.startsWith("+")) {
							whiteNameSet.add(lineStr.substring(1));
						} else if (lineStr.startsWith("*")) {
							whiteNameSet.add(lineStr.substring(1));
							premiumNameSet.add(lineStr.substring(1));
						} else {
							try {
								long id = Long.parseLong(lineStr);
								blackIdSet.add(id);
							} catch (NumberFormatException nfe) {
								nfe.printStackTrace(System.err);
							}
						}
					}

				}
			} catch (IOException ioe) {
				log.warn("cannot load black list file.");
				return;
			}
		} else {
			log.warn("set userlist.path property.");
			return;
		}

		// cron起動方式
		Path sendTimeFilePath = Paths.get(dataFileDir, "sendtime.txt");
		List<Long> lastSendTimeList = loadLastSendTimeList(sendTimeFilePath);

		if(lastSendTimeList.size() != TargetServiceConfig.TARGET_CONFIG_ARRAY.length){
			lastSendTimeList = new ArrayList<Long>();
			for (int i = 0; i < TargetServiceConfig.TARGET_CONFIG_ARRAY.length; i++) {
				lastSendTimeList.add(0L);
			}
		}

		// Java起動方式
		//List<Long> lastSendTimeList = new ArrayList<Long>();
		//for (int i = 0; i < TargetServiceConfig.TARGET_CONFIG_ARRAY.length; i++) {
		//	lastSendTimeList.add(0L);
		//}

		//while (true) {
			loop(lastSendTimeList, dataFileDir, tokenKey, blackIdSet,
					blackNameSet, whiteNameSet, premiumNameSet, isRelease);
		//	try {
		//		Thread.sleep(ACCESS_INTERVAL_TIME);
		//	} catch (InterruptedException ie) {
		//		ie.printStackTrace(System.err);
		//	}
		//}

		saveLastSendTimeList(sendTimeFilePath, lastSendTimeList);

	}

	private static void loop(List<Long> lastSendTimeList, String dataFileDir,
			String tokenKey, Set<Long> blackIdSet, Set<String> blackNameSet,
			Set<String> whiteNameSet, Set<String> premiumNameSet,
			boolean isRelease) {
		log.info("START DETECTION");

		List<List<TweetData>> tweetDataListCache = new ArrayList<List<TweetData>>();

		long currentTime = Calendar.getInstance().getTimeInMillis();

		for (int queryIndex = 0; queryIndex < QueryConfig.QUERY_STRING_ARRAY.length; queryIndex++) {
			List<TweetData> tweetDataList = new ArrayList<TweetData>();

			// キャッシュデータを読み込み

			String fileName = String.format("tw_%02d.txt", queryIndex);
			Path tweetFilePath = Paths.get(dataFileDir, fileName);

			List<TweetData> lastTweetDataList = loadTweetDataList(tweetFilePath);

			long lastStatusId = lastTweetDataList.isEmpty() ? 0
					: lastTweetDataList.get(0).getId();

			// ツイートを取得
			try {
				Twitter twitter = new TwitterFactory(CredentialConfig
						.getConfigurationBuilder().build()).getInstance();

				Query query = new Query(
						QueryConfig.QUERY_STRING_ARRAY[queryIndex]);
				query.setCount(QUERY_STATUS_NUM);

				// 差分取得モード (データ通信量削減対策)
				// 差分取得モードの場合は、最終取得Idを設定
				if (lastStatusId != 0) {
					query.setSinceId(lastStatusId);
				}

				log.debug("queryIndex = " + queryIndex + " lastStatusId = "
						+ lastStatusId);

				// ツイートを取得
				QueryResult result = twitter.search(query);

				// ブラックリストと、巡回ツイートを除去しつつ、TweetData 型リストに変換
				for (Status status : result.getTweets()) {
					String tmpText = status.getText().replace("\r", "")
							.replace("\n", "").replace("\t", "");

					// ブラックリストの方々は問答無用で退散
					if (blackIdSet.contains(status.getUser().getId())
							|| blackNameSet.contains(status.getUser()
									.getScreenName())) {
						log.info("EXCLUDED AS BL: QI=" + queryIndex + ","
								+ getUserInfoString(status.getUser()));

						// ホワイトリストが真の場合は除外しない
						// 自動除外判定
					} else if (!whiteNameSet.contains(status.getUser()
							.getScreenName())
							&& !isValidUser(status.getUser(), currentTime)) {
						log.info("EXCLUDED AUTO: QI=" + queryIndex + ","
								+ getUserInfoString(status.getUser()));

						// 除外文字列が入ってる場合は除外する
					} else if (Analyzer.isExcluded(tmpText)) {
						log.info("EXCLUDED UNEXPECTED STRING: QI=" + queryIndex + ","
								+ getUserInfoString(status.getUser()));

					} else {
						tweetDataList.add(new TweetData(status.getId(), status
								.getCreatedAt().getTime(), status.getUser()
								.getId(), status.getUser().getScreenName(),
								tmpText));
					}
				}

				if (lastStatusId != 0) {
					// 差分モードの場合、末尾に保存してたツイートデータを追加
					tweetDataList.addAll(lastTweetDataList);
					// 指定の数以上ある場合は、末尾を削除する
					while (tweetDataList.size() > CACHE_STATUS_NUM) {
						tweetDataList.remove(tweetDataList.size() - 1);
					}
				}

				// 新たに取得したツイートデータを保存する
				saveTweetDataList(tweetFilePath, tweetDataList);
			} catch (TwitterException e) {
				log.warn(e.toString());
			}

			tweetDataListCache.add(tweetDataList);
		}

		for (int targetIndex = 0; targetIndex < TargetServiceConfig.TARGET_CONFIG_ARRAY.length; targetIndex++) {
			TargetServiceConfig targetConfig = TargetServiceConfig.TARGET_CONFIG_ARRAY[targetIndex];
			// ツイートデータの中から、関連ツイートのみを抽出する。
			List<TweetData> tweetDataList = new ArrayList<TweetData>();

			List<TweetData> allTweetDataList = tweetDataListCache
					.get(targetConfig.getQueryIndex());

			for (int i = 0; i < allTweetDataList.size(); i++) {
				TweetData data = allTweetDataList.get(i);
				boolean related = false;
				for (String fetchString : targetConfig.getFetchStringList()) {
					if (data.getText().contains(fetchString)) {
						related = true;
						break;
					}
				}
				if (related) {
					tweetDataList.add(data);
				}
			}

			// ツイートデータリストをもとに、出現判定評価値を得る
			int score = Analyzer.calculateAppearanceScore(currentTime,
					tweetDataList, premiumNameSet);

			StringBuilder sb = new StringBuilder();
			sb.append("USERS:");

			for (TweetData status : tweetDataList) {
				sb.append(", " + status.getScreenName());
			}

			log.debug(sb.toString());

			log.info("SCORE REPORT: "+ targetConfig.getKey() + " score=" + score
					+ (score >= targetConfig.getThresholdScore() ? "!!APPEAR!!" : ""));

			// 出現判定が真の場合で直近でPushを送ってない場合、出現Push送信を行う。
			if (score >= targetConfig.getThresholdScore()
					&& currentTime >= lastSendTimeList.get(targetIndex)
							+ targetConfig.getIntervalTime()) {
				try {
					sendPush(targetConfig.getKey(), currentTime , tokenKey, isRelease);
					lastSendTimeList.set(targetIndex, currentTime);
					log.info("SEND PUSH SUCCESSFULLY: "
							+ targetConfig.getKey() + " as " + (isRelease ? "release" : "debug") + " mode.");
				} catch (IOException ioe) {
					log.warn(ioe.toString());
					log.warn("SEND PUSH FAILED");
				}

			}
		}

	}

	public static void saveTweetDataList(Path path, List<TweetData> list) {
		try (OutputStream os = Files.newOutputStream(path);
				PrintWriter printWriter = new PrintWriter(os)) {

			for (TweetData data : list) {
				printWriter.println(data.getId()
						+ "\t"
						+ data.getTime()
						+ "\t"
						+ data.getUserId()
						+ "\t"
						+ data.getScreenName()
						+ "\t"
						+ data.getText().replace("\r", "").replace("\n", "")
								.replace("\t", ""));
			}
		} catch (IOException ioe) {
			log.warn(ioe.toString());
		}

	}

	public static List<TweetData> loadTweetDataList(Path path) {
		List<TweetData> tweetDataList = new ArrayList<TweetData>();

		try (BufferedReader br = Files.newBufferedReader(path)) {
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}

				String[] p = line.split("\t", -1);
				if (p.length == 5) {
					tweetDataList
							.add(new TweetData(Long.parseLong(p[0]), Long
									.parseLong(p[1]), Long.parseLong(p[2]),
									p[3], p[4]));
				} else if (!"".equals(line)) {
					log.error("ILLEGAL CACHE DATA:str=" + line);
					tweetDataList = new ArrayList<TweetData>();
					break;
				}
			}
		} catch (FileNotFoundException fnfe) {
			log.info("CREATE NEW CACHE:" + path.toString());
			tweetDataList = new ArrayList<TweetData>();
		} catch (NoSuchFileException nsfe) {
			log.info("CREATE NEW CACHE:" + path.toString());
			tweetDataList = new ArrayList<TweetData>();
		} catch (NumberFormatException nfe) {
			log.info("CREATE NEW CACHE:" + path.toString());
			tweetDataList = new ArrayList<TweetData>();
		} catch (IOException ioe) {
			log.warn(ioe.toString());
			tweetDataList = new ArrayList<TweetData>();
		}

		return tweetDataList;
	}

	public static void saveLastSendTimeList(Path path, List<Long> list) {
		try (OutputStream os = Files.newOutputStream(path);
				PrintWriter printWriter = new PrintWriter(os)) {

			for (long t : list) {
				printWriter.println(t);
			}
		} catch (IOException ioe) {
			log.warn(ioe.toString());
		}
	}

	public static List<Long> loadLastSendTimeList(Path path) {
		List<Long> lastSendTimeList = new ArrayList<Long>();

		try (BufferedReader br = Files.newBufferedReader(path)) {
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}

				if(!"".equals(line)){
					lastSendTimeList.add(Long.parseLong(line));
				}
			}
		} catch (FileNotFoundException fnfe) {
			log.info("CREATE NEW CACHE:" + path.toString());
			lastSendTimeList = new ArrayList<Long>();
		} catch (NoSuchFileException nsfe) {
			log.info("CREATE NEW CACHE:" + path.toString());
			lastSendTimeList = new ArrayList<Long>();
		} catch (NumberFormatException nfe) {
			log.info("CREATE NEW CACHE:" + path.toString());
			lastSendTimeList = new ArrayList<Long>();
		} catch (IOException ioe) {
			log.warn(ioe.toString());
			lastSendTimeList = new ArrayList<Long>();
		}

		return lastSendTimeList;
	}


	public static void sendPush(String targetKey, long currentTime,String tokenKey, boolean isRelease)
			throws IOException {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost("https://fcm.googleapis.com/fcm/send");
			post.setHeader("Content-Type", "application/json");
			post.setHeader("Authorization",
					"key=" + CredentialConfig.getServerKey());

			String json = "{ \"to\": \""
					+ (isRelease ? TOPIC : tokenKey)
					+ "\", \"priority\": \"high\", \"data\" : { \"target\" : \""
					+ targetKey + "\", \"detect_time\" : \""
					+ String.format("%d", currentTime) + "\" } }";
			post.setEntity(new StringEntity(json));

			HttpResponse res = client.execute(post);

			if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				log.warn("SEND FAIL: statuscode=" + res.getStatusLine().getStatusCode());
				throw new IOException("Send Failed");
			}
		} catch (UnsupportedEncodingException uee) {
			throw new IOException(uee);
		}
	}



	private static String getUserInfoString(User user) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

		return "ID=" + user.getId() + ",NAME=" + user.getScreenName() + ",FWR="
				+ user.getFollowersCount() + ",FWING=" + user.getFriendsCount()
				+ ",STC=" + user.getStatusesCount() + ",CRAT="
				+ sdf.format(user.getCreatedAt());
	}

	private static boolean isValidUser(User user, long currentTime) {
		return currentTime > user.getCreatedAt().getTime() + VALID_CREATED_TIME
				&& user.getFollowersCount() >= VALID_FOLLOWER_COUNT
				&& user.getFriendsCount() >= VALID_FOLLOWING_COUNT
				&& user.getStatusesCount() >= VALID_FOLLOWING_COUNT;
	}


}
