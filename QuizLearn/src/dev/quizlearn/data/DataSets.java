package dev.quizlearn.data;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import android.os.Bundle;

public class DataSets implements QuizSets {
	/* Saved Datas */
	private Vector<QuizSheet> sheets = new Vector<QuizSheet>();
	private HashMap<String, QuizSheet> sheetMap = new HashMap<String, QuizSheet>();
	private HashMap<String, Vector<SheetAnswer>> answerMap = new HashMap<String, Vector<SheetAnswer>>();
	/* Semi-AI parameters */
	private final double constAttr1 = 10; // multiplier for scoring each sheet
	private final double constAttr2 = 0.8; // percent of being OK to go to next level
	private final int constAttr3 = 10; // number of accepted answer record for each word

	public DataSets(Vector<QuizSheet> sheets, Vector<SheetAnswer> answerdata) {
		for (QuizSheet quizSheet : sheets) {
			this.sheets.add(quizSheet);
			if (this.sheetMap.get(quizSheet.question) == null) {
				this.sheetMap.put(quizSheet.question, quizSheet);
			} else {
				System.err.println("Duplicate question found! " + quizSheet.question + " " + quizSheet.answer[quizSheet.correctAnswer]);
				System.err.println("Duplicate of : "
						+ this.sheetMap.get(quizSheet.question).answer[this.sheetMap.get(quizSheet.question).correctAnswer]);
			}
		}
		for (SheetAnswer anssht : answerdata) {
			if (answerMap.get(anssht.question) == null) {
				answerMap.put(anssht.question, new Vector<SheetAnswer>());
			}
			answerMap.get(anssht.question).add(anssht);
		}
		for (QuizSheet quizSheet : sheets) {
			this.calculateScore(quizSheet);
			this.calculateWeight(quizSheet);
		}
		this.closeTestSession();
	}

	public int getSheetCount() {
		return sheets.size();
	}

	public QuizSheet getSheetAt(int index) {
		return sheets.elementAt(index);
	}

	public QuizSheet getSheet(String question) {
		return sheetMap.get(question);
	}

	public QuizSheet getCurrent() {
		if (numAtList < 0)
			return null;
		return getSheet(answerList.elementAt(numAtList).question);
	}

	public int getCurrentIndex() {
		return numAtList;
	}

	public boolean moveForward() {
		if (++numAtList >= answerList.size()) {
			if (answerList.size() < this.getSheetCount()) {
				answerList.add(getNewSheet());
				return true;
			} else {
				numAtList--;
			}
		}
		return false;
	}

	public boolean moveBackward() {
		if (numAtList <= 0)
			return false;
		--numAtList;
		return true;
	}

	public String getUserAnswer() {
		return answerList.elementAt(numAtList).answer;
	}

	public void setUserAnswer(String text) {
		answerList.elementAt(numAtList).setAnswer(text);
		this.calculateScore(this.getCurrent());
		this.calculateWeight(this.getCurrent());
	}

	public QuizSheet getRandomSheet() {
		int r = (int) (System.currentTimeMillis() % this.getSheetCount());
		return this.getSheetAt(r);
	}

	public SheetAnswer getNewSheet() {
		if (answerList.size() == this.getSheetCount())
			return null;
		QuizSheet qs = this.getScoredRandomSheet();
		while (answerListMap.get(qs.question) != null) {
			qs = this.getScoredRandomSheet();
		}
		SheetAnswer sheetAnswer = new SheetAnswer(qs.question);
		answerListMap.put(qs.question, sheetAnswer);
		return sheetAnswer;
	}

	private double calculateScore(QuizSheet sh) {
		if (answerMap.get(sh.question) == null) {
			return 1000;
		}
		long currtime = System.currentTimeMillis();
		double score = 0;
		Vector<SheetAnswer> answerList = answerMap.get(sh.question);
		for (SheetAnswer shans : answerList) {
			if (sh.checkAnswer(shans.answer)) {
				score += Math.exp((shans.answertime - currtime) / 3.6E8);
			} else {
				score -= Math.exp((shans.answertime - currtime) / 3.6E8);
			}
		}
		return sh.score = score;
	}

	private double calculateWeight(QuizSheet sh) {
		return sh.weight = 1 - (1 / (1 + Math.exp(constAttr1 * sh.score)));
	}

	private QuizSheet getScoredRandomSheet() {
		String[] answerMapKeys = answerMap.keySet().toArray(new String[] {});
		if (sheetMap.size() >= answerMapKeys.length) {
			return getRandomSheet();
		}
		double tscore = 0, tw = 0;
		for (String key : answerMapKeys) {
			tscore += this.getSheet(key).score;
		}
		if (answerMapKeys.length < 5 || tscore / answerMapKeys.length > constAttr2) {
			return getRandomSheet();
		} else {
			double d = Math.exp(-(System.currentTimeMillis() % 20000) * 1.0E-4);
			for (String key : answerMapKeys) {
				if (sheetMap.get(getSheet(key).question) == null) {
					tw += this.getSheet(key).weight;
				}
			}
			int i = -1;
			while (d > 0 && i < answerMapKeys.length) {
				if (sheetMap.get(getSheet(answerMapKeys[++i]).question) == null) {
					d -= this.getSheet(answerMapKeys[i]).weight / tw;
				}
			}
			return this.getSheet(answerMapKeys[i]);
		}
	}

	public Vector<SheetAnswer> getAnswerRecordData() {
		Vector<SheetAnswer> answerData = new Vector<SheetAnswer>();
		Set<String> keys = answerMap.keySet();
		for (String key : keys) {
			answerData.addAll(answerMap.get(key));
		}
		return answerData;
	}

