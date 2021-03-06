package com.tytanapps.ptsd.va;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * A RecyclerView that allows you to search items
 * @param <K> View holder
 * @param <T> Item that the list contains (ex. Facility)
 */
public abstract class SearchableAdapter<K extends RecyclerView.ViewHolder, T extends Searchable> extends RecyclerView.Adapter<K> {

    public static final int DISPLAY_DEFAULT = 10;

    protected List<T> list;
    protected List<T> listAll;

    protected int numToDisplay;

    public SearchableAdapter(List<T> list) {
        this(list, DISPLAY_DEFAULT);
    }

    public SearchableAdapter(List<T> list, int numToDisplay) {
        this.numToDisplay = numToDisplay;
        this.listAll = list;
        this.list = new ArrayList<>(list.subList(0, numToDisplay));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filter(@Nullable String text) {
        if (text != null) {
            text = text.toLowerCase().trim();
        }

        ArrayList<T> result = new ArrayList<>();
        for (T item: listAll) {
            if (item.search(text)) {
                result.add(item);
            }
        }

        list.clear();
        for (int i = 0; i < numToDisplay && i < result.size(); i++) {
            list.add(result.get(i));
        }
        notifyDataSetChanged();
    }


}