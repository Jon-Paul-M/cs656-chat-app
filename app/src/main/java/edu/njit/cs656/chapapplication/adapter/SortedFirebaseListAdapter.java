package edu.njit.cs656.chapapplication.adapter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.google.firebase.database.DatabaseReference;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by jon-paul on 11/16/17.
 */

public abstract class SortedFirebaseListAdapter<T> extends FirebaseListAdapter<T> {
  private final ObservableSnapshotArray<T> sortedSnapshots;

  public SortedFirebaseListAdapter(FirebaseListOptions<T> options, Comparator<T> comparator) {
    super(options);
    sortedSnapshots = options.getSnapshots();
    Collections.sort(sortedSnapshots, comparator);
  }

  @Override
  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  public void startListening() {
    if (!sortedSnapshots.isListening(this)) {
      sortedSnapshots.addChangeEventListener(this);
    }
  }

  @Override
  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  public void stopListening() {
    sortedSnapshots.removeChangeEventListener(this);
    notifyDataSetChanged();
  }

  @Override
  public ObservableSnapshotArray<T> getSnapshots() {
    return sortedSnapshots;
  }

  @Override
  public T getItem(int position) {
    return sortedSnapshots.get(position);
  }

  @Override
  public DatabaseReference getRef(int position) {
    return sortedSnapshots.getSnapshot(position).getRef();
  }

  @Override
  public int getCount() {
    return sortedSnapshots.size();
  }

  @Override
  public long getItemId(int i) {
    // http://stackoverflow.com/questions/5100071/whats-the-purpose-of-item-ids-in-android-listview-adapter
    return sortedSnapshots.getSnapshot(i).getKey().hashCode();
  }

}
