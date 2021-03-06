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
public abstract class SectionListAdapter<Section,Item> extends BaseAdapter implements AbsListView.OnScrollListener {

    private Map<Section, List<Item>> sectionListMap;
    private Collection<List<Item>> values; // get the vales in one collection
    private List<Section> sectionList;
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
        this.sectionList = populateSectionList();
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

        //if the sticky header option is used, then do not show the first header in the list.
        if (stickeyHeader != null && position == 0){
            viewGroup.getChildAt(0).setVisibility(View.GONE);
            //set the sticky header to the first header in the list
            stickeyHeader = getHeaderView(getSections().get(getSectionIndexForPosition(position)), stickeyHeader, viewGroup);
        }
        else if ( displaySectionHeaders ) {
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

    private List<Section> populateSectionList(){
        List<Section> res = new ArrayList<Section>(sectionListMap.keySet().size());

        for ( Section s : sectionListMap.keySet()){
            res.add(s);
        }

        return res;
    }

    //TODO need to redo this as make it a little more efficient.
    private List<Section> getSections() {
        return sectionList;
    }

        private int currentSectionHeaderIndex = 0;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // left blank
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if( stickeyHeader != null && visibleItemCount != 0) {

                // check of the current section index is different  than the section at the top of the list
                if( currentSectionHeaderIndex != getSectionIndexForPosition(firstVisibleItem)) {
                    // update the current section index to the section of the first item
                    currentSectionHeaderIndex = getSectionIndexForPosition(firstVisibleItem);

                    // update the sticky header to the new Section header object
                    stickeyHeader = getHeaderView(getSections().get(getSectionIndexForPosition(firstVisibleItem)),
                                                  stickeyHeader,
                                                  stickeyHeader.getRootView());
                }
            }
        }

}