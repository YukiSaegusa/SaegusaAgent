package com.gmail.saegusa41010.aiwolf;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/** すべての役職のベースとなるクラス */
public class SaegusaBase implements Player {
	/** このエージェント */
	Agent me;
	/** 日付 */
	int day;

	/** talk()できるか時間帯か */
	boolean canTalk;
	/** whisper()できるか時間帯か */
	boolean canWhisper;
	/** 最新のゲーム情報 */
	GameInfo currentGameInfo;
	/** 最新のゲーム情報設定 */
	GameSetting gamesetting;
	/** 自分以外の生存エージェント */
	List<Agent> aliveOthers;
	/** 追放されたエージェント */
	List<Agent> executedAgents = new ArrayList<>();
	/** 殺されたエージェント */
	List<Agent> killedAgents = new ArrayList<>();
	/** 発言された占い結果報告のリスト */
	List<Judge> divinationList = new ArrayList<>();
	/** 発言された霊媒結果報告のリスト */
	List<Judge> identList = new ArrayList<>();
	/** 発言用待ち行列 */
	Deque<Content> talkQueue = new LinkedList<>();
	/** 囁き用待ち行列 */
	Deque<Content> whisperQueue = new LinkedList<>();
	/** 投票先候補 */
	Agent voteCandidate;
	/** 宣言済み投票先候補 */
	Agent declaredVoteCandidate;
	/** 襲撃投票先候補 */
	Agent attackVoteCandidate;

	/** agentリスト */
	List<Agent> AgentList;
	/** 宣言済み襲撃投票先候補 */
	Agent declaredAttackVoteCandidate;
	/** カミングアウト状況 */
	Map<Agent, Role> comingoutMap = new HashMap<>();
	/** GameInfo.talkList読み込みのヘッド */
	int talkListHead;
	/** 人間リスト */
	List<Agent> humans = new ArrayList<>();
	/** 人狼リスト */
	List<Agent> werewolves = new ArrayList<>();

	/** 人狼の数を数える */
	List<Role> RoleList;
	// = currentGameInfo.getExistingRoles();
	int numWolf; // 人狼の数
	/** エージェントの数を数える */
	int numAgent;
	// 現在、占い師と名乗っている人の数
	int alivenumSeer;
	// 現在、霊媒師と名乗っている人の数
	int alivenumMedium;
	// 死者含め占いと名乗っていた人数
	int numSeer;
	// 死者含め霊媒と名乗っていた数
	int numMedium;

	//
	Guess maxGuess = null;
	List<Agent> Seerlist = new ArrayList<>();
	List<Agent> Mediumlist = new ArrayList<>();
	List<Agent> Bodyguardlist = new ArrayList<>();
	List<Agent> Choosefirst = aliveOthers;

	/** 推理を全パターンまとめてリストにいれる */
	List<Guess> GuessList = new ArrayList<>();
	/** 個人の推理 */
	int[] IdGuessList;

	/** エージェントが生きているかどうかを返す */
	protected boolean isAlive(Agent agent) {
		return currentGameInfo.getStatusMap().get(agent) == Status.ALIVE;
	}

	/** エージェントが殺されたかどうかを返す */
	protected boolean isKilled(Agent agent) {
		return killedAgents.contains(agent);
	}

	/** エージェントがカミングアウトしたかどうかを返す */
	protected boolean isCo(Agent agent) {
		return comingoutMap.containsKey(agent);
	}

	/** 役職がカミングアウトされたかどうかを返す */
	protected boolean isCo(Role role) {
		return comingoutMap.containsValue(role);
	}

	/** エージェントが人間かどうかを返す */
	protected boolean isHuman(Agent agent) {
		return humans.contains(agent);
	}

	/** エージェントが人狼かどうかを返す */
	protected boolean isWerewolf(Agent agent) {
		return werewolves.contains(agent);
	}

