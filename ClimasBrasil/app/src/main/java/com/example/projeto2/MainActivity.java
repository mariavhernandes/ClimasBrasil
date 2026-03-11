package com.example.projeto2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView txtTemperatura, txtDescricao, txtCidade;
    private Button btnAtualizarClima;
    private EditText editCidade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        lerIdioma();
        setContentView(R.layout.activity_main);

        // Inicializar views
        txtTemperatura = findViewById(R.id.txtTemperatura);
        txtDescricao = findViewById(R.id.txtDescricao);
        txtCidade = findViewById(R.id.txtCidade);
        btnAtualizarClima = findViewById(R.id.btnAtualizarClima);
        editCidade = findViewById(R.id.editCidade);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar clique do botão atualizar
        btnAtualizarClima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarClima();
            }
        });
    }

    public void abrirIdioma(View v){
        Intent tela = new Intent(this, IdiomaActivity.class);
        startActivityForResult(tela, 3);
    }

    public void lerIdioma() {
        SharedPreferences dados = getSharedPreferences("fatec", MODE_PRIVATE);
        String lingua = dados.getString("idioma", "pt");
        Locale idioma = new Locale(lingua);
        Locale.setDefault(idioma);
        Context context = this;
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(idioma);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    private void buscarClima() {
        String cidade = editCidade.getText().toString().trim();

        if (cidade.isEmpty()) {
            Toast.makeText(this, "Digite o nome de uma cidade", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar "Carregando..." enquanto busca os dados
        txtCidade.setText("Carregando...");
        txtTemperatura.setText("Carregando...");
        txtDescricao.setText("Carregando...");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Para buscar por cidade, precisamos primeiro obter as coordenadas
                    String geocodingUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + cidade + "&count=1";

                    URL geocodingUrlObj = new URL(geocodingUrl);
                    HttpURLConnection geocodingConnection = (HttpURLConnection) geocodingUrlObj.openConnection();
                    geocodingConnection.setRequestMethod("GET");

                    BufferedReader geocodingReader = new BufferedReader(new InputStreamReader(geocodingConnection.getInputStream()));
                    StringBuilder geocodingResponse = new StringBuilder();
                    String line;

                    while ((line = geocodingReader.readLine()) != null) {
                        geocodingResponse.append(line);
                    }
                    geocodingReader.close();

                    JSONObject geocodingJson = new JSONObject(geocodingResponse.toString());

                    if (!geocodingJson.has("results") || geocodingJson.getJSONArray("results").length() == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtCidade.setText("Cidade não encontrada");
                                txtTemperatura.setText("--");
                                txtDescricao.setText("--");
                                Toast.makeText(MainActivity.this, "Cidade '" + cidade + "' não encontrada", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }

                    JSONObject cityData = geocodingJson.getJSONArray("results").getJSONObject(0);
                    final String cityName = cityData.getString("name");
                    double latitude = cityData.getDouble("latitude");
                    double longitude = cityData.getDouble("longitude");

                    // Agora busca o clima com as coordenadas
                    String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                            "&longitude=" + longitude +
                            "&current_weather=true";

                    URL weatherUrlObj = new URL(weatherUrl);
                    HttpURLConnection weatherConnection = (HttpURLConnection) weatherUrlObj.openConnection();
                    weatherConnection.setRequestMethod("GET");

                    BufferedReader weatherReader = new BufferedReader(new InputStreamReader(weatherConnection.getInputStream()));
                    StringBuilder weatherResponse = new StringBuilder();

                    while ((line = weatherReader.readLine()) != null) {
                        weatherResponse.append(line);
                    }
                    weatherReader.close();

                    JSONObject weatherJson = new JSONObject(weatherResponse.toString());
                    JSONObject currentWeather = weatherJson.getJSONObject("current_weather");

                    final double temperature = currentWeather.getDouble("temperature");

                    // ARREDONDAR PARA NÚMERO INTEIRO
                    final int temperaturaInteira = (int) Math.round(temperature);

                    final int weatherCode = currentWeather.getInt("weathercode");

                    // Converter weathercode para descrição
                    final String weatherDescription = getWeatherDescription(weatherCode);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // MOSTRAR APENAS O NÚMERO INTEIRO
                            txtTemperatura.setText(temperaturaInteira + "°C");
                            txtDescricao.setText(weatherDescription);
                            txtCidade.setText(cityName);
                            Toast.makeText(MainActivity.this, "Clima atualizado!", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtCidade.setText("Erro na conexão");
                            txtTemperatura.setText("--");
                            txtDescricao.setText("--");
                            Toast.makeText(MainActivity.this, "Erro ao buscar dados", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private String getWeatherDescription(int weatherCode) {
        switch (weatherCode) {
            case 0: return "Céu limpo";
            case 1: return "Principalmente limpo";
            case 2: return "Parcialmente nublado";
            case 3: return "Nublado";
            case 45: case 48: return "Nevoeiro";
            case 51: case 53: case 55: return "Chuvisco";
            case 56: case 57: return "Chuvisco congelante";
            case 61: case 63: case 65: return "Chuva";
            case 66: case 67: return "Chuva congelante";
            case 71: case 73: case 75: return "Neve";
            case 77: return "Grãos de neve";
            case 80: case 81: case 82: return "Pancadas de chuva";
            case 85: case 86: return "Pancadas de neve";
            case 95: return "Trovoada";
            case 96: case 99: return "Trovoada com granizo";
            default: return "Condição desconhecida";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 3){
            recreate();
        }
    }
}