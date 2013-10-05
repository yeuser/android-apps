package dev.quizlearn.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class OrderedKeyList<K, V> implements Iterable<V> {
	private ArrayList<V> objs = new ArrayList<V>();
	private HashMap<K, V> objsMap = new HashMap<K, V>();

	public V get(K key) {
		return objsMap.get(key);
	}

	public V get(int index) {
		return objs.get(index);
	}

	public Set<K> keySet() {
		return objsMap.keySet();
	}

	public Set<V> valueSet() {
		Set<V> ret = new HashSet<V>();
		for (V obj : objs) {
			ret.add(obj);
		}
		return ret;
	}

	public boolean put(K key, V value) {
		if (objsMap.get(key) != null)
			return false;
		objsMap.put(key, value);
		objs.add(value);
		return true;
	}

	public boolean put(int index, K key, V value) {
		if (objsMap.get(key) != null)
			return false;
		objsMap.put(key, value);
		objs.add(index, value);
		return true;
	}

	public int size() {
		return objs.size();
	}

	public void clear() {
		objs.clear();
		objsMap.clear();
	}

	@Override
	public Iterator<V> iterator() {
		return objs.iterator();
	}
}