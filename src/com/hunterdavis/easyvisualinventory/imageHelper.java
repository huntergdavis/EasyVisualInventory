package com.hunterdavis.easyvisualinventory;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

public class imageHelper {
	public static Boolean scaleURIAndDisplay(Context context, Uri uri, ImageView imgview) {
		double divisorDouble = 256;
		InputStream photoStream;
		try {
			photoStream = context.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize=2;
        Bitmap photoBitmap;
		
		photoBitmap = BitmapFactory.decodeStream(photoStream,null,options);
		if(photoBitmap == null)
		{
			return false;
		}
        int h = photoBitmap.getHeight();
        int w = photoBitmap.getWidth();
        if((w>h)&&(w>divisorDouble)){
            double ratio = divisorDouble/w;
            w=(int) divisorDouble;
            h=(int)(ratio*h);
        }
        else if((h>w)&&(h>divisorDouble)){
            double ratio = divisorDouble/h;
            h=(int) divisorDouble;
            w=(int)(ratio*w);
        }

         Bitmap scaled = Bitmap.createScaledBitmap(photoBitmap, w, h, true);
         photoBitmap.recycle();
         imgview.setImageBitmap(scaled);
         return true;
	}
}
