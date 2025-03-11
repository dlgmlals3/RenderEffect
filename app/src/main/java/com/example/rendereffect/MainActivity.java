package com.example.rendereffect;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PixelFormat;
import android.graphics.RecordingCanvas;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.RuntimeShader;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.intellij.lang.annotations.Language;

public class MainActivity extends Activity {
    private ImageView imageView;
    private SeekBar seekBar;
    private float blurRadius = 10f;

    @Language("AGSL")
    String shaderCode =
        "uniform shader source;\n" +
        "half4 main(float2 coord) {\n" +
        "    half4 color = source.eval(coord);\n" +
        "    return half4(color.g, color.r, color.b, color.a);\n" +
        "}";

    /*@Language("AGSL")
    String checkShaderCode =
        "uniform shader source;\n" +
        "half4 main(float2 coord) {\n" +
        "    half4 color = source.eval(coord);\n" +
        "    if (color.a < 1.0) {;\n" +
        "       return half4(0.0, 0.0, 1.0, 1.0);\n" +
        "    } else { ;\n" +
        "       return half4(color.g, color.r, color.b, color.a);\n" +
        "    };\n" +
        "}";*/

    @Language("AGSL")
    String checkShaderCode =
        "uniform shader source;\n" +
        "half4 main(float2 coord) {\n" +
        "    half4 color = source.eval(coord);\n" +
        "    return half4(color.r, color.g, color.b, 1.0)\n;" +
        "}";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        seekBar = findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                blurRadius = progress * 5f;
                Log.i(TAG, "radius : " + blurRadius);
                //applyRenderEffect();
                //applyRuntimeShaderEffect();
                //applyRuntimeShaderEffect3();
                //applyRuntimeShaderEffect5();
                applyRuntimeShaderEffect6();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void applyRenderEffect() {
        // ✅ Bitmap에서 BitmapShader 생성
        RuntimeShader runtimeShader = new RuntimeShader(shaderCode);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample_image).copy(Bitmap.Config.RGB_565, false);
        Shader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        RuntimeShader checkShader = new RuntimeShader(checkShaderCode);
        BitmapDrawable drawable2 = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap2 = drawable2.getBitmap();
        Shader bitmapShader2 = new BitmapShader(bitmap2, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);


        Matrix matrix = new Matrix();
        float scaleX = (float) imageView.getWidth() / bitmap.getWidth();
        float scaleY = (float) imageView.getHeight() / bitmap.getHeight();
        matrix.setScale(scaleX, scaleY);

        // Shader에 Matrix 적용
        bitmapShader.setLocalMatrix(matrix); // 크기 맞춤!
        bitmapShader2.setLocalMatrix(matrix); // 크기 맞춤!

        runtimeShader.setInputShader("source", bitmapShader); // Shader 설정
        checkShader.setInputShader("source", bitmapShader2); // Shader 설정

        // 블러 효과 추가
        //RenderEffect blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.DECAL);
        RenderEffect blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP);
        RenderEffect customShaderEffect = RenderEffect.createShaderEffect(runtimeShader);
        RenderEffect checkShaderEffect = RenderEffect.createShaderEffect(checkShader);

        RenderEffect combinedEffect = RenderEffect.createChainEffect(checkShaderEffect, customShaderEffect);
        combinedEffect = RenderEffect.createChainEffect(blurEffect, combinedEffect);

        //imageView.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null);
        imageView.setRenderEffect(combinedEffect);
    }

    private void applyRuntimeShaderEffect() {
        int width = imageView.getWidth();
        int height = imageView.getHeight();

        // ✅ Step 1: GPU 가속 강제 활성화
        imageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // ✅ Step 2: RuntimeShader 생성
        RuntimeShader runtimeShader = new RuntimeShader(shaderCode);

        // ✅ Step 3: RuntimeShader를 RenderEffect로 변환
        RenderEffect shaderEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "source");

        // ✅ Step 4: 블러 효과 적용 (GPU에서 직접 수행)
        RenderEffect blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, shaderEffect, Shader.TileMode.CLAMP);

        // ✅ Step 5: 최종 이펙트 적용
        imageView.setRenderEffect(blurEffect);
    }


    private void applyRuntimeShaderEffect5() {
        int width = imageView.getWidth();
        int height = imageView.getHeight();

        // ✅ Step 1: GPU 가속 강제 활성화
        imageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // ✅ Step 2: RuntimeShader 생성
        RuntimeShader runtimeShader = new RuntimeShader(shaderCode);

        // ✅ Step 3: 알파 보정용 Shader 추가
        String alphaFixShaderCode =
                "uniform shader source;\n" +
                        "half4 main(float2 coord) {\n" +
                        "    half4 color = source.eval(coord);\n" +
                        "    return half4(color.rgb, 1.0);\n" +  // ✅ 블러 전 알파 값 1.0으로 고정
                        "}";

        RuntimeShader alphaFixShader = new RuntimeShader(alphaFixShaderCode);
        RenderEffect alphaFixEffect = RenderEffect.createRuntimeShaderEffect(alphaFixShader, "source");

        // Step 4: RuntimeShader를 RenderEffect로 변환 (블러 전 알파값 보정)
        RenderEffect shaderEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "source");

        // Step 5: 블러 효과 적용 (알파 보정된 Shader 사용)
        RenderEffect blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, alphaFixEffect, Shader.TileMode.CLAMP);

        // Step 6: 블러 후 다시 알파 복원하는 Shader 적용
        String alphaRestoreShaderCode =
                "uniform shader source;\n" +
                        "half4 main(float2 coord) {\n" +
                        "    half4 color = source.eval(coord);\n" +
                        "    return half4(color.rgb, color.a);\n" +  // ✅ 원래 알파 값 복원
                        "}";

        RuntimeShader alphaRestoreShader = new RuntimeShader(alphaRestoreShaderCode);
        RenderEffect alphaRestoreEffect = RenderEffect.createRuntimeShaderEffect(alphaRestoreShader, "source");

        // Step 7: 체이닝하여 최종 이펙트 생성
        RenderEffect combinedEffect = RenderEffect.createChainEffect(blurEffect, alphaRestoreEffect);
        imageView.setRenderEffect(combinedEffect);
    }

    // recording canvas
    private void applyRuntimeShaderEffect6() {
        int width = imageView.getWidth();
        int height = imageView.getHeight();

        // ✅ Step 1: GPU 가속 강제 활성화
        imageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // ✅ Step 2: RuntimeShader 생성
        RuntimeShader runtimeShader = new RuntimeShader(shaderCode);

        // ✅ Step 3: GPU에서 RuntimeShader 결과를 Texture(Bitmap)으로 변환
        Bitmap shaderBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(shaderBitmap);
        Paint paint = new Paint();
        paint.setShader(runtimeShader);
        canvas.drawRect(0, 0, width, height, paint); // ✅ GPU에서 `RuntimeShader`를 Bitmap으로 렌더링

        // ✅ Step 4: 생성된 Texture(Bitmap)을 Shader로 변환
        Shader bitmapShader = new BitmapShader(shaderBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        RuntimeShader textureShader = new RuntimeShader(shaderCode);
        textureShader.setInputShader("source", bitmapShader);

        // ✅ Step 5: Shader를 RenderEffect로 변환 후 블러 효과 적용
        RenderEffect shaderEffect = RenderEffect.createRuntimeShaderEffect(textureShader, "source");
        RenderEffect blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, shaderEffect, Shader.TileMode.CLAMP);

        // ✅ Step 6: 최종 이펙트 적용
        imageView.setRenderEffect(blurEffect);
    }











}
