package com.yuri.notebook;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.EditText;

/**自定义带下划线的Edittext*/
public class LineEditText extends EditText {
    private Paint mPaint;
//    private Rect mRect;
    private float mult = 1.5f;
    private float add = 2.0f;
    private float margin;
    private int paperColor;

    public LineEditText(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }
    
    private void init(){
//    	mRect = new Rect();
    	mPaint = new Paint();
    	mPaint.setStyle(Style.STROKE);
    	mPaint.setColor(Color.GRAY);
    	mPaint.setAntiAlias(true);
    	this.setLineSpacing(add, mult);
    }

    protected void onDraw(Canvas paramCanvas) {
    	//画背景颜色
        paramCanvas.drawColor(this.paperColor);
        int i = getLineCount();
        int j = getHeight();
        int k = getLineHeight();
        int m = 1 + j / k;
        if (i < m)
            i = m;
        int n = getCompoundPaddingTop();
//        paramCanvas.drawLine(0.0F, n, getRight(), n, this.linePaint);
        for (int i2 = 1;; i2++) {
            if (i2 >= i) {
                setPadding(10 + (int) this.margin, 20 + (int) this.margin, 5, 10);
                super.onDraw(paramCanvas);
                paramCanvas.restore();
                return;
            }
            n += k;
            paramCanvas.drawLine(0.0F, n, getRight(), n, this.mPaint);
            paramCanvas.save();
        }
    }

}
