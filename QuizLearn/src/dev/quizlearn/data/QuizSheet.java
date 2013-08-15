package dev.quizlearn.data;

public class QuizSheet  {
	public String question;
	public String[] answer;
	public byte correctAnswer = 0;
	public double score;
	public double weight;
	public float level;

	public boolean checkAnswer(String useranswer) {
		return answer[correctAnswer].equalsIgnoreCase(useranswer);
	}

	public String getCorrectAnswer() {
		return answer[correctAnswer];
	}

	@Override
	public String toString() {
		String answerstr = "";
		for (int i = 0; i < answer.length; i++) {
			answerstr += "," + answer[i];
		}
		return "{\n\tquestion:" + question + ",\n\tanswer:{" + answerstr.substring(1) + "},\n\tcorrect:" + correctAnswer + ",\n\tlevel:"
				+ level + "\n}";
	}

	// @Override
	// public void LoadState(String savedStateString) {
	// QuizSheet sheet =new Gson().fromJson(savedStateString, QuizSheet.class);
	// this.question=sheet.question;
	// this.answer=sheet.answer;
	// this.correctAnswer=sheet.correctAnswer;
	// this.level=sheet.level;
	// this.score=sheet.score;
	// this.weight=sheet.weight;
	// // question = savedState.getString("question");
	// // Log.i("load_app", "question=" + question);
	// // answer = savedState.getStringArray("answer");
	// // Log.i("load_app", "answer=" + answer);
	// // correctAnswer = savedState.getByte("correctAnswer");
	// // Log.i("load_app", "correctAnswer=" + correctAnswer);
	// // score = savedState.getDouble("score");
	// // Log.i("load_app", "score=" + score);
	// // weight = savedState.getDouble("weight");
	// // Log.i("load_app", "weight=" + weight);
	// // level = savedState.getFloat("level");
	// // Log.i("load_app", "level=" + level);
	// }
	//
	// @Override
	// public String SaveState() {
	// return new Gson().toJson(this);
	// // Log.i("save_app", "question=" + question);
	// // outState.putString("question", question);
	// // Log.i("save_app", "answer=" + answer);
	// // outState.putStringArray("answer", answer);
	// // Log.i("save_app", "correctAnswer=" + correctAnswer);
	// // outState.putByte("correctAnswer", correctAnswer);
	// // Log.i("save_app", "score=" + score);
	// // outState.putDouble("score", score);
	// // Log.i("save_app", "weight=" + weight);
	// // outState.putDouble("weight", weight);
	// // Log.i("save_app", "level=" + level);
	// // outState.putFloat("level", level);
	// }
}
