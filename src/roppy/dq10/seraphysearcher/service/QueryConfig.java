package roppy.dq10.seraphysearcher.service;

public class QueryConfig {
	public final static int SERA = 0;
	public final static int FOS = 1;
	public final static int ASUB = 2;
	public final static int GEN = 3;
	public final static int SEAL = 4;

	public final static String QUERY_STRING_ARRAY[] = {
		"#野生のセラフィ OR #流浪のセラフィ OR 野生のセラフィ OR 流浪のセラフィ -いません -異常 -定点 exclude:retweets",
		"#野生のフォステイル OR #流浪のフォステイル OR 野生のフォステイル OR 流浪のフォステイル -いません -異常 -定点 exclude:retweets",
		"#野生のアスバル OR #流浪のアスバル OR 野生のアスバル OR 流浪のアスバル -いません -異常 -定点 exclude:retweets",
		"幻想画 -いません -異常 -いじょう -定点 exclude:retweets",
		"#モンスターシール -いません -異常 -定点 exclude:retweets"
	};

}
