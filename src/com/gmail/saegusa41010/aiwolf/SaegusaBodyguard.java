package com.gmail.saegusa41010.aiwolf;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/**
 * 狩人役エージェントクラス
 */
public class SaegusaBodyguard extends SaegusaVillager {
	/** 護衛したエージェント */
	Agent guardedAgent;

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		guardedAgent = null;
	}

	public Agent guard() {
		Agent guardCandidate = null;
		if (maxGuess != null) {
			// 推理リストを用いて、怪しくない人を護衛
			// 占い師coかつ、推理リストに入っていない人を護衛
			List<Agent> candidates2 = new ArrayList<>();
			for (Agent agent : aliveOthers) {
				if (comingoutMap.get(agent) == Role.SEER) {
					for (int i = 1; i < GuessList.size(); i++) {
						if (!(GuessList.get(i).getwolfarray().contains(agent.getAgentIdx()))) {
							candidates2.add(agent);
						}
					}
				}
			}
			// いなければ霊媒師coかつ、推理リストに入っていない人を護衛
			if (candidates2.isEmpty()) {
				for (Agent agent : aliveOthers) {
					if (comingoutMap.get(agent) == Role.MEDIUM) {
						for (int i = 1; i < GuessList.size(); i++) {
							if (!(GuessList.get(i).getwolfarray().contains(agent.getAgentIdx()))) {
								candidates2.add(agent);
							}
						}
					}
				}
			}
			// それでも見つからなければ推理リストに入っていないかつ、自分と人狼候補以外から護衛
			if (candidates2.isEmpty()) {
				for (Agent agent : aliveOthers) {
					if (!werewolves.contains(agent)) {
						for (int i = 1; i < GuessList.size(); i++) {
							if (!(GuessList.get(i).getwolfarray().contains(agent.getAgentIdx()))) {
								candidates2.add(agent);
							}
						}
					}
				}
			}
			// それでもいなければ推理リストに入っていないかつ、自分以外から護衛
			if (candidates2.isEmpty()) {
				for (Agent agent : aliveOthers) {
					for (int i = 1; i < GuessList.size(); i++) {
						if (!(GuessList.get(i).getwolfarray().contains(agent.getAgentIdx()))) {
							candidates2.add(agent);
						}
					}
				}
			}
			guardCandidate = randomSelect(candidates2);
			return guardCandidate;
		} else {
			boolean threeCandidate = false;
			Agent mediumPerson = null;
			// 前日の護衛が成功しているようなら同じエージェントを護衛
			if (guardedAgent != null && isAlive(guardedAgent) && currentGameInfo.getLastDeadAgentList().isEmpty()) {
				guardCandidate = guardedAgent;
			}
			// 新しい護衛先の選定
			{
				// 占い師をカミングアウトしていて，かつ人狼候補になっていないエージェントを探す
				List<Agent> candidates = new ArrayList<>();
				int seerNum = 0;
				int mediumNum = 0;
				for (Agent agent : aliveOthers) {
					if (comingoutMap.get(agent) == Role.SEER && !werewolves.contains(agent)) {
						candidates.add(agent);
						seerNum += 1;
					}
				}
				// 見つからなければ霊媒師をカミングアウトしていて，かつ人狼候補になっていないエージェントを探す
				if (candidates.isEmpty()) {
					for (Agent agent : aliveOthers) {
						if (comingoutMap.get(agent) == Role.MEDIUM && !werewolves.contains(agent)) {
							candidates.add(agent);
							mediumNum += 1;
							mediumPerson = agent;
						}
					}
				}
				// 占い師カミングアウトが２人以上いて、霊媒師カミングアウトが１人いる場合は霊媒師を守る
				if (seerNum >= 2 && mediumNum == 1) {
					threeCandidate = true;
				}
				// それでも見つからなければ自分と人狼候補以外から護衛
				if (candidates.isEmpty()) {
					for (Agent agent : aliveOthers) {
						if (!werewolves.contains(agent)) {
							candidates.add(agent);
						}
					}
				}
				// それでもいなければ自分以外から護衛
				if (candidates.isEmpty()) {
					candidates.addAll(aliveOthers);
				}
				// 護衛候補からランダムに護衛
				guardCandidate = randomSelect(candidates);
			}
			if (threeCandidate) {
				guardCandidate = mediumPerson;
				return guardCandidate;
			} else {
				return guardCandidate;
			}
		}
	}

}
