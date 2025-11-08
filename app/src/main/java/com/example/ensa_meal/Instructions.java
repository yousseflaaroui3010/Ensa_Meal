package com.example.ensa_meal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class Instructions extends AppCompatActivity {
    ImageView imageView;
    TextView IDmeal,Name,Inst;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_instructions);

       imageView=findViewById(R.id.imageInst);
       IDmeal=findViewById(R.id.IdInst);
       Name=findViewById(R.id.NameInstr);
       Inst=findViewById(R.id.Instr_Inst);

       Intent intent=getIntent();
       Bundle bundle=intent.getExtras();
       Plat plat=(Plat)bundle.getSerializable("MEAL");
       IDmeal.setText(plat.getId());
       Name.setText(plat.getName());
       Inst.setText(plat.getInstructions());
       Glide.with(this).load(plat.getImageURL()).into(imageView);


    }
}