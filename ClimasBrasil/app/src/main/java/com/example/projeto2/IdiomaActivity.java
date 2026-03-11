package com.example.projeto2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class IdiomaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_idioma);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void fechar(View v){
        finish();
    }

    // =============================
    // PORTUGUÊS
    // =============================
    public void idiomaPT(View v) {
        alterarIdioma("pt");
    }

    // =============================
    // INGLÊS
    // =============================
    public void idiomaEN(View v) {
        alterarIdioma("en");
    }

    // =============================
    // ESPANHOL
    // =============================
    public void idiomaES(View v) {
        alterarIdioma("es");
    }

    // =============================
    // ITALIANO
    // =============================
    public void idiomaIT(View v) {
        alterarIdioma("it");
    }

    // =============================
    // ALEMÃO
    // =============================
    public void idiomaDE(View v) {
        alterarIdioma("de");
    }

    // =============================
    // FRANCÊS
    // =============================
    public void idiomaFR(View v) {
        alterarIdioma("fr");
    }

    // =============================
    // MÉTODO GENÉRICO PARA EVITAR REPETIÇÃO
    // =============================
    private void alterarIdioma(String codigoIdioma) {
        Locale idioma = new Locale(codigoIdioma);
        Locale.setDefault(idioma);

        Context context = this;
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        config.setLocale(idioma);
        res.updateConfiguration(config, res.getDisplayMetrics());

        SharedPreferences.Editor dados = getSharedPreferences("fatec", MODE_PRIVATE).edit();
        dados.putString("idioma", codigoIdioma);
        dados.apply();

        finish();
    }
}
