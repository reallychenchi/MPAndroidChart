
package com.github.mikephil.charting.charts;

import static com.github.mikephil.charting.renderer.BarLineScatterCandleBubbleRenderer.isInBoundsX;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import com.github.mikephil.charting.R;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;

/**
 * Chart that draws lines, surfaces, circles, ...
 *
 * @author Philipp Jahoda
 */
public class LineChart extends BarLineChartBase<LineData> implements LineDataProvider {
    ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            for(Highlight highlight : mMarkerPos) {
                ILineDataSet set = getLineData().getDataSetByIndex(highlight.getDataSetIndex());
                Entry e = set.getEntryForIndex(highlight.getDataIndex());
                MPPointD pix = getTransformer(set.getAxisDependency())
                    .getPixelForValues(e.getX(), e.getY());
                highlight.setDraw((float)pix.x, (float)pix.y);
            }
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    };

    public LineChart(Context context) {
        super(context);
    }

    public LineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private Paint mAxisLabelPaint;
    private Paint mAxisBackgroundPainter;

    @Override
    protected void init() {
        super.init();

        mRenderer = new LineChartRenderer(this, mAnimator, mViewPortHandler);
        mAxisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAxisLabelPaint.setTextSize(20);
        mAxisBackgroundPainter = new Paint();
        mAxisBackgroundPainter.setColor(Color.CYAN);
    }

    @Override
    public LineData getLineData() {
        return mData;
    }

    @Override
    public void setData(LineData data) {
        for(int i = 0; i < data.getDataSetCount(); ++i) {
            ILineDataSet dataSet = data.getDataSetByIndex(i);
            for(int j = 0; j < dataSet.getEntryCount(); ++j) {
                Entry e = dataSet.getEntryForIndex(j);
                if (e.isShowMark()) {
                    Highlight highlight = new Highlight(e.getX(), e.getY(), i);
                    highlight.setDataIndex(j);
                    mMarkerPos.add(highlight);
                }
            }
        }
        super.setData(data);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        // releases the bitmap in the renderer to avoid oom error
        if (mRenderer != null && mRenderer instanceof LineChartRenderer) {
            ((LineChartRenderer) mRenderer).releaseBitmap();
        }
        super.onDetachedFromWindow();
    }

    public void setHighlighterPos(double precent) {
        LineData lineData = getLineData();
        if (null == lineData || lineData.getDataSetCount() == 0) {
            return;
        }
        ILineDataSet dataSet = lineData.getDataSetByIndex(0);
        int idx = (int)(precent * dataSet.getEntryCount());
        if (idx >= dataSet.getEntryCount()) {
            idx = dataSet.getEntryCount() - 1;
        }
        if (idx < 0) {
            idx = 0;
        }
        Entry e = dataSet.getEntryForIndex(idx);
        Highlight highlight = new Highlight(e.getX(), e.getY(), 0);
        mIndicesToHighlight = new Highlight[1];
        mIndicesToHighlight[0] = highlight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (valuesToHighlight()) {
            LineData lineData = getLineData();

            for (Highlight high : mIndicesToHighlight) {

                ILineDataSet set = lineData.getDataSetByIndex(high.getDataSetIndex());

                if (set == null || !set.isHighlightEnabled())
                    continue;

                Entry e = set.getEntryForXValue(high.getX(), high.getY());

                if (!isInBoundsX(e, set, mAnimator))
                    continue;

                MPPointD pix = getTransformer(set.getAxisDependency())
                    .getPixelForValues(e.getX(), e.getY() * mAnimator.getPhaseY());
                Drawable d = getResources().getDrawable(R.drawable.label_background, null);
                Utils.drawXAxisValue(canvas, "X: " + e.getX(),
                    (float) pix.x, mViewPortHandler.contentBottom(), mAxisLabelPaint, new MPPointF(0.5f, -0.5f), 0.0f, d);
                Utils.drawXAxisValue(canvas, "Y: " + e.getY(), mViewPortHandler.contentLeft(),
                    (float) pix.y, mAxisLabelPaint, new MPPointF(1f, -0.5f), 0.0f, d);
            }

        }
    }
}
