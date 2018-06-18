package com.gmail.saegusa41010.aiwolf;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinationContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class SaegusaMedium extends SaegusaVillager {
	int comingoutDay;

	boolean isCameout;

	Deque<Judge> identQueue = new LinkedList<>();

	Map<Agent, Species> myIdentMap = new HashMap<>();

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {

		super.initialize(gameInfo, gameSetting);

		comingoutDay = (int) (Math.random() * 3 + 1);

		isCameout = false;

		identQueue.clear();

		myIdentMap.clear();

	}

	public void dayStart() {

		super.dayStart();

		// 霊媒結果を待ち行列に入れる

		Judge ident = currentGameInfo.getMediumResult();

		if (ident != null) {

			identQueue.offer(ident);

			myIdentMap.put(ident.getTarget(), ident.getResult());

		}

	}

	protected void chooseVoteCandidate() {

		super.chooseVoteCandidate();

		// 霊媒師をカミングアウトしている他のエージェントは100点追加

		for (Agent agent : aliveOthers) {

			if (comingoutMap.get(agent) == Role.MEDIUM) {

				IdGuessList[agent.getAgentIdx()] += 100;

			}

		}

		// 自分や殺されたエージェントを人狼と判定，あるいは自分と異なる判定の占い師は100点追加

		for (Judge j : divinationList) {

			Agent agent = j.getAgent();

			Agent target = j.getTarget();

			if (j.getResult() == Species.WEREWOLF && (target == me || isKilled(target))

					|| (myIdentMap.containsKey(target) && j.getResult() != myIdentMap.get(target))) {

				if (isAlive(agent) && !werewolves.contains(agent)) {

					IdGuessList[agent.getAgentIdx()] += 100;

				}

			}

		}

		int maxscore = 0; // 一番ありえそうな推理を採用

		Guess maxGuess = null;

		for (Guess g : GuessList) { // 推理リストを順番に見ていき、点数をつける

			int score = 0; // guessのスコアを付けていく

			for (int w : g.getwolfarray()) {

				score += IdGuessList[w]; // 個人推理の結果を足していく

			}

			g.setscore(score);

			if (score > maxscore) {

				maxscore = score;

				maxGuess = g;

			}

		}

		if (maxGuess != null) { // nullじゃなければwerewolvesリストを更新

			for (Integer i : maxGuess.getwolfarray()) {

				werewolves.add(AgentList.get(i));

			}

		}

		// 候補がいない場合はランダム

		if (werewolves.isEmpty()) {

			if (!aliveOthers.contains(voteCandidate)) {

				voteCandidate = randomSelect(aliveOthers);

			}

		} else {

			if (!werewolves.contains(voteCandidate)) {

				voteCandidate = randomSelect(werewolves);

				// 以前の投票先から変わる場合，新たに推測発言と占い要請をする

				if (canTalk) {

					talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));

					talkQueue.offer(new Content(

							new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));

				}

			}

		}

	}

	public String talk() {

		// カミングアウトする日になったら，あるいは霊媒結果が人間で魔法使いカミングアウトをしていたら

		// あるいは霊媒師カミングアウトが出たらカミングアウト

		if (!isCameout && (day >= comingoutDay

				|| (!identQueue.isEmpty() && identQueue.peekLast().getResult() == Species.HUMAN
						&& comingoutMap.get(identQueue.peekLast().getTarget()) == Role.SEER)

				|| isCo(Role.MEDIUM))) {

			talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.MEDIUM)));

			isCameout = true;

		}

		// カミングアウトしたらこれまでの霊媒結果をすべて公開

		if (isCameout) {

			while (!identQueue.isEmpty()) {

				Judge ident = identQueue.poll();

				talkQueue.offer(new Content(new IdentContentBuilder(ident.getTarget(), ident.getResult())));

			}

		}

		return super.talk();

	}
}
