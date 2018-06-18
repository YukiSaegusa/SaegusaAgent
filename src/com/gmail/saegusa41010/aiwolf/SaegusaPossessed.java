package com.gmail.saegusa41010.aiwolf;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinationContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/**
 * 裏切り者役エージェントクラス
 */
public class SaegusaPossessed extends SaegusaVillager{
	int numWolves;
	boolean isCameout;
	List<Judge> fakeDivinationList = new ArrayList<>();
	Deque<Judge> fakeDivinationQueue = new LinkedList<>();
	List<Agent> divinedAgents = new ArrayList<>();

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		numWolves = gameSetting.getRoleNumMap().get(Role.WEREWOLF);
		isCameout = false;
		fakeDivinationList.clear();
		fakeDivinationQueue.clear();
		divinedAgents.clear();
	}

	private Judge getFakeDivination() {
		//基本的に、1～2ターンは白出し、3ターン目以降で黒出し開始
		Agent target = null;
		List<Agent> candidates = new ArrayList<>();
		boolean isDivWolf = false;
		//推理リストにある、人狼候補を優先的に占って白出しする(占い師coと既に占った人以外)
		for (Agent agent : aliveOthers) {
			if (!divinedAgents.contains(agent) && comingoutMap.get(agent) != Role.SEER) {
				for (int i = 1; i < GuessList.size(); i++) {
					if (GuessList.get(i).getwolfarray().contains(agent.getAgentIdx())) {
						candidates.add(agent);
						isDivWolf = true;
					}
				}
			}
		}
		//人狼候補がいなかったら、占い師以外を占う
		if (candidates.isEmpty()) {
			for (Agent a : aliveOthers) {
				if (!divinedAgents.contains(a) && comingoutMap.get(a) != Role.SEER) {
					candidates.add(a);
				}
			}
		}
		if (!candidates.isEmpty()) {
			target = randomSelect(candidates);
		} else {
			target = randomSelect(aliveOthers);
		}
		//他の占い師coの人数を調べる
		int otherDivNum = 0;
		for (Agent agent : aliveOthers) {
			if (comingoutMap.get(agent) == Role.SEER) {
				otherDivNum += 1;
			}
		}
		//占い師coが自分含め2人で、真占い師が黒出しした場合は
		//自分も誰かを黒出し(この場合は、推理リストの人狼候補を占った場合でも黒出し)
		if (otherDivNum == 1) {

		}

		//人狼候補を占ったら、必ず白出しする
		if (isDivWolf) {
			Species result = Species.HUMAN;
			return new Judge(day, me, target, result);
		}
		//1,2ターン目は白出しの確率を高くする(100%白出しでもいいかも)
		if (day == 1 || day == 2) {
			Species result = Species.HUMAN;
			if (Math.random() < 0.05) {
				result = Species.WEREWOLF;
			}
			return new Judge(day, me, target, result);
		}
		//占い師coが合計２人の時は、黒出しの割合を高くする
		if (otherDivNum == 1) {
			// 偽人狼に余裕があれば，人狼と人間の割合を勘案して，50%の確率で人狼と判定
			//但し、本物占い師が黒出しをした後なら、確率を下げた方がいいかも
			//本物占い師が黒出しした人数から、残りの人狼の人数を考慮して、その後判断がよいかも
			Species result = Species.HUMAN;
			int nFakeWolves = 0;
			for (Judge j : fakeDivinationList) {
				if (j.getResult() == Species.WEREWOLF) {
					nFakeWolves++;
				}
			}
			if (nFakeWolves < numWolves && Math.random() < 0.5) {
				result = Species.WEREWOLF;
			}
			return new Judge(day, me, target, result);
		}
		//占い師coが合計３人以上の時は、黒出しの割合を低くする→高くした方がいいかも
		//時間に余裕があれば、他の占い師coが霊媒師coを行った方がいいかも
		else {
			// 偽人狼に余裕があれば，人狼と人間の割合を勘案して，30%の確率で人狼と判定
			Species result = Species.HUMAN;
			int nFakeWolves = 0;
			for (Judge j : fakeDivinationList) {
				if (j.getResult() == Species.WEREWOLF) {
					nFakeWolves++;
				}
			}
			if (nFakeWolves < numWolves && Math.random() < 0.3) {
				result = Species.WEREWOLF;
			}
			return new Judge(day, me, target, result);
		}
	}

	public void dayStart() {
		super.dayStart();
		// 偽の判定
		if (day > 0) {
			Judge judge = getFakeDivination();
			if (judge != null) {
				fakeDivinationList.add(judge);
				fakeDivinationQueue.offer(judge);
				divinedAgents.add(judge.getTarget());
			}
		}
	}

	protected void chooseVoteCandidate() {
		werewolves.clear();
		List<Agent> candidates = new ArrayList<>();
		// 自分や殺されたエージェントを人狼と判定している占い師は人狼候補
		for (Judge j : divinationList) {
			if (j.getResult() == Species.WEREWOLF && (j.getTarget() == me || isKilled(j.getTarget()))) {
				if (!werewolves.contains(j.getAgent())) {
					werewolves.add(j.getAgent());
				}
			}
		}
		// 対抗カミングアウトのエージェントは投票先候補
		//		for (Agent a : aliveOthers) {
		//			if (!werewolves.contains(a) && comingoutMap.get(a) == Role.SEER) {
		//				candidates.add(a);
		//			}
		//		}
		// 人狼と判定したエージェントは投票先候補
		List<Agent> fakeHumans = new ArrayList<>();
		for (Judge j : fakeDivinationList) {
			if (j.getResult() == Species.HUMAN) {
				if (!fakeHumans.contains(j.getTarget())) {
					fakeHumans.add(j.getTarget());
				}
			} else {
				if (!candidates.contains(j.getTarget())) {
					candidates.add(j.getTarget());
				}
			}
		}
		// 候補がいなければ人間と判定していない村人陣営から
		if (candidates.isEmpty()) {
			for (Agent a : aliveOthers) {
				if (!werewolves.contains(a) && !fakeHumans.contains(a)) {
					candidates.add(a);
				}
			}
		}
		// それでも候補がいなければ村人陣営から
		if (candidates.isEmpty()) {
			for (Agent a : aliveOthers) {
				if (!werewolves.contains(a)) {
					candidates.add(a);
				}
			}
		}
		if (!candidates.contains(voteCandidate)) {
			voteCandidate = randomSelect(candidates);
			// 以前の投票先から変わる場合，新たに推測発言と占い要請をする
			if (canTalk) {
				talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
				talkQueue.offer(new Content(
						new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
			}
		}
	}

	public String talk() {
		// 即占い師カミングアウト
		if (!isCameout) {
			talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.SEER)));
			isCameout = true;
		}
		// カミングアウトしたらこれまでの偽占い結果をすべて公開
		if (isCameout) {
			while (!fakeDivinationQueue.isEmpty()) {
				Judge divination = fakeDivinationQueue.poll();
				talkQueue.offer(
						new Content(new DivinedResultContentBuilder(divination.getTarget(), divination.getResult())));
			}
		}
		return super.talk();
	}

}
