package com.run.unlock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.run.R;
import com.run.MainActivity;

public class SliderLayout extends RelativeLayout {
	
	private Bitmap dragBitmap = null; 	// 解锁滑动图标
	private Context mContext = null;	// Bitmap对象
	private TextView slider_icon;
	
	private int mLastMoveX = 0;
	
	private Handler mainHandler = null; // 与主Activity通信的Handler对象
	
	private Handler mHandler = new Handler();

	public SliderLayout(Context context) {
		super(context);
		mContext = context;
		initDragBitmap();
	}	

	public SliderLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initDragBitmap();
	}

	public SliderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initDragBitmap();
	}
	// 初始化滑动图标
	private void initDragBitmap() {
		if(null == dragBitmap) {
			dragBitmap = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.unlock);
		}
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		// 该控件主要判断是否处于滑动点击区域。滑动时处于INVISIBLE状态（消失），正常时处于VISIBLE（可见）状态
		slider_icon = (TextView) findViewById(R.id.slider_icon);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		// 图标随手移动，调用invalidate后会调用
		invalidateDrawable(canvas);
	}

	private void invalidateDrawable(Canvas canvas) {
		int drawX = mLastMoveX - dragBitmap.getWidth();
		int drawY = slider_icon.getTop();
		canvas.drawBitmap(dragBitmap, drawX < 0 ? 20 : drawX, drawY, null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastMoveX = (int) event.getX();
			// 处理ActionDown事件，判断点击事件是否在图标区域
			return handleActionDownEvent(event);
			
		case MotionEvent.ACTION_MOVE:
			mLastMoveX = x;	// 获取手指移动的距离
			invalidate();	// 重新绘制图标
			return true;
			
		case MotionEvent.ACTION_UP:
			// 处理Action_Up事件：  判断是否解锁成功，成功则结束我们的Activity ；否则 ，缓慢回退该图片。
			handleActionUpEvent(event);
			return true;
		}
		return super.onTouchEvent(event);
	}
	
	// 回退动画时间间隔值
	private static int BACK_DURATION = 20 ;   // 20ms
	// 水平方向前进速率  
	private static float VE_HORIZONTAL = 1.4f ;  //0.1dip/ms

	// 判断松开手指时，是否达到末尾即可以开锁了; 是，则开锁，否则，通过一定的算法使其回退。  
	private void handleActionUpEvent(MotionEvent event) {
		int x = (int)event.getX();
		// 当前移到的距离右边小于15dp,判断解锁成功
		boolean isSuccess = Math.abs(x - getRight()) <= 20;
		if(isSuccess) {
			Toast.makeText(mContext, "解锁成功", Toast.LENGTH_SHORT).show();
			virbate();	//手机震动一下
			resetViewState();
			// 向主界面发送消息解锁成功
			mainHandler.obtainMessage(MainActivity.MSG_LOCK_SUCESS).sendToTarget();
		} else {// 没有成功解锁，以一定的算法使其回退  
			mLastMoveX = x;
			int distance = x - slider_icon.getRight();
			// 当滑动一定距离时才会往回滑动
			if(distance >= 0) {
				mHandler.postDelayed(BackDragImgTask, BACK_DURATION);
			} else {
				resetViewState();
			}
		}
	}	

	private boolean handleActionDownEvent(MotionEvent event) {
		Rect rect = new Rect();
		slider_icon.getHitRect(rect);
		boolean isHit = rect.contains((int)event.getX(), (int)event.getY());
		if(isHit) {
			slider_icon.setVisibility(View.INVISIBLE);			
		}
		return isHit;
	}		
	
	//通过延时控制当前绘制bitmap的位置坐标
	private Runnable BackDragImgTask = new Runnable() {		
		@Override
		public void run() {
			// 下次Bitmap应该到达的坐标
			mLastMoveX = mLastMoveX - (int)(BACK_DURATION * VE_HORIZONTAL);
			// 位置改变后重新绘制图标
			invalidate();
			boolean isEnd = Math.abs(mLastMoveX - slider_icon.getRight()) <= 10;
			if(!isEnd) {
				mHandler.postDelayed(BackDragImgTask, BACK_DURATION);
			} else {
				// 解锁成功
				resetViewState();
			}
		}
	};
	
	private void resetViewState() {
		mLastMoveX = 0;
		invalidate();
	}
	
	// 手机震动一下
	private void virbate() {
		Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(200);
	}
	
	public void setMainHandle(Handler handler) {
		mainHandler = handler;
	}

}
