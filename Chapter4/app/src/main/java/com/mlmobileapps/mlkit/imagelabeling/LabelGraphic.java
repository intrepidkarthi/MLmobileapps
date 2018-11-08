
//



//

//





package com.mlmobileapps.mlkit.imagelabeling;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.mlmobileapps.mlkit.GraphicOverlay;

import java.util.List;

/** Graphic instance for rendering a label within an associated graphic overlay view. */
public class LabelGraphic extends GraphicOverlay.Graphic {

  private final Paint textPaint;
  private final GraphicOverlay overlay;

  private final List<FirebaseVisionLabel> labels;

  LabelGraphic(GraphicOverlay overlay, List<FirebaseVisionLabel> labels) {
    super(overlay);
    this.overlay = overlay;
    this.labels = labels;
    textPaint = new Paint();
    textPaint.setColor(Color.WHITE);
    textPaint.setTextSize(60.0f);
    postInvalidate();
  }

  @Override
  public synchronized void draw(Canvas canvas) {
    float x = overlay.getWidth() / 4.0f;
    float y = overlay.getHeight() / 2.0f;

    for (FirebaseVisionLabel label : labels) {
      canvas.drawText(label.getLabel(), x, y, textPaint);
      y = y - 62.0f;
    }
  }
}
