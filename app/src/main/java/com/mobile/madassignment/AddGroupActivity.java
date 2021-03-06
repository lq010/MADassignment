package com.mobile.madassignment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.mobile.madassignment.models.Group;
import com.mobile.madassignment.models.GroupMember;

import java.util.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


import static java.util.Currency.getAvailableCurrencies;
import static java.util.Currency.getInstance;


public class AddGroupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Button done;
    private ImageView back;
    private EditText et_groupName;
    private EditText et_userName;
    private Spinner s_currency;

    private DatabaseReference mFirebaseDatabaseReference;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Currency group_currency;


    private int eur_position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        done = (Button)findViewById(R.id.bt_add_group);
        back = (ImageView)findViewById(R.id.iv_new_group_back);
        et_groupName = (EditText)findViewById(R.id.et_group_name);
        et_userName = (EditText)findViewById(R.id.et_user_name);
        s_currency = (Spinner) findViewById(R.id.currency_spinner);

        if(user!= null){
            et_userName.setText(user.getDisplayName());
        }
        Set<Currency> myset = getAvailableCurrencies();
        List<Currency> myarray = new ArrayList<>(myset);
        ArrayAdapter<Currency> currencyAdapter = new ArrayAdapter<Currency>(this,
                android.R.layout.simple_spinner_dropdown_item,
                myarray
                );

        // currency spinner
        s_currency.setAdapter(currencyAdapter);
        s_currency.setOnItemSelectedListener(this);
        for(Currency i: myarray){

            if(i.equals(getInstance("EUR"))){
                break;
            }
            eur_position++;
        }
        s_currency.setSelection(eur_position);


        //cancel button
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddGroupActivity.this.finish();
            }
        });

        // submit button
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user!=null){
                    String groupName = et_groupName.getText().toString();
                    String userName = et_userName.getText().toString();
                    if(groupName.trim().matches("")){
                        Toast.makeText(getApplicationContext(),"please enter a group name",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(userName.trim().matches("")){
                        Toast.makeText(getApplicationContext(),"please enter your name",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Calendar calendar = new GregorianCalendar();
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    Toast.makeText(getApplicationContext(),groupName,Toast.LENGTH_SHORT).show();
                    Group newGroup = new Group(groupName,
                            s_currency.getSelectedItem().toString(),null);
                    ///TODO get the usrid
                    Map<String,String> group_member = new HashMap<String, String>();

                    group_member.put(user.getUid(),userName);
                    newGroup.setMembers(group_member);

                    String groudId =  mFirebaseDatabaseReference.push().getKey();
                    //add group (under groups node)
                    mFirebaseDatabaseReference.child("groups").child(groudId).setValue(newGroup);
                    // under users/groups
                    mFirebaseDatabaseReference.child("users").child(user.getUid()).child("groups").child(groudId).setValue("true");
                    AddGroupActivity.this.finish();

                }else{
                    Toast.makeText(getApplicationContext(),"please login",Toast.LENGTH_SHORT).show();
                    return;
                }




            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
