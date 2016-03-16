package com.dukei.android.apps.anybalance.plugins.tasker.ui;

import com.dukei.android.apps.anybalance.plugins.tasker.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class AccListItem extends LinearLayout implements Checkable {

	public AccListItem(Context context) {
		super(context);
	}

	public AccListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AccListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	protected Checkable getCheckable(){
		return (Checkable) findViewById(R.id.name);
	}
	
	@Override
	public void setChecked(boolean checked) {
		getCheckable().setChecked(checked);
	}

	@Override
	public boolean isChecked() {
		return getCheckable().isChecked();
	}

	@Override
	public void toggle() {
		getCheckable().toggle();
	}

}
