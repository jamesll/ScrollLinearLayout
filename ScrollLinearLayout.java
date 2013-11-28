package com.uvicsoft.banban.ui;

import com.uvicsoft.banban.activity.ImportFileActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * @author: lu_mg
 * @create_time: 2013-7-31 下午02:47:41
 * @modifier:
 * @modify_time:
 * @return:
 * @function:
 * @description:
 */
@SuppressLint("HandlerLeak")
public class ScrollLinearLayout extends LinearLayout{
	
	private final String TAG = "ScrollLinearLayout";
	
	private ImportFileActivity refActivity;
	
	private Scroller mScroller;
	
	private VelocityTracker mVelocityTracker;

	private GestureDetector mGestureDetector;
	
	private int totalScrollDistance;
	
	private int mScrollX;
	
	private float mMaximumVelocity,mMinimumVelocity;
	
	private int mActivePointerId;
	
	private float mLastMotionX;
	
	private  int mInitialVelocity; 
	
	private ImageView ivBlank;
	
	private Handler mHandler = new Handler(){
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
				case 0:
					smoothScrollBy(-itemView.getWidth() - 5, 0);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case 1:
					smoothScrollBy(itemView.getWidth() + 5, 0);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
			}
		};
	};
	
	
	LinearLayout.LayoutParams lpChildView;
	
	DisplayMetrics metrics;
	
	public ScrollLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		refActivity = (ImportFileActivity) context;
		mScroller = new Scroller(context);

		ViewConfiguration configuration = ViewConfiguration.get(context);
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		mGestureDetector = new GestureDetector(context,new CustomGestureListener());
		ivBlank = new ImageView(context);
		metrics = ((Activity)context).getResources().getDisplayMetrics();
		lpChildView = new LinearLayout.LayoutParams((int)(92*metrics.density), (int)(69*metrics.density));
		lpChildView.leftMargin = 5;
		lpChildView.rightMargin = 5;
	}

	@Override
	public void computeScroll() {
		if(getChildCount()==0)
			return;
		if (mScroller.computeScrollOffset()) {
           mScrollX = mScroller.getCurrX(); 
			if(mScrollX<0){ 
				mScroller.setFinalX(0);
				mScrollX = 0;
			}
			
			if(mScrollX == 0){
				totalScrollDistance = getChildAt(getChildCount()-1).getRight()-getWidth()+5;//-getScrollX()
				if(totalScrollDistance<0)
					totalScrollDistance = 0;
			}
	 
			if(mScrollX>totalScrollDistance){ 
				mScroller.setFinalX(totalScrollDistance);
				mScrollX = totalScrollDistance;
			}
			
             scrollTo(mScrollX, 0); 
             postInvalidate(); 
         } 
	}

	public void smoothScrollTo(int fx, int fy) {
		int dx = fx - mScroller.getFinalX();
		int dy = fy - mScroller.getFinalY();
		smoothScrollBy(dx, dy);
	}

	public void smoothScrollBy(int dx, int dy) {
		mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx,dy);
		invalidate();
	}
	
	class CustomGestureListener implements GestureDetector.OnGestureListener {
		
		int clickPosition;
		
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			clickPosition = pointToPosition((int)e.getRawX(),(int) e.getY());
			if(clickPosition!=INVALID_POSITION)
				refActivity.showRotateView(clickPosition);
			
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			onLongClickDrag(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if(getChildCount()>0&&Math.abs(mInitialVelocity)>mMinimumVelocity){
				smoothScrollBy(-mInitialVelocity, 0);
			}
			return true;
		}

	}
	
	private boolean isFirstFullScreen(){
		if(getChildCount() == 0)
			return false;
		View lastChildView = getChildAt(getChildCount()-1);
		if((lastChildView.getLeft()+lastChildView.getWidth())>metrics.widthPixels)
			return true;
		else
			return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mVelocityTracker == null)
			mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(event);
		
		
        final int action = event.getAction();
        
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
            	 int pointerIndex =(action & MotionEvent.ACTION_POINTER_INDEX_MASK)>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;              	 
            	 mActivePointerId = event.getPointerId(pointerIndex);
            	 mLastMotionX = event.getX();
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:
        		if(isDrag){
        			onDrag(event, (int)event.getX(),(int) event.getY());
        		}else{
        			if(mScrollX == 0&&!isFirstFullScreen()){
	        			return true;
        			}
                    final int activePointerIndex = event.findPointerIndex(mActivePointerId);
                    final float x = event.getX(activePointerIndex);
                    final int deltaX = (int) (mLastMotionX - x);
                    smoothScrollBy(deltaX, 0);
                    mLastMotionX = x;
        		}
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            	if(isDrag){
            		onDrop(event,(int)event.getX(), (int)event.getY());
            		stopDrag((int)event.getX(), (int)event.getY());
            		isDrag = false;
            	}else{
	                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
	                mInitialVelocity = (int) mVelocityTracker.getXVelocity(mActivePointerId);
	                if (mVelocityTracker != null) {
	                    mVelocityTracker.recycle();
	                    mVelocityTracker = null;
	                }
            	}
             
                break;
        }
		
		return mGestureDetector.onTouchEvent(event);
	}
	
	
	  
	int dragPosition, dropPosition;

	int dragPointX, dragPointY, dragOffsetX, dragOffsetY;

	boolean isDrag = false;

	int INVALID_POSITION = -1;
	 
	int mFirstPosition = 0;
	
	public int getIndexByX(int x){
		x = x + getScrollX();
		int count = getChildCount();
		Rect frame = new Rect();
		for (int i = count - 1; i >= 0; i--) {
			View child = getChildAt(i);
			child.getHitRect(frame);
			if (frame.left<=x&&frame.right>=x) {
				return  i;
			}
		}
		return INVALID_POSITION;
	}

	public int pointToPosition(int x, int y) {
		Rect frame = new Rect();
		x = x + getScrollX();

		final int count = getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			View child = getChildAt(i);
			child.getHitRect(frame);
			if (frame.contains(x, y)) {
				return i;
			}
			 
		}
		return INVALID_POSITION;
	}

	public int getFirstVisiblePosition() {
		return mFirstPosition;
	}
	
	View itemView;
	@SuppressLint("NewApi")
	private void onLongClickDrag(MotionEvent ev) {
		
		int x = (int) ev.getX();
 
		int y = (int) ev.getY();
		
		dragPosition = dropPosition = pointToPosition((int)ev.getRawX(), y);
		
		if (dragPosition == INVALID_POSITION) {
			return;
		}
		
		isDrag = true;
		
		itemView = (View) getChildAt(dragPosition);
		
		
		dragPointX = x - itemView.getLeft()+mScrollX;
		dragPointY = y - itemView.getTop();

		dragOffsetX = (int) (ev.getRawX() - x);
		dragOffsetY = (int) (ev.getRawY() - y);

		// 解决问题3
		// 每次都销毁一次cache，重新生成一个bitmap
		itemView.destroyDrawingCache();
		itemView.setDrawingCacheEnabled(true);
		Bitmap bm = Bitmap.createBitmap(itemView.getDrawingCache());
		if (Build.VERSION.SDK_INT >= 11)
			itemView.setAlpha(0);
		// 建立item的缩略图
		startDrag(bm, x, y,ev);
	}

	WindowManager.LayoutParams windowParams;

	WindowManager windowManager;

	private void startDrag(Bitmap bm, int x, int y,MotionEvent ev) {
		stopDrag(x, y);

		windowParams = new WindowManager.LayoutParams();
	 
		windowParams.gravity = Gravity.TOP | Gravity.LEFT;
		
		windowParams.x = (int) ev.getRawX() - dragPointX;
		windowParams.y = y - dragPointY + dragOffsetY ;
		
		// 设置宽和高
		windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

		windowParams.format = PixelFormat.TRANSLUCENT;
		windowParams.windowAnimations = 0;

		ImageView iv = new ImageView(getContext());
		iv.setImageBitmap(bm);
		windowManager = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		windowManager.addView(iv, windowParams);
		dragImageView = iv;
	}

	ImageView dragImageView;
	
	int lastPostion;

	private void onDrag(MotionEvent ev, int x, int y) {

		dragOffsetX = (int) (ev.getRawX() - x);
		dragOffsetY = (int) (ev.getRawY() - y);

		if (dragImageView != null) {
			windowParams.alpha = 0.6f;
			windowParams.x = (int) ev.getRawX()  - dragPointX;
			windowParams.y = y - dragPointY + dragOffsetY;
			windowManager.updateViewLayout(dragImageView, windowParams);
		}
		
		//如果拖动的位置和当前的位置不同，itemview 的位置调换；
		lastPostion = pointToPosition((int)ev.getRawX(), y);
		if(lastPostion == INVALID_POSITION||lastPostion>=getChildCount())
			return;
		if(lastPostion != dragPosition){
			removeView(itemView);
			addView(itemView, lastPostion);
		}
		
		//如果碰到屏幕边缘，滑动
		if(ev.getX()-dragPointX - lpChildView.leftMargin < 0){
			mHandler.sendEmptyMessage(0);
		}

		if((ev.getX()+itemView.getWidth()-dragPointX)>metrics.widthPixels){
			mHandler.sendEmptyMessage(1);
		}
	}

	private void onDrop(MotionEvent ev,int x, int y) {
		int tempPosition = pointToPosition((int)ev.getRawX(), y);
		if (tempPosition == INVALID_POSITION) {
			// 删除view
			if(lastPostion==INVALID_POSITION){
				if(checkIfInLinerlayout())
					return;
				removeViewAt(dragPosition);
				refActivity.removeFileInCollection(dragPosition);
				if(!isFirstFullScreen()){
					smoothScrollTo(0, 0);
					mScrollX = 0;
				}
			}
			else{
				if(checkIfInLinerlayout())
					return;
				removeViewAt(lastPostion);
				refActivity.removeFileInCollection(lastPostion);
				if(!isFirstFullScreen()){
					smoothScrollTo(0, 0);
					mScrollX = 0;
				}
			}
			return;
		}

		if(lastPostion == getChildCount()){
			return;
		}
		
		dropPosition = tempPosition;

		if (dropPosition != dragPosition) {
			// 交换view
			removeView(itemView);
			addView(itemView, dropPosition);
			refActivity.exchangeListFile(dragPosition, dropPosition);
		}
	}
	
	private boolean checkIfInLinerlayout(){
		Rect rectLine = new Rect();
		rectLine.left = ((View)this.getParent()).getLeft();
		rectLine.top = ((View)this.getParent()).getTop();
		rectLine.right = ((View)this.getParent()).getLeft()+ this.getWidth();
		rectLine.bottom = ((View)this.getParent()).getTop()+this.getHeight();
		if(rectLine.contains(windowParams.x, windowParams.y)){
			if(windowParams.x>0){
				removeView(itemView);
				addView(itemView);
				refActivity.exchangeListFile(dragPosition,getChildCount()-1);
			}
			return true;
		}
		return false;
	}
	
	@SuppressLint("NewApi")
	private void stopDrag(int x, int y) {
		
		if (dragImageView != null) {

			windowManager.removeView(dragImageView);
			
			if(Build.VERSION.SDK_INT>=11)
				itemView.setAlpha(1);
			
			dragImageView = null;
		}
		
	  
	}
	
	int lastIndex =-1;
	boolean isFirstAdd = true;
	
	public void insertTransparentImageInGroup(MotionEvent event){
    	int index =  getIndexByX((int)event.getRawX());
    	ivBlank.setLayoutParams(lpChildView);
    	
    	if(index !=-1&&index!=lastIndex){
    		if(isFirstAdd){
    			addView(ivBlank, index);
    			isFirstAdd = false;
    		}else{
    			removeView(ivBlank);
    			addView(ivBlank, index);
    		}
    		
    		lastIndex = index;
    	}
	}
	
	public int removeBlankView(){
		removeView(ivBlank);
		//返回删除的空白view的index，方便后面插入view
		return lastIndex;
	}
	
	@Override
	public void addView(View child) {
		LinearLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
		if(lp == null){
			lp = lpChildView;
		}
		this.addView(child, lp);
		lastIndex = -1;
	}
	
	@Override
	public void addView(View child, int index) {
		super.addView(child, index);
		//重置lastIndex
		lastIndex = -1;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		measureChildren(widthMeasureSpec, heightMeasureSpec);
	    setMeasuredDimension(widthSize, heightSize);  
	}
	

	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		
		int mTotalWidth = l+lpChildView.leftMargin;  
		
	    int childCount = getChildCount();  
	    for (int i = 0; i < childCount; i++) {  
	        View childView = getChildAt(i);  
	          
	        int measureHeight = childView.getMeasuredHeight();  
	        int measuredWidth = childView.getMeasuredWidth();
	        
	        int topMargin = 0;
	        if(topMargin == 0){
	        	topMargin = (getHeight()-measureHeight)/2;
	        }
	        childView.layout(mTotalWidth, topMargin, mTotalWidth+measuredWidth, topMargin+measureHeight);
	        mTotalWidth += measuredWidth+lpChildView.leftMargin;  
	    }  
	}
	
}
