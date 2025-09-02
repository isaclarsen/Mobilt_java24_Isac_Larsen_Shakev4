package com.example.shake;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor currentSensor;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor light;
    private TextView testText;
    private ImageView rotatingImage;
    private Switch onOffSwitch;
    private Button toggleButton;
    private Spinner delaySpinner;
    private int currentDelay;
    private int normalDelay;
    private int uiDelay;
    private int gameDelay;
    private double pitch;
    private double roll;
    private long lastShakeTime = 0;
    private float deviceLight;
    private float x;
    private float y;
    private float z;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Komponenter
        testText = (TextView) findViewById(R.id.textView);
        onOffSwitch = (Switch) findViewById(R.id.switch1);
        toggleButton = (Button) findViewById(R.id.toggleButton);
        delaySpinner = (Spinner) findViewById(R.id.spinner);
        rotatingImage = (ImageView) findViewById(R.id.imageView3);

        //Sensorer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        //Delays
        normalDelay = SensorManager.SENSOR_DELAY_NORMAL;
        uiDelay = SensorManager.SENSOR_DELAY_UI;
        gameDelay = SensorManager.SENSOR_DELAY_GAME;


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sensor_delays,
                android.R.layout.simple_spinner_item
        );
        delaySpinner.setAdapter(adapter);

        delaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentDelay == 0) {
                    return;
                }

                if (currentSensor == null) {
                    Toast.makeText(getBaseContext(), "Starta sensor först!", Toast.LENGTH_SHORT).show();
                    return;
                }

                switch (position) {
                    case 0:
                        currentDelay = normalDelay;
                        break;
                    case 1:
                        currentDelay = uiDelay;
                        break;
                    case 2:
                        currentDelay = gameDelay;
                        break;
                }
                Toast.makeText(getBaseContext(), "Delay changed", Toast.LENGTH_SHORT).show();
                sensorManager.unregisterListener(MainActivity.this, currentSensor);
                sensorManager.registerListener(MainActivity.this, currentSensor, currentDelay);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //Sätter accelerator som standard sensor och delay som normal innan användaren har valt.
                    currentSensor = accelerometer;
                    currentDelay = SensorManager.SENSOR_DELAY_NORMAL;
                    sensorManager.registerListener(MainActivity.this, currentSensor, currentDelay);
                } else {
                    sensorManager.unregisterListener(MainActivity.this);
                    currentSensor = null;
                    testText.setText("Avstängd");
                }
            }
        });

        //Sensor toggle
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentSensor == null) {
                    Toast.makeText(getBaseContext(), "Starta sensor först!", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensorManager.unregisterListener(MainActivity.this);

                if (currentSensor == accelerometer) {
                    currentSensor = gyroscope;
                    Toast.makeText(getBaseContext(), "Sensor changed to: gyroscope", Toast.LENGTH_SHORT);
                } else if (currentSensor == gyroscope) {
                    currentSensor = light;
                    Toast.makeText(getBaseContext(), "Sensor changed to: light", Toast.LENGTH_SHORT);
                } else if (currentSensor == light) {
                    currentSensor = accelerometer;
                    Toast.makeText(getBaseContext(), "Sensor changed to: accelerometer", Toast.LENGTH_SHORT);
                }

                sensorManager.registerListener(MainActivity.this, currentSensor, currentDelay);
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            testText.setText("Accelerometer: " + "\n \n" + "x: " + x + "\n" + "y: " + y + "\n" + "z: " + z + "\n");

            pitch = Math.toDegrees(Math.atan2(x, Math.sqrt(y*y + z*z)));
            roll = Math.toDegrees(Math.atan2(y, Math.sqrt(x*x + z*z)));
            rotatingImage.setRotationX((float) pitch);
            rotatingImage.setRotationY((float) roll);

            double magnitude = Math.sqrt(x * x + y * y + z * z);
            double delta = Math.abs(magnitude - SensorManager.GRAVITY_EARTH);
            long currentTime = System.currentTimeMillis();
            double THRESHOLD = 3;

            if (delta > THRESHOLD) {
                if (currentTime - lastShakeTime > 1000) {
                    lastShakeTime = currentTime;
                    Toast.makeText(this, "SHAKE!", Toast.LENGTH_LONG).show();
                }
            }

        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            testText.setText("Gyroscope: " + "\n \n" + "x: " + x + "\n" + "y: " + y + "\n" + "z: " + z + "\n");
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            deviceLight = event.values[0];
            testText.setText("Light: " + deviceLight);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (onOffSwitch.isChecked() && currentSensor != null) {
            sensorManager.registerListener(MainActivity.this, currentSensor, currentDelay);
        }
        super.onResume();
    }
}