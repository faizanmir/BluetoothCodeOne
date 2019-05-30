package avishkaar.com.bluetoothcodeone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import static avishkaar.com.bluetoothcodeone.MainActivity.bts;

public class TransferServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_service);

    }

    public void noIdea(View view) {
        bts.testFunction();
        bts.sendData("abc");
    }
}
