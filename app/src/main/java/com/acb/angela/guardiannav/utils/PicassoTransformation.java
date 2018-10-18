package com.acb.angela.guardiannav.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Create a circular image with Picasso.
 * To draw something, you need 4 basic components:
 *      a Bitmap to hold the pixels,
 *      a Canvas to host the draw calls (writing into the bitmap),
 *      a drawing primitive (e.g. Rect, Path, text, Bitmap), and
 *      a paint (to describe the colors and styles for the drawing).
 */
public class PicassoTransformation implements Transformation {

    // TODO Source: https://stackoverflow.com/questions/26112150/android-create-circular-image-with-picasso
    @Override
    public Bitmap transform(Bitmap source) {

        // Define the size to create a squared bitmap.
        int size = Math.min(source.getWidth(), source.getHeight());

        // Define the x and y coordinates of the first pixel in source.
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        // The method Bitmap createBitmap (Bitmap source, int x, int y, int width, int height)
        // returns an IMMUTABLE bitmap from the specified subset of the source bitmap. Parameters:
        // source	Bitmap: The bitmap we are subsetting, this value must never be null.
        // x	    int: The x coordinate of the first pixel in source
        // y	    int: The y coordinate of the first pixel in source
        // width	int: The number of pixels in each row
        // height	int: The number of rows
        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        // The method Bitmap createBitmap (int width, int height, Bitmap.Config config)
        // returns a MUTABLE bitmap with the specified width and height. Its initial density
        // is as per getDensity(). The newly created bitmap is in the sRGB color space.
        // Parameters
        // width	int: The width of the bitmap
        // height	int: The height of the bitmap
        // config	Bitmap.Config: The bitmap config to create. This value must never be null.
        // Use method Bitmap.Config getConfig () to retrieve the config. If the bitmap's internal
        // config is in one of the public formats, it returns that config, otherwise return null.
        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        // BitmapShader is a shader used to draw a bitmap as a texture. The bitmap can be
        // repeated or mirrored by setting the tiling mode.
        // Use the constructor BitmapShader (Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY)
        // to create a new shader that will draw with a bitmap. Parameters:
        // bitmap	Bitmap: The bitmap to use inside the shader. This value must never be null.
        // tileX	Shader.TileMode: The tiling mode for x to draw the bitmap in. This value must never be null.
        // tileY	Shader.TileMode: The tiling mode for y to draw the bitmap in. This value must never be null.
        // Use the tiling Shader.TileMode.CLAMP to replicate the edge color if the shader draws
        // outside of its original bounds.
        BitmapShader shader = new BitmapShader(squaredBitmap,
                BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);

        // The Paint class holds the style and color information about how to draw geometries,
        // text and bitmaps.
        Paint paint = new Paint();
        // Use method Shader setShader (Shader shader) to set or clear the shader object.
        // Pass null to clear any previous shader. As a convenience, the parameter passed is also returned.
        paint.setShader(shader);
        // The method void setAntiAlias (boolean aa) is a helper for setFlags(), setting or clearing
        // the ANTI_ALIAS_FLAG bit AntiAliasing smooths out the edges of what is being drawn,
        // but is has no impact on the interior of the shape.
        paint.setAntiAlias(true);

        // The Canvas class hosts the "draw" calls (writing into the bitmap).
        // Use constructor Canvas (Bitmap bitmap) to construct a canvas with the specified
        // bitmap to draw into. The bitmap must be mutable. The initial target density of the
        // canvas is the same as the given bitmap's density.
        Canvas canvas = new Canvas(bitmap);

        // Use method void drawCircle (float cx, float cy, float radius,  Paint paint)
        // to draw the specified circle using the specified paint. If radius is <= 0, then
        // nothing will be drawn. The circle will be filled or framed based on the Style in the paint.
        // Parameters
        // cx	    float: The x-coordinate of the center of the circle to be drawn
        // cy	    float: The y-coordinate of the center of the circle to be drawn
        // radius	float: The radius of the circle to be drawn
        // paint	Paint: The paint used to draw the circle. This value must never be null.
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        // Free the native object associated with this bitmap, and clear the reference
        // to the pixel data. This will not free the pixel data synchronously; it simply
        // allows it to be garbage collected if there are no other references. The bitmap
        // is marked as "dead", meaning it will throw an exception if getPixels() or setPixels()
        // is called, and will draw nothing. This operation cannot be reversed, so it should
        // only be called if you are sure there are no further uses for the bitmap.
        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}