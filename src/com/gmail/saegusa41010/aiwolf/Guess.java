package com.gmail.saegusa41010.aiwolf;

import java.util.ArrayList;

public class Guess { // 推理を保持しておくクラス

	private ArrayList<Integer> wolf = new ArrayList<Integer>(); // 狼候補をつっこむ

	private double score; // 推理に対するスコア

	public Guess(ArrayList<Integer> wolf) { // guessクラスのコンストラクタ
		this.wolf.add(null); // 1オリジンにする
		while (1 < wolf.size()) {
			this.wolf.add(wolf.remove(wolf.size() - 1)); // wolfの下から順番にwolfに突っ込んでいく
		}
		score = 0; // scoreは0点
	}

	double getscore() { // スコアを取得
		return score;
	}

	void plusscore(double a) { // スコアを加算
		score += a;
	}

	void minusscore(double a) { // スコアを減算
		score -= a;
	}

	ArrayList<Integer> getwolfarray() { // wolfリストを取得
		return wolf;
	}


	public void setscore(int score) {
		this.score = score;
		// TODO Auto-generated method stub

	}

}