	private Vector<SheetAnswer> reorderAnswerList(Vector<SheetAnswer> answers) {
		Vector<SheetAnswer> ret = new Vector<SheetAnswer>();
		for (SheetAnswer answer : answers) {
			int j = 0;
			for (; j < ret.size() && ret.elementAt(j).answertime < answer.answertime; j++) {
			}
			ret.add(j, answer);
		}
		return ret;
	}

	private void saveSessionAnswers() {
		for (SheetAnswer answer : answerList) {
			if (answerMap.get(answer.question) == null) {
				answerMap.put(answer.question, new Vector<SheetAnswer>());
			}
			answerMap.get(answer.question).add(answer);
		}
		Set<String> keys = answerMap.keySet();
		for (String key : keys) {
			answerMap.put(key, reorderAnswerList(answerMap.get(key)));
		}
	}

	private void clearSessionAnswers() {
		answerList.clear();
		numAtList = -1;
	}

	private void trimAnswerData() {
		Set<String> keys = answerMap.keySet();
		for (String key : keys) {
			Vector<SheetAnswer> ansvec = answerMap.get(key);
			while (ansvec.size() > constAttr3) {
				ansvec.remove(0);
			}
		}
	}

	public void closeTestSession() {
		saveSessionAnswers();
		clearSessionAnswers();
		trimAnswerData();
	}

	/* Test Session Data */
	private HashMap<String, SheetAnswer> answerListMap = new HashMap<String, SheetAnswer>();
	private Vector<SheetAnswer> answerList = new Vector<SheetAnswer>();
	private int numAtList = -1;

	public void LoadState(Bundle savedState) {
		int len = savedState.getInt("length");
		numAtList = savedState.getInt("numAtList");
		for (int i = 0; i < len; i++) {
			String question = savedState.getString("question[" + i + "]");
			SheetAnswer sheetAnswer = new SheetAnswer(question);
			String answer = savedState.getString("answer[" + i + "]");
			sheetAnswer.answer = answer;
			long answertime = savedState.getLong("answertime[" + i + "]");
			sheetAnswer.answertime = answertime;
			answerList.add(sheetAnswer);
		}
	}

	public void SaveState(Bundle outState) {
		outState.putInt("length", answerList.size());
		outState.putInt("numAtList", numAtList);
		for (int i = 0; i < answerList.size(); i++) {
			outState.putString("question[" + i + "]", answerList.elementAt(i).question);
			outState.putString("answer[" + i + "]", answerList.elementAt(i).answer);
			outState.putLong("answertime[" + i + "]", answerList.elementAt(i).answertime);
		}
	}

	// @Override
	// public void LoadState(String savedStateString) {
	// DataSets ds = new Gson().fromJson(savedStateString, DataSets.class);
	// this.quizList = ds.quizList;
	// this.quizMap = ds.quizMap;
	// this.answerList = ds.answerList;
	// this.numAtList = ds.numAtList;
	// // int quizListLength = savedState.getInt("quizListLength");
	// // Log.i("load_app", "quizListLength=" + quizListLength);
	// // String notf = "quizListLength=" + quizListLength;
	// // for (int i = 0; i < quizListLength; i++) {
	// // Log.i("load_app", "Starting load for quiz sheet " + i);
	// // Bundle b = savedState.getBundle("quiz_sheet_" + i);
	// // notf += ",quiz_sheet_" + i;
	// // QuizSheet q = new QuizSheet();
	// // q.LoadState(b);
	// // quizList.add(q);
	// // quizMap.put(q.question, q);
	// // Log.i("load_app", "Starting load for sheet answer " + i);
	// // b = savedState.getBundle("quiz_sheet_answer_" + i);
	// // notf += ",quiz_sheet_answer_" + i;
	// // SheetAnswer sa = new SheetAnswer(q.question);
	// // sa.LoadState(b);
	// // answerList.add(sa);
	// // }
	// // numAtList = savedState.getInt("numAtList");
	// // Log.i("load_app", "numAtList=" + numAtList);
	// // notf += ",numAtList=" + numAtList;
	// // MainActivity.makeNotification(notf);
	// }
	//
	// @Override
	// public String SaveState() {
	// return new Gson().toJson(this);
	// // Log.i("save_app", "quizListLength=" + quizList.size());
	// // outState.putInt("quizListLength", quizList.size());
	// // String notf = "quizListLength=" + "quizListLength=" + quizList.size();
	// // int i = 0;
	// // for (QuizSheet quizSheet : quizList) {
	// // Log.i("save_app", "Starting save for quiz sheet " + i);
	// // Bundle b = new Bundle();
	// // quizSheet.SaveState(b);
	// // notf += ",quiz_sheet_" + i;
	// // outState.putBundle("quiz_sheet_" + i++, b);
	// // }
	// // i = 0;
	// // for (SheetAnswer sheetAnswer : answerList) {
	// // Log.i("save_app", "Starting save for sheet answer " + i);
	// // Bundle b = new Bundle();
	// // sheetAnswer.SaveState(b);
	// // notf += ",quiz_sheet_answer_" + i;
	// // outState.putBundle("quiz_sheet_answer_" + i++, b);
	// // }
	// // Log.i("save_app", "numAtList=" + numAtList);
	// // outState.putInt("numAtList", numAtList);
	// // notf += ",numAtList=" + numAtList;
	// // MainActivity.makeNotification(notf);
	// }
}
