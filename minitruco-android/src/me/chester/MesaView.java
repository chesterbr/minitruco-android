package me.chester;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MesaView extends View {

	public MesaView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public MesaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.i("MesaView", "onDraw");
		// Paint verde = new Paint();
		// verde.setARGB(255, 0, 255, 0);
		canvas.drawRGB(0, 255, 0);
		Resources r = this.getContext().getResources();
		Drawable d = r.getDrawable(R.drawable.ap);

		// Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d
		// .getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.ap);

		Log.i("MesaView", bitmap.toString());
		Log.i("MesaView", "iw:" + d.getIntrinsicWidth());
		Log.i("MesaView", "w:" + bitmap.getWidth());
		canvas.drawBitmap(bitmap, 20, 20, null);
	}

	public MesaView(Context context) {
		super(context);
	}

}
