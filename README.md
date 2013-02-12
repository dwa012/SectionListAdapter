## Section List Adapter ##

*Note: Still working on the documentation and a sticky header implementation*

This project was created to make a simpler and easier version of a sectioned ListView. I found several examples that were entirely to complicated to implement.

All you need to do is extend  `SectionListAdapter` in your own adapter and implement the following **two** (You read that correct only **TWO** methods !!!!!) methods: 

     View getHeaderView(Section section, View convertView, View parent)
     View getItemView(Item item, View convertView, View parent);

You may notice that these are similar to the 

     getView(int position, View convertView, View parent)

 method in the `Adapter` interface. You can use them just as you did in a normal `getView()` implementation. The main difference is that you do not need to be worried about the position of the Item  or Header that is being called. That is handled by the super class. You will have an easier time with just the Item and not the index.

The reason for not having to worry about the item index is because of how the super class in implemented. 

The `SectionListAdapter` has a special constructor:

     SectionListAdapter(Map<Section, List<Item>> items, Context context)

you **MUST** call the `super()` constructor in your subclass. The `Map` is really critical to how the `Adapter` works. This is why you get to only worry about the item and not the index. 

The class uses two generic types. The `Section` is the Object that represent the section object that will be used as the header for that section. The `Item` is a item in the `ListView` that will be displayed.

The type of `Map` that you use will determine the order that the sections are displayed. I suggest using the `LinkedHashMap` if you want to perserve insertion order for the sections. You will have to break up your items into separate `List`s and insert them into the map with the `Section` object as the  key.





    