package com.gmail.saegusa41010.aiwolf;

import java.util.ArrayList;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinationContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

public class SaegusaVillager extends SaegusaBase {

	// TODO Auto-generated constructor stub
	protected void chooseVoteCandidate() {
		// 初日の動き
		if (currentGameInfo.getDay() == 1) {
			// 占い、霊媒、ボディーガード
			Seerlist = new ArrayList<>();
			Mediumlist = new ArrayList<>();
			Bodyguardlist = new ArrayList<>();
			Choosefirst = aliveOthers;
			for (Agent a : aliveOthers) {
				if (comingoutMap.get(a) == Role.SEER) {
					Seerlist.add(a);
				} else if (comingoutMap.get(a) == Role.MEDIUM) {
					Mediumlist.add(a);
				} else if (comingoutMap.get(a) == Role.BODYGUARD) {
					Bodyguardlist.add(a);
				}
			}
			// 霊媒０
			if (alivenumMedium == 0) {
				if (alivenumSeer > 1) {
					voteCandidate = randomSelect(Seerlist);
				} else if (alivenumSeer == 1) {
					Choosefirst.remove(Choosefirst.indexOf(Seerlist.get(0)));
					for (Judge j : divinationList) {
						if (j.getResult() == Species.HUMAN && Choosefirst.indexOf(j.getTarget()) != -1) {
							Choosefirst.remove(Choosefirst.indexOf(j.getTarget()));
						} else if (j.getResult() == Species.WEREWOLF && Choosefirst.indexOf(j.getTarget()) != -1) {
							Choosefirst.clear();
							Choosefirst.add(j.getTarget());
						}
					}
					voteCandidate = randomSelect(Choosefirst);
				} else {
					voteCandidate = randomSelect(Choosefirst);
				}
				// 霊媒１
			} else if (alivenumMedium == 1) {
				Choosefirst.remove(Mediumlist.get(0));
				if (alivenumSeer > 2) {
					randomSelect(Seerlist);
				} else if (alivenumSeer == 2) {
					Choosefirst.remove(Seerlist.get(0));
					Choosefirst.remove(Seerlist.get(1));
					for (Judge j : divinationList) {
						if (j.getResult() == Species.HUMAN && Choosefirst.indexOf(j.getTarget()) != -1) {
							Choosefirst.remove(Choosefirst.indexOf(j.getTarget()));
						} else {
							Choosefirst.clear();
							Choosefirst.add(j.getTarget());
						}
					}
					voteCandidate = randomSelect(Choosefirst);
				} else if (alivenumSeer == 1) {
					Choosefirst.remove(Choosefirst.indexOf(Seerlist.get(0)));
					for (Judge j : divinationList) {
						if (j.getResult() == Species.HUMAN && Choosefirst.indexOf(j.getTarget()) != -1) {
							Choosefirst.remove(Choosefirst.indexOf(j.getTarget()));
						} else if (j.getResult() == Species.WEREWOLF && Choosefirst.indexOf(j.getTarget()) != -1) {
							Choosefirst.clear();
							Choosefirst.add(j.getTarget());
						}
					}
					voteCandidate = randomSelect(Choosefirst);
				} else if (alivenumSeer == 0) {
					voteCandidate = randomSelect(Choosefirst);
				}
			}
			// 霊媒２
			else if (alivenumMedium >= 2) {
				if (alivenumSeer > 2) {
					for (Agent agent : Seerlist) {
						Choosefirst.remove(agent);
					}
					for (Agent agent : Mediumlist) {
						Choosefirst.remove(agent);
					}
					voteCandidate = Choose3_2();
				} else if (alivenumSeer == 2) {
					voteCandidate = randomSelect(Mediumlist);
				} else if (alivenumSeer == 1) {
					Choosefirst.remove(Choosefirst.indexOf(Seerlist.get(0)));
					for (Judge j : divinationList) {
						if (j.getResult() == Species.HUMAN && Choosefirst.indexOf(j.getTarget()) != -1) {
							Choosefirst.remove(Choosefirst.indexOf(j.getTarget()));
						} else {
							Choosefirst.clear();
							Choosefirst.add(j.getTarget());
						}
					}
					voteCandidate = randomSelect(Choosefirst);
				} else {
					voteCandidate = randomSelect(Mediumlist);
				}
			}
			if (canTalk) {
				talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
				talkQueue.offer(new Content(new RequestContentBuilder(null,
						new Content(new DivinationContentBuilder(voteCandidate)))));
			}
		} else {

			voteCandidate = randomSelect(aliveOthers);
			werewolves.clear(); // 人狼リストをクリア
			/**
			 * for(Judge j : divinationList) { //占いリストを見ていく
			 * if(j.getResult()==Species.WEREWOLF && (j.getTarget() == me ||
			 * isKilled(j.getTarget()))) { Agent candidate = j.getAgent();
			 * if(isAlive(candidate) && !werewolves.contains(candidate)) {
			 * werewolves.add(candidate); } } }
			 */

			// 死んだ人は人狼でない
			if (currentGameInfo.getLastDeadAgentList() != null) {
				for (Agent a : currentGameInfo.getLastDeadAgentList()) {
					IdGuessList[a.getAgentIdx()] -= 100000;
				}
			}
			for (Judge j : divinationList) { // 占いリストを見ていく
				if (j.getResult() == Species.WEREWOLF && (j.getTarget() == me || isKilled(j.getTarget()))) {
					IdGuessList[j.getAgent().getAgentIdx()] += 10000;
				}
				// 占い結果が黒って出た人は50点占い師が死んでたら100点
				// 占い結果が白って出た人は10下げ占い師が死んでたら-100点
				if (j.getResult() == Species.HUMAN) {
					IdGuessList[j.getTarget().getAgentIdx()] -= 10;
				}
				if (j.getResult() == Species.WEREWOLF) {
					IdGuessList[j.getTarget().getAgentIdx()] += 50;
				}
				// 占い師カミングアウトと霊媒師の意見が一致する場合は信用（対抗カミングアウトがいなかったら）
				for (Judge idenj : identList) {
					if (j.getResult() == idenj.getResult() && (numSeer == 1 || numMedium == 1)) {
						IdGuessList[j.getAgent().getAgentIdx()] -= 10000;
						IdGuessList[idenj.getAgent().getAgentIdx()] -= 10000;
					}
				}
			}
			// 霊媒複数ならちょっと疑う
			if (numMedium > 1) {
				for (Agent a : aliveOthers) {
					if (comingoutMap.get(a) == Role.MEDIUM) {
						IdGuessList[a.getAgentIdx()] += 5;
					}
				}
			}
			for (Judge j : identList) {
				if (j.getResult() == Species.HUMAN) {
					for (Judge j2 : divinationList) {
						if (j2.getResult() == Species.WEREWOLF) {
							// 霊媒師が白ていってるのに黒って言ってる占い師は100点(霊媒一人なら100信じる)二人以上なら50
							if (numMedium == 1) {
								IdGuessList[j2.getAgent().getAgentIdx()] += 100;
							} else {
								IdGuessList[j2.getAgent().getAgentIdx()] += 50;
							}
						}
					}
				}
			}
			// 占い師カミングアウトが襲撃された際、残りの占い師カミングアウトが一人の場合は間違いなく黒

			// if (comingoutMap.get(executedAgents.get(executedAgents.size())) == Role.SEER)
			// {
			// for (Agent a : aliveOthers) {
			// if (comingoutMap.get(a) == Role.SEER) {
			// IdGuessList[a.getAgentIdx()] += 10000;
			// }
			// }
			// }
			// 霊媒師カミングアウトが襲撃された際、残りの霊媒師カミングアウトが一人の場合は間違いなく黒
			// if (comingoutMap.get(executedAgents.get(executedAgents.size())) ==
			// Role.MEDIUM) {
			// for (Agent a : aliveOthers) {
			// if (comingoutMap.get(a) == Role.MEDIUM) {
			// IdGuessList[a.getAgentIdx()] += 10000;
			// }
			// }
			// }

			int maxscore = 0; // 一番ありえそうな推理を採用
			maxGuess = null;

			for (Guess g : GuessList) { // 推理リストを順番に見ていき、点数をつける
				if (g == null) {
					continue;
				}
				int score = 0; // guessのスコアを付けていく
				if (g.getwolfarray() != null || g != null) {
					for (int i = 0; i < g.getwolfarray().size(); i++) {
						if (i == 0) {
							continue;
						}
						score += IdGuessList[i]; // 個人推理の結果を足していく
						// 人狼同士が投票してたらそのリストは減点
					}
				}

				// for (int p : g.getpossessedarray()) {
				// score += IdGuessList[p]; // 個人推理の結果を足していく
				// }

				g.setscore(score);
				if (score > maxscore) {
					maxscore = score;
					maxGuess = g;
				}
			}

			if (maxGuess != null) { // nullじゃなければwerewolvesリストを更新
				Agent maxwolf = Agent.getAgent(maxGuess.getwolfarray().get(1));
				int maxwolfscore = IdGuessList[maxGuess.getwolfarray().get(1)];
				for (int i = 1; i < maxGuess.getwolfarray().size(); i++) {
					werewolves.add(AgentList.get(i));
					if(maxwolfscore < IdGuessList[maxGuess.getwolfarray().get(i)]) {
						maxwolf = Agent.getAgent(maxGuess.getwolfarray().get(1));
						maxwolfscore = IdGuessList[maxGuess.getwolfarray().get(i)];
					}
				}
				voteCandidate = maxwolf;
			} else if (werewolves.isEmpty()) {
				if (!aliveOthers.contains(voteCandidate)) {
					voteCandidate = randomSelect(aliveOthers);
				}
			} else {
				if (!werewolves.contains(voteCandidate)) {
					voteCandidate = randomSelect(werewolves);
					// 前回の投票と変わる場合
					if (canTalk) {
						talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
						talkQueue.offer(new Content(new RequestContentBuilder(null,
								new Content(new DivinationContentBuilder(voteCandidate)))));
					}
				}
			}
			if (voteCandidate == null) {
				voteCandidate = randomSelect(aliveOthers);
			}
		}
	}

	public String whisper() {
		throw new UnsupportedOperationException();
	}

	public Agent attack() {
		throw new UnsupportedOperationException();
	}

	public Agent divine() {
		throw new UnsupportedOperationException();
	}

	public Agent guard() {
		throw new UnsupportedOperationException();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// 自作関数
	// 3-2進行
	public Agent Choose3_2() {
		for (Judge j : divinationList) {
			if (j.getResult() == Species.WEREWOLF) {
				return j.getAgent();
			} else if (j.getResult() == Species.HUMAN) {
				Choosefirst.remove(j.getTarget());
			}
		}
		return randomSelect(Choosefirst);
	}
}
