package com.example.art_book;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.art_book.databinding.ActivityMainBinding;

public class ArtActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        View view = binding.getRoot();
        setContentView(view);

        applyEdgeToEdgePadding(view);
        ViewCompat.requestApplyInsets(view);
    }

    private void applyEdgeToEdgePadding(View view) {        //Applies WindowInsets (status/nav bars & notch) as padding to prevent UI overlap
        final int pL = view.getPaddingLeft();
        final int pT = view.getPaddingTop();
        final int pR = view.getPaddingRight();
        final int pB = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets sys = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );

            v.setPadding(
                    pL + sys.left,
                    pT + sys.top,
                    pR + sys.right,
                    pB + sys.bottom
            );
            return insets;
        });
    }

    public void choosePhoto(View view){

    }

    public void save(View view){

    }
}