	/** リストからランダムに選んで返す */
	protected <T> T randomSelect(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	public String getName() {
		return "SaegusaBasePlayer";
	}

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {

		// guessリストを作る
		RoleList = gameInfo.getExistingRoles();
		day = -1;
		gamesetting = gameSetting;
		me = gameInfo.getAgent();
		aliveOthers = new ArrayList<>(gameInfo.getAliveAgentList());
		aliveOthers.remove(me);
		numWolf = gamesetting.getRoleNum(Role.WEREWOLF);
		numAgent = gamesetting.getPlayerNum();
		executedAgents.clear();
		killedAgents.clear();
		divinationList.clear();
		identList.clear();
		comingoutMap.clear();
		humans.clear();
		werewolves.clear();
		GuessList.clear();
		alivenumSeer = 0;
		alivenumMedium = 0;
		numSeer = 0;
		numMedium = 0;

		AgentList = gameInfo.getAgentList();
		GuessList.add(null); // 1オリジンにする
		IdGuessList = new int[numAgent + 1]; // 個人推理リストの初期化
		for (int i = 1; i < IdGuessList.length; i++) {
			IdGuessList[i] = 0;
		}
		/**
		 * 以降GuessListの初期化
		 */
		for (int i = 1; i <= numAgent; i++) {
			for (int j = 1; j <= numAgent; j++) {
				if (j == i) {
					continue;
				}
				for (int k = 1; k <= numAgent; k++) {
					if (j == k || i == k) {
						continue;
					}

					ArrayList<Integer> wolf = new ArrayList<>();
					wolf.add(null);
					wolf.add(i);
					wolf.add(j);
					wolf.add(k);

					Guess g1 = new Guess(wolf);
					GuessList.add(g1);
				}
			}
		}
		// 自分が人狼のリストは削除
		for (int i = 1; i < GuessList.size(); i++) {
			if (GuessList.get(i).getwolfarray().contains(me.getAgentIdx())) {
				GuessList.remove(i);
			}
		}
	}

	public void update(GameInfo gameInfo) {
		currentGameInfo = gameInfo;
		// 1日の最初の呼び出しはdayStart()の前なので何もしない
		if (currentGameInfo.getDay() == day + 1) {
			day = currentGameInfo.getDay();
			return;
		}
		// 2回目の呼び出し以降
		// （夜限定）追放されたエージェントを登録

		// GameInfo.talkListからカミングアウト・占い報告・霊媒報告を抽出,占い結果に応じてリストも更新
		for (int i = talkListHead; i < currentGameInfo.getTalkList().size(); i++) {
			Talk talk = currentGameInfo.getTalkList().get(i);
			Agent talker = talk.getAgent();
			if (talker == me) {
				continue;
			}
			Content content = new Content(talk.getText());
			switch (content.getTopic()) {
			case COMINGOUT:
				comingoutMap.put(talker, content.getRole());
				break;
			case DIVINED:
				divinationList.add(new Judge(day, talker, content.getTarget(), content.getResult()));
				// 占い結果よりありえない結果を排除
				for (int i2 = 1; i2 < GuessList.size(); i2++) {
					if (!(GuessList.get(i2).getwolfarray().contains(talker.getAgentIdx()))) { // 占い師が白の推理において
						if ((content.getResult() == Species.WEREWOLF
								&& (!(GuessList.get(i2).getwolfarray().contains(content.getTarget().getAgentIdx()))))
								|| (content.getResult() == Species.HUMAN && GuessList.get(i2).getwolfarray()
										.contains(content.getTarget().getAgentIdx()))) { // 占いと矛盾してたら除外
							GuessList.remove(i2);
						}
					}
				}
				break;
			case IDENTIFIED:
				identList.add(new Judge(day, talker, content.getTarget(), content.getResult()));
				break;
			default:
				break;
			}
		}
		talkListHead = currentGameInfo.getTalkList().size();
		// 占いcoと霊媒coの人数を計算
		alivenumSeer = 0;
		alivenumMedium = 0;
		numMedium = 0;
		numSeer = 0;
		for (Agent agent : AgentList) {
			if (comingoutMap.get(agent) == Role.MEDIUM) {
				numMedium += 1;
				if (aliveOthers.contains(agent)) {
					alivenumMedium += 1;
				}
			}
			if (comingoutMap.get(agent) == Role.SEER) {
				numSeer += 1;
				if (aliveOthers.contains(agent)) {
					alivenumSeer += 1;
				}
			}
		}
		// guessリストからあり得ないものを削除
		Guess_dele();

		addExecutedAgent(currentGameInfo.getLatestExecutedAgent());

	}

	public void dayStart() {
		canTalk = true;
		canWhisper = false;
		if (currentGameInfo.getRole() == Role.WEREWOLF) {
			canWhisper = true;
		}
		talkQueue.clear();
		whisperQueue.clear();
		declaredVoteCandidate = null;
		voteCandidate = null;
		declaredAttackVoteCandidate = null;
		attackVoteCandidate = null;
		talkListHead = 0;
		// 前日に追放されたエージェントを登録
		addExecutedAgent(currentGameInfo.getExecutedAgent());
		// 昨夜死亡した（襲撃された）エージェントを登録
		if (!currentGameInfo.getLastDeadAgentList().isEmpty()) {
			addKilledAgent(currentGameInfo.getLastDeadAgentList().get(0));
		}
		// 昨夜死亡したエージェントが人狼のパターンは削除
		for (int i = 1; i < GuessList.size(); i++) {
			if (currentGameInfo.getExecutedAgent() != null) {
				if (GuessList.get(i).getwolfarray().contains(currentGameInfo.getExecutedAgent().getAgentIdx())) {
					GuessList.remove(i);
				}
			}
		}
		// 占いカミングアウトが襲撃された場合残りの占い師カミングアウトは全員黒
		// if (comingoutMap.size() > 0) {
		// if (currentGameInfo.getLastDeadAgentList().size() != 0) {
		//
		// if (comingoutMap.get(currentGameInfo.getLastDeadAgentList().get(0)) ==
		// Role.SEER) {
		// for (Guess g : GuessList) {
		// if (g == null) {
		// continue;
		// }
		// for (int k = 1; k < AgentList.size(); k++) {
		// if (comingoutMap.get(AgentList.get(k)) == Role.SEER &&
		// isAlive(AgentList.get(k))) { // 魔法使いカミングアウトで生きてるやつ
		// if (!(g.getwolfarray().contains(AgentList.get(k).getAgentIdx())
		// || g.getpossessedarray().contains(AgentList.get(k).getAgentIdx()))) {
		// GuessList.remove(g);
		// }
		// }
		// }
		// }
		// }
		// }
		// }

	}

	private void addExecutedAgent(Agent executedAgent) {
		if (executedAgent != null) {
			aliveOthers.remove(executedAgent);
			if (!executedAgents.contains(executedAgent)) {
				executedAgents.add(executedAgent);
			}
		}
	}

	private void addKilledAgent(Agent killedAgent) {
		if (killedAgent != null) {
			aliveOthers.remove(killedAgent);
			if (!killedAgents.contains(killedAgent)) {
				killedAgents.add(killedAgent);
			}
		}
	}

	/** 投票先候補を選びvoteCandidateにセットする */
	protected void chooseVoteCandidate() {
	}

	public String talk() {
		// エラー出る
		if (voteCandidate != null && voteCandidate != declaredVoteCandidate) {
			talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
			declaredVoteCandidate = voteCandidate;
		}
		return talkQueue.isEmpty() ? Talk.SKIP : talkQueue.poll().getText();
	}

	/** 襲撃先候補を選びattackVoteCandidateにセットする */
	protected void chooseAttackVoteCandidate() {
	}

	public String whisper() {
		chooseAttackVoteCandidate();
		if (attackVoteCandidate != null && attackVoteCandidate != declaredAttackVoteCandidate) {
			whisperQueue.offer(new Content(new AttackContentBuilder(attackVoteCandidate)));
			declaredAttackVoteCandidate = attackVoteCandidate;
		}
		return whisperQueue.isEmpty() ? Talk.SKIP : whisperQueue.poll().getText();
	}

	public Agent vote() {
		canTalk = false;
		chooseVoteCandidate();
		if (voteCandidate != null) {
			return voteCandidate;
		} else {
			return randomSelect(aliveOthers);
		}

	}

	public Agent attack() {
		canWhisper = false;
		chooseAttackVoteCandidate();
		canWhisper = true;
		return attackVoteCandidate;
	}

	public int co_num(Role r) {
		int sum = 0;
		for (Agent agent : aliveOthers) {
			if (comingoutMap.get(agent) == r) {
				numAgent += 1;
			}
		}
		return sum;
	}

	// guessリストからあり得ないものを省いていく
	public void Guess_dele() {
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
		//2-2、3-1、1-3はcoしていない人しかいないguessリストが除かれる
		if ((alivenumSeer == 2 && alivenumMedium == 2)||(alivenumSeer == 3 && alivenumMedium ==1)||(alivenumSeer == 1 && alivenumMedium ==3)) {
			// coなしがいるグループは削除
			int match_num = 0;
			for (int i = 1; i < GuessList.size(); i++) {
				for (int j = 1 ; j < GuessList.get(i).getwolfarray().size() ;j++ ) {
					int id = GuessList.get(i).getwolfarray().get(j);
					if ((Seerlist.size() == 2 && Mediumlist.size() == 2)||(Seerlist.size() == 3 && Mediumlist.size() == 1) ||(Seerlist.size() == 1 && Mediumlist.size() == 3)) {
						for(Agent seer : Seerlist) {
							if(seer.getAgentIdx() == id) {
								match_num += 1;
							}
						}
						for(Agent midium : Mediumlist) {
							if(midium.getAgentIdx() == id) {
								match_num += 1;
							}
						}
					}
				}
				if (match_num == 0) {
					GuessList.remove(i);
				}
			}
		}
		//3-2陣形の時はcoしていない人のみのguessと、coが一人しか含まれていないguessを削除
		if(alivenumSeer == 3 && alivenumMedium == 2 ) {
			int match_num = 0;
			for (int i = 1; i < GuessList.size(); i++) {
				for (int j = 1 ; j < GuessList.get(i).getwolfarray().size() ;j++ ) {
					int id = GuessList.get(i).getwolfarray().get(j);
					if (Seerlist.size() == 3 && Mediumlist.size() == 2) {
						for(Agent seer : Seerlist) {
							if(seer.getAgentIdx() == id) {
								match_num += 1;
							}
						}
						for(Agent midium : Mediumlist) {
							if(midium.getAgentIdx() == id) {
								match_num += 1;
							}
						}
					}
				}
				if (match_num == 0 || match_num ==1) {
					GuessList.remove(i);
				}
			}
		}
	}

	public Agent divine() {
		return null;
	}

	public Agent guard() {
		return null;
	}

	public void finish() {
	}

}
