package com.example.musicplayer.Music_Stuff

import android.content.Context
import android.graphics.*
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

class ImageFormatter {
    companion object {

        fun returnBlurredBackground(input: Bitmap, context: Context): Bitmap? {
            return try {
                val rsScript: RenderScript = RenderScript.create(context)
                val alloc: Allocation = Allocation.createFromBitmap(rsScript, input)
                val blur: ScriptIntrinsicBlur =
                    ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript))
                blur.setRadius(21F)
                blur.setInput(alloc)
                val result = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
                val outAlloc: Allocation = Allocation.createFromBitmap(rsScript, result)
                blur.forEach(outAlloc)
                outAlloc.copyTo(result)
                rsScript.destroy()
                result
            } catch (e: Exception) {
                // TODO: handle exception
                input
            }
        }


        fun getReflectionBackground(image: Bitmap): Bitmap? {
            // The gap we want between the reflection and the original image
            val reflectionGap = 4

            // Get you bit map from drawable folder
            val width = image.width
            val height = image.height

            // This will not scale but will flip on the Y axis
            val matrix = Matrix()
            matrix.preScale(-1F, 1F)

            // Create a Bitmap with the flip matix applied to it.
            // We only want the bottom half of the image
            val reflectionImage = Bitmap.createBitmap(
                image, 0,
                height / 2, width, height / 2, matrix, false
            )

            // Create a new bitmap with same width but taller to fit reflection
            val bitmapWithReflection = Bitmap.createBitmap(
                width,
                height + height / 2, Bitmap.Config.ARGB_8888
            )

            // Create a new Canvas with the bitmap that's big enough for
            // the image plus gap plus reflection
            val canvas = Canvas(bitmapWithReflection)
            // Draw in the original image
            canvas.drawBitmap(image, 0F, 0F, null)
            //Draw the reflection Image
            canvas.drawBitmap(reflectionImage, 0F, (height + reflectionGap).toFloat(), null)

            // Create a shader that is a linear gradient that covers the reflection
            val paint = Paint()
            val shader = LinearGradient(
                0F,
                image.height.toFloat(), 0F, (bitmapWithReflection.height
                        + reflectionGap).toFloat(), 0x70ffffff, 0x00ffffff, Shader.TileMode.CLAMP
            )
            // Set the paint to use this shader (linear gradient)
            paint.setShader(shader)
            // Set the Transfer mode to be porter duff and destination in
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
            // Draw a rectangle using the paint with our linear gradient
            canvas.drawRect(
                0F, height.toFloat(), width.toFloat(), (bitmapWithReflection.height
                        + reflectionGap).toFloat(), paint
            )
            return bitmapWithReflection
        }

    }
}