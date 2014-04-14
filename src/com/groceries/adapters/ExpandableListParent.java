package com.groceries.adapters;

import java.util.ArrayList;

public class ExpandableListParent{

    private Object parent;
    private ArrayList<Object> parentChildren;


    public ExpandableListParent() {
    }
    public ExpandableListParent(Object parent, ArrayList<Object> parentChildren) {
        this.parent = parent;
        this.parentChildren = parentChildren;
    }

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public ArrayList<Object> getParentChildren() {
        return parentChildren;
    }

    public void setParentChildren(ArrayList<Object> parentChildren) {
        this.parentChildren = parentChildren;
    }
}