package roppy.dq10.seraphysearcher.service;

import java.util.Arrays;
import java.util.List;

public class TargetServiceConfig {
	public final static TargetServiceConfig[] TARGET_CONFIG_ARRAY = {
		new TargetServiceConfig(
				"SERA",
				2L * 60L * 60L * 1000L,
				QueryConfig.SERA,
				50,
				Arrays.asList("") ),
		new TargetServiceConfig(
				"FOS",
				2L * 60L * 60L * 1000L,
				QueryConfig.FOS,
				50,
				Arrays.asList("") ),
		new TargetServiceConfig(
				"ASUB",
				2L * 60L * 60L * 1000L,
				QueryConfig.ASUB,
				50,
				Arrays.asList("") ),
		new TargetServiceConfig(
				"GEN",
				2L * 60L * 60L * 1000L,
				QueryConfig.GEN,
				50,
				Arrays.asList("幻想画") ),
		new TargetServiceConfig(
				"SHAB",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("シャボン", "ｼｬﾎﾞﾝ")),
		new TargetServiceConfig(
				"TENT",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("テントウ", "サウルス", "ﾃﾝﾄｳ", "ｻｳﾙｽ", "さうるす", "てんとう")),
		new TargetServiceConfig(
				"NAUM",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("ナウマン", "コック", "ﾅｳﾏﾝ", "ｺｯｸ", "なうまん")),
		new TargetServiceConfig(
				"MOGU",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("たたき", "モグラ", "ﾓｸﾞﾗ", "もぐら")),
		new TargetServiceConfig(
				"SOUJ",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("そうじ", "掃除")),
		new TargetServiceConfig(
				"CAME",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("カメラ", "ｶﾒﾗ")),
		new TargetServiceConfig(
				"HOTA",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("ホタル", "ﾎﾀﾙ")),
		new TargetServiceConfig(
				"OSUM",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("おすも", "もっこり")),
		new TargetServiceConfig(
				"KINA",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("きなこ", "こづち")),
		new TargetServiceConfig(
				"CURR",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("カレー", "サタン", "ｶﾚｰｻﾀﾝ")),
		new TargetServiceConfig(
				"HITC",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("ヒッチハイク", "デビル", "ﾋｯﾁﾊｲｸ", "ﾃﾞﾋﾞﾙ")),
		new TargetServiceConfig(
				"TYAB",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("ちゃばしら", "茶柱")),
		new TargetServiceConfig(
				"HANI",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("はにわ", "ハニワ", "埴輪", "ﾊﾆﾜ", "にんぎょう")),
		new TargetServiceConfig(
				"PRIN",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("プリンセス", "ﾌﾟﾘﾝｾｽ", "ニャン", "にゃんこ", "姫猫", "猫姫")),
		new TargetServiceConfig(
				"HAKU",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("はくば", "白馬", "おうじ", "王子")),
		new TargetServiceConfig(
				"RAIN",
				46L * 60L * 1000L,
				QueryConfig.SEAL,
				40,
				Arrays.asList("レイン", "ﾚｲﾝ", "コート", "れいん")),
			new TargetServiceConfig(
					"AKUR",
					46L * 60L * 1000L,
					QueryConfig.SEAL,
					40,
					Arrays.asList("アクロ", "バット", "ｱｸﾛﾊﾞｯﾄ")),
			new TargetServiceConfig(
					"INEK",
					46L * 60L * 1000L,
					QueryConfig.SEAL,
					40,
					Arrays.asList("いねかり", "いねかりぞく")),
			new TargetServiceConfig(
					"CREA",
					46L * 60L * 1000L,
					QueryConfig.SEAL,
					40,
					Arrays.asList("クリーム", "ｸﾘｰﾑ", "つむり")),
			new TargetServiceConfig(
					"SUIK",
					46L * 60L * 1000L,
					QueryConfig.SEAL,
					40,
					Arrays.asList("スイカ", "ｽｲｶ", "すいか", "西瓜")),
			new TargetServiceConfig(
					"HARA",
					46L * 60L * 1000L,
					QueryConfig.SEAL,
					40,
					Arrays.asList("ハラッヘリン", "ハラヘリ", "腹減り", "はらへり")),
			new TargetServiceConfig(
					"MANP",
					46L * 60L * 1000L,
					QueryConfig.SEAL,
					40,
					Arrays.asList("まんぷく", "マンプク", "ﾏﾝﾌﾟｸ")),
			new TargetServiceConfig(
					"PENC",
					46L * 60L * 1000L,
					QueryConfig.SEAL,
					40,
					Arrays.asList("ペンシル", "ボーイ", "ﾍﾟﾝｼﾙ", "ﾎﾞｰｲ", "えんぴつ", "鉛筆")),
			new TargetServiceConfig(
					"ONIB",
					46L * 60L * 1000L,
					QueryConfig.SEAL,
					40,
					Arrays.asList("おにび", "鬼火", "オニビ", "ｵﾆﾋﾞ"))
			};

	private String key;
	private long intervalTime;
	private int thresholdScore;
	private int queryIndex;
	private List<String> fetchStringList;
	public TargetServiceConfig(String key, long intervalTime, int queryIndex, int thresholdScore,
			List<String> fetchStringList) {
		super();
		this.key = key;
		this.intervalTime = intervalTime;
		this.thresholdScore = thresholdScore;
		this.queryIndex = queryIndex;
		this.fetchStringList = fetchStringList;
	}

	public String getKey() {
		return key;
	}
	public long getIntervalTime() {
		return intervalTime;
	}
	public int getQueryIndex() {
		return queryIndex;
	}
	public int getThresholdScore() {
		return thresholdScore;
	}
	public List<String> getFetchStringList() {
		return fetchStringList;
	}
}
