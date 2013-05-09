package com.yuri.notebook;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

/**自定义带下划线的Edittext*/
public class LineEditText2 extends EditText {
    private Paint mPaint;
    private Rect mRect;
    private float mult = 1.5f;
    private float add = 2.0f;

    public LineEditText2(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }
    
    public LineEditText2(Context context){
    	super(context);
    	
    	init();
    }
    
    private void init(){
    	mRect = new Rect();
    	mPaint = new Paint();
    	mPaint.setStyle(Style.STROKE);
    	mPaint.setColor(Color.GRAY);
    	mPaint.setAntiAlias(true);
    	this.setLineSpacing(add, mult);
    }

    protected void onDraw(Canvas paramCanvas) {
    	int count = getLineCount();//需要画多少条线
//    	int j = getHeight();
//        int k = getLineHeight();
//        System.out.println("i=" + j + "k=" + k);
//        int m = j / k;
//        if (count < m)
//        	count = m;
    	for (int i = 0; i < count; i++) {
			getLineBounds(i, mRect);
			int baseline = (i + 1) * getLineHeight();
			paramCanvas.drawLine(mRect.left, baseline, mRect.right, baseline, mPaint);
		}
    	super.onDraw(paramCanvas);
    }

}
