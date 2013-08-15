package dev.quizlearn.data;

public class SheetAnswer {
	String question;
	String answer;
	long answertime;

	public SheetAnswer(String question) {
		this.question = question;
		this.answer = null;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
		this.answertime = System.currentTimeMillis();
	}

	// @Override
	// public void LoadState(String savedStateString) {
	// SheetAnswer sh = new Gson().fromJson(savedStateString, SheetAnswer.class);
	// this.answer = sh.answer;
	// this.question = sh.question;
	// this.answertime = sh.answertime;
	// // question = savedState.getString("question");
	// // Log.i("load_app", "question=" + question);
	// // answer = savedState.getString("answer");
	// // Log.i("load_app", "answer=" + answer);
	// // answertime = savedState.getLong("answertime");
	// // Log.i("load_app", "answertime=" + answertime);
	// }
	//
	// @Override
	// public String SaveState() {
	// return new Gson().toJson(this);
	// // Log.i("save_app", "question=" + question);
	// // Log.i("save_app", "answer=" + answer);
	// // Log.i("save_app", "answertime=" + answertime);
	// // return "{question:\"" + question.replaceAll("\"", "\\\"") + "\"," + //
	// // "answer:\"" + answer.replaceAll("\"", "\\\"") + "\"," + //
	// // "answertime:" + answertime//
	// // + "}";
	// }
}
