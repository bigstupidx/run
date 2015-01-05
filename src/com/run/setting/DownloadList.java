package com.run.setting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.run.R;

public class DownloadList extends ListView implements OnTouchListener, OnGestureListener {

	private GestureDetector gestureDetector;
	private OnDeleteListener listener;
	private boolean isDeleteShown;
	private View deleteButton;
	private ViewGroup itemLayout;
	private int selectedItem;
	
	public DownloadList(Context context, AttributeSet attrs) {
		super(context, attrs);
		gestureDetector = new GestureDetector(context, this);
		setOnTouchListener(this);
	}
	
	public void setOnDeleteListener(OnDeleteListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		if (!isDeleteShown) {	// 如果删除按钮隐藏状态就不需要操作
			selectedItem = pointToPosition((int)e.getX(), (int)e.getY());
		}
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// Log.i("setting", "X: " + velocityX + " Y: " + velocityY);
		if(!isDeleteShown && velocityX < 0 && Math.abs(velocityX) > Math.abs(velocityY)) {
			deleteButton = LayoutInflater.from(getContext()).inflate(R.layout.delete_button, null);
			deleteButton.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {					
					listener.onDelete(selectedItem);
					itemLayout.removeView(deleteButton);
					deleteButton = null;
					isDeleteShown = false;
				}
			});
			itemLayout = (ViewGroup) getChildAt(selectedItem - getFirstVisiblePosition());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					300, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			itemLayout.addView(deleteButton, params);
			isDeleteShown = true;
		} else if (isDeleteShown && velocityX > 0  && 
				Math.abs(velocityX) > Math.abs(velocityY)) {
			itemLayout.removeView(deleteButton);
			deleteButton = null;
			isDeleteShown = false;
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}
	
	public interface OnDeleteListener {
		void onDelete(int index);
	}
	
	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {		
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {		
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}
	
}
