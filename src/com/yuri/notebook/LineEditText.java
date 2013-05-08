package com.yuri.notebook;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;


public class LineEditText extends EditText {
    private Paint linePaint;
    private float margin;
    private int paperColor;

    public LineEditText(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        this.linePaint = new Paint();
        //设置画线的颜色
        this.linePaint.setColor(Color.GRAY);
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
        paramCanvas.drawLine(0.0F, n, getRight(), n, this.linePaint);
        for (int i2 = 1;; i2++) {
            if (i2 >= i) {
                setPadding(10 + (int) this.margin, 20 + (int) this.margin, 5, 0);
                super.onDraw(paramCanvas);
                paramCanvas.restore();
                return;
            }
            n += k;
            paramCanvas.drawLine(0.0F, n, getRight(), n, this.linePaint);
            paramCanvas.save();
        }
    }

}
