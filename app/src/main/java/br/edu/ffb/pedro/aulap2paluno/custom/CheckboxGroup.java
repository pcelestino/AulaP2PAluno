package br.edu.ffb.pedro.aulap2paluno.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class CheckboxGroup extends LinearLayout {

    private List<CheckBox> checkBoxes;
    private List<String> checkedValues;
    private List<Integer> checkedIds;

    public CheckboxGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        checkBoxes = new ArrayList<>();
        checkedValues = new ArrayList<>();
        checkedIds = new ArrayList<>();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        checkBoxes.add((CheckBox) child);
    }

    public List<String> getCheckedValues() {
        checkedValues.clear();
        for (CheckBox checkbox : checkBoxes) {
            if (checkbox.isChecked())
                checkedValues.add(checkbox.getText().toString());
        }
        return checkedValues;
    }

    public List<Integer> getCheckedIds() {
        checkedIds.clear();
        for (CheckBox checkbox : checkBoxes) {
            if (checkbox.isChecked())
                checkedIds.add(checkbox.getId());
        }
        return checkedIds;
    }
}
