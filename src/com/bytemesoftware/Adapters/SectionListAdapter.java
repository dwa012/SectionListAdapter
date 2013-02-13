/*
 * Copyright 2013 Daniel Ward
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package com.bytemesoftware.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This an Adapter class that will facilitate sections with header for an Android {@link android.widget.AbsListView}.
 * There is no need for a custom ListView. You only need to use the standard ListView.
 *
 * To make the adapter work you will need the list items placed in a Map<Section, List<Item>>.
 * The Section is the Object that represents the header for a section. The value for the
 * Section key is the List of Items that are in that section.
 *
 * The concrete Map that you chose will affect the ordering of the sections. This is due to the
 * nature of how the map is implemented. I suggest that you use the LinkedHashMap to maintain
 * insertion order.
 *
 * (Note : if you use a TreeMap you should get natursectionListMapy ordered sections for free.)
 *
 * User: Daniel Ward ( dwa012@gmail.com )
 * Date: 2/11/13
 */
public abstract class SectionListAdapter<Section,Item> extends BaseAdapter {

    private Map<Section, List<Item>> sectionListMap;
    private Collection<List<Item>> values; // get the vales in one collection
    private LayoutInflater li;
    private Context context;
    
    private View stickeyHeader;

    public SectionListAdapter(Map<Section, List<Item>> items, Context context) {
        this(null, items,context);
    }

    public SectionListAdapter(View stickyHeader, Map<Section, List<Item>> items, Context context) {
        this.stickeyHeader = stickyHeader;
        this.sectionListMap = items;
        this.context = context;
        this.values = sectionListMap.values();
    }

    // Methods that need to be implemented in a subclass. This is all that needs to be implemented

    /**
     * Used to implement the Section header creation view.
     *
     * @param section The Section Object to be used to create the view
     * @param convertView See <a href="http://developer.android.com/reference/android/widget/Adapter.html#getView(int, android.view.View, android.view.ViewGroup)">android.widget.Adapter</a>
     * @param parent See <a href="http://developer.android.com/reference/android/widget/Adapter.html#getView(int, android.view.View, android.view.ViewGroup)">android.widget.Adapter</a>
     * @return View The inflated/populated view
     */
    protected abstract View getHeaderView(Section section, View convertView, View parent);

    protected abstract View getItemView(Item item, View convertView, View parent);

    // Methods inherited from BaseAdapter

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        int count = 0;

        for (List<Item> list : values){
            count += list.size();
        }

        return count;
    }

    @Override
    public Item getItem(int position) {
        int currentIndex = 0;
        Item result = null;

        for (List<Item> list : values){
            if (position >= currentIndex && ( position < ( currentIndex + list.size() ) )) {
                result = list.get(position - currentIndex);
                break; // using a break to exit the for each loop early. I have to use this loop with the map.
            }

            currentIndex += list.size();
        }

        return result;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        // setup the view

        if( convertView == null) {
            //create a new container view for the header and item
            LinearLayout containerView = new LinearLayout(context);
            containerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                    AbsListView.LayoutParams.WRAP_CONTENT));
            containerView.setOrientation(LinearLayout.VERTICAL);

            containerView.addView(getHeaderView(getSections().get(getSectionIndexForPosition(position)), null, containerView));
            containerView.addView(getItemView(getItem(position), null, containerView));

            convertView = containerView;
        }   else {
            LinearLayout layout = (LinearLayout) convertView;
            getHeaderView(getSections().get(getSectionIndexForPosition(position)), layout.getChildAt(0), layout);
            getItemView(getItem(position), layout.getChildAt(1), layout);
        }

        // see if the header needs to be shown
        bindSectionHeader(position, (ViewGroup)convertView);


        return convertView;
    }

    // Helper methods to make the whole thing work.

    private void bindSectionHeader(int position, ViewGroup viewGroup) {
        final int section = getSectionIndexForPosition(position);
        boolean displaySectionHeaders = (getPositionIndexForSection(section) == position);

        if ( displaySectionHeaders ) {
            viewGroup.getChildAt(0).setVisibility(View.VISIBLE);
        } else {
            viewGroup.getChildAt(0).setVisibility(View.GONE);
        }
    }

    private int getPositionIndexForSection(int section) {
        int resultIndex = 0;

        if (section < 0)
            section = 0;

        if (section >= values.size())
            section = values.size() - 1;

        int i = 0;
        for (List<Item> list : values) {
            if (section == i)
                break; // hate to use break, but need to use the for each loop here

            resultIndex += list.size(); i = i + 1;
        }

        return resultIndex;
    }

    private int getSectionIndexForPosition(int position) {
        int resultSection = 0;
        int currentIndex = 0;

        for (List<Item> list : values){
            if (position >= currentIndex && position < currentIndex + list.size())
                break;

            currentIndex += list.size(); resultSection = resultSection + 1;
        }

        return resultSection;
    }

    //TODO need to redo this as make it a little more efficient.
    private List<Section> getSections() {
        List<Section> res = new ArrayList<Section>(sectionListMap.keySet().size());

        for ( Section s : sectionListMap.keySet()){
            res.add(s);
        }

        return res;
    }

}