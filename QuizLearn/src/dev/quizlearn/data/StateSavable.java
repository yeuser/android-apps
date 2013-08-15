package dev.quizlearn.data;

import android.os.Bundle;

public interface StateSavable {
	public void LoadState(Bundle savedState);

	public void SaveState(Bundle outState);
}
