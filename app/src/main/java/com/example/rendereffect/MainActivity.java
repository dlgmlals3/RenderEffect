package com.example.rendereffect;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.RuntimeShader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.intellij.lang.annotations.Language;

public class MainActivity extends Activity {
    private ImageView imageView;
    private SeekBar seekBar;
    private float blurRadius = 10f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        seekBar = findViewById(R.id.seekBar);

        applyRenderEffect();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                blurRadius = progress * 5f;
                Log.i(TAG, "radius : " + blurRadius);
                applyRenderEffect();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void applyRenderEffect() {
        // ✅ AGSL 셰이더 코드 (컬러 변환)
        @Language("AGSL")
        String shaderCode =
            "uniform shader source;\n" +
            "half4 main(float2 coord) {\n" +
            "    half4 color = source.eval(coord);\n" +
            "    return half4(color.g, color.r, color.b, 1.0);\n" +
            "}";

        // ✅ Bitmap에서 BitmapShader 생성
        RuntimeShader runtimeShader = new RuntimeShader(shaderCode);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if (drawable != null) {
            Bitmap bitmap = drawable.getBitmap();
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample_image).copy(Bitmap.Config.RGB_565, false);
            Shader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            runtimeShader.setInputShader("source", bitmapShader); // Shader 설정
        }

        // ✅ 블러 효과 추가
        //RenderEffect blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.DECAL);
        RenderEffect blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP);
        RenderEffect customShaderEffect = RenderEffect.createShaderEffect(runtimeShader);
        RenderEffect combinedEffect = RenderEffect.createChainEffect(blurEffect, customShaderEffect);

        //imageView.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null);
        imageView.setRenderEffect(combinedEffect);
    }
}
