package roppy.dq10.seraphysearcher.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Analyzer {

	private static final long SHORT_TIME = 240*1000;
	private static final long MIDDLE_TIME = 600*1000;
	private static final long LONG_TIME = 1020*1000;
	private static final int SHORT_K = 0; // ショートレンジはしばし廃止
	private static final int MIDDLE_K = 11;
	private static final int LONG_K = 8;

	/**
	 * 巡回報告、消滅等、関係のないツイートであるかどうかをテキストから判断
	 * @return
	 */
	public static boolean isExcluded(String str){
		str = str.replace("ー", "").replace("～", "");
		return
				//否定語全般
				str.contains("ません")
				// 巡回・定点監視系
				|| str.contains("異常")
				|| str.contains("いじょう")
				|| str.contains("定点")
				|| str.contains("常駐")
				|| str.contains("監視")
				|| str.contains("回ります")
				|| str.contains("回ってます")
				|| str.contains("全滅")
				|| str.contains("不在")
				|| str.contains("確認できず")
				|| str.contains("いない")
				|| str.contains("おらず")
				|| str.contains("おらん")
				|| str.contains("でない")
				|| str.contains("出ない")
				// 消滅報告系
				|| str.contains("消え")
				|| str.contains("きえました")
				|| (!str.contains("消滅時刻") && str.contains("消滅"))
				|| str.contains("待機")
				|| str.contains("消灯")
				|| str.contains("終了")
				|| str.contains("帰宅")
				|| (!str.contains("沈没船") && str.contains("没"))
				|| str.contains("帰")
				|| str.contains("いなくな")
				|| str.contains("居なくな")
				// 次回予定共有系
				|| str.contains("次は")
				|| str.contains("次回は")
				|| str.contains("次の")
				|| str.contains("次回の")
				|| str.contains("予定")
				|| str.contains("タイム")
				// ガセ反応系
				|| str.contains("ガセ")
				|| str.contains("嘘")
				|| str.contains("騙")
				|| str.contains("スパム")
				|| str.contains("うそも")
				|| (!str.contains("デマト") && str.contains("デマ"));
	}

	/**
	 * 出現報告ツイートであるかどうかをテキストから判断
	 * @return
	 */
	public static boolean isAppear(String str){
		return str.contains("でました")
				|| str.contains("出ました")
				|| str.contains("でてます")
				|| str.contains("出てます")
				|| str.contains("でています")
				|| str.contains("出ています")
				|| str.contains("いました")
				|| str.contains("居ました")
				|| str.contains("会えました")
				|| str.contains("会えた")
				|| str.contains("あえました")
				|| str.contains("あえた")
				|| str.contains("出現中")
				|| str.contains("出現した")
				|| str.contains("出現しました")
				|| str.contains("確認");
	}

	/**
	 * 特定人物の定型ツイートを判断し、1人発言でもアラームを起動させる。
	 * @return
	 */
	public static boolean isSpecifiedUserTweet(TweetData data){
		// 非公開
		return false;
	}

	/**
	 * スコアを計算
	 * 平均1.5点のものが、1分間に約5回,3分間に約10回,6分間に約15回のいずれかで100点となるようなスコアを計算する)
	 *
	 * @param target
	 * @param statusList
	 * @return
	 */
	public static int calculateAppearanceScore(long target, List<TweetData> tweetDataList, Set<String> premiumNameSet){
		Map<Long, Integer> shortScoreMap = new HashMap<Long, Integer>();
		Map<Long, Integer> middleScoreMap = new HashMap<Long, Integer>();
		Map<Long, Integer> longScoreMap = new HashMap<Long, Integer>();

		for(TweetData data : tweetDataList){
			long t = data.getTime();
			long id = data.getUserId();
			if(target - SHORT_TIME < t ){
				updateScoreMap(shortScoreMap, id, addedScore(data, premiumNameSet));
			}
			if(target - MIDDLE_TIME < t ){
				updateScoreMap(middleScoreMap, id, addedScore(data, premiumNameSet));
			}
			if(target - LONG_TIME < t ){
				updateScoreMap(longScoreMap, id, addedScore(data, premiumNameSet));
			}
		}

		return Math.max(
				sumOfScoreMap(shortScoreMap)*SHORT_K,
				Math.max(
						sumOfScoreMap(middleScoreMap)*MIDDLE_K,
						sumOfScoreMap(longScoreMap)*LONG_K));
	}

	private static int addedScore(TweetData data, Set<String> premiumNameSet){
		int score = 1;

		if(Analyzer.isAppear(data.getText())) {
			score++;
		}

		if(premiumNameSet.contains(data.getScreenName())) {
			score++;
		}

		if(Analyzer.isSpecifiedUserTweet(data)) {
			score+=10;
		}
		return score;
	}

	private static void updateScoreMap(Map<Long, Integer> map, long id, int score){
		Integer c = map.get(id);

		if(c == null || score > c) {
			map.put(id, score);
		}
	}

	private static int sumOfScoreMap(Map<Long, Integer> map){
		int score = 0;
		for(int s : map.values()){
			score+=s;
		}
		return score;
	}
}
